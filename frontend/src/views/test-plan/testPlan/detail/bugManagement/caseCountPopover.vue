<template>
  <a-popover position="br" content-class="case-count-popover" @popup-visible-change="popupChange">
    <div class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">
      {{ props.bugItem.relateCases?.length ?? 0 }}
    </div>
    <template #content>
      <div class="w-[500px]">
        <MsBaseTable v-bind="propsRes" v-on="propsEvent">
          <template #num="{ record }">
            <MsButton size="mini" type="text" @click="goCaseDetail(record.id)">{{ record.num }}</MsButton>
          </template>
          <template #type="{ record }">
            <span>{{ getCaseType(record.type) || '-' }}</span>
          </template>
        </MsBaseTable>
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import type { PlanDetailBugItem } from '@/models/testPlan/testPlan';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    bugItem: PlanDetailBugItem;
  }>();

  const { openNewPage } = useOpenNewPage();
  const { t } = useI18n();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      slotName: 'num',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'testPlan.caseType',
      dataIndex: 'type',
      slotName: 'type',
      showTooltip: true,
      width: 200,
    },
  ];

  function getCaseType(type: CaseLinkEnum) {
    switch (type) {
      case CaseLinkEnum.API:
        return t('testPlan.testPlanIndex.apiCase');
      case CaseLinkEnum.FUNCTIONAL:
        return t('testPlan.testPlanIndex.functionalUseCase');
      case CaseLinkEnum.SCENARIO:
        return t('testPlan.testPlanIndex.apiScenarioCase');
      default:
        break;
    }
  }

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    size: 'mini',
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_BUG_TABLE_CASE_COUNT,
    scroll: { x: '100%', y: 400 },
    showSelectorAll: false,
    heightUsed: 340,
    showPagination: false,
  });

  function popupChange() {
    propsRes.value.data = props.bugItem.relateCases;
  }

  function goCaseDetail(id: string) {
    openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT_CASE, {
      id,
    });
  }
</script>

<style scoped lang="less">
  .case-count-popover {
    width: 540px;
    height: 500px;
    .arco-popover-content {
      @apply h-full;
    }
  }
</style>
