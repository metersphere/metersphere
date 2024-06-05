<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult().icon" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ReportStatusEnum } from '@/enums/reportEnum';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  const iconTypeStatus: Record<string, any> = {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'common.success',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'common.fail',
    },
    DEFAULT: {
      label: '-',
      color: '!text-[var(--color-text-input-border)]',
    },
  };

  function getExecutionResult(): IconType {
    return iconTypeStatus[props.status] ? iconTypeStatus[props.status] : iconTypeStatus.DEFAULT;
  }
</script>

<style scoped></style>
