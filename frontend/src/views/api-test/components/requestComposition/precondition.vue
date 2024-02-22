<template>
  <condition
    v-model:list="innerConfig.processors"
    :condition-types="[RequestConditionProcessor.SCRIPT, RequestConditionProcessor.TIME_WAITING]"
    add-text="apiTestDebug.precondition"
    @change="emit('change')"
  />

  <!-- <template #titleRight>
      <a-switch v-model:model-value="innerConfig.enableGlobal" size="small" type="line"></a-switch>
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.openGlobalPrecondition') }}</div>
      <a-tooltip :content="t('apiTestDebug.openGlobalPreconditionTip')" position="left">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </template> -->
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import condition from '@/views/api-test/components/condition/index.vue';

  import { ExecuteConditionConfig } from '@/models/apiTest/debug';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  // import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    config: ExecuteConditionConfig;
  }>();
  const emit = defineEmits<{
    (e: 'update:config', params: ExecuteConditionConfig): void;
    (e: 'change'): void;
  }>();

  // const { t } = useI18n();
  const innerConfig = useVModel(props, 'config', emit);
</script>

<style lang="less" scoped></style>
