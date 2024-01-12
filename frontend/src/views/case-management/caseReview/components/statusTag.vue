<template>
  <a-tag :color="statusMap[props.status]?.color" :class="statusMap[props.status]?.class">
    {{ t(statusMap[props.status]?.label) }}
  </a-tag>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import type { ReviewStatus } from '@/models/caseManagement/caseReview';

  const props = defineProps<{
    status: ReviewStatus;
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
  } as const;
</script>

<style lang="less" scoped></style>
