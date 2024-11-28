<template>
  <MsCard auto-height simple>
    <div class="flex items-center justify-between">
      <div
        class="font-medium text-[var(--color-text-1)]"
        :class="props.type !== 'my_todo' ? 'cursor-pointer' : ''"
        @click="goCaseReview"
      >
        {{ t('ms.workbench.myFollowed.feature.CASE_REVIEW') }}
      </div>
    </div>
    <ms-base-table v-bind="propsRes" no-disable filter-icon-align-left v-on="propsEvent">
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
        <a-tooltip :content="`${record.num}`">
          <a-button type="text" class="px-0 !text-[14px] !leading-[22px]" @click="openDetail(record.id)">
            <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
          </a-button>
        </a-tooltip>
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
      <template #passRate="{ record }">
        <div class="mr-[8px] w-[100px]">
          <passRateLine :review-detail="record" height="5px" />
        </div>
        <div class="text-[var(--color-text-1)]">
          {{ `${record.passRate}%` }}
        </div>
      </template>
    </ms-base-table>
  </MsCard>
</template>

<script setup lang="ts">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import passRateLine from '@/views/case-management/caseReview/components/passRateLine.vue';

  import { workbenchReviewList, workbenchTodoReviewList } from '@/api/modules/workbench';
  import { reviewStatusMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { ReviewStatus } from '@/models/caseManagement/caseReview';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const props = defineProps<{
    project: string;
    type: 'my_follow' | 'my_create' | 'my_todo';
    refreshId: string;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const reviewStatusOptions = computed(() => {
    const keys = Object.keys(reviewStatusMap).filter((key) => {
      return props.type !== 'my_todo' || key !== 'COMPLETED';
    });

    return keys.map((key) => ({
      value: key,
      label: t(reviewStatusMap[key as ReviewStatus].label),
    }));
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
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      showTooltip: true,
      width: 180,
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
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateColumn',
      showDrag: true,
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.caseCount',
      dataIndex: 'caseCount',
      showDrag: true,
      width: 80,
    },
    {
      title: 'caseManagement.caseReview.type',
      slotName: 'reviewPassRule',
      dataIndex: 'reviewPassRule',
      showDrag: true,
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'createUserName',
      width: 150,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
  ];

  const workbenchReviewPage = computed(() => {
    return props.type === 'my_todo' ? workbenchTodoReviewList : workbenchReviewList;
  });

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(workbenchReviewPage.value, {
    columns,
    scroll: { x: '100%' },
    showSetting: false,
    selectable: false,
    showSelectAll: false,
    paginationSize: 'mini',
  });

  function openDetail(id: number) {
    openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL, { id });
  }

  function goCaseReview() {
    if (props.type === 'my_todo') {
      return;
    }
    openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW, {
      view: props.type,
      pId: props.project,
    });
  }

  function init() {
    setLoadListParams({
      projectId: props.project,
      viewId: props.type,
      myTodo: props.type === 'my_todo',
    });
    loadList();
  }

  watch(
    () => props.refreshId,
    () => {
      init();
    }
  );

  onBeforeMount(() => {
    init();
  });
</script>

<style lang="less" scoped></style>
