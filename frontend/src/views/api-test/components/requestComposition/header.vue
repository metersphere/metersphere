<template>
  <div class="mb-[8px] flex items-center justify-between">
    <batchAddKeyVal
      :params="innerParams"
      :default-param-item="defaultHeaderParamsItem"
      no-param-type
      @apply="handleBatchParamApply"
    />
  </div>
  <paramTable
    v-model:params="innerParams"
    :columns="columns"
    :height-used="heightUsed"
    :scroll="scroll"
    :default-param-item="defaultHeaderParamsItem"
    draggable
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { responseHeaderOption } from '@/config/apiTest';

  import { EnableKeyValueParam } from '@/models/apiTest/common';

  import { filterKeyValParams } from '../utils';
  import { defaultHeaderParamsItem } from '@/views/api-test/components/config';

  const props = defineProps<{
    params: EnableKeyValueParam[];
    layout: 'horizontal' | 'vertical';
    secondBoxHeight: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:selectedKeys', value: string[]): void;
    (e: 'update:params', value: EnableKeyValueParam[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const innerParams = useVModel(props, 'params', emit);

  const columns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'key',
      slotName: 'key',
      inputType: 'autoComplete',
      autoCompleteParams: responseHeaderOption,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'description',
      slotName: 'description',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];

  const heightUsed = computed(() => {
    if (props.layout === 'horizontal') {
      return 428;
    }
    return 428 + props.secondBoxHeight;
  });
  const scroll = computed(() => (props.layout === 'horizontal' ? { x: '700px' } : { x: '100%' }));

  /**
   * 批量参数代码转换为参数表格数据
   */
  function handleBatchParamApply(resultArr: any[]) {
    const filterResult = filterKeyValParams(innerParams.value, defaultHeaderParamsItem);
    if (filterResult.lastDataIsDefault) {
      innerParams.value = [...resultArr, innerParams.value[innerParams.value.length - 1]].filter(Boolean);
    } else {
      innerParams.value = resultArr.filter(Boolean);
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
