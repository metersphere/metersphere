<template>
  <condition
    v-model:list="innerConfig.processors"
    :condition-types="[RequestConditionProcessor.SCRIPT]"
    add-text="apiTestDebug.postCondition"
    :response="props.response"
    :height-used="heightUsed"
    @change="emit('change')"
  >
    <!-- <template #titleRight>
      <a-switch v-model:model-value="innerConfig.enableGlobal" size="small" type="line"></a-switch>
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.openGlobalPostCondition') }}</div>
      <a-tooltip :content="t('apiTestDebug.openGlobalPostConditionTip')" position="left">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </template> -->
  </condition>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import condition from '@/views/api-test/components/condition/index.vue';

  import { ExecuteConditionConfig, ExecuteConditionProcessor } from '@/models/apiTest/common';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  // import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    config: ExecuteConditionConfig;
    secondBoxHeight?: number;
    layout: 'horizontal' | 'vertical';
    response?: string; // 响应内容
  }>();
  const emit = defineEmits<{
    (e: 'update:params', params: ExecuteConditionProcessor[]): void;
    (e: 'change'): void;
  }>();

  // const { t } = useI18n();
  const innerConfig = useVModel(props, 'config', emit);
  const heightUsed = computed(() => {
    if (props.layout === 'horizontal') {
      return 428;
    }
    return 428 + (props.secondBoxHeight || 0);
  });
</script>

<style lang="less" scoped></style>
