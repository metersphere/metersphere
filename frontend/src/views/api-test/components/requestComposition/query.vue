<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="flex items-center gap-[4px]">
      <div class="font-medium">Query</div>
      <a-tooltip :content="t('apiTestDebug.queryTip')" position="right">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </div>
    <batchAddKeyVal
      :params="innerParams"
      :disabled="props.disabledParamValue"
      :default-param-item="defaultRequestParamsItem"
      @apply="handleBatchParamApply"
    />
  </div>
  <paramTable
    :params="innerParams"
    :disabled-param-value="props.disabledParamValue"
    :disabled-except-param="props.disabledExceptParam"
    :columns="columns"
    :height-used="heightUsed"
    :scroll="{ minWidth: 1160 }"
    draggable
    :default-param-item="defaultRequestParamsItem"
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteRequestCommonParam } from '@/models/apiTest/common';
  import { RequestParamsType } from '@/enums/apiEnum';

  import { filterKeyValParams } from '../utils';
  import { defaultRequestParamsItem } from '@/views/api-test/components/config';

  const props = defineProps<{
    params: ExecuteRequestCommonParam[];
    layout: 'horizontal' | 'vertical';
    disabledParamValue?: boolean; // 参数值禁用
    disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
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
      dataIndex: 'key',
      slotName: 'key',
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'paramType',
      slotName: 'paramType',
      hasRequired: true,
      typeOptions: Object.values(RequestParamsType)
        .filter((val) => ![RequestParamsType.JSON, RequestParamsType.FILE].includes(val))
        .map((val) => ({
          label: val,
          value: val,
        })),
      width: 120,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiTestDebug.paramLengthRange',
      dataIndex: 'lengthRange',
      slotName: 'lengthRange',
      align: 'center',
      width: 200,
    },
    {
      title: 'apiTestDebug.encode',
      dataIndex: 'encode',
      slotName: 'encode',
      titleSlotName: 'encodeTitle',
      width: 80,
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'description',
      slotName: 'description',
    },
    {
      title: '',
      slotName: 'operation',
      fixed: 'right',
      width: 50,
    },
  ];

  const heightUsed = ref<number | undefined>(undefined);

  watch(
    () => props.layout,
    (val) => {
      heightUsed.value = val === 'horizontal' ? 428 : 428 + props.secondBoxHeight;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.secondBoxHeight,
    (val) => {
      if (props.layout === 'vertical') {
        heightUsed.value = 428 + val;
      }
    },
    {
      immediate: true,
    }
  );

  /**
   * 批量参数代码转换为参数表格数据
   */
  function handleBatchParamApply(resultArr: any[]) {
    const filterResult = filterKeyValParams(innerParams.value, defaultRequestParamsItem);
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
