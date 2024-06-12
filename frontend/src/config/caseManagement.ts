import { ReviewItem, ReviewResult, ReviewStatus } from '@/models/caseManagement/caseReview';
import type { DetailCase } from '@/models/caseManagement/featureCase';

// 评审结果
export type ReviewResultMap = Record<
  ReviewResult,
  {
    label: string;
    color: string;
    icon: string;
  }
>;
export const reviewResultMap: ReviewResultMap = {
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
};
// 评审状态
export type ReviewStatusMap = Record<
  ReviewStatus,
  {
    label: string;
    color: string;
    class: string;
  }
>;
export const reviewStatusMap: ReviewStatusMap = {
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
  // TODO: 第一版不上归档
  // ARCHIVED: {
  //   label: 'caseManagement.caseReview.archived',
  //   color: 'var(--color-text-n8)',
  //   class: '!text-[var(--color-text-4)]',
  // },
};
// 评审详情
export const reviewDefaultDetail: ReviewItem = {
  id: '',
  num: 0,
  moduleId: '',
  projectId: '',
  reviewPassRule: 'SINGLE',
  name: '',
  status: 'PREPARED' as ReviewStatus,
  caseCount: 0,
  passCount: 0,
  unPassCount: 0,
  reviewedCount: 0,
  underReviewedCount: 0,
  unReviewCount: 0,
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
// 脑图-标签
export const minderTagMap = {
  CASE: 'minder.tag.case',
  MODULE: 'minder.tag.module',
  PREREQUISITE: 'minder.tag.precondition',
  TEXT_DESCRIPTION: 'minder.tag.desc',
  EXPECTED_RESULT: 'minder.tag.expect',
  DESCRIPTION: 'minder.tag.remark',
};

export const defaultCaseDetail: DetailCase = {
  id: '',
  projectId: '',
  templateId: '',
  name: '',
  prerequisite: '', // prerequisite
  caseEditType: '', // 编辑模式：步骤模式/文本模式
  steps: '',
  textDescription: '',
  expectedResult: '', // 预期结果
  description: '',
  publicCase: false, // 是否公共用例
  moduleId: '',
  versionId: '',
  tags: [],
  customFields: [], // 自定义字段集合
  relateFileMetaIds: [], // 关联文件ID集合
  functionalPriority: '',
  reviewStatus: 'UN_REVIEWED',
};
