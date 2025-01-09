<template>
  <AllPrams
    v-model:keyword="innerKeyword"
    v-model:params="projectEnvStore.currentEnvDetailInfo.config.commonVariables"
    :table-key="TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_PARAM"
  />
</template>

<script lang="ts" setup>
  import AllPrams from '../allParams/index.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const innerKeyword = defineModel<string>('keyword', { default: '' });

  const projectEnvStore = useProjectEnvStore();
</script>

<style lang="less" scoped></style>
