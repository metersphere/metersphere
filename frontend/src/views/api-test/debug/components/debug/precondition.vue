<template>
  <condition
    v-model:list="preconditions"
    :condition-types="['SCRIPT', 'TIME_WAITING']"
    add-text="apiTestDebug.precondition"
    @change="emit('change')"
  >
    <!-- <template #titleRight>
      <a-switch v-model:model-value="openGlobalPrecondition" size="small" type="line"></a-switch>
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.openGlobalPrecondition') }}</div>
      <a-tooltip :content="t('apiTestDebug.openGlobalPreconditionTip')" position="left">
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

  import { ExecuteConditionProcessor } from '@/models/apiTest/debug';

  // import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: ExecuteConditionProcessor[];
  }>();
  const emit = defineEmits<{
    (e: 'update:params', params: ExecuteConditionProcessor[]): void;
    (e: 'change'): void;
  }>();

  // const { t } = useI18n();
  // 是否开启全局前置条件
  // const openGlobalPrecondition = ref(false);
  const preconditions = useVModel(props, 'params', emit);
</script>

<style lang="less" scoped></style>
