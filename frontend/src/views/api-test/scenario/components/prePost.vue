<template>
  <div class="condition">
    <div>
      <precondition v-model:config="preProcessorConfig" :is-definition="false" />
    </div>
    <a-divider class="my-[8px]" type="dashed" />
    <div>
      <postcondition v-model:config="postProcessorConfig" :is-definition="false" :layout="activeLayout" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';

  import { ExecuteConditionConfig } from '@/models/apiTest/common';

  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const preProcessorConfig = defineModel<ExecuteConditionConfig>('preProcessorConfig', {
    required: true,
  });
  const postProcessorConfig = defineModel<ExecuteConditionConfig>('postProcessorConfig', {
    required: true,
  });
</script>

<style lang="less" scoped>
  .condition {
    overflow: overlay;
    width: 100%;
    height: 700px;
    flex-shrink: 0;
  }
</style>
