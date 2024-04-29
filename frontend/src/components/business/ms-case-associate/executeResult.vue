<template>
  <div v-if="props.executeResult" class="flex items-center">
    <MsIcon
      :type="lastExecuteResultMap[props.executeResult]?.icon || ''"
      class="mr-1"
      :size="16"
      :style="{ color: lastExecuteResultMap[props.executeResult].color }"
    ></MsIcon>
    <span class="text-[14px]">{{ status?.text || '' }}</span>
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { LastExecuteResults, StatusType } from '@/enums/caseEnum';

  const { t } = useI18n();

  const props = defineProps<{
    executeResult?: LastExecuteResults;
  }>();

  const lastExecuteResultMap = {
    UN_EXECUTED: {
      label: 'UN_EXECUTED',
      icon: StatusType.UN_EXECUTED,
      statusText: 'caseManagement.featureCase.nonExecution',
      color: 'var(--color-text-brand)',
    },
    PASSED: {
      label: 'PASSED',
      icon: StatusType.PASSED,
      statusText: 'caseManagement.featureCase.passed',
      color: '',
    },
    SKIPPED: {
      label: 'SKIPPED',
      icon: StatusType.SKIPPED,
      statusText: 'caseManagement.featureCase.skip',
      color: 'rgb(var(--link-6))',
    },
    BLOCKED: {
      label: 'BLOCKED',
      icon: StatusType.BLOCKED,
      statusText: 'caseManagement.featureCase.chokeUp',
      color: 'rgb(var(--warning-6))',
    },
    FAILED: {
      label: 'FAILED',
      icon: StatusType.FAILED,
      statusText: 'caseManagement.featureCase.failure',
      color: '',
    },
  };

  const status = computed(() => {
    if (props.executeResult) {
      const config = lastExecuteResultMap[props.executeResult];
      return {
        text: t(config?.statusText || ''),
      };
    }
  });
</script>

<style lang="less" scoped></style>
