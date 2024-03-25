<template>
  <assertion v-model:params="assertionConfig.assertions" :is-definition="false" :assertion-config="assertionConfig" />
</template>

<script setup lang="ts">
  import assertion from '@/components/business/ms-assertion/index.vue';

  import { AssertionConfig } from '@/models/apiTest/scenario';

  const assertionConfig = defineModel<AssertionConfig>('assertionConfig', {
    required: true,
  });
</script>

<style lang="less" scoped></style>
