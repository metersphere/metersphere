<template>
  <div v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']" class="mb-[8px] flex items-center justify-between">
    <div class="font-medium">{{ t('apiTestDebug.header') }}</div>
    <batchAddKeyVal :no-param-type="props.noParamType" :params="innerParams" @apply="handleBatchParamApply" />
  </div>
  <paramsTable
    v-model:params="innerParams"
    :selectable="true"
    :show-setting="false"
    :columns="columns"
    :table-key="TableKeyEnum.PROJECT_MANAGEMENT_ENV_ALL_PARAM_HEADER"
    :default-param-item="defaultParamItem"
    @change="handleParamTableChange"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import batchAddKeyVal from '@/views/api-test/components/batchAddKeyVal.vue';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { responseHeaderOption } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  defineOptions({
    name: 'EnvManangeGloblaRequestHeader',
  });

  const props = defineProps<{
    params: any[];
    noParamType?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const { t } = useI18n();

  const innerParams = useVModel(props, 'params', emit);
  const defaultParamItem = {
    key: '',
    value: '',
    description: '',
  };

  const columns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'key',
      slotName: 'key',
      permission: ['PROJECT_ENVIRONMENT:READ+UPDATE'],
      inputType: 'autoComplete',
      autoCompleteParams: responseHeaderOption,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
      permission: ['PROJECT_ENVIRONMENT:READ+UPDATE'],
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'description',
      slotName: 'description',
      permission: ['PROJECT_ENVIRONMENT:READ+UPDATE'],
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
