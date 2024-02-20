<template>
  <div>
    <div>
      <a-radio-group v-model:model-value="innerParams.type" type="button" size="small">
        <a-radio v-for="item of responseRadios" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
    </div>
    <paramsTable
      v-model:params="innerParams.responseHeader"
      :selectable="false"
      :columns="columns"
      :scroll="{ minWidth: '700px' }"
      :default-param-item="defaultParamItem"
      @change="handleParamTableChange"
    />
  </div>
</template>

<script setup lang="ts">
  import { defineModel } from 'vue';

  import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  interface Param {
    [key: string]: any;
  }

  const innerParams = defineModel<Param>('modelValue', { default: { type: 'jsonPath' } });

  const emit = defineEmits<{
    (e: 'change'): void; //  数据发生变化
  }>();
  const { t } = useI18n();

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

  const responseRadios = [
    { label: 'ms.assertion.jsonPath', value: 'jsonPath' },
    { label: 'ms.assertion.xpath', value: 'xPath' },
    { label: 'ms.assertion.document', value: 'document' },
    { label: 'ms.assertion.regular', value: 'regular' },
    { label: 'ms.assertion.script', value: 'script' },
  ];

  const columns: ParamTableColumn[] = [
    {
      title: 'ms.assertion.responseHeader', // 响应头
      dataIndex: 'responseHeader',
      slotName: 'responseHeader',
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
      dataIndex: 'matchValue',
      slotName: 'matchValue',
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
