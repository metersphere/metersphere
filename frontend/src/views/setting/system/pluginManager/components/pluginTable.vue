<!-- eslint-disable vue/valid-v-for -->
<template>
  <div>
    <a-row class="grid-demo mb-4" :gutter="10">
      <a-col :span="5">
        <a-button class="mr-3" type="primary" @click="uploadPlugin">{{ t('system.plugin.uploadPlugin') }}</a-button>
      </a-col>
      <a-col :span="5" :offset="9">
        <a-select v-model="searchKeys.scene">
          <a-option v-for="item of sceneList" :key-="item.value" :value="item.value">{{ item.label }}</a-option>
        </a-select>
      </a-col>
      <a-col :span="5">
        <a-input-search
          v-model="searchKeys.name"
          :placeholder="t('system.plugin.searchPlugin')"
          @search="searchHanlder"
        ></a-input-search>
      </a-col>
    </a-row>
    <div class="mb-6" style="overflow: auto">
      <a-table
        :data="filterData"
        :pagination="false"
        :scroll="{ y: 'auto', x: 2400 }"
        :expandable="expandable"
        :loading="loading"
        row-key="id"
        :expanded-row-keys="expandedRowKeys"
        @expand="handleExpand"
      >
        <template #columns>
          <a-table-column fixed="left" :title="t('system.plugin.tableColunmName')">
            <template #cell="{ record }">
              {{ record.name }} <span class="text-[--color-text-4]">({{ record.pluginForms.length }})</span>
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
                v-show="record.organizations.length > 2"
                class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
                bordered
              >
                +{{ record.organizations.length - 2 }}
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
              <MsButton v-else>{{ t('system.plugin.tableEnable') }}</MsButton>
              <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
            </template>
          </a-table-column>
        </template>
        <template #expand-icon="{ record, expanded }">
          <span v-if="record.pluginForms.length && !expanded" class="collapsebtn"
            ><icon-plus :style="{ 'font-size': '12px' }"
          /></span>
          <span v-else-if="record.pluginForms.length && expanded" class="expand"
            ><icon-minus class="text-[rgb(var(--primary-6))]" :style="{ 'font-size': '12px' }"
          /></span>
          <span v-else class="empty-button"></span>
        </template>
        <template #expand-row="{ record }">
          <div v-for="(item, index) in record.pluginForms" :key="item.id" class="ms-self"
            ><span class="circle"> {{ index + 1 }} </span
            ><span class="cursor-pointer text-[rgb(var(--primary-6))]" @click="detailScript(record, item)">{{
              item.name
            }}</span></div
          >
        </template>
      </a-table>
    </div>
    <div class="ms-footerNum"
      >{{ t('system.plugin.totalNum') }}<span class="mx-2">{{ totalNum }}</span
      >{{ t('system.plugin.dataList') }}</div
    >
    <UploadModel :visible="uploadVisible" @cancel="uploadVisible = false" @success="okHandler" @brash="loadData()" />
    <UpdatePluginModal ref="updateModalRef" v-model:visible="updateVisible" @success="loadData()" />
    <scriptDetailDrawer v-model:visible="showDrawer" :value="detailYaml" :config="config" />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, reactive } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { getPluginList, deletePluginReq, updatePlugin, getScriptDetail } from '@/api/modules/setting/pluginManger';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import UploadModel from './uploadModel.vue';
  import UpdatePluginModal from './updatePluginModal.vue';
  import uploadSuccessModal from './uploadSuccessModal.vue';
  import scriptDetailDrawer from './scriptDetailDrawer.vue';
  import { useCommandComponent } from '@/hooks/useCommandComponent';
  import useButtonStyle from '@/hooks/useHiddenButton';
  import useModal from '@/hooks/useModal';
  import { Message } from '@arco-design/web-vue';
  import useVisit from '@/hooks/useVisit';
  import type { PluginForms, PluginList, PluginItem, Options, DrawerConfig } from '@/models/setting/plugin';
  import dayjs from 'dayjs';

  const { t } = useI18n();
  const visitedKey = 'doNotShowAgain';
  const { getIsVisited } = useVisit(visitedKey);
  const { hiddenButton, expandOrcollapseStyle, countHeight, cssHeight } = useButtonStyle('.ms-card', [
    '.arco-alert',
    '.arco-row',
    '.ms-footerNum',
  ]);

  const data = ref<PluginList>([]);
  const loading = ref<boolean>(false);
  const expandable = reactive({
    title: '',
    width: 54,
  });
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
      label: '全部',
      value: '',
    },
    {
      label: '接口测试',
      value: 'API',
    },
    {
      label: '项目管理',
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
      totalNum.value = result.length;
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
          return true;
        } catch (error) {
          console.log(error);
          return false;
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
          await updatePlugin({ enable: !record.enable });
          Message.success(t('system.plugin.disablePluginSuccess'));
          loadData();
          return true;
        } catch (error) {
          console.log(error);
          return false;
        }
      },
      hideCancel: false,
    });
  };
  const detailScript = async (record: PluginItem, item: PluginForms) => {
    showDrawer.value = true;
    config.value = {
      pluginId: record.pluginId as string,
      title: item.name,
    };
    try {
      const result = await getScriptDetail(record.pluginId as string, item.id);
      detailYaml.value = result || '';
    } catch (error) {
      console.log(error);
    }
  };
  const handleExpand = (rowKey: string | number) => {
    Object.assign(expandedRowKeys, [rowKey]);
    expandOrcollapseStyle();
  };
  onMounted(() => {
    data.value = [
      {
        id: 'string1',
        name: '插件一',
        pluginId: 'string',
        fileName: 'string',
        createTime: 0,
        updateTime: 3084234,
        createUser: '创建人',
        enable: true,
        global: true,
        xpack: true,
        description: 'string',
        scenario: 'API',
        pluginForms: [
          {
            id: '111',
            name: '步骤一',
          },
          {
            id: '222',
            name: '步骤二',
          },
        ],
        organizations: [
          {
            id: 'string',
            num: 0,
            name: 'string',
          },
        ],
      },
      {
        id: 'string2',
        name: '插件二',
        pluginId: 'string',
        fileName: 'string',
        createTime: 0,
        updateTime: 3084234,
        createUser: '创建人',
        enable: true,
        global: true,
        xpack: true,
        description: 'string',
        scenario: 'PLATFORM',
        pluginForms: [],
        organizations: [
          {
            id: 'string',
            num: 0,
            name: 'string',
          },
        ],
      },
      {
        id: 'string3',
        name: '插件3',
        pluginId: 'string',
        fileName: 'string',
        createTime: 0,
        updateTime: 3084234,
        createUser: '创建人',
        enable: true,
        global: true,
        xpack: true,
        description: 'string',
        scenario: 'PLATFORM',
        pluginForms: [
          {
            id: '111',
            name: '步骤一',
          },
        ],
        organizations: [
          {
            id: 'string',
            num: 0,
            name: 'string',
          },
        ],
      },
      {
        id: 'string4',
        name: '插件4',
        pluginId: 'string',
        fileName: 'string',
        createTime: 0,
        updateTime: 3084234,
        createUser: '创建人',
        enable: true,
        global: true,
        xpack: true,
        description: 'string',
        scenario: 'API',
        pluginForms: [
          {
            id: '111',
            name: '步骤一',
          },
          {
            id: '222',
            name: '步骤二',
          },
        ],
        organizations: [
          {
            id: 'string',
            num: 0,
            name: 'string',
          },
        ],
      },
      {
        id: 'string5',
        name: '插件5',
        pluginId: 'string',
        fileName: 'string',
        createTime: 0,
        updateTime: 3084234,
        createUser: '创建人',
        enable: true,
        global: true,
        xpack: true,
        description: 'string',
        scenario: 'PLATFORM',
        pluginForms: [],
        organizations: [
          {
            id: 'string',
            num: 0,
            name: 'string',
          },
        ],
      },
    ];
    loadData();
    filterData.value = [...data.value];
    hiddenButton();
    cssHeight.height = countHeight();
  });
</script>

<style scoped lang="less">
  .circle {
    color: var(--color-text-3);
    background: var(--color-fill-3);
    @apply ml-6 mr-10 inline-block h-4 w-4 text-center text-xs leading-4;
  }
  :deep(.arco-table-tr-expand .arco-table-td) {
    background: none;
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 0 !important;
  }
  :deep(.arco-table) {
    overflow: hidden !important;
    height: v-bind('cssHeight.height') !important;
  }
  .ms-self {
    height: 40px;
    line-height: 40px;
    border-bottom: 1px solid var(--color-text-n8);
    @apply flex items-center align-middle leading-6;
  }
  .ms-footerNum {
    @apply mt-4 text-sm text-slate-500;
  }
</style>
