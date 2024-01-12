import { ReviewItem } from '@/models/caseManagement/caseReview';

// 评审结果
export const reviewResultMap = {
  UN_REVIEWED: {
    label: 'caseManagement.caseReview.unReview',
    color: 'var(--color-text-input-border)',
    icon: 'icon-icon_block_filled',
  },
  UNDER_REVIEWED: {
    label: 'caseManagement.caseReview.reviewing',
    color: 'rgb(var(--link-6))',
    icon: 'icon-icon_testing',
  },
  PASS: {
    label: 'caseManagement.caseReview.reviewPass',
    color: 'rgb(var(--success-6))',
    icon: 'icon-icon_succeed_filled',
  },
  UN_PASS: {
    label: 'caseManagement.caseReview.fail',
    color: 'rgb(var(--danger-6))',
    icon: 'icon-icon_close_filled',
  },
  RE_REVIEWED: {
    label: 'caseManagement.caseReview.reReview',
    color: 'rgb(var(--warning-6))',
    icon: 'icon-icon_resubmit_filled',
  },
} as const;
// 评审状态
export const reviewStatusMap = {
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
// 评审详情
export const reviewDefaultDetail: ReviewItem = {
  id: '',
  num: 0,
  moduleId: '',
  projectId: '',
  reviewPassRule: 'SINGLE',
  name: '',
  status: 'PREPARED',
  caseCount: 0,
  passCount: 0,
  unPassCount: 0,
  reviewedCount: 0,
  underReviewedCount: 0,
  pos: 5000,
  startTime: 0,
  endTime: 0,
  passRate: 0,
  tags: [],
  description: '',
  createTime: 0,
  createUser: '',
  updateTime: 0,
  updateUser: '',
  reviewers: [],
  reReviewedCount: 0,
  followFlag: false,
};
