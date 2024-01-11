<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium">{{ t('ms.apiTestDebug.header') }}</div>
    <batchAddKeyVal :params="innerParams" @apply="handleBatchParamApply" />
  </div>
  <AllParamsTable
    v-model:params="innerParams"
    :show-setting="false"
    :columns="columns"
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import AllParamsTable from '../allParams/AllParamsTable.vue';
  import batchAddKeyVal from '@/views/api-test/debug/components/debug/batchAddKeyVal.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: any[];
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);

  const columns: MsTableColumn = [
    {
      title: 'ms.apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: 'ms.apiTestDebug.desc',
      dataIndex: 'desc',
      slotName: 'desc',
    },
    {
      title: 'project.environmental.mustContain',
      dataIndex: 'mustContain',
      slotName: 'mustContain',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];

  /**
   * 批量参数代码转换为参数表格数据
   */
  function handleBatchParamApply(resultArr: any[]) {
    if (resultArr.length < innerParams.value.length) {
      innerParams.value.splice(0, innerParams.value.length - 1, ...resultArr);
    } else {
      innerParams.value = [...resultArr, innerParams.value[innerParams.value.length - 1]];
    }
    emit('change');
  }

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    innerParams.value = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }
</script>

<style lang="less" scoped></style>
