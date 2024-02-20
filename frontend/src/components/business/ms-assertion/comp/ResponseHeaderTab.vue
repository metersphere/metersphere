<template>
  <div>
    <div>
      <a-radio-group v-model:model-value="innerParams.type" type="button" size="small">
        <a-radio v-for="item of responseRadios" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
    </div>
    <div v-if="innerParams.type === 'jsonPath'">
      <MsJsonPathPicker data="" />
    </div>
    <div v-else-if="innerParams.type === 'xPath'">
      <MsXPathPicker :xml-string="innerParams.response" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { defineModel } from 'vue';

  import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';
  import MsJsonPathPicker from '@/components/pure/ms-jsonpath-picker/index.vue';
  import MsXPathPicker from '@/components/pure/ms-jsonpath-picker/xpath.vue';
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

  const responseRadios = [
    { label: 'ms.assertion.jsonPath', value: 'jsonPath' },
    { label: 'ms.assertion.xpath', value: 'xPath' },
    { label: 'ms.assertion.document', value: 'document' },
    { label: 'ms.assertion.regular', value: 'regular' },
    { label: 'ms.assertion.script', value: 'script' },
  ];
</script>
