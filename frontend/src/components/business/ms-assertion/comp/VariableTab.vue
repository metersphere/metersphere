<template>
  <div>
    <paramsTable
      v-model:params="innerParams"
      :selectable="false"
      :columns="columns"
      :scroll="{ minWidth: '700px' }"
      :default-param-item="defaultParamItem"
      @change="handleParamTableChange"
    />
  </div>
</template>

<script setup lang="ts">
  import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  interface Param {
    [key: string]: any;
  }

  const innerParams = defineModel<Param[]>('modelValue', { default: [] });

  const emit = defineEmits<{
    (e: 'change'): void; //  数据发生变化
  }>();

  const defaultParamItem = {
    responseHeader: '',
    matchCondition: '',
    matchValue: '',
    enable: true,
  };

  const responseHeaderOption = [
    { label: 'Accept', value: 'accept' },
    { label: 'Accept-Encoding', value: 'acceptEncoding' },
    { label: 'Accept-Language', value: 'acceptLanguage' },
    { label: 'Cache-Control', value: 'cacheControl' },
    { label: 'Content-Type', value: 'contentType' },
    { label: 'Content-Length', value: 'contentLength' },
    { label: 'User-Agent', value: 'userAgent' },
    { label: 'Referer', value: 'referer' },
    { label: 'Cookie', value: 'cookie' },
    { label: 'Authorization', value: 'authorization' },
    { label: 'If-None-Match', value: 'ifNoneMatch' },
    { label: 'If-Modified-Since', value: 'ifModifiedSince' },
  ];

  const columns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.variableName', // 变量名
      dataIndex: 'name',
      slotName: 'name',
      showInTable: true,
      showDrag: true,
      options: responseHeaderOption,
    },
    {
      title: 'ms.assertion.matchCondition', // 匹配条件
      dataIndex: 'matchCondition',
      slotName: 'matchCondition',
      showInTable: true,
      showDrag: true,
      options: statusCodeOptions,
    },
    {
      title: 'ms.assertion.matchValue', // 匹配值
      dataIndex: 'value',
      slotName: 'value',
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
    innerParams.value = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }
</script>
