<template>
  <setting v-model:params="otherConfig" />
</template>

<script setup lang="ts">
  import setting from '@/views/api-test/components/requestComposition/setting.vue';

  import { ExecuteOtherConfig } from '@/models/apiTest/common';

  const otherConfig = ref({} as ExecuteOtherConfig);
</script>

<style lang="less" scoped></style>
