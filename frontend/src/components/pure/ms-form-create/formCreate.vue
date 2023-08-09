<template>
  <FormCreate v-model:api="fApi" :rule="rule" :option="option"></FormCreate>
</template>

<script setup lang="ts">
  import PassWord from './formcreate-password.vue';
  import formCreate, { Rule, Api } from '@form-create/arco-design';

  formCreate.component('PassWord', PassWord);
  const FormCreate = formCreate.$form();

  defineProps<{
    rule: Rule[];
    option: any;
  }>();

  const fApi = {};
</script>

<style scoped></style>
