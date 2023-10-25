<template>
  <FormCreate v-model:api="formApi" :rule="rule" :option="option"></FormCreate>
</template>

<script setup lang="ts">
  import { ref, watch, watchEffect } from 'vue';

  import PassWord from './formcreate-password.vue';
  import SearchSelect from './searchSelect.vue';

  import formCreate, { FormRule } from '@form-create/arco-design';

  formCreate.component('PassWord', PassWord);
  formCreate.component('SearchSelect', SearchSelect);
  const FormCreate = formCreate.$form();

  const props = defineProps<{
    rule: FormRule | undefined; // 表单的规则
    option: any; // 全局配置项
    api: any; // 收集表单的值
  }>();

  const emits = defineEmits<{
    (e: 'update:api', val: any): void;
  }>();
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
</script>

<style scoped></style>
