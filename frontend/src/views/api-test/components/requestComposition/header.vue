<template>
  <div v-if="props.typeTitle" class="mb-[8px]">
    {{ props.typeTitle }}
  </div>
  <paramTable
    v-model:params="innerParams"
    :disabled-param-value="props.disabledParamValue"
    :disabled-except-param="props.disabledExceptParam"
    :columns="columns"
    :scroll="scroll"
    :default-param-item="defaultHeaderParamsItem"
    :draggable="!props.disabledExceptParam"
    @change="handleParamTableChange"
    @batch-add="() => (batchAddKeyValVisible = true)"
  />
  <batchAddKeyVal
    v-model:visible="batchAddKeyValVisible"
    :disabled="props.disabledExceptParam"
    :params="innerParams"
    :default-param-item="defaultHeaderParamsItem"
    no-param-type
    :type-title="props.typeTitle"
    @apply="handleBatchParamApply"
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
    disabledParamValue?: boolean; // 参数值禁用
    disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
    typeTitle?: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:selectedKeys', value: string[]): void;
    (e: 'update:params', value: EnableKeyValueParam[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const innerParams = useVModel(props, 'params', emit);

  const columns = computed<ParamTableColumn[]>(() => [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'key',
      slotName: 'key',
      inputType: 'autoComplete',
      needValidRepeat: true,
      autoCompleteParams: responseHeaderOption,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      slotName: 'description',
    },
    ...(props.disabledExceptParam
      ? []
      : [
          {
            title: '',
            dataIndex: 'operation',
            slotName: 'operation',
            titleSlotName: 'batchAddTitle',
            width: 70,
          },
        ]),
  ]);

  const batchAddKeyValVisible = ref(false);
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
