<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult()?.icon" :class="`text-[${getExecutionResult()?.color}]`" size="14" />
    <span class="ml-1">{{ t(getExecutionResult()?.label || '-') }}</span>
    <!-- <a-tooltip v-if="props.scriptIdentifier" :content="getMsg()">
      <MsTag
        class="ml-2"
        :self-style="{
          border: `1px solid ${methodColor}`,
          color: methodColor,
          backgroundColor: 'var(--color-text-fff)',
        }"
      >
        {{ t('report.detail.script.error') }}
      </MsTag>
    </a-tooltip> -->
  </div>
</template>

<script setup lang="ts">
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { TaskCenterEnum } from '@/enums/taskCenter';

  import { executeResultMap } from './config';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
    scriptIdentifier?: string;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  function getExecutionResult(): IconType {
    return executeResultMap[props.status] ? executeResultMap[props.status] : executeResultMap.DEFAULT;
  }
  const methodColor = 'rgb(var(--warning-7))';

  // function getMsg() {
  //   if (props.moduleType === TaskCenterEnum.CASE && props.scriptIdentifier) {
  //     return t('report.detail.scenario.errorTip');
  //   }
  //   return t('report.detail.api.errorTip');
  // }
</script>

<style scoped></style>
