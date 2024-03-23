<template>
  <div>
    <paramsTable
      v-model:params="condition.assertions"
      :selectable="false"
      :columns="columns"
      :scroll="{ minWidth: '700px' }"
      :default-param-item="defaultParamItem"
      :disabled="props.disabled"
      @change="handleParamTableChange"
    />
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { responseHeaderOption } from '@/config/apiTest';

  import type { ExecuteAssertionItem } from '@/models/apiTest/common';

  interface Param {
    [key: string]: any;
    assertions: ExecuteAssertionItem[];
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
    header: '',
    condition: '',
    expectedValue: '',
    enable: true,
  };

  const columns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.responseHeader', // 响应头
      dataIndex: 'header',
      slotName: 'key',
      showInTable: true,
      showDrag: true,
      inputType: 'autoComplete',
      autoCompleteParams: responseHeaderOption,
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
    {
      title: '',
      columnTitle: 'common.operation',
      slotName: 'operation',
      width: 50,
      showInTable: true,
      showDrag: true,
    },
  ];

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    condition.value.assertions = [...resultArr];
    if (!isInit) {
      emit('change', { ...condition.value });
    }
  }
</script>
