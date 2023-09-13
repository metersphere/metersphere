<!-- eslint-disable vue/valid-v-for -->
<template>
  <div>
    <a-row class="grid-demo mb-4" :gutter="10">
      <a-col :span="5">
        <a-button class="mr-3" type="primary" @click="uploadPlugin">{{ t('system.plugin.uploadPlugin') }}</a-button>
      </a-col>
      <a-col :span="5" :offset="9">
        <a-select v-model="searchKeys.scene" @change="searchHandler">
          <a-option v-for="item of sceneList" :key="item.value" :value="item.value">{{ t(item.label) }}</a-option>
        </a-select>
      </a-col>
      <a-col :span="5">
        <a-input-search
          v-model="searchKeys.name"
          :max-length="250"
          :placeholder="t('system.plugin.searchPlugin')"
          allow-clear
          @search="searchHandler"
          @press-enter="searchHandler"
        ></a-input-search>
      </a-col>
    </a-row>
    <div class="pr-2">
      <a-table
        :data="filterData"
        :pagination="false"
        :scroll="{ y: 380, x: 2000, maxHeight: 200 }"
        :expandable="expandable"
        :loading="loading"
        row-key="id"
        :expanded-row-keys="expandedRowKeys"
        @expand="handleExpand"
      >
        <template #columns>
          <a-table-column
            :width="300"
            fixed="left"
            :title="t('system.plugin.tableColumnsName')"
            :ellipsis="true"
            :tooltip="true"
          >
            <template #cell="{ record }">
              {{ record.name }} <span class="text-[--color-text-4]">({{ (record.pluginForms || []).length }})</span>
            </template>
          </a-table-column>
          <a-table-column
            :title="t('system.plugin.tableColumnsDescription')"
            data-index="description"
            :ellipsis="true"
            :tooltip="true"
            :width="150"
          />
          <a-table-column :title="t('system.plugin.tableColumnsStatus')">
            <template #cell="{ record }">
              <div v-if="record.enable" class="flex items-center">
                <icon-check-circle-fill class="mr-[2px] text-[rgb(var(--success-6))]" />
                {{ t('system.user.tableEnable') }}
              </div>
              <div v-else class="flex items-center text-[var(--color-text-4)]">
                <MsIcon type="icon-icon_disable" class="mr-[2px] text-[var(--color-text-4)]" />
                {{ t('system.user.tableDisable') }}
              </div>
            </template>
          </a-table-column>
          <a-table-column :title="t('system.plugin.tableColumnsApplicationScene')" data-index="scenario">
            <template #cell="{ record }">{{ getScenarioType(record.scenario) }}</template>
          </a-table-column>
          <a-table-column :title="t('system.plugin.tableColumnsOrg')" :width="300">
            <template #cell="{ record }">
              <MsTagGroup :tag-list="record.organizations || []" type="primary" theme="outline" />
            </template>
          </a-table-column>
          <a-table-column
            :title="t('system.plugin.tableColumnsDescription')"
            :ellipsis="true"
            :tooltip="true"
            data-index="fileName"
          />
          <a-table-column
            :title="t('system.plugin.tableColumnsVersion')"
            data-index="pluginId"
            :width="200"
            :ellipsis="true"
            :tooltip="true"
          />
          <a-table-column :title="t('system.plugin.tableColumnsAuthorization')" :width="180">
            <template #cell="{ record }">
              <span>{{
                record.xpack ? t('system.plugin.uploadCompSource') : t('system.plugin.uploadOpenSource')
              }}</span>
            </template>
          </a-table-column>
          <a-table-column
            :title="t('system.plugin.tableColumnsCreatedBy')"
            :ellipsis="true"
            :tooltip="true"
            data-index="createUser"
          />
          <a-table-column :title="t('system.plugin.tableColumnsUpdateTime')" :width="200">
            <template #cell="{ record }">
              <span>{{ getTime(record.updateTime) }}</span>
            </template>
          </a-table-column>
          <a-table-column :width="180" fixed="right" :bordered="false">
            <template #title>
              {{ t('system.plugin.tableColumnsActions') }}
            </template>
            <template #cell="{ record }">
              <div class="flex">
                <MsButton @click="update(record)">{{ t('system.plugin.edit') }}</MsButton>
                <MsButton v-if="record.enable" @click="disableHandler(record)">{{
                  t('system.plugin.tableDisable')
                }}</MsButton>
                <MsButton v-else @click="enableHandler(record)">{{ t('system.plugin.tableEnable') }}</MsButton>
                <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
              </div>
            </template>
          </a-table-column>
        </template>
        <template #expand-icon="{ record, expanded }">
          <span v-if="(record.pluginForms || []).length && !expanded" class="collapsebtn"
            ><icon-plus :style="{ 'font-size': '12px' }"
          /></span>
          <span v-else-if="(record.pluginForms || []).length && expanded" class="expand"
            ><icon-minus class="text-[rgb(var(--primary-6))]" :style="{ 'font-size': '12px' }"
          /></span>
        </template>
      </a-table>
    </div>
    <div class="ms-footerNum"
      >{{ t('system.plugin.totalNum') }}<span class="mx-2 text-[rgb(var(--primary-5))]">{{ totalNum }}</span
      >{{ t('system.plugin.dataList') }}</div
    >
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
  import { ref, onBeforeMount, reactive, h } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { getPluginList, deletePluginReq, updatePlugin, getScriptDetail } from '@/api/modules/setting/pluginManger';
  import { getSystemOrgOption } from '@/api/modules/setting/organizationAndProject';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import UploadModel from './uploadModel.vue';
  import UpdatePluginModal from './updatePluginModal.vue';
  import UploadSuccessModal from './uploadSuccessModal.vue';
  import scriptDetailDrawer from './scriptDetailDrawer.vue';
  import useModal from '@/hooks/useModal';
  import { Message, TableData, SelectOptionData } from '@arco-design/web-vue';
  import useVisit from '@/hooks/useVisit';
  import type { PluginForms, PluginList, PluginItem, DrawerConfig, UpdatePluginModel } from '@/models/setting/plugin';
  import dayjs from 'dayjs';
  import TableExpand from './tableExpand.vue';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();
  const visitedKey = 'doNotShowAgain';
  const { getIsVisited } = useVisit(visitedKey);

  const data = ref<PluginList>([]);
  const loading = ref<boolean>(false);
  const expandedRowKeys = reactive([]);

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
  const filterData = ref<PluginList>([]);
  const searchKeys = reactive({
    scene: '',
    name: '',
  });
  const totalNum = ref<number>(0);
  const showDrawer = ref<boolean>(false);
  const detailYaml = ref('');
  const { openModal } = useModal();
  const sceneList = ref([
    {
      label: 'system.plugin.all',
      value: '',
    },
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
  const uploadVisible = ref<boolean>(false);
  const updateVisible = ref<boolean>(false);
  const updateModalRef = ref();

  const getTime = (time: string): string => {
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
  };
  const loadData = async () => {
    loading.value = true;
    try {
      const result = await getPluginList();
      data.value = result;
      filterData.value = result;
      totalNum.value = (result || []).length;
    } catch (error) {
      console.log(error);
      data.value = [];
    } finally {
      loading.value = false;
    }
  };
  const searchHandler = () => {
    filterData.value = data.value.filter(
      (item) => item.name?.includes(searchKeys.name) && item.scenario?.indexOf(searchKeys.scene) !== -1
    );
  };
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

  function uploadPlugin() {
    uploadVisible.value = true;
  }
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
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };
  const enableHandler = async (record: PluginItem) => {
    try {
      const params: UpdatePluginModel = {
        id: record.id,
        enable: !record.enable,
      };
      await updatePlugin(params);
      Message.success(t('system.plugin.disablePluginSuccess'));
      loadData();
    } catch (error) {
      console.log(error);
    }
  };
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
      console.log(error);
    }
  };
  const expandable = reactive({
    title: '',
    width: 54,
    expandedRowRender: (record: TableData) => {
      if (record.pluginForms && record.pluginForms.length > 0) {
        return h(
          // @ts-ignore
          TableExpand,
          {
            record,
            onMessageEvent: (recordItem: PluginItem, item: PluginForms) => detailScript(recordItem, item),
          },
          null
        );
      }
    },
  });
  const handleExpand = (rowKey: string | number) => {
    Object.assign(expandedRowKeys, [rowKey]);
  };

  const organizeList = ref<SelectOptionData>([]);
  onBeforeMount(async () => {
    loadData();
    organizeList.value = await getSystemOrgOption();
  });
</script>

<style scoped lang="less">
  :deep(.arco-table-tr-expand .arco-table-td) {
    padding: 0;
    background: none;
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 0 !important;
  }
  :deep(.collapsebtn) {
    padding: 0 1px;
    border: 1px solid var(--color-text-4);
    border-radius: 3px;
    @apply bg-white;
  }
  :deep(.expand) {
    padding: 0 1px;
    border: 1px solid rgb(var(--primary-5));
    border-radius: 3px;
    @apply bg-white;
  }
  :deep(.arco-table-expand-btn) {
    width: 16px;
    height: 16px;
    border-width: 2px;
    border-radius: 3px;
    @apply bg-white;
  }
  .ms-footerNum {
    width: 100%;
    @apply absolute bottom-0 z-20 mt-4 bg-white pt-4 text-sm text-slate-500;
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
  :deep(.arco-table-content + .arco-scrollbar-track-direction-horizontal .arco-scrollbar-thumb-direction-horizontal) {
    width: 0 !important;
  }
  :deep(.arco-table-hover) {
    .arco-table-tr-expand:not(.arco-table-tr-empty):hover {
      .arco-table-td:not(.arco-table-col-fixed-left):not(.arco-table-col-fixed-right) {
        background: none !important;
      }
    }
  }
</style>
