<template>
  <a-tag :color="statusMap[props.status]?.color" :class="statusMap[props.status]?.class" :size="props.size">
    {{ t(statusMap[props.status]?.label) }}
  </a-tag>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ReportExecStatus } from '@/enums/apiEnum';

  const props = defineProps<{
    status: ReportExecStatus;
    size?: 'small' | 'medium' | 'large';
  }>();
  const { t } = useI18n();

  const statusMap = {
    PENDING: {
      label: 'common.unExecute',
      color: 'var(--color-text-n8)',
      class: '!text-[var(--color-text-1)]',
    },
    RUNNING: {
      label: 'common.running',
      color: 'rgb(var(--link-2))',
      class: '!text-[rgb(var(--link-6))]',
    },
    STOPPED: {
      label: 'common.stopped',
      color: 'rgb(var(--warning-2))',
      class: '!text-[rgb(var(--warning-6))]',
    },
    COMPLETED: {
      label: 'report.completed',
      color: 'rgb(var(--success-2))',
      class: '!text-[rgb(var(--success-6))]',
    },
  } as const;
</script>

<style lang="less" scoped></style>
