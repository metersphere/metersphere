<template>
  <a-scrollbar class="h-full overflow-y-auto">
    <MsFormTable
      :data="props.requestResult?.responseResult.extractResults"
      :columns="columns"
      :selectable="false"
      :scroll="props.scroll"
    >
      <template #type="{ record }">
        {{ t(extractTypeOptions.find((item) => item.value === record.type)?.label || '') }}
      </template>
    </MsFormTable>
  </a-scrollbar>
</template>

<script setup lang="ts">
  import MsFormTable from '@/components/pure/ms-form-table/index.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';

  import { extractTypeOptions } from '@/views/api-test/components/config';

  const { t } = useI18n();
  const props = defineProps<{
    requestResult?: RequestResult;
    scroll?: {
      x?: number | string;
      y?: number | string;
      maxHeight?: number | string;
      minWidth?: number | string;
    };
  }>();

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'apiTestDebug.extractValue',
      dataIndex: 'value',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'type',
      slotName: 'type',
      width: 120,
    },
    {
      title: 'apiTestDebug.expression',
      dataIndex: 'expression',
      showTooltip: true,
      width: 200,
    },
  ];
</script>

<style lang="less" scoped>
  .arco-scrollbar {
    @apply h-full;
  }
</style>
