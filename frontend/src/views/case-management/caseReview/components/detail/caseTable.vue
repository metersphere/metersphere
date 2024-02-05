<template>
  <div class="px-[24px] py-[16px]">
    <div class="mb-[16px] flex flex-wrap items-center justify-end">
      <MsAdvanceFilter
        v-model:keyword="keyword"
        :filter-config-list="filterConfigList"
        :row-count="filterRowCount"
        :search-placeholder="t('caseManagement.caseReview.searchPlaceholder')"
        @keyword-search="(val, filter) => searchCase(filter)"
        @adv-search="searchCase"
        @refresh="searchCase"
      >
        <template #right>
          <div class="flex items-center">
            <a-radio-group v-model:model-value="showType" type="button" class="case-show-type">
              <a-radio value="list" class="show-type-icon p-[2px]">
                <MsIcon type="icon-icon_view-list_outlined" />
              </a-radio>
              <a-radio value="mind" class="show-type-icon p-[2px]">
                <MsIcon type="icon-icon_mindnote_outlined" />
              </a-radio>
            </a-radio-group>
          </div>
        </template>
      </MsAdvanceFilter>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
    >
      <template #resultTitle>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('caseManagement.caseReview.reviewResult') }}
          <a-tooltip :content="t('caseManagement.caseReview.reviewResultTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
      <template #num="{ record }">
        <a-tooltip :content="record.num">
          <a-button type="text" class="px-0" @click="review(record)">
            <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
          </a-button>
        </a-tooltip>
      </template>
      <template #reviewNames="{ record }">
        <a-tooltip :content="record.reviewNames.join('、')">
          <div class="one-line-text">{{ record.reviewNames.join('、') }}</div>
        </a-tooltip>
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
        <MsButton type="text" class="!mr-0" @click="review(record)">
          {{ t('caseManagement.caseReview.review') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsPopconfirm
          :title="t('caseManagement.caseReview.disassociateTip')"
          :sub-title-tip="t('caseManagement.caseReview.disassociateTipContent')"
          :ok-text="t('common.confirm')"
          :loading="disassociateLoading"
          type="error"
          @confirm="(val, done) => handleDisassociateReviewCase(record, done)"
        >
          <MsButton type="text" class="!mr-0">
            {{ t('caseManagement.caseReview.disassociate') }}
          </MsButton>
        </MsPopconfirm>
      </template>
      <template v-if="keyword.trim() === ''" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="createCase">
            {{ t('caseManagement.caseReview.crateCase') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
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
          <div class="ml-[8px] font-normal text-[var(--color-text-4)]">
            ({{ t('caseManagement.caseReview.selectedCase', { count: tableSelected.length }) }})
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
          <a-radio-group v-model:model-value="dialogForm.result" @change="() => dialogFormRef?.resetFields()">
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
              :upload-image="handleUploadImage"
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
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsSelect from '@/components/business/ms-select';

  import {
    batchChangeReviewer,
    batchDisassociateReviewCase,
    batchReview,
    disassociateReviewCase,
    getReviewDetailCasePage,
    getReviewUsers,
  } from '@/api/modules/case-management/caseReview';
  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { getProjectMemberCommentOptions } from '@/api/modules/project-management/projectMember';
  import { reviewResultMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';

  import { ReviewCaseItem, ReviewItem, ReviewPassRule, ReviewResult } from '@/models/caseManagement/caseReview';
  import { BatchApiParams, ModuleTreeNode } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    activeFolder: string | number;
    onlyMine: boolean;
    reviewPassRule: ReviewPassRule; // 评审规则
    offspringIds: string[]; // 当前选中节点的所有子节点id
    moduleTree: ModuleTreeNode[];
  }>();
  const emit = defineEmits(['init', 'refresh']);

  const router = useRouter();
  const route = useRoute();

  const appStore = useAppStore();
  const userStore = useUserStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const keyword = ref('');
  const showType = ref<'list' | 'mind'>('list');
  const filterRowCount = ref(0);
  const filterConfigList = ref<FilterFormItem[]>([]);
  const tableParams = ref<Record<string, any>>({});
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
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.reviewer',
      dataIndex: 'reviewNames',
      slotName: 'reviewNames',
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.reviewResult',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'resultTitle',
      width: 110,
    },
    {
      title: 'caseManagement.caseReview.version',
      dataIndex: 'versionName',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
    },
  ];
  const tableStore = useTableStore();
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, getTableQueryParams } = useTable(
    getReviewDetailCasePage,
    {
      scroll: { x: '100%' },
      tableKey: TableKeyEnum.CASE_MANAGEMENT_REVIEW_CASE,
      heightUsed: 484,
      showSetting: true,
      selectable: true,
      showSelectAll: true,
      draggable: { type: 'handle', width: 32 },
    }
  );
  const batchActions = {
    baseAction: [
      {
        label: 'caseManagement.caseReview.review',
        eventTag: 'review',
      },
      {
        label: 'caseManagement.caseReview.changeReviewer',
        eventTag: 'changeReviewer',
      },
      {
        label: 'caseManagement.caseReview.disassociate',
        eventTag: 'disassociate',
      },
      {
        label: 'caseManagement.caseReview.reReview',
        eventTag: 'reReview',
      },
    ],
  };

  function searchCase(filter?: FilterResult) {
    tableParams.value = {
      projectId: appStore.currentProjectId,
      reviewId: route.query.id,
      moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
      keyword: keyword.value,
      viewFlag: props.onlyMine,
      combine: filter
        ? {
            ...filter.combine,
          }
        : {},
    };
    setLoadListParams(tableParams.value);
    loadList();
    emit('init', {
      ...tableParams.value,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
      total: propsRes.value.msPagination?.total,
      moduleIds: [],
    });
  }

  onBeforeMount(() => {
    searchCase();
  });

  watch(
    () => props.onlyMine,
    () => {
      searchCase();
    }
  );

  watch(
    () => props.activeFolder,
    () => {
      searchCase();
    }
  );

  const tableSelected = ref<(string | number)[]>([]);
  const batchParams = ref<BatchApiParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [] as string[],
    condition: {},
  });

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

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
  async function handleDisassociateReviewCase(record: ReviewCaseItem, done) {
    try {
      disassociateLoading.value = true;
      await disassociateReviewCase(route.query.id as string, record.caseId);
      emit('refresh');
      done();
      Message.success(t('caseManagement.caseReview.disassociateSuccess'));
      loadList();
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
    openModal({
      type: 'warning',
      title: t('caseManagement.caseReview.disassociateConfirmTitle', { count: tableSelected.value.length }),
      content: t('caseManagement.caseReview.disassociateTipContent'),
      okText: t('caseManagement.caseReview.disassociate'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          dialogLoading.value = true;
          await batchDisassociateReviewCase({
            reviewId: route.query.id as string,
            userId: props.onlyMine ? userStore.id || '' : '',
            selectIds: batchParams.value.selectIds,
            selectAll: batchParams.value.selectAll,
            excludeIds: batchParams.value.excludeIds,
            condition: batchParams.value.condition,
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
          });
          Message.success(t('common.updateSuccess'));
          resetSelector();
          loadList();
          emit('refresh');
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

  // 批量重新评审
  async function reReview() {
    try {
      dialogLoading.value = true;
      await batchReview({
        reviewId: route.query.id as string,
        userId: props.onlyMine ? userStore.id || '' : '',
        reviewPassRule: props.reviewPassRule,
        status: 'RE_REVIEWED',
        content: dialogForm.value.reason,
        notifier: dialogForm.value.commentIds.join(';'),
        selectIds: batchParams.value.selectIds,
        selectAll: batchParams.value.selectAll,
        excludeIds: batchParams.value.excludeIds,
        condition: batchParams.value.condition,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
      });
      Message.success(t('common.updateSuccess'));
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

  // 批量更换评审人
  function changeReviewer() {
    dialogFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          dialogLoading.value = true;
          await batchChangeReviewer({
            reviewId: route.query.id as string,
            userId: props.onlyMine ? userStore.id || '' : '',
            reviewerId: dialogForm.value.reviewer,
            append: dialogForm.value.isAppend, // 是否追加
            selectIds: batchParams.value.selectIds,
            selectAll: batchParams.value.selectAll,
            excludeIds: batchParams.value.excludeIds,
            condition: batchParams.value.condition,
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
          });
          Message.success(t('common.updateSuccess'));
          dialogVisible.value = false;
          resetSelector();
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

  // 提交评审结果
  function commitResult() {
    dialogFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          dialogLoading.value = true;
          await batchReview({
            reviewId: route.query.id as string,
            userId: props.onlyMine ? userStore.id || '' : '',
            reviewPassRule: props.reviewPassRule,
            status: dialogForm.value.result as ReviewResult,
            content: dialogForm.value.reason,
            notifier: dialogForm.value.commentIds.join(';'),
            selectIds: batchParams.value.selectIds,
            selectAll: batchParams.value.selectAll,
            excludeIds: batchParams.value.excludeIds,
            condition: batchParams.value.condition,
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
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

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = { ...params, selectIds: params?.selectedIds || [], condition: {} };
    switch (event.eventTag) {
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

  function createCase() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
      query: {
        reviewId: route.query.id,
      },
    });
  }

  onBeforeMount(async () => {
    const [, memberRes] = await Promise.all([
      initReviewers(),
      getProjectMemberCommentOptions(appStore.currentProjectId, keyword.value),
    ]);
    const memberOptions = memberRes.map((e) => ({ label: e.name, value: e.id }));
    filterConfigList.value = [
      {
        title: 'ID',
        dataIndex: 'id',
        type: FilterType.INPUT,
      },
      {
        title: 'caseManagement.caseReview.caseName',
        dataIndex: 'name',
        type: FilterType.INPUT,
      },
      {
        title: 'caseManagement.caseReview.reviewer',
        dataIndex: 'reviewers',
        type: FilterType.SELECT,
        selectProps: {
          mode: 'static',
          options: reviewersOptions.value,
        },
      },
      {
        title: 'caseManagement.caseReview.reviewResult',
        dataIndex: 'status',
        type: FilterType.SELECT,
        selectProps: {
          mode: 'static',
          options: Object.keys(reviewResultMap).map((e) => ({
            label: t(reviewResultMap[e as ReviewResult].label),
            value: e,
          })),
        },
      },
      {
        title: 'caseManagement.caseReview.creator',
        dataIndex: 'createUser',
        type: FilterType.SELECT,
        selectProps: {
          mode: 'static',
          options: memberOptions,
        },
      },
    ];
  });

  defineExpose({
    searchCase,
    resetSelector,
  });

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
</style>
