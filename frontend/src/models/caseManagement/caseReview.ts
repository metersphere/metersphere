import { BatchApiParams, TableQueryParams } from '@/models/common';

// 评审模块
export interface ReviewModule {
  projectId: string;
  name: string;
  parentId: string;
}
// 更新评审模块入参
export interface UpdateReviewModuleParams {
  id: string;
  name: string;
}
// 评审模块列表项
export interface ReviewModuleItem {
  id: string;
  name: string;
  type: string;
  parentId: string;
  children: ReviewModuleItem[];
  count: number; // 模块内评审数量
}
// 评审类型
export type ReviewPassRule = 'SINGLE' | 'MULTIPLE';
// 评审
export interface Review {
  projectId: string;
  name: string;
  moduleId: string;
  reviewPassRule: ReviewPassRule; // 评审通过规则
  startTime: number;
  endTime: number;
  tags: string[];
  description: string;
  reviewers: string[]; // 评审人员
  caseIds: string[]; // 关联用例
}
// 更新评审入参
export interface UpdateReviewParams extends Omit<Review, 'caseIds'> {
  id: string;
}
// 关联用例入参
export interface AssociateReviewCaseParams {
  reviewId: string;
  projectId: string;
  caseIds: string[];
  reviewers: string[];
}
// 关注/取消关注评审入参
export interface FollowReviewParams {
  userId: string; // 用户id
  caseReviewId: string;
}
// 批量操作评审参数
export interface BatchMoveReviewParams extends BatchApiParams {
  projectId: string;
  moveModuleId: string; // 移动到的评审模块id
  moduleIds: string[];
}
// 评审拖拽排序类型
export type ReviewMoveMode = 'BEFORE' | 'AFTER' | 'APPEND';
// 评审拖拽排序入参
export interface SortReviewParams {
  projectId: string;
  targetId: string; // 目标评审id
  moveMode: ReviewMoveMode;
  moveId: string; // 被移动的评审id
}
// 文件列表查询参数
export interface ReviewListQueryParams extends TableQueryParams {
  moduleIds: string[];
  projectId: string;
}
export type ReviewStatus = 'PREPARED' | 'UNDERWAY' | 'COMPLETED' | 'ARCHIVED'; // 评审状态, PREPARED: 待开始, UNDERWAY: 进行中, COMPLETED: 已完成, ARCHIVED: 已归档
// 评审列表项
export interface ReviewItem {
  id: string;
  num: number;
  name: string;
  moduleId: string;
  projectId: string;
  status: ReviewStatus;
  reviewPassRule: ReviewPassRule;
  pos: number; // 自定义排序，间隔5000
  startTime: number;
  endTime: number;
  caseCount: number;
  passRate: number;
  tags: string;
  description: string;
  createTime: number;
  createUser: string;
  updateTime: number;
  updateUser: string;
  reviewers: string[];
  passCount: number;
  unPassCount: number;
  reReviewedCount: number;
  underReviewedCount: number;
  reviewedCount: number;
  followFlag: boolean; // 关注标识
}
// 评审人员列表项
export interface ReviewUserItem {
  id: string;
  name: string;
  email: string;
  password: string;
  enable: boolean;
  createTime: number;
  updateTime: number;
  language: string;
  lastOrganizationId: string;
  phone: string;
  source: string;
  lastProjectId: string;
  createUser: string;
  updateUser: string;
  deleted: boolean;
}
