<template>
  <assertion v-model:params="assertions" :is-definition="false" :assertion-config="assertionConfig" />
</template>

<script setup lang="ts">
  import assertion from '@/components/business/ms-assertion/index.vue';

  import { AssertionConfig } from '@/models/apiTest/scenario';

  const assertions = ref([]);
  const assertionConfig = defineModel<AssertionConfig>('assertionConfig', {
    required: true,
  });
</script>

<style lang="less" scoped></style>
