<template>
  <AllPrams v-model:params="AllParams" :table-key="TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_PARAM" />
</template>

<script lang="ts" setup>
  import AllPrams from '../allParams/index.vue';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const AllParams = ref<[]>([]);
</script>

<style lang="less" scoped></style>
