<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult().icon" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
    <a-tooltip v-if="props.scriptIdentifier" :content="t('report.detail.scenario.errorTip')">
      <MsTag
        class="ml-2"
        :self-style="{
          border: `1px solid ${methodColor}`,
          color: methodColor,
          backgroundColor: 'white',
        }"
      >
        {{ t('report.detail.script.error') }}
      </MsTag>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { ResourceTypeMapKey } from '@/enums/taskCenter';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
    moduleType: ResourceTypeMapKey;
    scriptIdentifier?: string;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  const iconTypeStatus: Record<ResourceTypeMapKey, any> = {
    [TaskCenterEnum.API_CASE]: {
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'common.success',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'common.fail',
      },
      FAKE_ERROR: {
        icon: 'icon-icon_warning_colorful',
        label: 'common.fakeError',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'common.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      // RERUNNING: {
      //   icon: 'icon-icon_testing',
      //   label: 'project.taskCenter.rerun',
      //   color: '!text-[rgb(var(--link-6))]',
      // },
      PENDING: {
        icon: 'icon-icon_block_filled',
        label: 'common.unExecute',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
    [TaskCenterEnum.API_SCENARIO]: {
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'common.success',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'common.fail',
      },
      FAKE_ERROR: {
        icon: 'icon-icon_warning_colorful',
        label: 'common.fakeError',
      },
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'project.taskCenter.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'common.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      // RERUNNING: {
      //   icon: 'icon-icon_testing',
      //   label: 'project.taskCenter.rerun',
      //   color: '!text-[rgb(var(--link-6))]',
      // },
      PENDING: {
        icon: 'icon-icon_block_filled',
        label: 'common.unExecute',
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
        label: 'common.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'common.fail',
      },
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'common.success',
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
        label: 'common.unExecute',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'common.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      // RERUNNING: {
      //   icon: 'icon-icon_testing',
      //   label: 'project.taskCenter.rerun',
      //   color: '!text-[rgb(var(--link-6))]',
      // },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'common.fail',
      },
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'common.success',
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
        label: 'common.unExecute',
        color: '!text-[var(--color-text-input-border)]',
      },
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'common.success',
      },
      STARTING: {
        icon: 'icon-icon_restarting',
        label: 'project.taskCenter.starting',
        color: '!text-[rgb(var(--link-6))]',
      },
    },
  };

  function getExecutionResult(): IconType {
    return iconTypeStatus[props.moduleType][props.status];
  }
  const methodColor = 'rgb(var(--warning-7))';
</script>

<style scoped></style>
