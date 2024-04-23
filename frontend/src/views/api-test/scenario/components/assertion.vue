<template>
  <assertion
    v-model:params="assertionConfig.assertions"
    :is-definition="false"
    :assertion-config="assertionConfig"
    :show-extraction="true"
    script-code-editor-height="calc(100vh - 340px)"
    @change="emit('change')"
  />
</template>

<script setup lang="ts">
  import assertion from '@/components/business/ms-assertion/index.vue';

  import { AssertionConfig } from '@/models/apiTest/scenario';

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const assertionConfig = defineModel<AssertionConfig>('assertionConfig', {
    required: true,
  });
</script>

<style lang="less" scoped></style>
