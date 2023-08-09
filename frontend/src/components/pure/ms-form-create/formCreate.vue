<template>
  <FormCreate v-model:api="formApi" :rule="rule" :option="option"></FormCreate>
</template>

<script setup lang="ts">
  import { ref, watchEffect, watch } from 'vue';
  import PassWord from './formcreate-password.vue';
  import formCreate, { Rule } from '@form-create/arco-design';

  formCreate.component('PassWord', PassWord);
  const FormCreate = formCreate.$form();

  const props = defineProps<{
    rule: Rule[]; // 表单的规则
    option: any; // 全局配置项
    api: any; // 收集表单的值
  }>();

  const emits = defineEmits<{
    (e: 'update:api', val: any): void;
  }>();
  const formApi = ref({});

  watchEffect(() => {
    formApi.value = props.api;
  });
  watch(
    () => formApi.value,
    (val) => {
      emits('update:api', val);
    }
  );
</script>

<style scoped></style>
