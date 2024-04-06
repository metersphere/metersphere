<template>
  <FormCreate
    v-model:api="innerApi"
    :rule="formRules"
    :option="props.option || options"
    @mounted="handleMounted"
    @reload="handleReload"
    @change="handleChange"
  ></FormCreate>
</template>

<script setup lang="ts">
  /**
   * @description 用于原生字段form-create
   */
  import { ref, watch, watchEffect } from 'vue';

  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import JiraKey from './comp/jiraKey.vue';
  import PassWord from './formcreate-password.vue';
  import SearchSelect from './searchSelect.vue';

  import formCreate, { Api, FormRule } from '@form-create/arco-design';

  formCreate.component('PassWord', PassWord);
  formCreate.component('SearchSelect', SearchSelect);
  formCreate.component('JiraKey', JiraKey);
  formCreate.component('MsTagsInput', MsTagsInput);
  const FormCreate = formCreate.$form();

  const props = defineProps<{
    rule?: FormRule; // 表单的规则
    option: any; // 全局配置项
    api?: Api; // 收集表单的值
  }>();

  const innerApi = ref<any>();

  const emits = defineEmits(['update:api', 'update:rule', 'mounted', 'reload', 'change']);

  const formApi = computed({
    get() {
      return props.api;
    },
    set(val) {
      emits('update:api', val);
    },
  });

  watch(
    () => formApi.value,
    (val) => {
      emits('update:api', val);
    },
    { deep: true }
  );

  const formRules = ref<FormRule | undefined>([]);
  watchEffect(() => {
    formRules.value = props.rule;
    formApi.value = props.api || innerApi.value;
  });
  watch(
    () => props.rule,
    (val) => {
      formRules.value = val;
      formApi.value?.refresh();
    }
  );

  watch(
    () => formRules.value,
    (val) => {
      emits('update:rule', val);
    },
    {
      deep: true,
    }
  );

  function handleMounted() {
    emits('mounted');
  }

  function handleReload() {
    emits('reload');
  }

  function handleChange(value: any) {
    formApi.value?.validateField(value);
    emits('update:api', formApi.value);
    emits('change', value, formApi.value);
  }
  const options = {
    resetBtn: false, // 不展示默认配置的重置和提交
    submitBtn: false,
    on: false, // 取消绑定on事件
    form: {
      layout: 'vertical',
      labelAlign: 'left',
    },
    // 暂时默认
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
    },
  };
</script>

<style scoped></style>
