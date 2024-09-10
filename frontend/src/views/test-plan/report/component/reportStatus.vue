<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult()?.icon" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { iconTypeStatus } from '../detail/component/reportConfig';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  function getExecutionResult(): IconType {
    return iconTypeStatus[props.status] ? iconTypeStatus[props.status] : iconTypeStatus.DEFAULT;
  }
</script>

<style scoped></style>
