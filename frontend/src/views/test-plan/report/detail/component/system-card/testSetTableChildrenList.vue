<template>
  <MsList ref="listRef" class="w-full" :data="props.list" :virtual-list-props="{ height: 400, threshold: 200 }">
    <template #item="{ item }">
      <div :class="`arco-table-tr flex h-[48px] items-center ${getRowClass(item)} `">
        <template v-for="column in columns" :key="column.dataIndex">
          <div
            v-if="['collectionName', 'operation'].includes(column.dataIndex as string)"
            :class="`arco-table-cell w-[${column.width}px]`"
          >
          </div>
          <div
            v-if="['name','planName', 'bugCount', 'executeUser', 'moduleName'].includes(column.dataIndex as string)"
            :class="`arco-table-cell w-[${column.width}px]`"
          >
            <a-tooltip
              position="tl"
              content-class="max-w-[400px]"
              :content="String(item[column.dataIndex as string])"
              :disabled="
                item[column.dataIndex as string] === '' || item[column.dataIndex as string] === undefined || item[column.dataIndex as string] === null
              "
            >
              <div class="one-line-text w-full">
                {{ item[column.dataIndex as string] }}
              </div>
            </a-tooltip>
          </div>
          <div v-if="column.dataIndex === 'num'" :class="`arco-table-cell w-[${column.width}px]`">
            <MsButton type="text" @click="toDetail(item)">{{ item.num }}</MsButton>
          </div>
          <div v-if="column.dataIndex === 'priority'" :class="`arco-table-cell w-[${column.width}px]`">
            <CaseLevel :case-level="item.priority" />
          </div>
          <div
            v-if="column.dataIndex === 'executeResult'"
            :class="`arco-table-cell flex items-center w-[${column.width}px]`"
          >
            <ExecuteResult
              v-if="props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL"
              :execute-result="item.executeResult"
            />
            <MsButton
              v-if="props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL"
              class="ml-[8px]"
              :disabled="!item.reportId"
              @click="openExecuteHistory(item)"
            >
              {{ t('common.detail') }}
            </MsButton>
            <ExecutionStatus
              v-if="props.activeType !== ReportCardTypeEnum.FUNCTIONAL_DETAIL"
              :module-type="ReportEnum.API_REPORT"
              :status="item.executeResult"
              :class="[!item.executeResult ? '' : 'cursor-pointer']"
              @click="showReport(item)"
            />
          </div>
        </template>
      </div>
    </template>
  </MsList>
  <MsDrawer
    v-model:visible="showDetailVisible"
    :title="t('common.detail')"
    :width="1200"
    :footer="false"
    no-content-padding
    unmount-on-close
  >
    <div class="p-[16px]">
      <ExecutionHistory show-step-result :loading="executeLoading" :execute-list="executeList" />
    </div>
  </MsDrawer>
  <CaseAndScenarioReportDrawer
    v-model:visible="reportVisible"
    :report-id="selectedId"
    do-not-show-share
    :is-scenario="props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL"
    :report-detail="
      props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL ? reportScenarioDetail : reportCaseDetail
    "
    :get-report-step-detail="
      props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL ? reportStepDetail : reportCaseStepDetail
    "
  />
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import CaseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import ExecutionHistory from '@/views/test-plan/testPlan/detail/featureCase/detail/executionHistory/index.vue';

  import {
    getFunctionalExecuteStep,
    reportCaseDetail,
    reportCaseStepDetail,
    reportScenarioDetail,
    reportStepDetail,
  } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useTableStore } from '@/store';

  import { ApiOrScenarioCaseItem } from '@/models/testPlan/report';
  import type { ExecuteHistoryItem } from '@/models/testPlan/testPlan';
  import { ReportEnum } from '@/enums/reportEnum';
  import { ApiTestRouteEnum, CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  const props = defineProps<{
    list: ApiOrScenarioCaseItem[];
    activeType: ReportCardTypeEnum;
    reportId: string;
    shareId?: string;
    tableKey: TableKeyEnum;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const tableStore = useTableStore();

  // 处理属性顺序/显示/宽度
  const columns = ref<MsTableColumn>([]);
  async function initColumn() {
    const tableColumns = await tableStore.getShowInTableColumns(props.tableKey);
    columns.value = [...tableColumns];
  }
  onMounted(async () => {
    await initColumn();
  });

  // 去详情页面
  function toDetail(record: ApiOrScenarioCaseItem) {
    let route;

    switch (props.activeType) {
      case ReportCardTypeEnum.FUNCTIONAL_DETAIL:
        route = CaseManagementRouteEnum.CASE_MANAGEMENT_CASE;
        break;
      case ReportCardTypeEnum.SCENARIO_CASE_DETAIL:
        route = ApiTestRouteEnum.API_TEST_SCENARIO;
        break;
      case ReportCardTypeEnum.API_CASE_DETAIL:
      default:
        route = ApiTestRouteEnum.API_TEST_MANAGEMENT;
        break;
    }

    openNewPage(route, {
      [props.activeType === ReportCardTypeEnum.API_CASE_DETAIL ? 'cId' : 'id']: record.id,
      pId: record.projectId,
    });
  }

  // 执行结果详情
  const showDetailVisible = ref<boolean>(false);

  const executeReportId = ref<string>('');
  const executeList = ref<ExecuteHistoryItem[]>([]);
  const executeLoading = ref<boolean>(false);
  // 执行历史步骤
  async function getExecuteStep() {
    executeLoading.value = true;
    try {
      const res = await getFunctionalExecuteStep({
        reportId: executeReportId.value,
        shareId: props.shareId,
      });
      executeList.value = [res];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      executeLoading.value = false;
    }
  }

  const selectedId = ref<string>('');
  function getRowClass(record: ApiOrScenarioCaseItem) {
    return record.reportId === selectedId.value ? 'selected-row-class' : '';
  }

  function openExecuteHistory(record: ApiOrScenarioCaseItem) {
    executeReportId.value = record.reportId;
    selectedId.value = record.reportId;
    showDetailVisible.value = true;
    getExecuteStep();
  }

  // 显示执行报告
  const reportVisible = ref(false);
  function showReport(record: ApiOrScenarioCaseItem) {
    if (!record.reportId) {
      return;
    }
    if (!record.executeResult || record.executeResult === 'STOPPED') return;
    reportVisible.value = true;
    selectedId.value = record.reportId;
  }
</script>

<style lang="less" scoped>
  .arco-table-tr {
    border-bottom: 1px solid var(--color-text-n8) !important;
  }
</style>
