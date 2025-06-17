<template>
  <div class="overflow-hidden p-[16px_22px]">
    <div :class="['mb-[16px]', 'flex', 'items-center', 'justify-between']">
      <div class="flex gap-[12px]">
        <a-button
          v-show="props.isApi"
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+ADD']"
          type="primary"
          @click="createCase"
        >
          {{ t('caseManagement.featureCase.creatingCase') }}
        </a-button>
        <MsAiButton v-if="aiStore.aiSourceNameList.length > 0" :text="t('settings.navbar.ai')" @click="openAI" />
      </div>
      <MsAdvanceFilter
        ref="msAdvanceFilterRef"
        v-model:keyword="keyword"
        :view-type="ViewTypeEnum.API_CASE"
        :filter-config-list="filterConfigList"
        :search-placeholder="t('apiTestManagement.searchPlaceholder')"
        :view-name="viewName"
        @keyword-search="loadCaseList()"
        @adv-search="handleAdvSearch"
        @refresh="loadCaseList()"
      />
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      :not-show-table-filter="isAdvancedSearchMode"
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
      @drag-change="handleDragChange"
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
        <div class="flex min-w-0 flex-nowrap items-center gap-[8px]">
          <MsAiTag v-if="record.aiCreate" />
          <MsButton class="!mr-0 flex-1 overflow-hidden" type="text" @click="openCaseTab(record)">
            <a-tooltip :content="record.num.toString()" :mouse-enter-delay="300">
              <div class="one-line-text">
                {{ record.num }}
              </div>
            </a-tooltip>
          </MsButton>
          <a-tooltip v-if="record.apiChange" class="ms-tooltip-white">
            <!-- 接口参数发生变更提示 -->
            <MsIcon class="flex-shrink-0" type="icon-icon_warning_colorful" size="16" />
            <template #content>
              <div class="flex flex-row">
                <span class="text-[var(--color-text-1)]">
                  {{ t('case.apiParamsHasChange') }}
                </span>
                <MsButton class="ml-[8px]" @click="showDifferences(record)">
                  {{ t('case.changeDifferences') }}
                </MsButton>
              </div>
            </template>
          </a-tooltip>
        </div>
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
        <a-tooltip :content="`${record.createName}`" position="tr">
          <div class="one-line-text">{{ record.createName }}</div>
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
        :class="`${selectedTagType === TagUpdateTypeEnum.CLEAR ? 'mb-0' : 'mb-[16px]'}`"
        field="type"
        :label="t('common.type')"
      >
        <a-radio-group v-model:model-value="selectedTagType" size="small">
          <a-radio :value="TagUpdateTypeEnum.UPDATE"> {{ t('common.update') }}</a-radio>
          <a-radio :value="TagUpdateTypeEnum.APPEND"> {{ t('caseManagement.featureCase.appendTag') }}</a-radio>
          <a-radio :value="TagUpdateTypeEnum.CLEAR">{{ t('common.clear') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'tags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
        field="values"
        :label="t('common.batchUpdate')"
        :validate-trigger="['blur', 'input']"
        :rules="[{ required: false, message: t('common.inputPleaseEnterTags') }]"
        asterisk-position="end"
        class="mb-0"
        required
      >
        <MsTagsInput
          v-model:model-value="batchForm.values"
          placeholder="common.tagsInputPlaceholder"
          allow-clear
          unique-value
          empty-priority-highest
          retain-input-value
        />
        <div class="text-[12px] leading-[20px] text-[var(--color-text-4)]">{{ t('ms.tagsInput.tagLimitTip') }}</div>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr !== 'tags'"
        field="value"
        :label="t('common.batchUpdate')"
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
      <div class="flex justify-end">
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
    v-model:visible="showCaseVisible"
    :api-detail="apiDetail"
    @load-case="loadCaseListAndResetSelector()"
    @show-diff="showDifferences"
  />
  <!-- TODO 之后要去掉 使用页面代替抽屉 -->
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
  <caseAndScenarioReportDrawer
    v-model:visible="showExecuteResult"
    :case-name="currentCaseName"
    :case-id="currentId"
    is-filter-step
    :report-id="activeReportId"
  />
  <!-- 同步抽屉 -->
  <SyncModal
    ref="syncModalRef"
    v-model:visible="showSyncModal"
    :loading="syncLoading"
    :batch-params="batchParams"
    @batch-sync="handleBatchSync"
  />
  <!-- diff对比抽屉 -->
  <DifferentDrawer
    v-model:visible="showDifferentDrawer"
    :active-api-case-id="activeApiCaseId"
    :active-defined-id="activeDefinedId"
    @close="closeDifferent"
    @clear-this-change="handleClearThisChange"
    @sync="syncParamsHandler"
    @load-list="loadCaseListAndResetSelector"
  />
  <!-- AI 生成 -->
  <MsAIDrawer
    v-if="isInitAiDrawer"
    v-model:visible="aiDrawerVisible"
    type="api"
    :api-definition-id="props.apiDetail?.id"
    @sync-api-case="handleSyncApiCase"
    @sync-success="loadCaseList()"
  />
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsAdvanceFilter from '@/components/pure/ms-advance-filter/index.vue';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsAiButton from '@/components/pure/ms-ai-button/index.vue';
  import MsAiTag from '@/components/pure/ms-ai-tag/index.vue';
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
  import DifferentDrawer from './differentDrawer.vue';
  import SyncModal from './syncModal.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import BatchRunModal from '@/views/api-test/components/batchRunModal.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import {
    batchDeleteCase,
    batchEditCase,
    batchExecuteCase,
    caseTableBatchSync,
    deleteCase,
    dragSort,
    getCaseDetail,
    getCasePage,
    getCaseStatistics,
    updateCasePriority,
    updateCaseStatus,
  } from '@/api/modules/api-test/management';
  import { NAV_NAVIGATION } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import useAIStore from '@/store/modules/setting/ai';
  import { characterLimit, operationWidth } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ProtocolItem } from '@/models/apiTest/common';
  import type { batchSyncForm } from '@/models/apiTest/management';
  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { DragSortParams } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { RequestCaseStatus } from '@/enums/apiEnum';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';
  import { TagUpdateTypeEnum } from '@/enums/commonEnum';
  import { ReportEnum } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { WorkNavValueEnum } from '@/enums/workbenchEnum';

  import {
    casePriorityOptions,
    caseStatusOptions,
    lastReportStatusListOptions,
  } from '@/views/api-test/components/config';
  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const MsAIDrawer = defineAsyncComponent(() => import('@/components/business/ms-ai-drawer/index.vue'));

  defineOptions({
    name: CacheTabTypeEnum.API_TEST_CASE_TABLE,
  });
  const props = defineProps<{
    isApi: boolean; // 接口定义详情的case tab下
    activeModule: string;
    selectedProtocols: string[]; // 查看的协议类型
    apiDetail?: RequestParam;
    offspringIds: string[];
    heightUsed?: number;
  }>();
  const cacheStore = useCacheStore();

  const caseExecute = ref(false);

  const emit = defineEmits<{
    (e: 'openCaseTab', record: ApiCaseDetail): void;
    (e: 'openCaseTabAndExecute', record: ApiCaseDetail): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();

  const route = useRoute();
  const appStore = useAppStore();
  const aiStore = useAIStore();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const { openModal } = useModal();

  const keyword = ref('');

  const isInitAiDrawer = ref<boolean>(false);
  const aiDrawerVisible = ref<boolean>(false);
  function openAI() {
    isInitAiDrawer.value = true;
    aiDrawerVisible.value = true;
  }

  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'PROJECT_API_DEFINITION_CASE:READ+DELETE',
      'PROJECT_API_DEFINITION_CASE:READ+ADD',
      'PROJECT_API_DEFINITION_CASE:READ+EXECUTE',
    ])
  );
  const protocolList = inject<Ref<ProtocolItem[]>>('protocols', ref([]));
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
      width: 170,
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
      width: 80,
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
      width: 100,
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
        options: caseStatusOptions,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_STATUS,
        disabledTooltip: true,
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
      dataIndex: 'updateName',
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
      },
      showInTable: true,
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
  const { propsRes, propsEvent, viewId, advanceFilter, setAdvanceFilter, loadList, setLoadListParams, resetSelector } =
    useTable(getCasePage, {
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
        label: 'case.apiSyncChange',
        eventTag: 'sync',
        permission: ['PROJECT_API_DEFINITION_CASE:READ+UPDATE'],
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

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all' && !isAdvancedSearchMode.value) {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_TEST_MANAGEMENT_CASE);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }

  async function initStatistics() {
    try {
      const res = await getCaseStatistics(propsRes.value.data.map((item) => item.id));
      propsRes.value.data.forEach((e) => {
        const item = res.find((i: any) => i.scenarioId === e.id);
        if (item) {
          e.passRate = item.passRate;
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => propsRes.value.data,
    (arr) => {
      if (arr.length > 0) {
        initStatistics();
      }
    }
  );

  async function loadCaseList() {
    const selectModules = await getModuleIds();

    const params = {
      apiDefinitionId: props.apiDetail?.id,
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: selectModules,
      protocols: isAdvancedSearchMode.value ? protocolList.value.map((item) => item.protocol) : props.selectedProtocols,
      filter: propsRes.value.filter,
      viewId: viewId.value,
      combineSearch: advanceFilter,
    };
    setLoadListParams(params);
    await loadList();
  }
  function loadCaseListAndResetSelector() {
    resetSelector();
    loadCaseList();
  }

  const isActivated = computed(() => cacheStore.cacheViews.includes(CacheTabTypeEnum.API_TEST_CASE_TABLE));

  onBeforeMount(() => {
    cacheStore.clearCache();
    if (!isActivated.value) {
      loadCaseList();
      cacheStore.setCache(CacheTabTypeEnum.API_TEST_CASE_TABLE);
    }
  });

  onActivated(() => {
    if (isActivated.value) {
      loadCaseList();
    }
  });

  watch(
    () => props.activeModule,
    () => {
      if (isAdvancedSearchMode.value) return;
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

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'protocol',
        valueKey: 'protocol',
        options: protocolList.value,
      },
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: casePriorityOptions,
      },
    },
    {
      title: 'case.apiParamsChange',
      dataIndex: 'apiChange',
      type: FilterType.SELECT_EQUAL,
      selectProps: {
        options: [
          { label: t('case.withoutChanges'), value: false },
          { label: t('case.withChanges'), value: true },
        ],
      },
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: caseStatusOptions.map((item) => ({ label: t(item.label), value: item.value })),
      },
    },
    {
      title: 'case.lastReportStatus',
      dataIndex: 'lastReportStatus',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: lastReportStatusListOptions.value,
      },
    },
    {
      title: 'case.caseEnvironment',
      dataIndex: 'environmentName',
      type: FilterType.SELECT,
      selectProps: {
        labelKey: 'name',
        valueKey: 'id',
        multiple: true,
        options: appStore.envList,
      },
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'common.creator',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
    {
      title: 'common.updateUserName',
      dataIndex: 'updateUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      type: FilterType.DATE_PICKER,
    },
  ]);

  const viewName = ref('');

  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string, isStartAdvance: boolean) => {
    resetSelector();
    emit('handleAdvSearch', isStartAdvance);
    keyword.value = '';
    setAdvanceFilter(filter, id);
    await loadCaseList(); // 基础筛选都清空
  };

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
        viewId: viewId.value,
        combineSearch: advanceFilter,
      },
      projectId: appStore.currentProjectId,
      protocols: isAdvancedSearchMode.value ? protocolList.value.map((item) => item.protocol) : props.selectedProtocols,
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

  const selectedTagType = ref<TagUpdateTypeEnum>(TagUpdateTypeEnum.UPDATE);

  function cancelBatchEdit() {
    showBatchEditModal.value = false;
    batchFormRef.value?.resetFields();
    batchForm.value = {
      attr: '',
      value: '',
      values: [],
      append: false,
    };
    selectedTagType.value = TagUpdateTypeEnum.UPDATE;
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
            append: selectedTagType.value === TagUpdateTypeEnum.APPEND,
            clear: selectedTagType.value === TagUpdateTypeEnum.CLEAR,
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

  const showSyncModal = ref<boolean>(false);
  function syncParams() {
    showSyncModal.value = true;
  }

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
      case 'sync':
        syncParams();
        break;
      default:
        break;
    }
  }

  const createAndEditCaseDrawerRef = ref<InstanceType<typeof createAndEditCaseDrawer>>();
  function createCase() {
    createAndEditCaseDrawerRef.value?.open(props.apiDetail?.id as string);
  }

  function handleSyncApiCase(detail: ApiCaseDetail) {
    aiDrawerVisible.value = false;
    createAndEditCaseDrawerRef.value?.open(props.apiDetail?.id as string, detail, false, true);
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
  const currentId = ref<string>('');
  const currentCaseName = ref<string>('');
  const showExecuteResult = ref(false);
  async function showResult(record: ApiCaseDetail) {
    if (!record.lastReportId) return;
    activeReportId.value = record.lastReportId;
    currentId.value = record.id;
    currentCaseName.value = record.name;
    showExecuteResult.value = true;
  }

  const activeApiCaseId = ref<string>('');
  const activeDefinedId = ref<string>('');
  const showDifferentDrawer = ref<boolean>(false);

  async function showDifferences(record: ApiCaseDetail) {
    activeApiCaseId.value = record.id;
    activeDefinedId.value = record.apiDefinitionId;
    showDifferentDrawer.value = true;
  }
  function closeDifferent() {
    showDifferentDrawer.value = false;
    activeApiCaseId.value = '';
    activeDefinedId.value = '';
  }

  const syncLoading = ref<boolean>(false);
  const syncModalRef = ref<InstanceType<typeof SyncModal>>();
  // 批量同步
  async function handleBatchSync(syncForm: batchSyncForm) {
    try {
      syncLoading.value = true;
      const selectModules = await getModuleIds();
      const params = await genBatchConditionParams();
      await caseTableBatchSync({
        selectIds: batchParams.value?.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        ...params,
        ...syncForm,
        moduleIds: selectModules,
      });
      Message.success(t('bugManagement.syncSuccess'));
      syncModalRef.value?.resetForm();
      resetSelector();
      loadCaseListAndResetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      syncLoading.value = false;
    }
  }
  const showCaseVisible = ref(false);
  // 清除本次变更
  async function handleClearThisChange(isEvery: boolean) {
    await loadCaseList();
    await getCaseDetailInfo(activeApiCaseId.value);
    if (showCaseVisible.value) {
      createAndEditCaseDrawerRef.value?.open(caseDetail.value.apiDefinitionId, caseDetail.value as RequestParam, false);
    }
    if (isEvery) {
      showDifferentDrawer.value = false;
    }
  }

  // 对比抽屉同步成功打开编辑
  async function syncParamsHandler(mergedRequestParam: RequestParam) {
    await getCaseDetailInfo(activeApiCaseId.value);
    caseDetail.value = { ...caseDetail.value, ...mergedRequestParam };
    createAndEditCaseDrawerRef.value?.open(caseDetail.value.apiDefinitionId, caseDetail.value as RequestParam, false);
  }

  onBeforeMount(() => {
    if (route.query.view) {
      setAdvanceFilter({ conditions: [], searchMode: 'AND' }, route.query.view as string);
      viewName.value = route.query.view as string;
    }

    if (route.query.home) {
      propsRes.value.filter = { ...NAV_NAVIGATION[route.query.home as WorkNavValueEnum] };
    }

    if (route.query.openAi === 'Y') {
      openAI();
    }
  });

  defineExpose({
    loadCaseList,
    isAdvancedSearchMode,
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
