<template>
  <div>
    <div class="flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.testPlanList') }}</div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
      ></a-input-search>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record }">
        <a-button type="text" class="px-0">{{ record.name }}</a-button>
      </template>
      <template #status="{ record }">
        <statusTag :status="record.status" />
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import statusTag from '@/views/case-management/caseReview/components/statusTag.vue';

  import { getRecycleListRequest } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const keyword = ref<string>('');

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.defectID',
      dataIndex: 'id',
      showTooltip: true,
      width: 90,
    },
    {
      title: 'caseManagement.featureCase.testPlanName',
      slotName: 'name',
      dataIndex: 'name',
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
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      slotName: 'executionResult',
      dataIndex: 'executionResult',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.executionTime',
      slotName: 'executionTime',
      dataIndex: 'executionTime',
      width: 200,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getRecycleListRequest, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_TEST_PLAN,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });
</script>

<style scoped></style>
