<template>
  <div id="app">
    <form-create v-model="value" v-model:api="fApi" :rule="rule" :option="option"></form-create>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';

  const value = ref({});
  const fApi = ref({});
  const rule = reactive([
    {
      type: 'input',
      field: 'goods_name',
      title: '商品名称',
    },
    {
      type: 'datePicker',
      field: 'created_at',
      title: '创建时间',
    },
  ]);
  const option = {
    onSubmit(formData: any) {
      // eslint-disable-next-line no-alert
      alert(JSON.stringify(formData));
    },
  };
</script>
