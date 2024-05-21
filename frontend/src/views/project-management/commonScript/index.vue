<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <a-button v-permission="['PROJECT_CUSTOM_FUNCTION:READ+ADD']" type="outline" @click="addCommonScript">
        {{ t('project.commonScript.addPublicScript') }}
      </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiScenario.params.searchPlaceholder')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="searchList"
      />
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record }">
        <div class="flex items-center">
          <div
            class="one-line-text max-w-[200px] cursor-pointer text-[rgb(var(--primary-5))]"
            @click="showDetail(record)"
            >{{ record.name }}
          </div>
          <a-popover :title="characterLimit(record.name)" position="right">
            <a-button type="text" class="ml-2 px-0">{{ t('project.commonScript.preview') }}</a-button>
            <template #content>
              <div class="w-[436px] bg-[var(--color-bg-3)] px-2 pb-2">
                <MsCodeEditor
                  v-model:model-value="record.script"
                  :show-theme-change="false"
                  title=""
                  width="100%"
                  height="376px"
                  theme="MS-text"
                  :read-only="true"
                  :show-full-screen="false"
                />
              </div>
            </template>
          </a-popover>
        </div>
      </template>
      <template #status="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_CUSTOM_FUNCTION:READ+UPDATE'])"
          v-model:model-value="record.status"
          class="param-input w-[110px]"
          size="mini"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <CommonScriptStatus :status="record.status" />
          </template>
          <a-option :key="CommonScriptStatusEnum.PASSED" :value="CommonScriptStatusEnum.PASSED">
            <CommonScriptStatus :status="CommonScriptStatusEnum.PASSED" />
          </a-option>
          <a-option :key="CommonScriptStatusEnum.DRAFT" :value="CommonScriptStatusEnum.DRAFT">
            <CommonScriptStatus :status="CommonScriptStatusEnum.DRAFT" />
          </a-option>
        </a-select>
        <CommonScriptStatus v-else :status="record.status" />
      </template>
      <template #[FilterSlotNameEnum.PROJECT_MANAGEMENT_COMMON_SCRIPT]="{ filterContent }">
        <CommonScriptStatus :status="filterContent.value" />
      </template>
      <template #operation="{ record }">
        <MsButton v-permission="['PROJECT_CUSTOM_FUNCTION:READ+UPDATE']" status="primary" @click="editHandler(record)">
          {{ t('common.edit') }}
        </MsButton>
        <MsTableMoreAction
          v-if="!record.internal"
          v-permission="['PROJECT_CUSTOM_FUNCTION:READ+DELETE']"
          :list="actions"
          @select="(item:ActionsItem) => handleMoreActionSelect(item,record)"
        />
      </template>
    </ms-base-table>
    <template v-if="(keyword || '').trim() === ''" #empty>
      <div class="flex items-center justify-center">
        {{ t('caseManagement.caseReview.tableNoData') }}
        <MsButton class="ml-[8px]"> {{ t('project.commonScript.addPublicScript') }}</MsButton>
      </div>
    </template>
    <AddScriptDrawer
      v-model:visible="showScriptDrawer"
      v-model:params="paramsList"
      :confirm-loading="confirmLoading"
      :script-id="isEditId"
      :enable-radio-selected="radioSelected"
      @save="saveHandler"
    />
    <ScriptDetailDrawer
      ref="scriptDetailDrawer"
      v-model:visible="showDetailDrawer"
      :script-id="scriptId"
      @update="updateHandler"
    />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddScriptDrawer from '@/components/business/ms-common-script/ms-addScriptDrawer.vue';
  import CommonScriptStatus from './components/commonScriptStatus.vue';
  import ScriptDetailDrawer from './components/scriptDetailDrawer.vue';

  import {
    addOrUpdateCommonScriptReq,
    deleteCommonScript,
    getCommonScriptPage,
    updateStatusCommonScript,
  } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type {
    AddOrUpdateCommonScript,
    CommonScriptItem,
    ParamsRequestType,
  } from '@/models/projectManagement/commonScript';
  import { CommonScriptStatusEnum } from '@/enums/commonScriptStatusEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const tableStore = useTableStore();

  const { openModal } = useModal();

  const { t } = useI18n();
  const keyword = ref<string>('');

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['PROJECT_CUSTOM_FUNCTION:READ+UPDATE', 'PROJECT_CUSTOM_FUNCTION:READ+DELETE'])
  );

  const statusFilterOptions = computed(() => {
    return Object.values(CommonScriptStatusEnum).map((key) => {
      return {
        value: key,
        label: key,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'project.commonScript.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 300,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'common.desc',
      slotName: 'description',
      dataIndex: 'description',
      width: 200,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.commonScript.enable',
      dataIndex: 'status',
      slotName: 'status',
      showInTable: true,
      width: 150,
      showDrag: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: statusFilterOptions.value,
        filterSlotName: FilterSlotNameEnum.PROJECT_MANAGEMENT_COMMON_SCRIPT,
      },
    },
    {
      title: 'project.commonScript.tags',
      dataIndex: 'tags',
      slotName: 'tags',
      showInTable: true,
      isTag: true,
      width: 456,
      showDrag: true,
    },
    {
      title: 'project.commonScript.createUser',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 200,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.commonScript.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'system.resourcePool.tableColumnUpdateTime',
      dataIndex: 'updateTime',
      width: 180,
      showDrag: true,
    },
    {
      title: hasOperationPermission.value ? 'project.commonScript.tableColumnActions' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 140 : 50,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setProps } = useTable(
    getCommonScriptPage,
    {
      scroll: { x: 1200 },
      tableKey: TableKeyEnum.ORGANIZATION_PROJECT_COMMON_SCRIPT,
      heightUsed: 290,
      showSetting: true,
    },
    (record) => {
      return {
        ...record,
        tags: (record.tags || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
      };
    }
  );

  const actions: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  function initData() {
    setLoadListParams({
      projectId: currentProjectId.value,
      keyword: keyword.value,
    });
    loadList();
  }

  function deleteScript(record: CommonScriptItem) {
    openModal({
      type: 'error',
      title: t('project.commonScript.deleteTitleTip', { name: characterLimit(record.name) }),
      content: t('project.commonScript.deleteTitleContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteCommonScript(record.id);
          initData();
          Message.success(t('common.deleteSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function handleMoreActionSelect(item: ActionsItem, record: CommonScriptItem) {
    if (item.eventTag === 'delete') {
      deleteScript(record);
    }
  }

  const showScriptDrawer = ref<boolean>(false);
  const scriptId = ref<string>('');
  const isEditId = ref<string>('');
  const showDetailDrawer = ref<boolean>(false);

  // 脚本详情
  function showDetail(record: CommonScriptItem) {
    scriptId.value = record.id;
    showDetailDrawer.value = true;
  }

  const paramsList = ref<ParamsRequestType[]>([]);
  const confirmLoading = ref<boolean>(false);
  const scriptDetailDrawer = ref();

  // 保存自定义代码片段应用
  async function saveHandler(form: AddOrUpdateCommonScript) {
    try {
      confirmLoading.value = true;
      const { status, params } = form;
      paramsList.value = JSON.parse(params);
      // const paramTableList = paramsList.value.slice(0, -1);
      const paramsObj: AddOrUpdateCommonScript = {
        ...form,
        status: status || 'DRAFT',
        projectId: currentProjectId.value,
        params,
      };
      await addOrUpdateCommonScriptReq(paramsObj);
      showScriptDrawer.value = false;
      resetSelector();
      initData();
      Message.success(form.id ? t('common.updateSuccess') : t('common.createSuccess'));
      if (showDetailDrawer.value) {
        scriptDetailDrawer.value.getDetail();
      }
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  function addCommonScript() {
    isEditId.value = '';
    showScriptDrawer.value = true;
  }

  function updateHandler(id: string) {
    isEditId.value = id;
    showScriptDrawer.value = true;
  }

  const radioSelected = ref<boolean>(false);

  function editHandler(record: AddOrUpdateCommonScript) {
    isEditId.value = record.id as string;
    showScriptDrawer.value = true;
  }
  async function handleStatusChange(record: CommonScriptItem) {
    try {
      await updateStatusCommonScript({
        id: record.id,
        status: record.status,
        projectId: record.projectId,
      });
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onMounted(() => {
    initData();
  });

  const searchList = debounce(() => {
    initData();
  }, 300);

  await tableStore.initColumn(TableKeyEnum.ORGANIZATION_PROJECT_COMMON_SCRIPT, columns, 'drawer');
</script>

<style lang="less" scoped>
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
</style>
