<template>
  <condition
    v-model:list="postConditions"
    :condition-types="['script', 'sql', 'extract']"
    add-text="apiTestDebug.postCondition"
    :response="props.response"
    :height-used="heightUsed"
    @change="emit('change')"
  >
    <template #titleRight>
      <a-switch v-model:model-value="openGlobalPostCondition" size="small" type="line"></a-switch>
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.openGlobalPostCondition') }}</div>
      <a-tooltip :content="t('apiTestDebug.openGlobalPostConditionTip')" position="left">
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

  const props = defineProps<{
    params: any[];
    secondBoxHeight?: number;
    layout: 'horizontal' | 'vertical';
    response?: string; // 响应内容
  }>();
  const emit = defineEmits<{
    (e: 'update:params', params: any[]): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();
  // 是否开启全局后置条件
  const openGlobalPostCondition = ref(false);
  const postConditions = useVModel(props, 'params', emit);
  const heightUsed = computed(() => {
    if (props.layout === 'horizontal') {
      return 422;
    }
    return 422 + (props.secondBoxHeight || 0);
  });
</script>

<style lang="less" scoped></style>
