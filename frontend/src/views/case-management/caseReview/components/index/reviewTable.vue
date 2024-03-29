<template>
  <div class="px-[24px] py-[16px]">
    <div class="mb-[16px]">
      <MsAdvanceFilter
        v-model:keyword="keyword"
        :filter-config-list="filterConfigList"
        :row-count="filterRowCount"
        :search-placeholder="t('caseManagement.caseReview.list.searchPlaceholder')"
        @keyword-search="(val, filter) => searchReview(filter)"
        @adv-search="searchReview"
        @refresh="searchReview"
      >
        <template #left>
          <div class="flex items-center">
            <div class="mr-[4px] text-[var(--color-text-1)]">{{ t('caseManagement.caseReview.allReviews') }}</div>
            <div class="text-[var(--color-text-4)]">({{ propsRes.msPagination?.total }})</div>
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
      @batch-action="handleTableBatch"
      @module-change="searchReview"
    >
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click="statusFilterVisible = true">
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of Object.keys(reviewStatusMap)" :key="key" :value="key">
                    <a-tag
                      :color="reviewStatusMap[key].color"
                      :class="[reviewStatusMap[key].class, 'px-[4px]']"
                      size="small"
                    >
                      {{ t(reviewStatusMap[key].label) }}
                    </a-tag>
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetStatusFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
              </div>
            </div>
          </template>
        </a-trigger>
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
        <a-tooltip :content="`${record.num}`">
          <a-button type="text" class="px-0" @click="openDetail(record.id)">
            <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
          </a-button>
        </a-tooltip>
      </template>
      <template #status="{ record }">
        <statusTag :status="record.status" />
      </template>
      <template #reviewPassRule="{ record }">
        {{
          record.reviewPassRule === 'SINGLE'
            ? t('caseManagement.caseReview.single')
            : t('caseManagement.caseReview.multi')
        }}
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
          {{ t('caseManagement.caseReview.tableNoData') }}
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
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import passRateLine from '../passRateLine.vue';
  import statusTag from '../statusTag.vue';
  import deleteReviewModal from './deleteReviewModal.vue';
  import ModuleTree from './moduleTree.vue';

  import { getReviewList, getReviewUsers, moveReview } from '@/api/modules/case-management/caseReview';
  import { getProjectMemberCommentOptions } from '@/api/modules/project-management/projectMember';
  import { reviewStatusMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    ReviewDetailReviewersItem,
    ReviewItem,
    ReviewListQueryParams,
    ReviewStatus,
  } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

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
  }>();
  const userStore = useUserStore();
  const appStore = useAppStore();
  const router = useRouter();
  const { t } = useI18n();
  const { openModal } = useModal();

  const keyword = ref('');

  const filterRowCount = ref(0);
  const filterConfigList = ref<FilterFormItem[]>([]);

  onBeforeMount(async () => {
    try {
      const [userRes, memberRes] = await Promise.all([
        getReviewUsers(appStore.currentProjectId, keyword.value),
        getProjectMemberCommentOptions(appStore.currentProjectId, keyword.value),
      ]);
      const userOptions = userRes.map((e) => ({ label: e.name, value: e.id }));
      const memberOptions = memberRes.map((e) => ({ label: e.name, value: e.id }));
      filterConfigList.value = [
        {
          title: 'ID',
          dataIndex: 'id',
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
        },
        {
          title: 'caseManagement.caseReview.status',
          dataIndex: 'status',
          type: FilterType.SELECT,
          selectProps: {
            mode: 'static',
            options: [
              {
                label: t(reviewStatusMap.PREPARED.label),
                value: 'PREPARED',
              },
              {
                label: t(reviewStatusMap.UNDERWAY.label),
                value: 'UNDERWAY',
              },
              {
                label: t(reviewStatusMap.COMPLETED.label),
                value: 'COMPLETED',
              },
              // {
              //   label: t(reviewStatusMap.ARCHIVED.label),
              //   value: 'ARCHIVED',
              // },
            ],
          },
        },
        {
          title: 'caseManagement.caseReview.passRate',
          dataIndex: 'passRate',
          type: FilterType.NUMBER,
        },
        {
          title: 'caseManagement.caseReview.type',
          dataIndex: 'reviewPassRule',
          type: FilterType.SELECT,
          selectProps: {
            mode: 'static',
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
            mode: 'static',
            options: userOptions,
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
        {
          title: 'caseManagement.caseReview.module',
          dataIndex: 'module',
          type: FilterType.TREE_SELECT,
          treeSelectData: props.moduleTree,
          treeSelectProps: {
            fieldNames: {
              title: 'name',
              key: 'id',
              children: 'children',
            },
          },
        },
        {
          title: 'caseManagement.caseReview.tag',
          dataIndex: 'tags',
          type: FilterType.TAGS_INPUT,
        },
        {
          title: 'caseManagement.caseReview.desc',
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
  });

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
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.status',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateColumn',
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.type',
      slotName: 'reviewPassRule',
      dataIndex: 'reviewPassRule',
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.reviewer',
      slotName: 'reviewers',
      dataIndex: 'reviewers',
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 120,
    },
    {
      title: 'caseManagement.caseReview.module',
      dataIndex: 'moduleName',
      slotName: 'moduleName',
      width: 120,
    },
    {
      title: 'caseManagement.caseReview.tag',
      dataIndex: 'tags',
      isTag: true,
      width: 170,
    },
    {
      title: 'caseManagement.caseReview.desc',
      dataIndex: 'description',
      width: 150,
      showTooltip: true,
    },
    {
      title: 'caseManagement.caseReview.cycle',
      dataIndex: 'cycle',
      width: 350,
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 110 : 50,
    },
  ];
  const tableStore = useTableStore();
  await tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_REVIEW, columns, 'drawer', true);
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getReviewList,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_REVIEW,
      showSetting: true,
      selectable: true,
      showSelectAll: true,
      heightUsed: 344,
      showSubdirectory: true,
    },
    (item) => {
      return {
        ...item,
        tags: (item.tags || []).map((e: string) => ({ id: e, name: e })),
        reviewers: item.reviewers.map((e: ReviewDetailReviewersItem) => e.userName),
        moduleName: props.treePathMap[item.moduleId].path,
        fullModuleName: props.treePathMap[item.moduleId].fullPath,
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
      },
    ],
  };

  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);
  const tableQueryParams = ref<any>();
  async function searchReview(filter?: FilterResult) {
    let moduleIds: string[] = [];
    if (props.activeFolder && props.activeFolder !== 'all') {
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
      createByMe: props.showType === 'createByMe' ? userStore.id : undefined,
      reviewByMe: props.showType === 'reviewByMe' ? userStore.id : undefined,
      filter: { status: statusFilters.value },
      combine: filter
        ? {
            ...filter.combine,
          }
        : {},
    };
    setLoadListParams(params);
    loadList();
    tableQueryParams.value = {
      ...params,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    };
    emit('init', {
      ...tableQueryParams.value,
    });
  }

  onBeforeMount(() => {
    searchReview();
  });

  watch(
    () => props.showType,
    () => {
      searchReview();
    }
  );

  function handleFilterHidden(val: boolean) {
    if (!val) {
      searchReview();
    }
  }

  function resetStatusFilter() {
    statusFilters.value = [];
    statusFilterVisible.value = false;
    searchReview();
  }

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
  const selectedModuleKeys = ref<string[]>([]);
  const batchMoveFileLoading = ref(false);

  async function handleReviewMove() {
    try {
      batchMoveFileLoading.value = true;
      await moveReview({
        selectIds: batchParams.value?.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        currentSelectCount: batchParams.value?.currentSelectCount || 0,
        condition: { keyword: keyword.value },
        projectId: appStore.currentProjectId,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder],
        moveModuleId: selectedModuleKeys.value[0],
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
</script>

<style lang="less" scoped></style>
