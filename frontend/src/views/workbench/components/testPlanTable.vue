<template>
  <MsCard auto-height simple>
    <div class="flex items-center justify-between">
      <div
        class="font-medium text-[var(--color-text-1)]"
        :class="props.type !== 'my_todo' ? 'cursor-pointer' : ''"
        @click="goTestPlan"
      >
        {{ t('ms.workbench.myFollowed.feature.TEST_PLAN') }}
      </div>
      <a-radio-group
        v-if="!props.hideShowType"
        v-model="showType"
        type="button"
        class="file-show-type mr-2"
        size="small"
        @change="init"
      >
        <a-radio :value="testPlanTypeEnum.ALL" class="show-type-icon p-[2px]">
          {{ t('testPlan.testPlanIndex.all') }}
        </a-radio>
        <a-radio :value="testPlanTypeEnum.TEST_PLAN" class="show-type-icon p-[2px]">
          {{ t('testPlan.testPlanIndex.plan') }}
        </a-radio>
        <a-radio :value="testPlanTypeEnum.GROUP" class="show-type-icon p-[2px]">
          {{ t('testPlan.testPlanIndex.testPlanGroup') }}
        </a-radio>
      </a-radio-group>
    </div>
    <MsBaseTable
      v-bind="propsRes"
      ref="tableRef"
      class="mt-4"
      filter-icon-align-left
      :expanded-keys="expandedKeys"
      :first-column-width="32"
      v-on="propsEvent"
    >
      <template #num="{ record }">
        <div class="flex items-center">
          <a-tooltip v-if="props.type === 'my_todo'" :content="`${record.num}`">
            <a-button type="text" class="px-0 !text-[14px] !leading-[22px]" @click="openDetail(record.id)">
              <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
            </a-button>
          </a-tooltip>
          <PlanExpandRow
            v-else
            v-model:expanded-keys="expandedKeys"
            :record="record"
            @action="openDetail(record.id)"
            @expand="expandHandler(record)"
          />
        </div>
      </template>
      <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
        <MsStatusTag :status="filterContent.value" />
      </template>
      <template #status="{ record }">
        <MsStatusTag v-if="getStatus(record.id)" :status="getStatus(record.id)" />
        <span v-else>-</span>
      </template>
      <template #createTime="{ record }">
        <a-tooltip :content="`${dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss')}`" position="tl">
          <div class="one-line-text">{{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
        </a-tooltip>
      </template>
      <template #passRate="{ record }">
        <div class="mr-[8px] w-[100px]">
          <StatusProgress :status-detail="defaultCountDetailMap[record.id]" height="5px" :type="record.type" />
        </div>
        <div class="text-[var(--color-text-1)]">
          {{ `${defaultCountDetailMap[record.id]?.passRate ? defaultCountDetailMap[record.id].passRate : '-'}%` }}
        </div>
      </template>
      <template #passRateTitleSlot="{ columnConfig }">
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t(columnConfig.title as string) }}
          <a-tooltip position="right" :content="t('testPlan.testPlanIndex.passRateTitleTip')">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
      <template #functionalCaseCount="{ record }">
        <caseCountPopper :id="record.id" :default-count-detail-map="defaultCountDetailMap" />
      </template>
    </MsBaseTable>
  </MsCard>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn, MsTableProps } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import caseCountPopper from '@/views/test-plan/testPlan/components/caseCountPopper.vue';
  import PlanExpandRow from '@/views/test-plan/testPlan/components/planExpandRow.vue';
  import StatusProgress from '@/views/test-plan/testPlan/components/statusProgress.vue';

  import {
    workbenchTestPlanList,
    workbenchTestPlanStatistic,
    workbenchTodoTestPlanList,
  } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { PassRateCountDetail, TestPlanItem } from '@/models/testPlan/testPlan';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import { planStatusOptions } from '@/views/test-plan/testPlan/config';

  const props = defineProps<{
    project: string;
    type: 'my_follow' | 'my_create' | 'my_todo';
    refreshId: string;
    hideShowType?: boolean;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const showType = ref(testPlanTypeEnum.ALL);

  const defaultCountDetailMap = ref<Record<string, PassRateCountDetail>>({});

  function getStatus(id: string) {
    return defaultCountDetailMap.value[id]?.status;
  }

  async function getStatistics(selectedPlanIds: string[]) {
    try {
      const result = await workbenchTestPlanStatistic(selectedPlanIds);
      result.forEach((item: PassRateCountDetail) => {
        defaultCountDetailMap.value[item.id] = item;
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const columns: MsTableColumn = [
    {
      title: 'testPlan.testPlanIndex.ID',
      slotName: 'num',
      dataIndex: 'num',
      width: 150,
      showInTable: true,
      showDrag: false,
    },
    {
      title: 'testPlan.testPlanIndex.testPlanName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      fixed: 'left',
      width: 180,
      showDrag: false,
    },
    {
      title: 'common.status',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: planStatusOptions,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      },
      showInTable: true,
      showDrag: true,
      width: 100,
    },
    {
      title: 'testPlan.testPlanIndex.passRate',
      dataIndex: 'passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateTitleSlot',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.useCount',
      slotName: 'functionalCaseCount',
      dataIndex: 'functionalCaseCount',
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'createUserName',
      width: 150,
    },
    {
      title: 'testPlan.testPlanIndex.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
      showTooltip: true,
    },
  ];

  const tableProps = ref<Partial<MsTableProps<TestPlanItem>>>({
    columns,
    scroll: { x: '100%' },
    selectable: false,
    showSetting: false,
    paginationSize: 'mini',
    showSelectorAll: false,
  });

  const getTestPlanList = computed(() => {
    return props.type === 'my_todo' ? workbenchTodoTestPlanList : workbenchTestPlanList;
  });

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getTestPlanList.value, tableProps.value);

  const planData = computed(() => {
    return propsRes.value.data;
  });

  const expandedKeys = ref<string[]>([]);
  // 展开折叠
  function expandHandler(record: TestPlanItem) {
    if (expandedKeys.value.includes(record.id)) {
      expandedKeys.value = expandedKeys.value.filter((key) => key !== record.id);
    } else {
      expandedKeys.value = [...expandedKeys.value, record.id];
      if (record.type === 'GROUP' && record.childrenCount) {
        const testPlanId = record.children.map((item: TestPlanItem) => item.id);
        getStatistics(testPlanId);
      }
    }
  }

  // 测试计划详情
  function openDetail(id: string) {
    openNewPage(TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL, {
      pId: props.project,
      id,
    });
  }

  watch(
    () => planData.value,
    (val) => {
      if (val) {
        const selectedPlanIds: string[] = propsRes.value.data.map((e) => e.id) || [];
        if (selectedPlanIds.length) {
          getStatistics(selectedPlanIds);
        }
      }
    },
    {
      immediate: true,
    }
  );

  function init() {
    setLoadListParams({
      type: showType.value,
      projectId: props.project,
      viewId: props.type,
      myTodo: props.type === 'my_todo',
    });
    loadList();
  }

  function goTestPlan() {
    if (props.type === 'my_todo') {
      return;
    }
    openNewPage(TestPlanRouteEnum.TEST_PLAN_INDEX, {
      showType: showType.value,
      view: props.type,
      pId: props.project,
    });
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

<style lang="less" scoped>
  :deep(.arco-table-cell-expand-icon .arco-table-cell-inline-icon) {
    display: none;
  }
</style>
