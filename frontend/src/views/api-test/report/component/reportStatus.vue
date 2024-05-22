<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult().icon" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
    <a-tooltip v-if="props.scriptIdentifier" :content="getMsg()">
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

  import { ReportEnum } from '@/enums/reportEnum';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
    scriptIdentifier?: string;
    moduleType: keyof typeof ReportEnum;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  // TODO: Record<string,any>
  const iconTypeStatus: Record<string, any> = {
    [ReportEnum.API_REPORT]: {
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
        label: 'common.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'common.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      // RERUNNING: {
      //   icon: 'icon-icon_testing',
      //   label: 'report.status.rerunning',
      //   color: '!text-[rgb(var(--link-6))]',
      // },
      PENDING: {
        icon: 'icon-icon_block_filled',
        label: 'common.unExecute',
        color: '!text-[var(--color-text-input-border)]',
      },
      null: {
        icon: 'icon-icon_block_filled',
        label: 'common.unExecute',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
    [ReportEnum.API_SCENARIO_REPORT]: {
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
        label: 'common.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'common.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      // RERUNNING: {
      //   icon: 'icon-icon_testing',
      //   label: 'report.status.rerunning',
      //   color: '!text-[rgb(var(--link-6))]',
      // },
      PENDING: {
        icon: 'icon-icon_wait',
        label: 'common.unExecute',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
  };

  function getExecutionResult(): IconType {
    return iconTypeStatus[props.moduleType]?.[props.status];
  }
  const methodColor = 'rgb(var(--warning-7))';

  function getMsg() {
    if (props.moduleType === ReportEnum.API_SCENARIO_REPORT && props.scriptIdentifier) {
      return t('report.detail.scenario.errorTip');
    }
    return t('report.detail.api.errorTip');
  }
</script>

<style scoped></style>
