<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult().icon" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ReportEnum } from '@/enums/reportEnum';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
    moduleType: keyof typeof ReportEnum;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  const iconTypeStatus = ref({
    [ReportEnum.API_REPORT]: {
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'report.successful',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'report.failure',
      },
      FAKE_ERROR: {
        icon: 'icon-icon_warning_colorful',
        label: 'report.fake.error',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'report.stopped',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'report.status.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      RERUNNING: {
        icon: 'icon-icon_testing',
        label: 'report.status.rerunning',
        color: '!text-[rgb(var(--link-6))]',
      },
      PENDING: {
        icon: 'icon-icon_wait',
        label: 'report.status.pending',
        color: '!text-[rgb(var(--link-6))]',
      },
    },
    [ReportEnum.API_SCENARIO_REPORT]: {
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'report.successful',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'report.failure',
      },
      FAKE_ERROR: {
        icon: 'icon-icon_warning_colorful',
        label: 'report.fake.error',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'report.stopped',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'report.status.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      RERUNNING: {
        icon: 'icon-icon_testing',
        label: 'report.status.rerunning',
        color: '!text-[rgb(var(--link-6))]',
      },
      PENDING: {
        icon: 'icon-icon_wait',
        label: 'report.status.pending',
        color: '!text-[rgb(var(--link-6))]',
      },
    },
  });

  function getExecutionResult(): IconType {
    return iconTypeStatus.value[props.moduleType][props.status];
  }
</script>

<style scoped></style>