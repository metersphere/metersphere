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
        <statusTag :status="record.planStatus" />
      </template>
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button
            type="text"
            class="arco-btn-text--secondary p-[8px_4px] text-[14px]"
            @click="statusFilterVisible = true"
          >
            {{ t(columnConfig.title as string) }}
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of Object.keys(planStatusMap)" :key="key" :value="key">
                    <a-tag
                      :color="planStatusMap[key as planStatusType].color"
                      :class="[planStatusMap[key as planStatusType].class, 'px-[4px]']"
                      size="small"
                    >
                      {{ t(planStatusMap[key as planStatusType].label) }}
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
      <template #lastExecResult="{ record }">
        <execute-result :execute-result="record.lastExecResult" />
      </template>
      <template #lastExecResultFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="lastExecResultVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button
            type="text"
            class="arco-btn-text--secondary p-[8px_4px] text-[14px]"
            @click="lastExecResultVisible = true"
          >
            {{ t(columnConfig.title as string) }}
            <icon-down :class="lastExecResultVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="lastExecResultFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of Object.keys(executionResultMap)" :key="key" :value="key">
                    <MsIcon
                      :type="executionResultMap[key]?.icon || ''"
                      class="mr-1"
                      :class="[executionResultMap[key].color]"
                    ></MsIcon>
                    <span>{{ executionResultMap[key]?.statusText || '' }} </span>
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetLastExecuteResultFilter">
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
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { debounce } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import statusTag from '@/views/case-management/caseReview/components/statusTag.vue';

  import { getLinkedCaseTestPlanList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import { AssociateFunctionalCaseItem, planStatusType } from '@/models/testPlan/testPlan';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';
  import { planStatusMap } from '@/views/test-plan/testPlan/config';

  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();
  const keyword = ref<string>('');

  const props = defineProps<{
    caseId: string; // 用例id
  }>();

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
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.projectName',
      dataIndex: 'projectName',
      slotName: 'projectName',
      width: 150,
    },
    {
      title: 'caseManagement.featureCase.planStatus',
      slotName: 'planStatus',
      dataIndex: 'planStatus',
      titleSlotName: 'statusFilter',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      slotName: 'lastExecResult',
      dataIndex: 'lastExecResult',
      titleSlotName: 'lastExecResultFilter',
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

  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);

  const lastExecResultVisible = ref(false);
  const lastExecResultFilters = ref<string[]>([]);

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getLinkedCaseTestPlanList,
    {
      columns,
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_TEST_PLAN,
      scroll: { x: '100%' },
      heightUsed: 340,
      enableDrag: false,
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
      filter: {
        planStatus: statusFilters.value,
        lastExecResult: lastExecResultFilters.value,
      },
    });
    await loadList();
  }

  const searchList = debounce(() => {
    initData();
  }, 100);

  function handleFilterHidden(val: boolean) {
    if (!val) {
      statusFilterVisible.value = false;
      lastExecResultVisible.value = false;
      searchList();
    }
  }

  function resetStatusFilter() {
    statusFilterVisible.value = false;
    statusFilters.value = [];
    searchList();
  }

  function resetLastExecuteResultFilter() {
    lastExecResultVisible.value = false;
    lastExecResultFilters.value = [];
    searchList();
  }

  // 去测试计划页面
  function goToPlan(record: AssociateFunctionalCaseItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX,
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

  onMounted(() => {
    initData();
  });
</script>

<style scoped></style>
