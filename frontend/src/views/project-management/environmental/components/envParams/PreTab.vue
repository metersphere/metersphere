<template>
  <condition
    v-model:list="innerParams"
    :condition-types="[RequestConditionProcessor.SCRIPT, RequestConditionProcessor.SQL]"
    add-text="apiTestDebug.precondition"
    response=""
    :height-used="600"
  >
  </condition>
</template>

<script lang="ts" setup>
  import condition from '@/views/api-test/components/condition/index.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const store = useProjectEnvStore();
  const innerParams = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.preProcessorConfig.apiProcessorConfig = value || [];
    },
    get: () => store.currentEnvDetailInfo.config.preProcessorConfig.apiProcessorConfig || [],
  });
</script>

<style lang="less" scoped></style>
