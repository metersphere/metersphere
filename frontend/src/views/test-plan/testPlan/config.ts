import type { planStatusType } from '@/models/testPlan/testPlan';

export type PlanStatusMap = Record<
  planStatusType,
  {
    label: string;
    color: string;
    class: string;
  }
>;
export const planStatusMap: PlanStatusMap = {
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
};
