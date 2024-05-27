<template>
  <a-tag :color="statusMap[props.status]?.color" :class="statusMap[props.status]?.class" :size="props.size">
    {{ t(statusMap[props.status]?.label) }}
  </a-tag>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import type { ReviewStatus } from '@/models/caseManagement/caseReview';
  import type { planStatusType } from '@/models/testPlan/testPlan';

  const props = defineProps<{
    status: ReviewStatus | planStatusType;
    size?: 'small' | 'medium' | 'large';
  }>();
  const { t } = useI18n();

  const statusMap = {
    PREPARED: {
      label: 'caseManagement.caseReview.unStart',
      color: 'var(--color-text-n8)',
      class: '!text-[var(--color-text-1)]',
    },
    UNDERWAY: {
      label: 'caseManagement.caseReview.going',
      color: 'rgb(var(--link-2))',
      class: '!text-[rgb(var(--link-6))]',
    },
    COMPLETED: {
      label: 'caseManagement.caseReview.finished',
      color: 'rgb(var(--success-2))',
      class: '!text-[rgb(var(--success-6))]',
    },
    ARCHIVED: {
      label: 'caseManagement.caseReview.archived',
      color: 'var(--color-text-n8)',
      class: '!text-[var(--color-text-4)]',
    },
    SUCCESS: {
      label: 'common.success',
      color: 'rgb(var(--success-2))',
      class: '!text-[rgb(var(--success-6))]',
    },
    ERROR: {
      label: 'common.fail',
      color: 'rgb(var(--danger-2))',
      class: '!text-[rgb(var(--danger-6))]',
    },
    PENDING: {
      label: 'common.unExecute',
      color: 'var(--color-text-n8)',
      class: '!text-[var(--color-text-1)]',
    },
    BLOCKED: {
      label: 'common.block',
      color: 'var(--color-bg-6)',
      class: '!text-[rgb(var(--primary-6))]',
    },
    UN_EXECUTED: {
      label: 'common.unExecute',
      color: 'var(--color-text-n8)',
      class: '!text-[var(--color-text-1)]',
    },
  } as const;
</script>

<style lang="less" scoped></style>
