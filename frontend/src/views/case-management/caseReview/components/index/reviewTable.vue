<template>
  <div class="p-[16px]">
    <div class="mb-[16px]">
      <MsAdvanceFilter
        ref="msAdvanceFilterRef"
        v-model:keyword="keyword"
        :view-type="ViewTypeEnum.CASE_REVIEW"
        :filter-config-list="filterConfigList"
        :search-placeholder="t('caseManagement.caseReview.list.searchPlaceholder')"
        :view-name="viewName"
        @keyword-search="searchReview()"
        @adv-search="handleAdvSearch"
        @refresh="searchReview()"
      >
        <template #left>
          <!-- <div class="flex items-center">
            <div class="mr-[4px] text-[var(--color-text-1)]">{{ t('caseManagement.caseReview.allReviews') }}</div>
            <div class="text-[var(--color-text-4)]">({{ propsRes.msPagination?.total }})</div>
          </div> -->
          <a-radio-group v-model:model-value="innerShowType" type="button" class="file-show-type">
            <a-radio value="all">{{ t('common.all') }}</a-radio>
            <a-radio value="reviewByMe">{{ t('caseManagement.caseReview.waitMyReview') }}</a-radio>
            <a-radio value="createByMe">{{ t('caseManagement.caseReview.myCreate') }}</a-radio>
          </a-radio-group>
        </template>
      </MsAdvanceFilter>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      no-disable
      filter-icon-align-left
      :not-show-table-filter="isAdvancedSearchMode"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_STATUS]="{ filterContent }">
        <a-tag
          :color="reviewStatusMap[filterContent.value as ReviewStatus].color"
          :class="[reviewStatusMap[filterContent.value as ReviewStatus].class, 'px-[4px]']"
          size="small"
        >
          {{ t(reviewStatusMap[filterContent.value as ReviewStatus].label) }}
        </a-tag>
      </template>
      <template #passRateColumn>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('caseManagement.caseReview.passRate') }}
          <a-tooltip :content="t('caseManagement.caseReview.passRateTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
      <template #num="{ record }">
        <a-button type="text" class="px-0 !text-[14px] !leading-[22px]" @click="openDetail(record.id)">
          <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
        </a-button>
      </template>
      <template #status="{ record }">
        <MsStatusTag :status="record.status" />
      </template>
      <template #reviewPassRule="{ record }">
        <a-tag
          :color="record.reviewPassRule === 'SINGLE' ? 'rgb(var(--success-2))' : 'rgb(var(--link-2))'"
          :class="record.reviewPassRule === 'SINGLE' ? '!text-[rgb(var(--success-6))]' : '!text-[rgb(var(--link-6))]'"
        >
          {{
            record.reviewPassRule === 'SINGLE'
              ? t('caseManagement.caseReview.single')
              : t('caseManagement.caseReview.multi')
          }}
        </a-tag>
      </template>
      <template #reviewers="{ record }">
        <a-tooltip :content="record.reviewers.join('、')">
          <div class="one-line-text">{{ record.reviewers.join('、') }}</div>
        </a-tooltip>
      </template>
      <template #passRate="{ record }">
        <div class="mr-[8px] w-[100px]">
          <passRateLine :review-detail="record" height="5px" />
        </div>
        <div class="text-[var(--color-text-1)]">
          {{ `${record.passRate}%` }}
        </div>
      </template>
      <template #moduleName="{ record }">
        <a-tooltip :content="record.fullModuleName">
          <div class="one-line-text">{{ record.moduleName }}</div>
        </a-tooltip>
      </template>
      <template #action="{ record }">
        <MsButton
          v-permission="['CASE_REVIEW:READ+UPDATE']"
          type="text"
          class="!mr-0"
          @click="() => editReview(record)"
        >
          {{ t('common.edit') }}
        </MsButton>
        <!-- <a-divider direction="vertical" :margin="8"></a-divider>
        <MsButton type="text" class="!mr-0">
          {{ t('common.export') }}
        </MsButton> -->
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction
          v-permission="['CASE_REVIEW:READ+DELETE']"
          :list="getMoreAction(record.status)"
          @select="handleMoreActionSelect($event, record)"
        />
      </template>
      <template v-if="keyword.trim() === ''" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{
            hasAllPermission(['CASE_REVIEW:READ+ADD'])
              ? t('caseManagement.caseReview.tableNoData')
              : t('caseManagement.caseReview.tableNoDataNoPermission')
          }}
          <MsButton v-permission="['CASE_REVIEW:READ+ADD']" class="ml-[8px]" @click="() => emit('goCreate')">
            {{ t('caseManagement.caseReview.create') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <deleteReviewModal v-model:visible="dialogVisible" :record="activeRecord" @success="removeReviewModal" />
    <a-modal
      v-model:visible="moveModalVisible"
      title-align="start"
      class="ms-modal-no-padding ms-modal-small"
      :mask-closable="false"
      :ok-text="t('caseManagement.caseReview.batchMoveConfirm', { count: batchParams.currentSelectCount })"
      :ok-button-props="{ disabled: selectedModuleKeys.length === 0 }"
      :cancel-button-props="{ disabled: batchMoveFileLoading }"
      :on-before-ok="handleReviewMove"
      @close="handleMoveModalCancel"
    >
      <template #title>
        <div class="flex items-center">
          {{ t('caseManagement.caseReview.batchMove') }}
          <div class="ml-[4px] text-[var(--color-text-4)]">
            {{ t('caseManagement.caseReview.batchMoveTitleSub', { count: batchParams.currentSelectCount }) }}
          </div>
        </div>
      </template>
      <ModuleTree
        v-if="moveModalVisible"
        v-model:selected-keys="selectedModuleKeys"
        :is-expand-all="true"
        :active-folder="props.activeFolder"
        is-modal
        @folder-node-select="folderNodeSelect"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsAdvanceFilter from '@/components/pure/ms-advance-filter/index.vue';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import passRateLine from '../passRateLine.vue';
  import deleteReviewModal from './deleteReviewModal.vue';
  import ModuleTree from './moduleTree.vue';

  import { getReviewList, getReviewUsers, moveReview } from '@/api/modules/case-management/caseReview';
  import { getProjectMemberCommentOptions } from '@/api/modules/project-management/projectMember';
  import { reviewStatusMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import useUserStore from '@/store/modules/user';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import {
    ReviewDetailReviewersItem,
    ReviewItem,
    ReviewListQueryParams,
    ReviewStatus,
  } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const props = defineProps<{
    activeFolder: string;
    moduleTree: ModuleTreeNode[];
    treePathMap: Record<
      string,
      {
        path: string;
        fullPath: string;
      }
    >;
    showType: string;
    offspringIds: string[];
  }>();

  const emit = defineEmits<{
    (e: 'goCreate'): void;
    (e: 'init', params: ReviewListQueryParams): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();
  const userStore = useUserStore();
  const appStore = useAppStore();
  const cacheStore = useCacheStore();

  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();
  const { openModal } = useModal();

  const keyword = ref('');

  const filterConfigList = ref<FilterFormItem[]>([]);
  const memberOptions = ref<{ label: string; value: string }[]>([]);

  const innerShowType = useVModel(props, 'showType', emit);

  const reviewStatusOptions = computed(() => {
    return Object.keys(reviewStatusMap).map((key) => {
      return {
        value: key,
        label: t(reviewStatusMap[key as ReviewStatus].label),
      };
    });
  });
  const isActivated = computed(() => cacheStore.cacheViews.includes(CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW));

  async function mountedLoad() {
    try {
      const [userRes, memberRes] = await Promise.all([
        getReviewUsers(appStore.currentProjectId, keyword.value),
        getProjectMemberCommentOptions(appStore.currentProjectId, keyword.value),
      ]);
      const reviewUsers = userRes.map((e) => ({ label: e.name, value: e.id }));
      memberOptions.value = memberRes.map((e) => ({ label: e.name, value: e.id }));
      filterConfigList.value = [
        {
          title: 'caseManagement.featureCase.tableColumnID',
          dataIndex: 'num',
          type: FilterType.INPUT,
        },
        {
          title: 'caseManagement.caseReview.name',
          dataIndex: 'name',
          type: FilterType.INPUT,
        },
        {
          title: 'caseManagement.caseReview.caseCount',
          dataIndex: 'caseCount',
          type: FilterType.NUMBER,
          numberProps: {
            min: 0,
            precision: 0,
          },
        },
        {
          title: 'caseManagement.caseReview.status',
          dataIndex: 'status',
          type: FilterType.SELECT,
          selectProps: {
            multiple: true,
            options: reviewStatusOptions.value,
          },
        },
        {
          title: 'caseManagement.caseReview.passRate',
          dataIndex: 'passRate',
          type: FilterType.NUMBER,
          numberProps: {
            min: 0,
            suffix: '%',
          },
        },
        {
          title: 'caseManagement.caseReview.type',
          dataIndex: 'reviewPassRule',
          type: FilterType.SELECT,
          selectProps: {
            multiple: true,
            options: [
              {
                label: t('caseManagement.caseReview.single'),
                value: 'SINGLE',
              },
              {
                label: t('caseManagement.caseReview.multi'),
                value: 'MULTIPLE',
              },
            ],
          },
        },
        {
          title: 'caseManagement.caseReview.reviewer',
          dataIndex: 'reviewers',
          type: FilterType.SELECT,
          selectProps: {
            multiple: true,
            options: reviewUsers,
          },
        },
        {
          title: 'caseManagement.caseReview.creator',
          dataIndex: 'createUser',
          type: FilterType.SELECT,
          selectProps: {
            multiple: true,
            options: memberOptions.value,
          },
        },
        {
          title: 'caseManagement.caseReview.module',
          dataIndex: 'moduleId',
          type: FilterType.TREE_SELECT,
          treeSelectData: props.moduleTree,
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
          title: 'caseManagement.caseReview.tag',
          dataIndex: 'tags',
          type: FilterType.TAGS_INPUT,
          numberProps: {
            min: 0,
            precision: 0,
          },
        },
        {
          title: 'common.desc',
          dataIndex: 'description',
          type: FilterType.INPUT,
        },
        {
          title: 'caseManagement.caseReview.startTime',
          dataIndex: 'startTime',
          type: FilterType.DATE_PICKER,
        },
        {
          title: 'caseManagement.caseReview.endTime',
          dataIndex: 'endTime',
          type: FilterType.DATE_PICKER,
        },
      ];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['CASE_REVIEW:READ+UPDATE', 'CASE_REVIEW:READ+DELETE'])
  );
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
      title: 'caseManagement.caseReview.name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.caseCount',
      dataIndex: 'caseCount',
      showDrag: true,
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.status',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: reviewStatusOptions.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_STATUS,
      },
      showDrag: true,
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateColumn',
      showDrag: true,
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.type',
      slotName: 'reviewPassRule',
      dataIndex: 'reviewPassRule',
      showDrag: true,
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.reviewer',
      slotName: 'reviewers',
      dataIndex: 'reviewers',
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
      },
      showDrag: true,
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'createUserName',
      showTooltip: true,
      showDrag: true,
      width: 120,
    },
    {
      title: 'caseManagement.caseReview.module',
      dataIndex: 'moduleName',
      slotName: 'moduleName',
      showDrag: true,
      width: 120,
    },
    {
      title: 'caseManagement.caseReview.tag',
      dataIndex: 'tags',
      isTag: true,
      showDrag: true,
      width: 170,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      width: 150,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'caseManagement.caseReview.cycle',
      dataIndex: 'cycle',
      showDrag: true,
      width: 350,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      showDrag: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 110 : 50,
    },
  ];
  const selectedModuleKeys = ref<string[]>([]);
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
    resetFilterParams,
  } = useTable(
    getReviewList,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_REVIEW,
      showSetting: true,
      selectable: true,
      showSelectAll: true,
      heightUsed: 232,
      paginationSize: 'mini',
    },
    (item) => {
      return {
        ...item,
        tags: (item.tags || []).map((e: string) => ({ id: e, name: e })),
        reviewers: item.reviewers.map((e: ReviewDetailReviewersItem) => e.userName),
        moduleName: props.treePathMap[item.moduleId].path,
        fullModuleName: props.treePathMap[item.moduleId].fullPath,
        createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
        cycle:
          item.startTime && item.endTime
            ? `${dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss')} - ${dayjs(item.endTime).format(
                'YYYY-MM-DD HH:mm:ss'
              )}`
            : '',
      };
    }
  );
  const batchActions = {
    baseAction: [
      {
        label: 'caseManagement.caseReview.move',
        eventTag: 'move',
        permission: ['CASE_REVIEW:READ+UPDATE'],
      },
    ],
  };

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);

  const tableQueryParams = ref<any>();
  async function searchReview() {
    let moduleIds: string[] = [];
    if (props.activeFolder && props.activeFolder !== 'all' && !isAdvancedSearchMode.value) {
      moduleIds = [props.activeFolder];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.CASE_MANAGEMENT_REVIEW);
      if (getAllChildren) {
        moduleIds = [props.activeFolder, ...props.offspringIds];
      }
    }
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds,
      createByMe: innerShowType.value === 'createByMe' ? userStore.id : undefined,
      reviewByMe: innerShowType.value === 'reviewByMe' ? userStore.id : undefined,
    };
    setLoadListParams({ ...params, viewId: viewId.value, combineSearch: advanceFilter });
    loadList();
    tableQueryParams.value = {
      ...params,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    };
    if (!isAdvancedSearchMode.value) {
      emit('init', {
        ...tableQueryParams.value,
      });
    }
  }

  const viewName = ref<string>('');

  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string, isStartAdvance: boolean) => {
    resetSelector();
    emit('handleAdvSearch', isStartAdvance);
    keyword.value = '';
    setAdvanceFilter(filter, id);
    await searchReview(); // 基础筛选都清空
  };

  watch(
    () => innerShowType.value,
    () => {
      resetFilterParams();
      searchReview();
    }
  );

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  const dialogVisible = ref<boolean>(false);
  const activeRecord = ref({
    id: '',
    name: '',
    status: 'PREPARED' as ReviewStatus,
  });
  const confirmReviewName = ref('');
  /**
   * 根据评审状态获取更多按钮列表
   * @param status 评审状态
   */
  function getMoreAction(status: number) {
    if (status === 2) {
      return [
        {
          label: 'caseManagement.caseReview.archive',
          eventTag: 'archive',
        },
        {
          isDivider: true,
        },
        {
          label: 'common.delete',
          eventTag: 'delete',
          danger: true,
          permission: ['CASE_REVIEW:READ+DELETE'],
        },
      ];
    }
    return [
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['CASE_REVIEW:READ+DELETE'],
      },
    ];
  }

  function editReview(record: ReviewItem) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
      query: {
        id: record.id,
      },
    });
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

  const moveModalVisible = ref(false);

  const batchMoveFileLoading = ref(false);

  async function handleReviewMove() {
    try {
      batchMoveFileLoading.value = true;
      tableQueryParams.value = {
        ...tableQueryParams.value,
        moveModuleId: selectedModuleKeys.value[0],
        selectIds: batchParams.value?.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        currentSelectCount: batchParams.value?.currentSelectCount || 0,
        condition: {
          keyword: keyword.value,
          filter: propsRes.value.filter,
          viewId: viewId.value,
          combineSearch: advanceFilter,
        },
      };
      await moveReview({
        ...tableQueryParams.value,
      });
      Message.success(t('caseManagement.caseReview.batchMoveSuccess'));
      loadList();
      resetSelector();
      emit('init', { ...tableQueryParams.value });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchMoveFileLoading.value = false;
    }
  }

  function handleMoveModalCancel() {
    moveModalVisible.value = false;
    selectedModuleKeys.value = [];
  }

  function removeReviewModal() {
    loadList();
    resetSelector();
    emit('init', { ...tableQueryParams.value });
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: string[]) {
    selectedModuleKeys.value = keys;
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = params;
    switch (event.eventTag) {
      case 'move':
        moveModalVisible.value = true;
        break;
      default:
        break;
    }
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleMoreActionSelect(item: ActionsItem, record: ReviewItem) {
    switch (item.eventTag) {
      case 'delete':
        activeRecord.value = record;
        confirmReviewName.value = '';
        dialogVisible.value = true;
        break;
      case 'archive':
        handleArchive(record);
        break;
      default:
        break;
    }
  }

  watch(
    () => props.activeFolder,
    () => {
      if (isAdvancedSearchMode.value) return;
      searchReview();
    }
  );

  function openDetail(id: string) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
      query: {
        id,
      },
    });
  }

  onBeforeMount(() => {
    if (route.query.view) {
      setAdvanceFilter({ conditions: [], searchMode: 'AND' }, route.query.view as string);
      viewName.value = route.query.view as string;
    }
    if (!isActivated.value) {
      mountedLoad();
      searchReview();
    }
  });

  onActivated(() => {
    if (isActivated.value) {
      mountedLoad();
      searchReview();
    }
  });

  defineExpose({
    searchReview,
    isAdvancedSearchMode,
  });

  await tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_REVIEW, columns, 'drawer', true);
</script>

<style lang="less" scoped></style>
