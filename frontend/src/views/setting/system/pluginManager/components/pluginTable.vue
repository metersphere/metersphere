<!-- eslint-disable vue/valid-v-for -->
<template>
  <div>
    <a-row class="grid-demo mb-4" :gutter="10">
      <a-col :span="5">
        <a-button class="mr-3" type="primary" @click="uploadPlugin">{{ t('system.plugin.uploadPlugin') }}</a-button>
      </a-col>
      <a-col :span="5" :offset="9">
        <a-select v-model="searchKeys.scene">
          <a-option v-for="item of sceneList" :key="item.value" :value="item.value">{{ t(item.label) }}</a-option>
        </a-select>
      </a-col>
      <a-col :span="5">
        <a-input-search
          v-model="searchKeys.name"
          :max-length="250"
          :placeholder="t('system.plugin.searchPlugin')"
          @search="searchHanlder"
          @press-enter="searchHanlder"
        ></a-input-search>
      </a-col>
    </a-row>
    <div class="ms-table-wrapper flex flex-col justify-between">
      <div>
        <a-table
          :data="filterData"
          :pagination="false"
          :scroll="{ y: 500, x: 2400 }"
          :expandable="expandable"
          :loading="loading"
          row-key="id"
          :expanded-row-keys="expandedRowKeys"
          @expand="handleExpand"
        >
          <template #columns>
            <a-table-column fixed="left" :title="t('system.plugin.tableColunmName')">
              <template #cell="{ record }">
                {{ record.name }} <span class="text-[--color-text-4]">({{ (record.pluginForms || []).length }})</span>
              </template>
            </a-table-column>
            <a-table-column :title="t('system.plugin.tableColunmDescription')" data-index="description" />
            <a-table-column :title="t('system.plugin.tableColunmStatus')">
              <template #cell="{ record }">
                <div v-if="record.enable" class="flex items-center">
                  <icon-check-circle-fill class="mr-[2px] text-[rgb(var(--success-6))]" />
                  {{ t('system.user.tableEnable') }}
                </div>
                <div v-else class="flex items-center text-[var(--color-text-4)]">
                  <icon-stop class="mr-[2px]" />
                  {{ t('system.user.tableDisable') }}
                </div>
              </template>
            </a-table-column>
            <a-table-column :title="t('system.plugin.tableColunmApplicationScene')" data-index="scenario">
              <template #cell="{ record }">{{
                record.scenario === 'API' ? t('system.plugin.secneApi') : t('system.plugin.secneProManger')
              }}</template>
            </a-table-column>
            <a-table-column :title="t('system.user.tableColunmOrg')">
              <template #cell="{ record }">
                <a-tag
                  v-for="org of record.organizations"
                  :key="org.id"
                  class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
                  bordered
                >
                  {{ org.name }}
                </a-tag>
                <a-tag
                  v-show="(record.organizations || []).length > 2"
                  class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
                  bordered
                >
                  +{{ (record.organizations || []).length - 2 }}
                </a-tag>
              </template>
            </a-table-column>
            <a-table-column :title="t('system.plugin.tableColunmDescription')" data-index="fileName" />
            <a-table-column :title="t('system.plugin.tableColunmVersion')" data-index="pluginId" />
            <a-table-column :title="t('system.plugin.tableColunmAuthorization')">
              <template #cell="{ record }">
                <span>{{
                  record.xpack ? t('system.plugin.uploadOpenSource') : t('system.plugin.uploadCompSource')
                }}</span>
              </template>
            </a-table-column>
            <a-table-column :title="t('system.plugin.tableColunmCreatedBy')" data-index="createUser" />
            <a-table-column :title="t('system.plugin.tableColunmUpdateTime')">
              <template #cell="{ record }">
                <span>{{ getTime(record.updateTime) }}</span>
              </template>
            </a-table-column>
            <a-table-column :width="200" fixed="right" align="center" :bordered="false">
              <template #title>
                {{ t('system.plugin.tableColunmActions') }}
              </template>
              <template #cell="{ record }">
                <MsButton @click="update(record)">{{ t('system.plugin.edit') }}</MsButton>
                <MsButton v-if="record.enable" @click="disableHandler(record)">{{
                  t('system.plugin.tableDisable')
                }}</MsButton>
                <MsButton v-else @click="enableHandler(record)">{{ t('system.plugin.tableEnable') }}</MsButton>
                <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
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
        >{{ t('system.plugin.totalNum') }}<span class="mx-2">{{ totalNum }}</span
        >{{ t('system.plugin.dataList') }}</div
      >
    </div>
    <UploadModel
      v-model:visible="uploadVisible"
      :originize-list="originizeList"
      @success="okHandler"
      @brash="loadData()"
    />
    <UpdatePluginModal
      ref="updateModalRef"
      v-model:visible="updateVisible"
      :originize-list="originizeList"
      @success="loadData()"
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
  import { getAllOrgList } from '@/api/modules/setting/orgnization';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import UploadModel from './uploadModel.vue';
  import UpdatePluginModal from './updatePluginModal.vue';
  import uploadSuccessModal from './uploadSuccessModal.vue';
  import scriptDetailDrawer from './scriptDetailDrawer.vue';
  import { useCommandComponent } from '@/hooks/useCommandComponent';
  import useModal from '@/hooks/useModal';
  import { Message, TableData, SelectOptionData } from '@arco-design/web-vue';
  import useVisit from '@/hooks/useVisit';
  import type {
    PluginForms,
    PluginList,
    PluginItem,
    Options,
    DrawerConfig,
    UpdatePluginModel,
  } from '@/models/setting/plugin';
  import dayjs from 'dayjs';
  import TableExpand from './tableExpand.vue';

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
      label: 'system.plugin.apiTest',
      value: 'API',
    },
    {
      label: 'system.plugin.proMangement',
      value: 'PLATFORM',
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
  const searchHanlder = () => {
    filterData.value = data.value.filter(
      (item) => item.name?.includes(searchKeys.name) && item.scenario?.indexOf(searchKeys.scene) !== -1
    );
  };
  function deletePlugin(record: any) {
    openModal({
      type: 'warning',
      title: t('system.plugin.deletePluginTip', { name: record.name }),
      content: '',
      okText: t('system.plugin.deletePluginConfirm'),
      cancelText: t('system.plugin.pluginCancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          deletePluginReq(record.id);
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
  function uploadPlugin() {
    uploadVisible.value = true;
  }
  function update(record: PluginItem) {
    updateVisible.value = true;
    updateModalRef.value.open(record);
  }
  const myUploadSuccessDialog = useCommandComponent(uploadSuccessModal);
  const uploadSuccessOptions = reactive({
    title: 'system.plugin.uploadPlugin',
    visible: false,
    onOpen: () => uploadPlugin(),
    onClose: () => {
      myUploadSuccessDialog.close();
    },
  });
  const dialogOpen = (options: Options) => {
    options.visible = true;
    myUploadSuccessDialog(uploadSuccessOptions);
  };
  const okHandler = () => {
    const isOpen = getIsVisited();
    if (!isOpen) {
      dialogOpen(uploadSuccessOptions);
    }
  };
  const disableHandler = (record: PluginItem) => {
    openModal({
      type: 'info',
      title: t('system.plugin.disablePluginTip', { name: record.name }),
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

  const originizeList = ref<SelectOptionData>([]);
  onBeforeMount(async () => {
    loadData();
    originizeList.value = await getAllOrgList();
  });
</script>

<style scoped lang="less">
  :deep(.arco-table-tr-expand .arco-table-td) {
    background: none;
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 0 !important;
  }
  :deep(.arco-table) {
    margin-right: -10px;
    padding-right: 10px;
    max-width: 100%;
  }
  :deep(.collapsebtn) {
    padding: 0 1px;
    border: 1px solid var(--color-text-4);
    @apply bg-white;
  }
  :deep(.expand) {
    padding: 0 1px;
    border: 1px solid rgb(var(--primary-5));
    @apply bg-white;
  }
  :deep(.arco-table-expand-btn) {
    @apply bg-white;
  }
  .ms-footerNum {
    height: 22px;
    line-height: 22px;
    @apply mt-4 text-sm text-slate-500;
  }
  .ms-table-wrapper {
    height: calc(100vh - 236px);
    min-height: 400px;
  }
</style>
