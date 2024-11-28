<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult().icon || '-'" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
    <a-tooltip v-if="props.scriptIdentifier" :content="getMsg()">
      <MsTag
        class="ml-2"
        :tooltip-disabled="true"
        :self-style="{
          border: `1px solid ${methodColor}`,
          color: methodColor,
          backgroundColor: 'var(--color-text-fff)',
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
    DEFAULT: {
      label: '-',
      color: '!text-[var(--color-text-input-border)]',
    },
  };

  function getExecutionResult(): IconType {
    return iconTypeStatus[props.status] ? iconTypeStatus[props.status] : iconTypeStatus.DEFAULT;
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
