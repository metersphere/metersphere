<template>
  <div class="flex items-center justify-start">
    <MsIcon
      :type="executionResultMap[props.status]?.icon || ''"
      class="mr-1"
      :class="[executionResultMap[props.status].color]"
    ></MsIcon>
    <span>{{ executionResultMap[props.status]?.statusText || '' }} </span>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 功能用例的执行结果状态
   */
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import { StatusType } from '@/enums/caseEnum';

  const { t } = useI18n();

  const props = defineProps<{
    status: StatusType;
  }>();

  const executionResultMap: Record<string, any> = {
    UN_EXECUTED: {
      key: 'UN_EXECUTED',
      icon: StatusType.UN_EXECUTED,
      statusText: t('caseManagement.featureCase.nonExecution'),
      color: 'text-[var(--color-text-brand)]',
    },
    PASSED: {
      key: 'PASSED',
      icon: StatusType.PASSED,
      statusText: t('caseManagement.featureCase.passed'),
      color: '',
    },
    /* SKIPPED: {
    key: 'SKIPPED',
    icon: StatusType.SKIPPED,
    statusText: t('caseManagement.featureCase.skip'),
    color: 'text-[rgb(var(--link-6))]',
  }, */
    BLOCKED: {
      key: 'BLOCKED',
      icon: StatusType.BLOCKED,
      statusText: t('caseManagement.featureCase.chokeUp'),
      color: 'text-[rgb(var(--warning-6))]',
    },
    FAILED: {
      key: 'FAILED',
      icon: StatusType.FAILED,
      statusText: t('caseManagement.featureCase.failure'),
      color: '',
    },
  };
</script>

<style scoped lang="less"></style>
