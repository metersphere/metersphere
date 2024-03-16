<template>
  <assertion v-model:params="assertions" :is-definition="false" :assertion-config="assertionConfig" />
</template>

<script setup lang="ts">
  import assertion from '@/components/business/ms-assertion/index.vue';

  const assertions = ref([]);
  const assertionConfig = ref({
    enableGlobal: false,
    assertions: [],
  });
</script>

<style lang="less" scoped></style>
