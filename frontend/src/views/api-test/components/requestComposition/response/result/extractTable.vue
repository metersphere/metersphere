<template>
  <a-scrollbar class="h-full overflow-y-auto">
    <MsFormTable
      :data="props.requestResult?.responseResult.extractResults"
      :columns="columns"
      :selectable="false"
      :scroll="props.scroll"
    >
      <template #type="{ record }">
        <div class="pl-[8px]">{{ t(extractTypeOptions.find((item) => item.value === record.type)?.label || '') }}</div>
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
      slotName: 'name',
      showTooltip: true,
      inputType: 'text',
      width: 200,
    },
    {
      title: 'apiTestDebug.extractValue',
      dataIndex: 'value',
      slotName: 'value',
      showTooltip: true,
      inputType: 'text',
      width: 200,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'type',
      slotName: 'type',
      inputType: 'text',
      width: 120,
    },
    {
      title: 'apiTestDebug.expression',
      dataIndex: 'expression',
      slotName: 'expression',
      showTooltip: true,
      inputType: 'text',
      width: 200,
    },
  ];
</script>

<style lang="less" scoped>
  .arco-scrollbar {
    @apply h-full;
  }
</style>
