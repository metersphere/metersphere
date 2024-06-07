<template>
  <div class="overflow-hidden p-[16px_22px]">
    <div :class="['mb-[16px]', 'flex', 'items-center', props.isApi ? 'justify-between' : 'justify-end']">
      <a-button
        v-show="props.isApi"
        v-permission="['PROJECT_API_DEFINITION_CASE:READ+ADD']"
        type="primary"
        @click="createCase"
      >
        {{ t('caseManagement.featureCase.creatingCase') }}
      </a-button>
      <div class="flex gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadCaseList"
          @press-enter="loadCaseList"
          @clear="loadCaseList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadCaseList">
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
      @drag-change="handleDragChange"
      @module-change="loadCaseList"
    >
      <template v-if="hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+ADD']) && props.isApi" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('apiTestManagement.tableNoDataAndPlease') }}
          <MsButton class="ml-[8px]" @click="createCase">
            {{ t('caseManagement.featureCase.creatingCase') }}
          </MsButton>
        </div>
      </template>
      <template #num="{ record }">
        <MsButton type="text" @click="isApi ? openCaseDetailDrawer(record.id) : openCaseTab(record)">
          {{ record.num }}
        </MsButton>
      </template>
      <template #protocol="{ record }">
        <apiMethodName :method="record.protocol" />
      </template>
      <template #caseLevel="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])"
          v-model:model-value="record.priority"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
          @change="() => handleCaseLevelChange(record)"
        >
          <template #label>
            <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.priority" /></span>
          </template>
          <a-option v-for="item of casePriorityOptions" :key="item.value" :value="item.value">
            <caseLevel :case-level="item.label as CaseLevel" />
          </a-option>
        </a-select>
        <span v-else class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.priority" /></span>
      </template>
      <!-- 用例等级 -->
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
        <caseLevel :case-level="filterContent.value" />
      </template>
      <template #status="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])"
          v-model:model-value="record.status"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <apiStatus :status="record.status" />
          </template>
          <a-option v-for="item of Object.values(RequestCaseStatus)" :key="item" :value="item">
            <apiStatus :status="item" />
          </a-option>
        </a-select>
        <apiStatus v-else :status="record.status" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_STATUS]="{ filterContent }">
        <apiStatus :status="filterContent.value" />
      </template>
      <template #createName="{ record }">
        <a-tooltip :content="`${record.createName}`" position="tl">
          <div class="one-line-text">{{ characterLimit(record.createName) }}</div>
        </a-tooltip>
      </template>
      <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
        <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
      </template>
      <template #lastReportStatus="{ record }">
        <ExecutionStatus
          :module-type="ReportEnum.API_REPORT"
          :status="record.lastReportStatus"
          :class="[!record.lastReportId ? '' : 'cursor-pointer']"
          @click="showResult(record)"
        />
      </template>
      <template #passRateColumn>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('case.passRate') }}
          <a-tooltip :content="t('case.passRateTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
      <template #operation="{ record }">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
          type="text"
          class="!mr-0"
          @click="editOrCopyCase(record, false)"
        >
          {{ t('common.edit') }}
        </MsButton>
        <a-divider
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
          direction="vertical"
          :margin="8"
        ></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="isApi ? openCaseDetailDrawerAndExecute(record.id) : openCaseTabAndExecute(record)"
        >
          {{ t('apiTestManagement.execute') }}
        </MsButton>
        <a-divider
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
          direction="vertical"
          :margin="8"
        ></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+ADD']"
          type="text"
          class="!mr-0"
          @click="editOrCopyCase(record, true)"
        >
          {{ t('common.copy') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="tableMoreActionList" @select="handleTableMoreActionSelect($event, record)" />
      </template>
    </ms-base-table>
  </div>
  <a-modal
    v-model:visible="showBatchEditModal"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="480"
  >
    <template #title>
      {{ t('common.edit') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('case.batchModalSubTitle', {
            count: batchParams.currentSelectCount || tableSelected.length,
          })
        }}
      </div>
    </template>
    <a-form ref="batchFormRef" class="rounded-[4px]" :model="batchForm" layout="vertical">
      <a-form-item
        field="attr"
        :label="t('apiTestManagement.chooseAttr')"
        :rules="[{ required: true, message: t('apiTestManagement.attrRequired') }]"
        asterisk-position="end"
      >
        <a-select v-model="batchForm.attr" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of attrOptions" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'tags'"
        field="values"
        :label="t('apiTestManagement.batchUpdate')"
        :validate-trigger="['blur', 'input']"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
        required
      >
        <MsTagsInput
          v-model:model-value="batchForm.values"
          placeholder="common.tagsInputPlaceholder"
          allow-clear
          unique-value
          retain-input-value
        />
      </a-form-item>
      <a-form-item
        v-else
        field="value"
        :label="t('apiTestManagement.batchUpdate')"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-select v-model="batchForm.value" :placeholder="t('common.pleaseSelect')" :disabled="batchForm.attr === ''">
          <a-option v-for="item of valueOptions" :key="item.value" :value="item.value">
            {{ t(item.label) }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="flex" :class="[batchForm.attr === 'tags' ? 'justify-between' : 'justify-end']">
        <div
          v-if="batchForm.attr === 'tags'"
          class="flex flex-row items-center justify-center"
          style="padding-top: 10px"
        >
          <a-switch v-model="batchForm.append" class="mr-1" size="small" type="line" />
          <span class="flex items-center">
            <span class="mr-1">{{ t('caseManagement.featureCase.appendTag') }}</span>
            <span class="mt-[2px]">
              <a-tooltip>
                <IconQuestionCircle class="h-[16px] w-[16px] text-[rgb(var(--primary-5))]" />
                <template #content>
                  <div>{{ t('caseManagement.featureCase.enableTags') }}</div>
                  <div>{{ t('caseManagement.featureCase.closeTags') }}</div>
                </template>
              </a-tooltip>
            </span>
          </span>
        </div>
        <div class="flex justify-end">
          <a-button type="secondary" :disabled="batchEditLoading" @click="cancelBatchEdit">
            {{ t('common.cancel') }}
          </a-button>
          <a-button class="ml-3" type="primary" :loading="batchEditLoading" @click="handleBatchEditCase">
            {{ t('common.update') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
  <createAndEditCaseDrawer
    ref="createAndEditCaseDrawerRef"
    :api-detail="apiDetail"
    @load-case="loadCaseListAndResetSelector()"
  />
  <caseDetailDrawer
    v-model:visible="caseDetailDrawerVisible"
    v-model:execute-case="caseExecute"
    :detail="caseDetail as RequestParam"
    :api-detail="apiDetail as RequestParam"
    @update-follow="caseDetail.follow = !caseDetail.follow"
    @load-case="(id: string) => loadCase(id)"
    @delete-case="deleteCaseByDetail"
  />
  <batchRunModal
    v-model:visible="showBatchExecute"
    :batch-condition-params="batchConditionParams"
    :batch-params="batchParams"
    :table-selected="tableSelected"
    :batch-run-func="batchExecuteCase"
    @finished="loadCaseListAndResetSelector"
  />
  <!-- 执行结果抽屉 -->
  <caseAndScenarioReportDrawer v-model:visible="showExecuteResult" :report-id="activeReportId" />
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import caseDetailDrawer from './caseDetailDrawer.vue';
  import createAndEditCaseDrawer from './createAndEditCaseDrawer.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import BatchRunModal from '@/views/api-test/components/batchRunModal.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import {
    batchDeleteCase,
    batchEditCase,
    batchExecuteCase,
    deleteCase,
    dragSort,
    getCaseDetail,
    getCasePage,
    updateCasePriority,
    updateCaseStatus,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit, operationWidth } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { DragSortParams } from '@/models/common';
  import { RequestCaseStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions, caseStatusOptions } from '@/views/api-test/components/config';
  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const props = defineProps<{
    isApi: boolean; // 接口定义详情的case tab下
    activeModule: string;
    selectedProtocols: string[]; // 查看的协议类型
    apiDetail?: RequestParam;
    offspringIds: string[];
    memberOptions: { label: string; value: string }[];
    heightUsed?: number;
  }>();

  const caseExecute = ref(false);

  const emit = defineEmits(['openCaseTab', 'openCaseTabAndExecute']);

  const appStore = useAppStore();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const { openModal } = useModal();

  const keyword = ref('');

  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'PROJECT_API_DEFINITION_CASE:READ+DELETE',
      'PROJECT_API_DEFINITION_CASE:READ+ADD',
      'PROJECT_API_DEFINITION_CASE:READ+EXECUTE',
    ])
  );

  const requestCaseStatusOptions = computed(() => {
    return Object.values(RequestCaseStatus).map((key) => {
      return {
        value: key,
        label: key,
      };
    });
  });
  const lastReportStatusListOptions = computed(() => {
    return Object.keys(ReportStatus).map((key) => {
      return {
        value: key,
        ...Object.keys(ReportStatus[key]),
      };
    });
  });
  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 130,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      slotName: 'protocol',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: requestCaseStatusOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_STATUS,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      showDrag: true,
      width: 400,
    },
    {
      title: 'case.lastReportStatus',
      dataIndex: 'lastReportStatus',
      slotName: 'lastReportStatus',
      filterConfig: {
        options: lastReportStatusListOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
      },
      showInTable: false,
      width: 150,
      showDrag: true,
    },
    {
      title: 'case.passRate',
      dataIndex: 'passRate',
      titleSlotName: 'passRateColumn',
      showInTable: false,
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'case.caseEnvironment',
      dataIndex: 'environmentName',
      showTooltip: true,
      showInTable: false,
      width: 150,
      showDrag: true,
    },
    {
      title: 'case.tableColumnUpdateUser',
      dataIndex: 'updateUser',
      showInTable: false,
      showTooltip: true,
      width: 180,
      showDrag: true,
    },
    {
      title: 'case.tableColumnUpdateTime',
      dataIndex: 'updateTime',
      showInTable: false,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: 'case.tableColumnCreateUser',
      slotName: 'createName',
      dataIndex: 'createUser',
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        placeholderText: t('caseManagement.featureCase.PleaseSelect'),
      },
      showInTable: true,
      showTooltip: true,
      width: 180,
      showDrag: true,
    },
    {
      title: 'case.tableColumnCreateTime',
      dataIndex: 'createTime',
      showTooltip: true,
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: operationWidth(230, hasOperationPermission.value ? 200 : 50),
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getCasePage, {
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.API_TEST_MANAGEMENT_CASE,
    showSetting: true,
    selectable: hasAnyPermission([
      'PROJECT_API_DEFINITION_CASE:READ+DELETE',
      'PROJECT_API_DEFINITION_CASE:READ+EXECUTE',
      'PROJECT_API_DEFINITION_CASE:READ+UPDATE',
    ]),
    showSelectAll: true,
    draggable: hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])
      ? { type: 'handle', width: 32 }
      : undefined,
    heightUsed: (props.heightUsed || 0) + 282,
    showSubdirectory: true,
    paginationSize: 'mini',
  });
  const batchActions = {
    baseAction: [
      {
        label: 'common.edit',
        eventTag: 'edit',
        permission: ['PROJECT_API_DEFINITION_CASE:READ+UPDATE'],
      },
      {
        label: 'system.log.operateType.execute',
        eventTag: 'execute',
        permission: ['PROJECT_API_DEFINITION_CASE:READ+EXECUTE'],
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_API_DEFINITION_CASE:READ+DELETE'],
      },
    ],
  };
  const tableMoreActionList = [
    {
      eventTag: 'delete',
      label: t('common.delete'),
      danger: true,
      permission: ['PROJECT_API_DEFINITION_CASE:READ+DELETE'],
    },
  ];

  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all') {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_TEST_MANAGEMENT_CASE);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }
  async function loadCaseList() {
    const selectModules = await getModuleIds();
    const params = {
      apiDefinitionId: props.apiDetail?.id,
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: selectModules,
      protocols: props.selectedProtocols,
    };
    setLoadListParams(params);
    loadList();
  }
  function loadCaseListAndResetSelector() {
    resetSelector();
    loadCaseList();
  }

  onBeforeMount(() => {
    loadCaseList();
  });

  watch(
    () => props.activeModule,
    () => {
      loadCaseListAndResetSelector();
    }
  );

  watch(
    () => props.selectedProtocols,
    () => {
      if (props.isApi) return;
      loadCaseListAndResetSelector();
    }
  );

  async function handleStatusChange(record: ApiCaseDetail) {
    try {
      await updateCaseStatus(record.id, record.status);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function handleCaseLevelChange(record: ApiCaseDetail) {
    try {
      await updateCasePriority(record.id, record.priority);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 拖拽排序
  async function handleDragChange(params: DragSortParams) {
    try {
      await dragSort(params);
      Message.success(t('caseManagement.featureCase.sortSuccess'));
      loadCaseList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const tableSelected = ref<(string | number)[]>([]); // 表格选中的
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  async function genBatchConditionParams() {
    const selectModules = await getModuleIds();
    return {
      condition: {
        keyword: keyword.value,
        filter: propsRes.value.filter,
      },
      projectId: appStore.currentProjectId,
      protocols: props.selectedProtocols,
      moduleIds: selectModules,
      apiDefinitionId: props.apiDetail?.id as string,
    };
  }

  function handleDeleteCase(record?: ApiCaseDetail, isBatch?: boolean) {
    const title = isBatch
      ? t('case.batchDeleteCaseTip', {
          count: batchParams.value.currentSelectCount || tableSelected.value.length,
        })
      : t('apiTestManagement.deleteApiTipTitle', { name: characterLimit(record?.name) });

    openModal({
      type: 'error',
      title,
      content: t('case.deleteCaseTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            const batchConditionParams = await genBatchConditionParams();
            await batchDeleteCase({
              selectIds: tableSelected.value as string[],
              selectAll: batchParams.value.selectAll,
              excludeIds: batchParams.value?.excludeIds || [],
              ...batchConditionParams,
            });
          } else {
            await deleteCase(record?.id as string);
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadCaseListAndResetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 处理表格更多按钮事件
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiCaseDetail) {
    switch (item.eventTag) {
      case 'delete':
        handleDeleteCase(record);
        break;
      default:
        break;
    }
  }

  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  // 用例编辑
  const showBatchEditModal = ref(false);
  // 用例执行
  const showBatchExecute = ref(false);
  const batchEditLoading = ref(false);
  const batchFormRef = ref<FormInstance>();
  const batchForm = ref({
    attr: '',
    value: '',
    values: [],
    append: false,
  });

  const attrOptions = [
    {
      name: 'case.caseLevel',
      value: 'priority',
    },
    {
      name: 'apiTestManagement.apiStatus',
      value: 'status',
    },
    {
      name: 'common.tag',
      value: 'tags',
    },
  ];
  const valueOptions = computed(() => {
    batchForm.value.value = '';
    switch (batchForm.value.attr) {
      case 'priority':
        return casePriorityOptions;
      case 'status':
        return caseStatusOptions;
      default:
        return [];
    }
  });

  function cancelBatchEdit() {
    showBatchEditModal.value = false;
    batchFormRef.value?.resetFields();
    batchForm.value = {
      attr: '',
      value: '',
      values: [],
      append: false,
    };
  }
  function handleBatchEditCase() {
    batchFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchEditLoading.value = true;
          const batchConditionParams = await genBatchConditionParams();
          await batchEditCase({
            selectIds: batchParams.value?.selectedIds || [],
            selectAll: !!batchParams.value?.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            ...batchConditionParams,
            type: batchForm.value.attr.charAt(0).toUpperCase() + batchForm.value.attr.slice(1), // 首字母大写
            append: batchForm.value.append,
            [batchForm.value.attr]: batchForm.value.attr === 'tags' ? batchForm.value.values : batchForm.value.value,
          });
          Message.success(t('common.updateSuccess'));
          cancelBatchEdit();
          resetSelector();
          loadCaseListAndResetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchEditLoading.value = false;
        }
      }
    });
  }

  const batchConditionParams = ref<any>();

  // 处理表格选中后批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        handleDeleteCase(undefined, true);
        break;
      case 'edit':
        showBatchEditModal.value = true;
        break;
      case 'execute':
        genBatchConditionParams().then((data) => {
          batchConditionParams.value = data;
          showBatchExecute.value = true;
        });
        break;
      default:
        break;
    }
  }

  const createAndEditCaseDrawerRef = ref<InstanceType<typeof createAndEditCaseDrawer>>();
  function createCase() {
    createAndEditCaseDrawerRef.value?.open(props.apiDetail?.id as string);
  }

  function openCaseTab(record: ApiCaseDetail) {
    emit('openCaseTab', record);
  }

  const caseDetailDrawerVisible = ref(false);

  const defaultCaseParams = inject<RequestParam>('defaultCaseParams');
  const caseDetail = ref<Record<string, any>>({});

  async function getCaseDetailInfo(id: string) {
    try {
      const res = await getCaseDetail(id);
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      caseDetail.value = {
        ...cloneDeep(defaultCaseParams as RequestParam),
        ...({
          ...res.request,
          ...res,
          url: res.path,
          ...parseRequestBodyResult,
        } as Partial<TabItem>),
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  async function openCaseDetailDrawer(id: string) {
    await getCaseDetailInfo(id);
    caseExecute.value = false;
    caseDetailDrawerVisible.value = true;
  }

  async function openCaseDetailDrawerAndExecute(id: string) {
    await getCaseDetailInfo(id);
    caseExecute.value = true;
    caseDetailDrawerVisible.value = true;
  }

  function openCaseTabAndExecute(record: ApiCaseDetail) {
    emit('openCaseTabAndExecute', record);
  }

  function deleteCaseByDetail() {
    caseDetailDrawerVisible.value = false;
    loadCaseList();
  }

  async function editOrCopyCase(record: ApiCaseDetail, isCopy: boolean) {
    await getCaseDetailInfo(record.id);
    createAndEditCaseDrawerRef.value?.open(record.apiDefinitionId, caseDetail.value as RequestParam, isCopy);
  }

  // 在api下的用例里打开用例详情抽屉，点击编辑，编辑后在此刷新数据
  function loadCase(id: string) {
    getCaseDetailInfo(id);
    loadCaseList();
  }

  const activeReportId = ref('');
  const showExecuteResult = ref(false);
  async function showResult(record: ApiCaseDetail) {
    if (!record.lastReportId) return;
    activeReportId.value = record.lastReportId;
    showExecuteResult.value = true;
  }

  defineExpose({
    loadCaseList,
  });

  await tableStore.initColumn(TableKeyEnum.API_TEST_MANAGEMENT_CASE, columns, 'drawer', true);
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
