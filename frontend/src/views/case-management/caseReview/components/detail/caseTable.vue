<template>
  <div class="h-full px-[24px] py-[16px]">
    <div class="mb-[16px]">
      <MsAdvanceFilter
        ref="msAdvanceFilterRef"
        v-model:keyword="keyword"
        :view-type="ViewTypeEnum.REVIEW_FUNCTIONAL_CASE"
        :filter-config-list="filterConfigList"
        :custom-fields-config-list="searchCustomFields"
        :count="modulesCount[props.activeFolder] || 0"
        :name="moduleNamePath"
        :not-show-input-search="showType !== 'list'"
        :search-placeholder="t('caseManagement.caseReview.searchPlaceholder')"
        @keyword-search="searchCase()"
        @adv-search="handleAdvSearch"
        @refresh="handleRefreshAll"
      >
        <template v-if="showType !== 'list'" #nameRight>
          <div v-if="reviewPassRule === 'MULTIPLE'" class="ml-[16px]">
            <a-switch v-model:model-value="onlyMineStatus" size="small" class="mr-[4px]" type="line" />
            {{ t('caseManagement.caseReview.myReviewStatus') }}
          </div>
          <span class="ml-[16px] !text-[rgb(var(--warning-6))]">
            {{ t('caseManagement.caseReview.cannotReviewTip') }}
          </span>
        </template>
        <template #right>
          <a-radio-group
            v-model:model-value="showType"
            type="button"
            size="small"
            class="list-show-type"
            @change="handleShowTypeChange"
          >
            <a-radio value="list" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_view-list_outlined" />
            </a-radio>
            <a-radio value="minder" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_mindnote_outlined" />
            </a-radio>
          </a-radio-group>
        </template>
      </MsAdvanceFilter>
    </div>
    <!-- 表格 -->
    <template v-if="showType === 'list'">
      <ms-base-table
        v-bind="propsRes"
        :action-config="batchActions"
        no-disable
        filter-icon-align-left
        :not-show-table-filter="isAdvancedSearchMode"
        v-on="propsEvent"
        @filter-change="getModuleCount"
        @batch-action="handleTableBatch"
        @sorter-change="handleSorterChange"
      >
        <template #[FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_RESULT]="{ filterContent }">
          <a-tag :color="reviewResultMap[filterContent.value as ReviewResult].color" class="px-[4px]" size="small">
            {{ t(reviewResultMap[filterContent.value as ReviewResult].label) }}
          </a-tag>
        </template>
        <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
          <caseLevel :case-level="filterContent.text" />
        </template>
        <template #reviewerTitle="{ columnConfig }">
          <div class="flex items-center gap-[4px]">
            <div class="text-[var(--color-text-3)]">{{ t(columnConfig.title as string) }}</div>
            <a-tooltip
              v-model:popupVisible="reviewerTitlePopupVisible"
              :content="t('caseManagement.caseReview.reviewerTip')"
            >
              <icon-question-circle
                class="text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </template>
        <template #num="{ record }">
          <a-tooltip :content="record.num">
            <a-button type="text" class="px-0 !text-[14px] !leading-[22px]" @click="review(record)">
              <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
            </a-button>
          </a-tooltip>
        </template>
        <template #caseLevel="{ record }">
          <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.caseLevel" /></span>
        </template>
        <template #reviewNames="{ record }">
          <MsTagGroup
            v-if="record.showModuleTree"
            :tag-list="record.reviewNames"
            is-string-tag
            :show-num="1"
            allow-edit
            show-table
            theme="outline"
            @click="record.showModuleTree = false"
          />
          <MsSelect
            v-else
            v-model:model-value="record.reviewers"
            v-model:loading="dialogLoading"
            :max-tag-count="1"
            class="w-full"
            :options="reviewersOptions"
            :search-keys="['label']"
            allow-search
            :multiple="true"
            :placeholder="t('project.messageManagement.receiverPlaceholder')"
            :at-least-one="true"
            :fallback-option="
              (val) => ({
                label: reviewersOptions.find((e) => e.value === val)?.label || (val as string),
                value: val,
              })
            "
            @popup-visible-change="(value) => selectChangeReviewer(value, record)"
          >
          </MsSelect>
        </template>
        <template #status="{ record }">
          <div class="flex items-center gap-[4px]">
            <MsIcon
              :type="reviewResultMap[record.status as ReviewResult].icon"
              :style="{
              color: reviewResultMap[record.status as ReviewResult].color
            }"
            />
            {{ t(reviewResultMap[record.status as ReviewResult].label) }}
          </div>
        </template>
        <template #action="{ record }">
          <a-tooltip :content="t('caseManagement.caseReview.reviewDisabledTip')" :disabled="userIsReviewer(record)">
            <MsButton
              v-permission="['CASE_REVIEW:READ+REVIEW']"
              :disabled="!userIsReviewer(record)"
              type="text"
              class="!mr-0"
              @click="review(record)"
            >
              {{ t('caseManagement.caseReview.review') }}
            </MsButton>
          </a-tooltip>
          <a-divider
            v-permission.all="['CASE_REVIEW:READ+REVIEW', 'CASE_REVIEW:READ+RELEVANCE']"
            direction="vertical"
            :margin="8"
          ></a-divider>
          <MsPopconfirm
            :title="t('caseManagement.caseReview.disassociateTip')"
            :sub-title-tip="t('caseManagement.caseReview.disassociateTipContent')"
            :ok-text="t('common.confirm')"
            :loading="disassociateLoading"
            type="error"
            @confirm="(val, done) => handleDisassociateReviewCase(record, done)"
          >
            <MsButton v-permission="['CASE_REVIEW:READ+RELEVANCE']" type="text" class="!mr-0">
              {{ t('caseManagement.caseReview.disassociate') }}
            </MsButton>
          </MsPopconfirm>
        </template>
        <template v-if="keyword.trim() === ''" #empty>
          <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
            {{ t('caseManagement.caseReview.tableNoData') }}
            <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD']" class="ml-[8px]" @click="emit('link')">
              {{ t('caseManagement.featureCase.linkCase') }}
            </MsButton>
          </div>
        </template>
      </ms-base-table>
    </template>
    <!-- 脑图 -->
    <div v-else class="h-[calc(100%-48px)] border-t border-[var(--color-text-n8)]">
      <MsCaseReviewMinder
        ref="msCaseReviewMinderRef"
        :module-id="props.activeFolder"
        :view-status-flag="onlyMineStatus"
        :module-tree="moduleTree"
        :review-progress="props.reviewProgress"
        :review-pass-rule="props.reviewPassRule"
        :table-sorter="tableSorter"
        @operation="handleMinderOperation"
        @handle-review-done="emit('refresh')"
      />
    </div>
    <a-modal
      v-model:visible="dialogVisible"
      class="p-[4px]"
      title-align="start"
      body-class="p-0"
      :width="['review', 'reReview'].includes(dialogShowType) ? 680 : 480"
      :mask-closable="false"
      @close="handleDialogCancel"
    >
      <template #title>
        <div class="flex items-center justify-start">
          <div class="text-[var(--color-text-1)]">
            {{ dialogTitle }}
          </div>
          <a-tooltip
            v-if="dialogShowType === 'review'"
            :content="t('caseManagement.caseReview.batchReviewTip')"
            position="right"
          >
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
          <div v-show="showType === 'list'" class="ml-[8px] font-normal text-[var(--color-text-4)]">
            ({{ t('caseManagement.caseReview.selectedCase', { count: batchParams.currentSelectCount }) }})
          </div>
        </div>
      </template>
      <a-form ref="dialogFormRef" :model="dialogForm" layout="vertical">
        <a-form-item
          v-if="dialogShowType === 'review'"
          field="reason"
          :label="t('caseManagement.caseReview.reviewResult')"
          class="mb-[16px]"
        >
          <a-radio-group v-model:model-value="dialogForm.result">
            <a-radio value="PASS">
              <div class="inline-flex items-center">
                <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
                {{ t('caseManagement.caseReview.pass') }}
              </div>
            </a-radio>
            <a-radio value="UN_PASS">
              <div class="inline-flex items-center">
                <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
                {{ t('caseManagement.caseReview.fail') }}
              </div>
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          v-if="dialogShowType === 'review' || dialogShowType === 'reReview'"
          field="reason"
          :label="t('caseManagement.caseReview.reason')"
          :rules="
            dialogForm.result === 'fail' && dialogShowType !== 'reReview'
              ? [{ required: true, message: t('caseManagement.caseReview.reasonRequired') }]
              : []
          "
          asterisk-position="end"
          class="mb-0"
        >
          <div class="flex w-full items-center">
            <MsRichText
              v-model:raw="dialogForm.reason"
              v-model:commentIds="dialogForm.commentIds"
              v-model:filed-ids="reviewCommentFileIds"
              :upload-image="handleUploadImage"
              :auto-height="false"
              :preview-url="`${PreviewEditorImageUrl}/${appStore.currentProjectId}`"
              class="w-full"
            />
          </div>
        </a-form-item>
        <a-form-item
          v-if="dialogShowType === 'changeReviewer'"
          field="reviewer"
          :label="t('caseManagement.caseReview.chooseReviewer')"
          :rules="[{ required: true, message: t('caseManagement.caseReview.reviewerRequired') }]"
          asterisk-position="end"
          class="mb-0"
        >
          <MsSelect
            v-model:modelValue="dialogForm.reviewer"
            mode="static"
            :loading="reviewerLoading"
            :placeholder="t('caseManagement.caseReview.reviewerPlaceholder')"
            :options="reviewersOptions"
            :search-keys="['label']"
            allow-search
            multiple
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <div class="flex items-center justify-end">
          <div v-if="dialogShowType === 'changeReviewer'" class="mr-auto flex items-center">
            <a-switch v-model:model-value="dialogForm.isAppend" size="small" class="mr-[4px]" type="line"></a-switch>
            {{ t('caseManagement.caseReview.append') }}
            <a-tooltip :content="t('caseManagement.caseReview.reviewResultTip')" position="right">
              <template #content>
                <div>{{ t('caseManagement.caseReview.appendTip1') }}</div>
                <div>{{ t('caseManagement.caseReview.appendTip2') }}</div>
              </template>
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
          <a-button type="secondary" :disabled="dialogLoading" @click="handleDialogCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button
            v-if="dialogShowType === 'review'"
            type="primary"
            class="ml-[12px]"
            :loading="dialogLoading"
            :disabled="submitReviewDisabled"
            @click="commitResult"
          >
            {{ t('caseManagement.caseReview.submitReview') }}
          </a-button>
          <a-button
            v-if="dialogShowType === 'changeReviewer'"
            type="primary"
            class="ml-[12px]"
            :loading="dialogLoading"
            @click="changeReviewer"
          >
            {{ t('common.update') }}
          </a-button>
          <a-button
            v-if="dialogShowType === 'reReview'"
            type="primary"
            class="ml-[12px]"
            :loading="dialogLoading"
            @click="reReview"
          >
            {{ t('caseManagement.caseReview.reReview') }}
          </a-button>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { getFilterCustomFields, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import MsCaseReviewMinder from '@/components/business/ms-minders/caseReviewMinder/index.vue';
  import { getMinderOperationParams } from '@/components/business/ms-minders/caseReviewMinder/utils';
  import MsSelect from '@/components/business/ms-select';

  import {
    batchChangeReviewer,
    batchDisassociateReviewCase,
    batchReview,
    disassociateReviewCase,
    getReviewDetailCasePage,
    getReviewUsers,
  } from '@/api/modules/case-management/caseReview';
  import {
    editorUploadFile,
    getCaseDefaultFields,
    getCustomFieldsTable,
  } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { reviewResultMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import useCaseReviewStore from '@/store/modules/case/caseReview';
  import useUserStore from '@/store/modules/user';
  import { characterLimit, findNodeByKey } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ReviewCaseItem, ReviewItem, ReviewPassRule, ReviewResult } from '@/models/caseManagement/caseReview';
  import { BatchApiParams, TableQueryParams } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { StartReviewStatus } from '@/enums/caseEnum';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { executionResultMap, getCaseLevels } from '@/views/case-management/caseManagementFeature/components/utils';

  const caseLevelFields = ref<Record<string, any>>({});
  const caseLevelList = computed(() => {
    return caseLevelFields.value?.options || [];
  });

  const props = defineProps<{
    activeFolder: string;
    reviewPassRule: ReviewPassRule; // 评审规则
    offspringIds: string[]; // 当前选中节点的所有子节点id
    reviewProgress: string; // 评审进度
  }>();
  const emit = defineEmits(['refresh', 'link', 'selectParentNode', 'handleAdvSearch']);

  const router = useRouter();
  const route = useRoute();
  const cacheStore = useCacheStore();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const caseReviewStore = useCaseReviewStore();

  const minderSelectData = ref<MinderJsonNodeData>(); // 当前脑图选中的数据
  const minderParams = ref();
  const keyword = ref('');
  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  const tableParams = ref<Record<string, any>>({});
  const onlyMineStatus = ref(false);
  const showType = ref<'list' | 'minder'>('list');
  const msCaseReviewMinderRef = ref<InstanceType<typeof MsCaseReviewMinder>>();

  const moduleTree = computed(() => unref(caseReviewStore.moduleTree));

  async function initModules() {
    await caseReviewStore.initModules(route.query.id as string);
  }

  const moduleNamePath = computed(() => {
    return props.activeFolder === 'all'
      ? t('caseManagement.featureCase.allCase')
      : findNodeByKey<Record<string, any>>(moduleTree.value, props.activeFolder, 'id')?.name;
  });

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['CASE_REVIEW:READ+REVIEW', 'CASE_REVIEW:READ+RELEVANCE'])
  );

  const executeResultOptions = computed(() => {
    return Object.keys(executionResultMap).map((key) => {
      return {
        value: key,
        label: executionResultMap[key].statusText,
      };
    });
  });
  const reviewResultOptions = computed(() => {
    return Object.keys(reviewResultMap).map((key) => {
      return {
        value: key,
        label: t(reviewResultMap[key as ReviewResult].label),
      };
    });
  });
  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.caseName',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 150,
    },
    {
      title: 'caseManagement.featureCase.tableColumnLevel',
      slotName: 'caseLevel',
      dataIndex: 'caseLevel',
      showInTable: true,
      showDrag: true,
      filterConfig: {
        options: caseLevelList.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.reviewer',
      dataIndex: 'reviewNames',
      slotName: 'reviewNames',
      showInTable: true,
      titleSlotName: 'reviewerTitle',
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.reviewResult',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: reviewResultOptions.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_RESULT,
      },
      width: 110,
    },
    // {
    //   title: 'caseManagement.caseReview.version',
    //   dataIndex: 'versionName',
    //   width: 100,
    //   showTooltip: true,
    // },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
      },
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 140 : 50,
    },
  ];
  const tableStore = useTableStore();
  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    setAdvanceFilter,
    loadList,
    setLoadListParams,
    resetSelector,
    getTableQueryParams,
  } = useTable(
    getReviewDetailCasePage,
    {
      scroll: { x: '100%' },
      tableKey: TableKeyEnum.CASE_MANAGEMENT_REVIEW_CASE,
      heightUsed: 399,
      showSetting: true,
      selectable: true,
      showSelectAll: true,
      paginationSize: 'mini',
      draggable: { type: 'handle', width: 32 },
    },
    (record) => {
      return {
        ...record,
        caseLevel: getCaseLevels(record.customFields),
        showModuleTree: true,
      };
    }
  );
  const batchActions = {
    baseAction: [
      {
        label: 'caseManagement.caseReview.review',
        eventTag: 'review',
        permission: ['CASE_REVIEW:READ+REVIEW'],
      },
      {
        label: 'caseManagement.caseReview.changeReviewer',
        eventTag: 'changeReviewer',
        permission: ['CASE_REVIEW:READ+UPDATE'],
      },
      {
        label: 'caseManagement.caseReview.disassociate',
        eventTag: 'disassociate',
        permission: ['CASE_REVIEW:READ+RELEVANCE'],
      },
      {
        label: 'caseManagement.caseReview.reReview',
        eventTag: 'reReview',
        permission: ['CASE_REVIEW:READ+UPDATE'],
      },
    ],
  };

  const tableSorter = ref<Record<string, string>>({});
  function handleSorterChange(sorter: { [key: string]: string }) {
    tableSorter.value = sorter;
  }

  function userIsReviewer(record: ReviewCaseItem) {
    return record.reviewers.some((e) => e === userStore.id);
  }

  const modulesCount = computed(() => caseReviewStore.modulesCount);

  async function getModuleCount() {
    if (isAdvancedSearchMode.value) return;
    let params: TableQueryParams;
    if (showType.value === 'list') {
      params = {
        ...tableParams.value,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
        total: propsRes.value.msPagination?.total,
      };
    } else {
      params = { projectId: appStore.currentProjectId, pageSize: 10, current: 1 };
    }
    await caseReviewStore.getModuleCount({
      ...params,
      moduleIds: [],
      reviewId: route.query.id as string,
    });
  }

  function searchCase() {
    tableParams.value = {
      projectId: appStore.currentProjectId,
      reviewId: route.query.id,
      moduleIds:
        props.activeFolder === 'all' || isAdvancedSearchMode.value ? [] : [props.activeFolder, ...props.offspringIds],
      keyword: keyword.value,
      viewId: viewId.value,
      combineSearch: advanceFilter,
    };
    setLoadListParams(tableParams.value);
    resetSelector();
    loadList();
    getModuleCount();
  }
  const isActivated = computed(() =>
    cacheStore.cacheViews.includes(CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL)
  );
  const reviewerTitlePopupVisible = ref(true);

  function loadReviewCase() {
    searchCase();
    setTimeout(() => {
      reviewerTitlePopupVisible.value = false;
    }, 5000);
  }
  onBeforeMount(() => {
    if (!isActivated.value) {
      loadReviewCase();
    }
  });

  onActivated(() => {
    if (isActivated.value) {
      loadReviewCase();
    }
  });

  /**
   * 更新数据
   * @param getCount 获取模块树数量
   */
  async function refresh(getCount = true) {
    if (showType.value === 'list') {
      if (getCount) {
        searchCase();
      } else {
        resetSelector();
        loadList();
      }
    } else {
      if (getCount) {
        await getModuleCount();
      }
      msCaseReviewMinderRef.value?.initCaseTree();
    }
  }

  async function handleRefreshAll() {
    emit('refresh');
    await initModules();
    refresh();
  }

  watch(
    () => props.activeFolder,
    () => {
      if (showType.value === 'list' && !isAdvancedSearchMode.value) {
        searchCase();
      }
    }
  );

  function handleShowTypeChange(val: string | number | boolean) {
    if (val === 'minder') {
      // 切换到脑图刷新模块统计
      getModuleCount();
    } else {
      searchCase();
    }
  }

  const batchParams = ref<BatchApiParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [] as string[],
    condition: {},
    currentSelectCount: 0,
  });

  const dialogVisible = ref<boolean>(false);
  const defaultDialogForm = {
    result: 'PASS',
    reason: '',
    reviewer: [] as string[],
    isAppend: false,
    fileList: [] as MsFileItem[],
    commentIds: [] as string[],
  };
  const dialogForm = ref({ ...defaultDialogForm });
  const dialogFormRef = ref<FormInstance>();
  const dialogShowType = ref<'review' | 'changeReviewer' | 'reReview'>('review'); // 弹窗类型，review: 批量评审，changeReviewer: 批量更换评审人，reReview: 批量重新评审
  const dialogLoading = ref(false);
  const dialogTitle = computed(() => {
    switch (dialogShowType.value) {
      case 'review':
        return t('caseManagement.caseReview.batchReview');
      case 'changeReviewer':
        return t('caseManagement.caseReview.changeReviewer');
      case 'reReview':
        return t('caseManagement.caseReview.reReview');
      default:
        return '';
    }
  });

  function handleDialogCancel() {
    dialogVisible.value = false;
    dialogFormRef.value?.resetFields();
    dialogForm.value = { ...defaultDialogForm };
  }

  const disassociateLoading = ref(false);

  /**
   * 解除关联
   * @param record 关联用例项
   * @param done 关闭弹窗
   */
  async function handleDisassociateReviewCase(record: ReviewCaseItem, done?: () => void) {
    try {
      disassociateLoading.value = true;
      await disassociateReviewCase(route.query.id as string, record.caseId);
      initModules();
      emit('refresh');
      if (done) {
        done();
      }
      Message.success(t('caseManagement.caseReview.disassociateSuccess'));
      loadList();
      getModuleCount();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      disassociateLoading.value = false;
    }
  }

  function handleArchive(record: ReviewItem) {
    openModal({
      type: 'warning',
      title: t('caseManagement.caseReview.archivedTitle', { name: record.name }),
      content: t('caseManagement.caseReview.archivedContent'),
      okText: t('caseManagement.caseReview.archive'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          // await resetUserPassword({
          //   selectIds,
          //   selectAll: !!params?.selectAll,
          //   excludeIds: params?.excludeIds || [],
          //   condition: { keyword: keyword.value },
          // });
          Message.success(t('caseManagement.caseReview.archiveSuccess'));
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 批量解除关联用例拦截
  function batchDisassociate() {
    const batchDisassociateTitle =
      showType.value !== 'list' && minderSelectData.value?.caseId?.length
        ? t('testPlan.featureCase.disassociateTip', { name: characterLimit(minderSelectData.value?.text) })
        : t('caseManagement.caseReview.disassociateConfirmTitle', {
            count: showType.value !== 'list' ? minderSelectData.value?.count : batchParams.value.currentSelectCount,
          });
    openModal({
      type: 'warning',
      title: batchDisassociateTitle,
      content: t('caseManagement.caseReview.disassociateTipContent'),
      okText: t('caseManagement.caseReview.disassociate'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          dialogLoading.value = true;
          await batchDisassociateReviewCase({
            reviewId: route.query.id as string,
            ...(showType.value === 'list'
              ? {
                  moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
                  ...batchParams.value,
                  ...tableParams.value,
                }
              : minderParams.value),
          });
          Message.success(t('common.updateSuccess'));
          dialogLoading.value = false;
          const folderTree = cloneDeep(moduleTree.value);
          emit('refresh');
          await initModules();
          await getModuleCount();
          if (!Object.keys(modulesCount.value).includes(props.activeFolder)) {
            // 模块树选中返回上一级
            emit('selectParentNode', folderTree);
          } else {
            refresh(false);
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          dialogLoading.value = false;
        }
      },
      hideCancel: false,
    });
  }

  // 获取用例等级数据
  async function getCaseLevelFields() {
    const result = await getCaseDefaultFields(appStore.currentProjectId);
    caseLevelFields.value = result.customFields.find(
      (item: any) => item.internal && item.internalFieldKey === 'functional_priority'
    );
    columns = columns.map((item) => {
      if (item.dataIndex === 'caseLevel') {
        return {
          title: 'caseManagement.featureCase.tableColumnLevel',
          slotName: 'caseLevel',
          dataIndex: 'caseLevel',
          showInTable: true,
          width: 100,
          showDrag: true,
          filterConfig: {
            options: cloneDeep(caseLevelFields.value.options),
            filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
          },
        };
      }
      return item;
    });
  }

  // 批量重新评审
  async function reReview() {
    try {
      dialogLoading.value = true;
      await batchReview({
        reviewId: route.query.id as string,
        reviewPassRule: props.reviewPassRule,
        status: 'RE_REVIEWED',
        content: dialogForm.value.reason,
        notifier: dialogForm.value.commentIds.join(';'),
        ...(showType.value === 'list'
          ? {
              moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
              ...batchParams.value,
              ...tableParams.value,
            }
          : minderParams.value),
      });
      Message.success(t('common.updateSuccess'));
      dialogVisible.value = false;
      if (showType.value === 'list') {
        resetSelector();
        loadList();
      } else {
        msCaseReviewMinderRef.value?.updateResource('RE_REVIEWED');
      }
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      dialogLoading.value = false;
    }
  }

  // 批量更换评审人
  function changeReviewer(record?: any) {
    dialogFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          dialogLoading.value = true;
          await batchChangeReviewer({
            reviewId: route.query.id as string,
            reviewerId: dialogForm.value.reviewer.length > 0 ? dialogForm.value.reviewer : record.reviewers,
            append: dialogForm.value.isAppend, // 是否追加
            ...(showType.value === 'list'
              ? {
                  moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
                  ...batchParams.value,
                  ...tableParams.value,
                  selectIds: batchParams.value.selectIds.length > 0 ? batchParams.value.selectIds : [record.id],
                }
              : minderParams.value),
          });
          Message.success(t('common.updateSuccess'));
          dialogVisible.value = false;
          emit('refresh');
          refresh();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          record.showModuleTree = true;
          dialogLoading.value = false;
        }
      }
    });
  }

  function selectChangeReviewer(val: boolean, record?: any) {
    if (!val) {
      changeReviewer(record);
    }
  }

  const reviewCommentFileIds = ref<string[]>([]);

  // 提交评审结果
  function commitResult() {
    dialogFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          dialogLoading.value = true;
          await batchReview({
            reviewId: route.query.id as string,
            reviewPassRule: props.reviewPassRule,
            status: dialogForm.value.result as StartReviewStatus,
            content: dialogForm.value.reason,
            notifier: dialogForm.value.commentIds.join(';'),
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
            ...batchParams.value,
            ...tableParams.value,
            reviewCommentFileIds: reviewCommentFileIds.value,
          });
          Message.success(t('caseManagement.caseReview.reviewSuccess'));
          dialogVisible.value = false;
          resetSelector();
          emit('refresh');
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          dialogLoading.value = false;
        }
      }
    });
  }

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  const submitReviewDisabled = computed(
    () =>
      dialogForm.value.result !== 'PASS' &&
      (dialogForm.value.reason === '' || dialogForm.value.reason.trim() === '<p style=""></p>')
  );

  const reviewersOptions = ref<SelectOptionData[]>([]);
  const reviewerLoading = ref(false);

  async function initReviewers() {
    try {
      reviewerLoading.value = true;
      const res = await getReviewUsers(appStore.currentProjectId, '');
      reviewersOptions.value = res.map((e) => ({ label: e.name, value: e.id }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      reviewerLoading.value = false;
    }
  }

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      type: FilterType.TREE_SELECT,
      treeSelectData: moduleTree.value,
      treeSelectProps: {
        fieldNames: {
          title: 'name',
          key: 'id',
          children: 'children',
        },
        multiple: true,
        treeCheckable: true,
        treeCheckStrictly: true,
      },
    },
    {
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'status',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: reviewResultOptions.value,
      },
    },
    {
      title: 'caseManagement.caseReview.reviewer',
      dataIndex: 'reviewers',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: reviewersOptions.value,
      },
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      dataIndex: 'lastExecuteResult',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: executeResultOptions.value,
      },
    },
    {
      title: 'caseManagement.featureCase.associatedDemand',
      dataIndex: 'demand',
      type: FilterType.INPUT,
    },
    {
      title: 'caseManagement.featureCase.relatedAttachments',
      dataIndex: 'attachment',
      type: FilterType.INPUT,
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
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
  ]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  async function initFilter() {
    try {
      const result = await getCustomFieldsTable(appStore.currentProjectId);
      searchCustomFields.value = getFilterCustomFields(result); // 处理自定义字段
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string, isStartAdvance: boolean) => {
    emit('handleAdvSearch', isStartAdvance);
    keyword.value = '';
    setAdvanceFilter(filter, id);
    searchCase();
  };

  function handleOperation(type?: string) {
    switch (type) {
      case 'review':
        dialogVisible.value = true;
        dialogShowType.value = 'review';
        break;
      case 'changeReviewer':
        initReviewers();
        dialogVisible.value = true;
        dialogShowType.value = 'changeReviewer';
        break;
      case 'disassociate':
        batchDisassociate();
        break;
      case 'reReview':
        dialogVisible.value = true;
        dialogShowType.value = 'reReview';
        break;
      default:
        break;
    }
  }

  // 脑图操作
  function handleMinderOperation(type: string, node: MinderJsonNode) {
    minderSelectData.value = node.data;
    minderParams.value = getMinderOperationParams(node);
    handleOperation(type);
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = {
      ...params,
      selectIds: params?.selectedIds || [],
      condition: {
        filter: propsRes.value.filter,
      },
    };
    handleOperation(event.eventTag);
  }

  // 去用例评审页面
  function review(record: ReviewCaseItem) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.caseId,
      },
      state: {
        params: JSON.stringify(getTableQueryParams()),
      },
    });
  }

  async function mountedLoad() {
    initReviewers();
    await initFilter();
  }

  onBeforeMount(() => {
    if (!isActivated.value) {
      mountedLoad();
    }
  });

  onActivated(() => {
    if (isActivated.value) {
      mountedLoad();
    }
  });

  defineExpose({
    refresh,
    resetSelector,
  });
  await getCaseLevelFields();
  await tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_REVIEW_CASE, columns, 'drawer');
</script>

<style lang="less" scoped>
  .case-show-type {
    @apply grid grid-cols-2;
    .show-type-icon {
      :deep(.arco-radio-button-content) {
        @apply flex;

        padding: 4px;
        line-height: 20px;
      }
    }
  }
  :deep(.arco-radio-label) {
    @apply inline-flex;
  }
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
      .arco-input-suffix {
        @apply invisible;
      }
    }
  }
  .list-show-type {
    padding: 0;
    :deep(.arco-radio-button-content) {
      padding: 4px 6px;
    }
  }
</style>
