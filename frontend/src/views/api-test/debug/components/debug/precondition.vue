<template>
  <condition
    v-model:list="preconditions"
    :condition-types="['script', 'sql', 'waitTime']"
    add-text="apiTestDebug.precondition"
    @change="emit('change')"
  >
    <template #titleRight>
      <a-switch v-model:model-value="openGlobalPrecondition" size="small" type="line"></a-switch>
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

  import condition from '../../../components/condition/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: any[];
  }>();
  const emit = defineEmits<{
    (e: 'update:params', params: any[]): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();
  // 是否开启全局前置条件
  const openGlobalPrecondition = ref(false);
  const preconditions = useVModel(props, 'params', emit);
</script>

<style lang="less" scoped></style>
