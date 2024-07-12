<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="flex items-center gap-[4px]">
      <div class="font-medium">REST</div>
      <a-tooltip :content="t('apiTestDebug.restTip', { id: '{id}' })" position="right">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </div>
  </div>
  <paramTable
    :params="innerParams"
    :columns="columns"
    :draggable="!props.disabledExceptParam"
    :disabled-param-value="props.disabledParamValue"
    :disabled-except-param="props.disabledExceptParam"
    :scroll="{ minWidth: 1160 }"
    :show-setting="true"
    :table-key="TableKeyEnum.API_TEST_DEBUG_REST"
    :default-param-item="defaultRequestParamsItem"
    @change="handleParamTableChange"
    @batch-add="batchAddKeyValVisible = true"
  />
  <batchAddKeyVal
    v-model:visible="batchAddKeyValVisible"
    :params="innerParams"
    :disabled="props.disabledExceptParam"
    :default-param-item="defaultRequestParamsItem"
    :accept-types="restParamsTypes"
    has-standard
    @apply="handleBatchParamApply"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { TableColumnData } from '@arco-design/web-vue';

  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteRequestCommonParam } from '@/models/apiTest/common';
  import { RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { filterKeyValParams } from '../utils';
  import { defaultRequestParamsItem } from '@/views/api-test/components/config';

  const props = defineProps<{
    params: ExecuteRequestCommonParam[];
    disabledParamValue?: boolean; // 参数值禁用
    disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);
  const restParamsTypes = Object.values(RequestParamsType).filter(
    (val) => ![RequestParamsType.JSON, RequestParamsType.FILE].includes(val)
  );
  const columns = computed<ParamTableColumn[]>(() => [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'key',
      slotName: 'key',
      needValidRepeat: true,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'paramType',
      slotName: 'paramType',
      hasRequired: true,
      options: Object.values(RequestParamsType)
        .filter((val) => ![RequestParamsType.JSON, RequestParamsType.FILE].includes(val as RequestParamsType))
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
            fixed: 'right' as TableColumnData['fixed'],
            width: 100,
          },
        ]),
  ]);

  const batchAddKeyValVisible = ref(false);

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
