<template>
  <div>
    <div class="flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.testPlanList') }}</div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="searchList"
      ></a-input-search>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #testPlanNum="{ record }">
        <a-button type="text" class="px-0" @click="goToPlan(record)">{{ record.testPlanNum }}</a-button>
      </template>
      <template #planStatus="{ record }">
        <MsStatusTag v-if="getStatus(record.testPlanId)" :status="getStatus(record.testPlanId)" />
        <span v-else>-</span>
      </template>
      <template #lastExecResult="{ record }">
        <ExecuteResult :execute-result="record.lastExecResult || 'PENDING'" />
      </template>
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
        <ExecuteResult :execute-result="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER]="{ filterContent }">
        <MsStatusTag :status="filterContent.value" />
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { debounce } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';

  import { getLinkedCaseTestPlanList } from '@/api/modules/case-management/featureCase';
  import { getPlanPassRate } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';

  import { AssociateFunctionalCaseItem, PassRateCountDetail, planStatusType } from '@/models/testPlan/testPlan';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';
  import { planStatusMap } from '@/views/test-plan/testPlan/config';

  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();
  const keyword = ref<string>('');

  const props = defineProps<{
    caseId: string; // 用例id
  }>();

  const executeResultOptions = computed(() => {
    return Object.keys(executionResultMap).map((key) => {
      return {
        value: key,
        label: executionResultMap[key].statusText,
      };
    });
  });
  const planStatusOptions = computed(() => {
    return Object.keys(planStatusMap).map((key) => {
      return {
        value: key,
        label: planStatusMap[key as planStatusType].label,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'testPlanNum',
      slotName: 'testPlanNum',
      showTooltip: true,
      width: 90,
    },
    {
      title: 'caseManagement.featureCase.testPlanName',
      slotName: 'testPlanName',
      dataIndex: 'testPlanName',
      showInTable: true,
      showTooltip: true,
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.projectName',
      dataIndex: 'projectName',
      slotName: 'projectName',
      width: 150,
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.planStatus',
      slotName: 'planStatus',
      dataIndex: 'planStatus',
      // TODO : 这个版本先不上过滤后台没有做
      // filterConfig: {
      //   options: planStatusOptions.value,
      //   filterSlotName: FilterSlotNameEnum.TEST_PLAN_STATUS_FILTER,
      // },
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      slotName: 'lastExecResult',
      dataIndex: 'lastExecResult',
      filterConfig: {
        options: executeResultOptions.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.executionTime',
      slotName: 'lastExecTime',
      dataIndex: 'lastExecTime',
      width: 200,
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getLinkedCaseTestPlanList,
    {
      columns,
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_TEST_PLAN,
      scroll: { x: '100%' },
      heightUsed: 340,
    },
    (item) => {
      return {
        ...item,
        lastExecTime: item.lastExecTime ? `${dayjs(item.lastExecTime).format('YYYY-MM-DD HH:mm:ss')}` : '',
      };
    }
  );

  async function initData() {
    setLoadListParams({
      keyword: keyword.value,
      caseId: props.caseId,
    });
    await loadList();
  }

  const searchList = debounce(() => {
    initData();
  }, 100);

  // 去测试计划页面
  function goToPlan(record: AssociateFunctionalCaseItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
      query: {
        ...route.query,
        id: record.testPlanId,
      },
      state: {
        params: JSON.stringify(setLoadListParams()),
      },
    });
  }

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        initData();
      }
    }
  );

  const defaultCountDetailMap = ref<Record<string, PassRateCountDetail>>({});

  function getStatus(id: string) {
    return defaultCountDetailMap.value[id]?.status;
  }

  // 获取状态统计
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

  watch(
    () => propsRes.value.data,
    (val) => {
      if (val) {
        const selectedPlanIds: (string | undefined)[] = propsRes.value.data.map((e) => e.testPlanId) || [];
        if (selectedPlanIds && selectedPlanIds.length > 0) {
          getStatistics(selectedPlanIds);
        }
      }
    },
    {
      deep: true,
    }
  );

  onMounted(() => {
    initData();
  });
</script>

<style scoped></style>
