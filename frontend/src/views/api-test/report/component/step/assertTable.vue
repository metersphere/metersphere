<template>
  <ms-base-table ref="tableRef" v-bind="propsRes" no-disable :indent-size="0" v-on="propsEvent">
    <template #pass="{ record }">
      <MsTag theme="light" :type="record.pass ? 'success' : 'danger'">
        {{ record.pass ? t('report.detail.api.resSuccess') : t('report.detail.api.resError') }}
      </MsTag>
    </template>
    <template #script="{ record }">
      {{ record.script }}
    </template>
  </ms-base-table>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { AssertionItem } from '@/models/apiTest/report';

  const { t } = useI18n();

  const props = defineProps<{
    data: AssertionItem[];
  }>();

  const columns: MsTableColumn = [
    {
      title: 'report.detail.api.content',
      dataIndex: 'content',
      slotName: 'content',
      showTooltip: true,
      headerCellClass: 'assertTitleClass',
      bodyCellClass: 'assertCellClass',
    },
    {
      title: 'report.detail.api.assertStatus',
      dataIndex: 'pass',
      slotName: 'pass',
      showTooltip: true,
    },
    {
      title: '',
      dataIndex: 'script',
      slotName: 'script',
      showTooltip: true,
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    scroll: { x: 'auto' },
    showPagination: false,
    hoverable: false,
    showExpand: true,
    rowKey: 'id',
    rowClass: (record: any) => {
      if (record.children) {
        return 'gray-td-bg';
      }
    },
  });

  watchEffect(() => {
    propsRes.value.data = props.data;
  });
</script>

<style scoped lang="less">
  :deep(.arco-table-th) {
    background: var(--color-text-n9) !important;
  }
  :deep(.assertTitleClass.arco-table-th) {
    padding-left: 36px;
  }
  :deep(.arco-table-td.assertCellClass) {
    padding-left: 36px;
  }
</style>
