<template>
  <div>
    <div class="flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.caseReviewList') }}</div>
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
      title: 'ID',
      dataIndex: 'id',
      sortIndex: 1,
      showTooltip: true,
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.name',
      slotName: 'name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 150,
    },
    {
      title: 'caseManagement.featureCase.reviewResult',
      slotName: 'reviewResult',
      dataIndex: 'reviewResult',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.reviewTime',
      slotName: 'reviewTime',
      dataIndex: 'reviewTime',
      width: 200,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getRecycleListRequest, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_REVIEW,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });
</script>

<style scoped></style>
