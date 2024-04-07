<template>
  <condition
    v-model:list="innerConfig.processors"
    :disabled="props.disabled"
    :condition-types="conditionTypes"
    add-text="apiTestDebug.precondition"
    @change="emit('change')"
  >
    <template v-if="props.isDefinition" #titleRight>
      <a-switch
        v-model:model-value="innerConfig.enableGlobal"
        :disabled="props.disabled"
        size="small"
        type="line"
      ></a-switch>
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.openGlobalPrecondition') }}</div>
      <a-tooltip :content="t('apiTestDebug.openGlobalPreconditionTip')" position="left">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </template>
  </condition>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import condition from '@/views/api-test/components/condition/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteConditionConfig } from '@/models/apiTest/common';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const props = defineProps<{
    config: ExecuteConditionConfig;
    isDefinition?: boolean; // 是否是定义页面
    isScenario?: boolean; // 是否是场景页面
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:config', params: ExecuteConditionConfig): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();
  const innerConfig = useVModel(props, 'config', emit);

  const conditionTypes = computed(() => {
    // 接口定义
    if (props.isDefinition) {
      return [RequestConditionProcessor.SCRIPT, RequestConditionProcessor.SQL, RequestConditionProcessor.TIME_WAITING];
    }
    // 接口场景
    if (props.isScenario) {
      return [RequestConditionProcessor.SCRIPT, RequestConditionProcessor.SQL];
    }
    // 接口调试
    return [RequestConditionProcessor.SCRIPT, RequestConditionProcessor.TIME_WAITING];
  });
</script>

<style lang="less" scoped></style>
