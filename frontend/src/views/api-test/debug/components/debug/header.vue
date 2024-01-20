<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium">{{ t('apiTestDebug.header') }}</div>
    <batchAddKeyVal :params="innerParams" @apply="handleBatchParamApply" />
  </div>
  <paramTable
    v-model:params="innerParams"
    :columns="columns"
    :height-used="heightUsed"
    :scroll="scroll"
    draggable
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import paramTable, { ParamTableColumn } from '../../../components/paramTable.vue';
  import batchAddKeyVal from './batchAddKeyVal.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: any[];
    layout: 'horizontal' | 'vertical';
    secondBoxHeight: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);

  const columns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'desc',
      slotName: 'desc',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];

  const heightUsed = computed(() => {
    if (props.layout === 'horizontal') {
      return 422;
    }
    return 422 + props.secondBoxHeight;
  });
  const scroll = computed(() => (props.layout === 'horizontal' ? { x: '700px' } : { x: '100%' }));

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
