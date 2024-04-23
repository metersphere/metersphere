<template>
  <div class="w-full">
    <paramsTable
      v-model:params="condition.assertions"
      :selectable="true"
      :columns="columns"
      :scroll="{ minWidth: '100%' }"
      :default-param-item="defaultParamItem"
      :disabled-except-param="props.disabled"
      @change="handleParamTableChange"
    />
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { EQUAL } from '@/components/pure/ms-advance-filter/index';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import type { ExecuteAssertionItem } from '@/models/apiTest/common';

  import { responseHeaderOption, statusCodeOptions } from './utils';

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
    condition: EQUAL.value,
    expectedValue: '',
    enable: true,
  };

  const columns = computed<ParamTableColumn[]>(() => [
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
      addLineDisabled: true,
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
    condition.value.assertions = [...resultArr];
    if (!isInit) {
      emit('change', { ...condition.value });
    }
  }
</script>
