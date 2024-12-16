<template>
  <div>
    <a-row class="grid-demo mb-4" :gutter="10">
      <a-col :span="5">
        <a-button v-permission="['SYSTEM_PLUGIN:READ+ADD']" class="mr-3" type="primary" @click="uploadPlugin">
          {{ t('system.plugin.uploadPlugin') }}
        </a-button>
      </a-col>
      <a-col :span="5" :offset="9">
        <a-select v-model="searchKeys.scene" @change="searchHandler">
          <a-option value="">{{ t('common.all') }}</a-option>
          <a-option v-for="item of sceneList" :key="item.value" :value="item.value">{{ t(item.label) }}</a-option>
        </a-select>
      </a-col>
      <a-col :span="5">
        <a-input-search
          v-model="searchKeys.name"
          :max-length="255"
          :placeholder="t('system.plugin.searchPlugin')"
          allow-clear
          @search="searchHandler"
          @press-enter="searchHandler"
          @clear="searchHandler"
        ></a-input-search>
      </a-col>
    </a-row>
    <MsBaseTable
      :expandable="expandable"
      :expanded-keys="expandedKeys"
      v-bind="propsRes"
      @enable-change="enableChange"
      v-on="propsEvent"
    >
      <template #name="{ record }">
        <a-tooltip :content="`${record.name}（${(record.pluginForms || []).length}）`">
          <div class="one-line-text max-w-[calc(100%-24px)]">
            {{ `${record.name}` }}
          </div>
          <span class="text-[--color-text-4]">（{{ (record.pluginForms || []).length }}）</span>
        </a-tooltip>
      </template>
      <template #scenario="{ record }">
        <a-tooltip :content="getScenarioType(record.scenario)">
          <div class="one-line-text">
            {{ getScenarioType(record.scenario) }}
          </div>
        </a-tooltip>
      </template>
      <template #organizations="{ record }">
        <MsTagGroup
          v-if="(record.organizations || []).length"
          :tag-list="record.organizations || []"
          type="primary"
          theme="outline"
        />
        <MsTag v-else type="primary" theme="outline">
          {{ t('system.plugin.allOrganize') }}
        </MsTag>
      </template>
      <template #xpack="{ record }">
        {{ record.xpack ? t('system.plugin.uploadCompSource') : t('system.plugin.uploadOpenSource') }}
      </template>

      <template #action="{ record }">
        <div class="flex">
          <MsButton v-permission="['SYSTEM_PLUGIN:READ+UPDATE']" @click="update(record)">
            {{ t('system.plugin.edit') }}
          </MsButton>
          <MsTableMoreAction
            v-permission="['SYSTEM_PLUGIN:READ+DELETE']"
            :list="tableActions"
            @select="handleSelect($event, record)"
          />
        </div>
      </template>
    </MsBaseTable>

    <div class="ms-footerNum">
      {{ t('system.plugin.totalNum') }}
      <span class="mx-2 text-[rgb(var(--primary-5))]">{{ totalNum }}</span>
      {{ t('system.plugin.dataList') }}
    </div>

    <UploadModel
      v-model:visible="uploadVisible"
      :organize-list="organizeList"
      @success="okHandler"
      @brash="loadData()"
    />
    <UpdatePluginModal
      ref="updateModalRef"
      v-model:visible="updateVisible"
      :organize-list="organizeList"
      @success="loadData()"
    />
    <UploadSuccessModal
      v-model:visible="uploadSuccessVisible"
      @open-upload-modal="uploadPlugin()"
      @close="closeHandler"
    />
    <scriptDetailDrawer v-model:visible="showDrawer" :value="detailYaml" :config="config" :read-only="true" />
  </div>
</template>

<script setup lang="ts">
  import { h, onBeforeMount, reactive, ref } from 'vue';
  import { Message, SelectOptionData } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import scriptDetailDrawer from './scriptDetailDrawer.vue';
  import TableExpand from './tableExpand.vue';
  import UpdatePluginModal from './updatePluginModal.vue';
  import UploadModel from './uploadModel.vue';
  import UploadSuccessModal from './uploadSuccessModal.vue';

  import { getSystemOrgOption } from '@/api/modules/setting/organizationAndProject';
  import { deletePluginReq, getPluginList, getScriptDetail, updatePlugin } from '@/api/modules/setting/pluginManger';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useVisit from '@/hooks/useVisit';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { DrawerConfig, PluginForms, PluginItem, PluginList, UpdatePluginModel } from '@/models/setting/plugin';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const { openModal } = useModal();

  const tableStore = useTableStore();

  const expandedKeys = ref([]);

  const visitedKey = 'doNotShowAgain';
  const { getIsVisited } = useVisit(visitedKey);

  const hasOperationPluginPermission = computed(() =>
    hasAnyPermission(['SYSTEM_PLUGIN:READ+UPDATE', 'SYSTEM_PLUGIN:READ+DELETE'])
  );

  const columns: MsTableColumn = [
    {
      title: 'system.plugin.tableColumnsName',
      dataIndex: 'name',
      slotName: 'name',
      showInTable: true,
      showDrag: false,
      columnSelectorDisabled: true,
      width: 200,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showInTable: true,
      showTooltip: true,
      showDrag: true,
      width: 150,
    },
    {
      title: 'system.plugin.tableColumnsStatus',
      dataIndex: 'enable',
      width: 150,
      showInTable: true,
      showTooltip: true,
      showDrag: true,
      permission: ['SYSTEM_PLUGIN:READ+UPDATE'],
    },
    {
      title: 'system.plugin.tableColumnsApplicationScene',
      dataIndex: 'scenario',
      slotName: 'scenario',
      showInTable: true,
      width: 100,
      showDrag: true,
    },
    {
      title: 'system.plugin.tableColumnsOrg',
      slotName: 'organizations',
      dataIndex: 'organizations',
      showInTable: true,
      showDrag: true,
      width: 200,
      isTag: true,
      isStringTag: true,
    },
    {
      title: 'system.plugin.tableColumnsJar',
      slotName: 'fileName',
      dataIndex: 'fileName',
      showInTable: true,
      showTooltip: true,
      showDrag: true,
      allowEditTag: true,
      width: 300,
    },
    {
      title: 'system.plugin.tableColumnsVersion',
      slotName: 'pluginId',
      dataIndex: 'pluginId',
      showInTable: true,
      showTooltip: true,
      width: 100,
      showDrag: true,
    },
    {
      title: 'system.plugin.tableColumnsAuthorization',
      slotName: 'xpack',
      dataIndex: 'xpack',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'system.plugin.tableColumnsCreatedBy',
      slotName: 'createUser',
      dataIndex: 'createUser',
      showInTable: true,
      width: 200,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'system.plugin.tableColumnsUpdateTime',
      slotName: 'updateTime',
      dataIndex: 'updateTime',
      showInTable: true,
      width: 200,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: hasOperationPluginPermission.value ? 'system.plugin.tableColumnsActions' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPluginPermission.value ? 150 : 50,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    tableKey: TableKeyEnum.SYSTEM_PLUGIN,
    scroll: { x: '100%' },
    selectable: false,
    showPagination: false,
    heightUsed: 280,
    showSetting: true,
    size: 'default',
  });

  const filterData = ref<PluginList>([]);
  const searchKeys = reactive({
    scene: '',
    name: '',
  });
  const totalNum = ref<number>(0);

  const loading = ref<boolean>(false);

  const loadData = async () => {
    loading.value = true;
    try {
      filterData.value = [];
      const result = await getPluginList();
      const resData = result.map((e) => {
        return {
          ...e,
          updateTime: dayjs(e.updateTime).format('YYYY-MM-DD HH:mm:ss'),
          createTime: dayjs(e.createTime).format('YYYY-MM-DD HH:mm:ss'),
        };
      });
      filterData.value = resData;
      propsRes.value.data = filterData.value;
      totalNum.value = (result || []).length;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  const config = ref<DrawerConfig>({
    title: '',
    pluginId: '',
  });

  const tableActions: ActionsItem[] = [
    {
      label: 'system.plugin.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const sceneList = ref([
    {
      label: 'system.plugin.interfaceTest',
      value: 'API_PROTOCOL',
    },
    {
      label: 'system.plugin.projectManagement',
      value: 'PLATFORM',
    },
    {
      label: 'system.plugin.databaseDriver',
      value: 'JDBC_DRIVER',
    },
  ]);

  const searchHandler = () => {
    propsRes.value.data = filterData.value.filter(
      (item) => item.name?.includes(searchKeys.name) && item.scenario?.indexOf(searchKeys.scene) !== -1
    );
  };

  const uploadVisible = ref<boolean>(false);
  function uploadPlugin() {
    uploadVisible.value = true;
  }

  const updateVisible = ref<boolean>(false);
  const updateModalRef = ref();
  function update(record: PluginItem) {
    updateVisible.value = true;
    updateModalRef.value.open(record);
  }

  const uploadSuccessVisible = ref<boolean>(false);
  const dialogSuccessOpen = () => {
    uploadSuccessVisible.value = true;
  };

  const closeHandler = () => {
    uploadSuccessVisible.value = false;
  };

  const okHandler = () => {
    const isOpen = getIsVisited();
    if (!isOpen) {
      dialogSuccessOpen();
    }
  };

  function getScenarioType(scenario: string) {
    switch (scenario) {
      case 'API_PROTOCOL':
        return t('system.plugin.interfaceTest');
      case 'JDBC_DRIVER':
        return t('system.plugin.databaseDriver');
      case 'PLATFORM':
        return t('system.plugin.projectManagement');
      default:
        break;
    }
  }

  /**
   * 删除插件
   */
  function deletePlugin(record: any) {
    openModal({
      type: 'error',
      title: t('system.plugin.deletePluginTip', { name: characterLimit(record.name) }),
      content: t('system.plugin.deleteContentTip'),
      okText: t('system.plugin.deletePluginConfirm'),
      cancelText: t('system.plugin.pluginCancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deletePluginReq(record.id);
          Message.success(t('system.plugin.deletePluginSuccess'));
          loadData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function handleSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'delete':
        deletePlugin(record);
        break;
      default:
        break;
    }
  }

  /**
   * 获取插件脚本详情
   */
  const showDrawer = ref<boolean>(false);
  const detailYaml = ref('');
  const detailScript = async (record: PluginItem, item: PluginForms) => {
    showDrawer.value = true;
    config.value = {
      pluginId: record.id as string,
      title: item.name,
    };
    try {
      const result = await getScriptDetail(record.id as string, item.id);
      detailYaml.value = result || '';
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const expandable = reactive({
    title: '',
    width: 54,
    expandedRowRender: (record: Record<string, any>) => {
      if (record.pluginForms && record.pluginForms.length > 0) {
        return h(TableExpand, { recordItem: record, onMessageEvent: detailScript });
      }
      return undefined;
    },
  });

  /**
   * 启用插件
   */
  const disableHandler = (record: PluginItem) => {
    openModal({
      type: 'info',
      title: t('system.plugin.disablePluginTip', { name: characterLimit(record.name) }),
      content: t('system.plugin.disablePluginContent'),
      okText: t('system.plugin.disablePluginConfirm'),
      cancelText: t('system.plugin.pluginCancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          const params: UpdatePluginModel = {
            id: record.id,
            enable: !record.enable,
          };
          await updatePlugin(params);
          Message.success(t('system.plugin.disablePluginSuccess'));
          loadData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  /**
   * 启用插件
   */
  const enableHandler = async (record: PluginItem) => {
    try {
      const params: UpdatePluginModel = {
        id: record.id,
        enable: !record.enable,
      };
      await updatePlugin(params);
      Message.success(t('system.plugin.enablePluginSuccess'));
      loadData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  function enableChange(record: PluginItem, newValue: string | number | boolean) {
    if (newValue) {
      enableHandler(record);
    } else {
      disableHandler(record);
    }
  }

  const organizeList = ref<SelectOptionData>([]);
  onBeforeMount(async () => {
    loadData();
    organizeList.value = await getSystemOrgOption();
  });

  await tableStore.initColumn(TableKeyEnum.SYSTEM_PLUGIN, columns, 'drawer');
</script>

<style scoped lang="less">
  :deep(.arco-table-tr-expand .arco-table-td) {
    padding: 0;
    background: none;
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 0 !important;
  }
  .ms-footerNum {
    width: 100%;
    @apply sticky bottom-0 left-0 z-20  pt-4 text-sm text-slate-500;

    background-color: var(--color-text-fff);
  }
  :deep(.arco-table-tr .arco-table-td) {
    height: 54px !important;
  }
  .ms-table-expand :deep(.arco-scrollbar-container + .arco-scrollbar-track-direction-vertical) {
    left: 0 !important;
  }
  :deep(.arco-table-content + .arco-scrollbar-track-direction-vertical .arco-scrollbar-thumb-direction-vertical) {
    height: 0 !important;
  }
  :deep(.arco-table-hover) {
    .arco-table-tr-expand:not(.arco-table-tr-empty):hover {
      .arco-table-td:not(.arco-table-col-fixed-left):not(.arco-table-col-fixed-right) {
        background: none !important;
      }
    }
  }
  :deep(.arco-table-tr.arco-table-tr-expand):hover {
    background: none !important;
  }
</style>
