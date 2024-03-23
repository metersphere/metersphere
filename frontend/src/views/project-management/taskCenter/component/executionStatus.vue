<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult().icon" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import { TaskCenterEnum } from '@/enums/taskCenter';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
    moduleType: keyof typeof TaskCenterEnum;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  const iconTypeStatus = ref({
    [TaskCenterEnum.API_CASE]: {
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'project.taskCenter.successful',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'project.taskCenter.failure',
      },
      FAKE_ERROR: {
        icon: 'icon-icon_warning_colorful',
        label: 'project.taskCenter.falseAlarm',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'project.taskCenter.inExecution',
        color: '!text-[rgb(var(--link-6))]',
      },
      RERUNNING: {
        icon: 'icon-icon_testing',
        label: 'project.taskCenter.rerun',
        color: '!text-[rgb(var(--link-6))]',
      },
      PENDING: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.queuing',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
    [TaskCenterEnum.API_SCENARIO]: {
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'project.taskCenter.successful',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'project.taskCenter.failure',
      },
      FAKE_ERROR: {
        icon: 'icon-icon_warning_colorful',
        label: 'project.taskCenter.falseAlarm',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'project.taskCenter.inExecution',
        color: '!text-[rgb(var(--link-6))]',
      },
      RERUNNING: {
        icon: 'icon-icon_testing',
        label: 'project.taskCenter.rerun',
        color: '!text-[rgb(var(--link-6))]',
      },
      PENDING: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.queuing',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
    [TaskCenterEnum.LOAD_TEST]: {
      STARTING: {
        icon: 'icon-icon_restarting',
        label: 'project.taskCenter.starting',
        color: '!text-[rgb(var(--link-6))]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'project.taskCenter.inExecution',
        color: '!text-[rgb(var(--link-6))]',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'project.taskCenter.failure',
      },
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'project.taskCenter.successful',
      },
      COMPLETED: {
        icon: 'icon-icon_succeed_colorful',
        label: 'project.taskCenter.complete',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
    [TaskCenterEnum.UI_TEST]: {
      PENDING: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.queuing',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'project.taskCenter.inExecution',
        color: '!text-[rgb(var(--link-6))]',
      },
      RERUNNING: {
        icon: 'icon-icon_testing',
        label: 'project.taskCenter.rerun',
        color: '!text-[rgb(var(--link-6))]',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'project.taskCenter.failure',
      },
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'project.taskCenter.successful',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
    [TaskCenterEnum.TEST_PLAN]: {
      RUNNING: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.queuing',
        color: '!text-[var(--color-text-input-border)]',
      },
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'project.taskCenter.successful',
      },
      STARTING: {
        icon: 'icon-icon_restarting',
        label: 'project.taskCenter.starting',
        color: '!text-[rgb(var(--link-6))]',
      },
    },
  });

  function getExecutionResult(): IconType {
    return iconTypeStatus.value[props.moduleType][props.status];
  }
</script>

<style scoped></style>
