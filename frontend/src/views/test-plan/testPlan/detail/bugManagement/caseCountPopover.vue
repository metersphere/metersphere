<template>
  <a-popover position="bottom" content-class="case-count-popover" @popup-visible-change="popupChange">
    <div class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">{{
      props.record.relateCase.length
    }}</div>
    <template #content>
      <div class="w-[500px]">
        <MsBaseTable v-bind="propsRes" v-on="propsEvent"></MsBaseTable>
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';

  import type { PlanDetailBugItem } from '@/models/testPlan/testPlan';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const props = defineProps<{
    record: PlanDetailBugItem;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 100,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'case.caseName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 200,
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_BUG_TABLE_CASE_COUNT,
    scroll: { x: '100%' },
    showSelectorAll: false,
    heightUsed: 340,
    enableDrag: false,
    showPagination: false,
  });

  function popupChange() {
    propsRes.value.data = props.record.relateCase;
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
