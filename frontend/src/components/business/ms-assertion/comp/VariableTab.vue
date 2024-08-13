<template>
  <paramsTable
    v-model:params="condition.variableAssertionItems"
    :selectable="true"
    :columns="columns"
    :scroll="{ minWidth: '100%' }"
    :default-param-item="defaultParamItem"
    :disabled-except-param="props.disabled"
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { EQUAL, statusCodeOptions } from '@/components/pure/ms-advance-filter/index';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  interface Param {
    [key: string]: any;
    variableAssertionItems: any[];
  }

  const props = defineProps<{
    data: Param;
    disabled?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'change', data: Param): void;
  }>();

  const condition = useVModel(props, 'data', emit);

  const defaultParamItem = {
    variableName: '',
    condition: EQUAL.value,
    expectedValue: '',
    enable: true,
  };

  const columns = computed<ParamTableColumn[]>(() => [
    {
      title: 'ms.assertion.variableName', // 变量名
      dataIndex: 'variableName',
      slotName: 'key',
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'ms.assertion.matchCondition', // 匹配条件
      dataIndex: 'condition',
      slotName: 'condition',
      showInTable: true,
      showDrag: true,
      options: statusCodeOptions,
    },
    {
      title: 'ms.assertion.matchValue', // 匹配值
      dataIndex: 'expectedValue',
      slotName: 'expectedValue',
      showInTable: true,
      showDrag: true,
    },
    ...(props.disabled
      ? []
      : [
          {
            title: '',
            columnTitle: 'common.operation',
            slotName: 'operation',
            width: 50,
            showInTable: true,
            showDrag: true,
          },
        ]),
  ]);

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.variableAssertionItems = [...resultArr];
    if (!isInit) {
      emit('change', { ...condition.value });
    }
  }
</script>
