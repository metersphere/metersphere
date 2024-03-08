<template>
  <MsFormTable :data="data" :columns="props.columns"> </MsFormTable>
</template>

<script setup lang="ts">
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';

  const props = defineProps<{
    columns: FormTableColumn[];
  }>();

  const data = defineModel<Record<string, any>[]>('data', {
    required: true,
    default: () => [],
  });
</script>

<style lang="less" scoped></style>
