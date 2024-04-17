<template>
  <div class="condition">
    <div>
      <precondition
        v-model:config="preProcessorConfig"
        :is-definition="false"
        sql-code-editor-height="300px"
        :tip-content="t('apiScenario.openGlobalPreConditionTip')"
        is-scenario
        @change="emit('change', true)"
      />
    </div>
    <a-divider class="my-[8px]" type="dashed" />
    <div>
      <postcondition
        v-model:config="postProcessorConfig"
        :is-definition="false"
        :layout="activeLayout"
        sql-code-editor-height="300px"
        :tip-content="t('apiScenario.openGlobalPostConditionTip')"
        is-scenario
        @change="emit('change', false)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import postcondition from '@/views/api-test/components/requestComposition/postcondition.vue';
  import precondition from '@/views/api-test/components/requestComposition/precondition.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteConditionConfig } from '@/models/apiTest/common';

  const emit = defineEmits<{
    (e: 'change', isChangePre: boolean): void;
  }>();

  const { t } = useI18n();

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
    .ms-scroll-bar();
    @apply h-full  overflow-auto;
  }
</style>
