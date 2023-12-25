<template>
  <FormCreate v-model:api="formApi" :rule="formRules" :option="option"></FormCreate>
</template>

<script setup lang="ts">
  /**
   * @description 用于原生字段form-create
   */
  import { ref, watch, watchEffect } from 'vue';

  import JiraKey from './comp/jiraKey.vue';
  import PassWord from './formcreate-password.vue';
  import SearchSelect from './searchSelect.vue';

  import formCreate, { FormRule } from '@form-create/arco-design';

  formCreate.component('PassWord', PassWord);
  formCreate.component('SearchSelect', SearchSelect);
  formCreate.component('JiraKey', JiraKey);
  const FormCreate = formCreate.$form();

  const props = defineProps<{
    rule: FormRule | undefined; // 表单的规则
    option: any; // 全局配置项
    api: any; // 收集表单的值
  }>();

  const emits = defineEmits(['update:api', 'update:rule']);

  const formApi = ref<any>({});

  watchEffect(() => {
    formApi.value = props.api;
  });
  watch(
    () => formApi.value,
    (val) => {
      emits('update:api', val);
    }
  );

  const formRules = ref<FormRule | undefined>([]);
  watchEffect(() => {
    formRules.value = props.rule;
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
    }
  );
</script>

<style scoped></style>
