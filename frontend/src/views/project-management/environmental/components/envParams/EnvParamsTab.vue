<template>
  <AllPrams v-model:params="allParams" :table-key="TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_PARAM" />
</template>

<script lang="ts" setup>
  import AllPrams from '../allParams/index.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const store = useProjectEnvStore();

  const allParams = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.commonVariables = value;
    },
    get: () => store.currentEnvDetailInfo.config?.commonVariables || [],
  });
</script>

<style lang="less" scoped></style>
