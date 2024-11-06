<template>
  <MsCard auto-height simple>
    <div class="flex items-center justify-between">
      <div class="cursor-pointer font-medium text-[var(--color-text-1)]" @click="goTestPlan">
        {{ t('ms.workbench.myFollowed.feature.TEST_PLAN') }}
      </div>
      <a-radio-group v-model="showType" type="button" class="file-show-type mr-2" @change="fetchData">
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
          <PlanExpandRow
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
        <a-popover position="bottom" content-class="p-[16px]" :disabled="getFunctionalCount(record.id) < 1">
          <div>{{ getFunctionalCount(record.id) }}</div>
          <template #content>
            <table class="min-w-[140px] max-w-[176px]">
              <tr>
                <td class="popover-label-td">
                  <div>{{ t('testPlan.testPlanIndex.TotalCases') }}</div>
                </td>
                <td class="popover-value-td">
                  {{ defaultCountDetailMap[record.id]?.caseTotal ?? '0' }}
                </td>
              </tr>
              <tr>
                <td class="popover-label-td">
                  <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.functionalUseCase') }}</div>
                </td>
                <td class="popover-value-td">
                  {{ defaultCountDetailMap[record.id]?.functionalCaseCount ?? '0' }}
                </td>
              </tr>
              <tr>
                <td class="popover-label-td">
                  <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.apiCase') }}</div>
                </td>
                <td class="popover-value-td">
                  {{ defaultCountDetailMap[record.id]?.apiCaseCount ?? '0' }}
                </td>
              </tr>
              <tr>
                <td class="popover-label-td">
                  <div class="text-[var(--color-text-1)]">{{ t('testPlan.testPlanIndex.apiScenarioCase') }}</div>
                </td>
                <td class="popover-value-td">
                  {{ defaultCountDetailMap[record.id]?.apiScenarioCount ?? '0' }}
                </td>
              </tr>
            </table>
          </template>
        </a-popover>
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
  import PlanExpandRow from '@/views/test-plan/testPlan/components/planExpandRow.vue';
  import StatusProgress from '@/views/test-plan/testPlan/components/statusProgress.vue';

  import { getPlanPassRate, getTestPlanList } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { PassRateCountDetail, TestPlanItem } from '@/models/testPlan/testPlan';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import { planStatusOptions } from '@/views/test-plan/testPlan/config';

  const props = defineProps<{
    project: string;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const showType = ref(testPlanTypeEnum.ALL);

  const defaultCountDetailMap = ref<Record<string, PassRateCountDetail>>({});

  function getFunctionalCount(id: string) {
    return defaultCountDetailMap.value[id]?.caseTotal ?? 0;
  }

  function getStatus(id: string) {
    return defaultCountDetailMap.value[id]?.status;
  }

  async function getStatistics(selectedPlanIds: (string | undefined)[]) {
    try {
      const result = await getPlanPassRate(selectedPlanIds);
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
      width: 180,
      showInTable: true,
      showDrag: false,
    },
    {
      title: 'testPlan.testPlanIndex.testPlanName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
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
      width: 150,
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
    selectable: false,
    showSetting: false,
    paginationSize: 'mini',
    showSelectorAll: false,
  });

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getTestPlanList, tableProps.value);

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
      id,
    });
  }

  watch(
    () => planData.value,
    (val) => {
      if (val) {
        const selectedPlanIds: (string | undefined)[] = propsRes.value.data.map((e) => e.id) || [];
        if (selectedPlanIds.length) {
          getStatistics(selectedPlanIds);
        }
      }
    },
    {
      immediate: true,
    }
  );

  function fetchData() {
    setLoadListParams({
      type: showType.value,
      projectId: props.project,
      viewId: 'my_follow',
    });
    loadList();
  }

  function goTestPlan() {
    openNewPage(TestPlanRouteEnum.TEST_PLAN_INDEX, {
      showType: showType.value,
      view: 'my_follow',
    });
  }

  onBeforeMount(() => {
    fetchData();
  });
</script>

<style lang="less" scoped></style>
