<template>
  <FormCreate
    v-model:api="formApi"
    :rule="formRules"
    :option="props.option || options"
    :in-for="props.isInFor"
    @mounted="handleMounted"
    @reload="handleReload"
    @change="handleChange"
  ></FormCreate>
</template>

<script setup lang="ts">
  /**
   * @description 用于原生字段form-create
   */
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
    option: any; // 全局配置项
    isInFor?: boolean; // 是否处于循环中
  }>();

  const emits = defineEmits<{
    (e: 'mounted'): void;
    (e: 'reload'): void;
    (e: 'change', filed: string): void;
  }>();

  const formApi = defineModel<Api>('api', {
    default: undefined,
  });

  const formRules = defineModel<FormRule | undefined>('rule', {
    default: {},
  });

  watch(
    () => formRules.value,
    () => {
      formApi.value?.refresh();
    }
  );

  function handleMounted() {
    emits('mounted');
  }

  function handleReload() {
    emits('reload');
  }

  function handleChange(filed: string) {
    formApi.value?.validateField(filed);
    emits('change', filed);
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
