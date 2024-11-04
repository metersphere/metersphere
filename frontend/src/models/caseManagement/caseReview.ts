import { OptionItem } from '@/api/modules/message/index';

import { BatchApiParams, TableQueryParams } from '@/models/common';
import { StartReviewStatus } from '@/enums/caseEnum';

// 评审状态, PREPARED: 待开始, UNDERWAY: 进行中, COMPLETED: 已完成, ARCHIVED: 已归档(暂时没有)
export type ReviewStatus = 'PREPARED' | 'UNDERWAY' | 'COMPLETED';
// 评审结果，UN_REVIEWED：未评审，UNDER_REVIEWED：评审中，PASS：通过，UN_PASS：未通过，RE_REVIEWED：重新提审
export type ReviewResult = 'UN_REVIEWED' | 'UNDER_REVIEWED' | 'PASS' | 'UN_PASS' | 'RE_REVIEWED';
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
// 用例评审关联用例入参
export interface BaseAssociateCaseRequest {
  excludeIds: string[];
  selectIds: string[];
  selectAll: boolean;
  condition: Record<string, any>;
  moduleIds: string[];
  versionId: string;
  refId: string;
  projectId: string;
  totalCount?: number;
}
// 评审
export interface Review {
  projectId: string;
  name: string;
  moduleId: string;
  reviewPassRule: ReviewPassRule; // 评审通过规则
  startTime: number | null;
  endTime: number | null;
  tags: string[];
  description: string;
  reviewers: string[]; // 评审人员
  baseAssociateCaseRequest: BaseAssociateCaseRequest; // 关联用例
}
// 复制评审入参
export interface CopyReviewParams extends Omit<Review, 'baseAssociateCaseRequest'> {
  copyId: string;
}
// 复制评审响应内容
export interface CopyReviewResponse {
  caseCount: number;
  createTime: number;
  createUser: string;
  description: string;
  endTime: number | null;
  id: string;
  moduleId: string;
  name: string;
  num: number;
  passRate: number;
  pos: number;
  projectId: string;
  reviewPassRule: ReviewPassRule;
  startTime: number | null;
  status: ReviewStatus;
  tags: null;
  updateTime: number;
  updateUser: string;
}
// 更新评审入参
export interface UpdateReviewParams extends Omit<Review, 'baseAssociateCaseRequest'> {
  id: string;
}
// 关联用例入参
export interface AssociateReviewCaseParams {
  reviewId: string;
  projectId: string;
  reviewers: string[];
  baseAssociateCaseRequest: BaseAssociateCaseRequest;
}
// 关注/取消关注评审入参
export interface FollowReviewParams {
  userId: string; // 用户id
  caseReviewId: string;
}
// 批量操作评审参数
export interface BatchMoveReviewParams extends BatchApiParams {
  moveModuleId: string; // 移动到的评审模块id
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
// 评审列表查询参数
export interface ReviewListQueryParams extends TableQueryParams {
  moduleIds: string[];
  projectId: string;
  createByMe?: string;
  reviewByMe?: string;
}
// 评审详情-用例列表查询参数
export interface ReviewDetailCaseListQueryParams extends TableQueryParams {
  reviewId: string;
  viewStatusFlag?: boolean; // 我的评审状态
}
// 评审详情-用例拖拽排序入参
export interface SortReviewCaseParams {
  projectId: string;
  targetId: string; // 目标用例id
  moveMode: ReviewMoveMode;
  moveId: string; // 被移动的用例id
  reviewId: string; // 所属评审id
}
// 评审详情-批量评审用例
export interface BatchReviewCaseParams extends BatchApiParams {
  reviewId: string; // 评审id
  reviewPassRule: ReviewPassRule; // 评审规则
  status: StartReviewStatus; // 评审结果
  content: string; // 评论内容
  notifier: string; // 评论@的人的Id, 多个以';'隔开
  reviewCommentFileIds?: string[]; // 富文本ids
}
// 评审详情-批量修改评审人
export interface BatchChangeReviewerParams extends BatchApiParams {
  reviewId: string; // 评审id
  reviewerId: string[]; // 评审人员id
  append: boolean; // 是否追加
}
// 评审详情-批量取消关联用例
export interface BatchCancelReviewCaseParams extends BatchApiParams {
  reviewId: string; // 评审id
}
export interface ReviewDetailReviewersItem {
  avatar: string;
  reviewId: string;
  userId: string;
  userName: string;
}
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
  tags: string[];
  description: string;
  createTime: number;
  createUser: string;
  updateTime: number;
  updateUser: string;
  reviewers: ReviewDetailReviewersItem[];
  passCount: number;
  unPassCount: number;
  reReviewedCount: number;
  underReviewedCount: number;
  unReviewCount: number;
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
  avatar: string;
}
// 评审详情-用例列表项
export interface ReviewCaseItem {
  id: string;
  name: string;
  num: string;
  caseId: string;
  reviewId: string;
  versionId: string;
  versionName: string;
  reviewers: string[];
  reviewNames: string[];
  status: ReviewResult;
  myStatus: ReviewResult;
  moduleId: string;
  moduleName: string;
}
// 评审详情-提交评审入参
export interface ReviewFormParams {
  status: StartReviewStatus;
  content: string;
  notifiers?: string[];
  reviewCommentFileIds?: string[];
}
export interface CommitReviewResultParams extends ReviewFormParams {
  projectId: string;
  reviewId: string;
  caseId: string;
  reviewPassRule: ReviewPassRule;
  notifier: string;
}
export interface MinderReviewCaseParams extends ReviewFormParams {
  reviewId: string;
  caseId: string;
  userId?: string;
}
// 评审详情-获取用例评审历史
export interface ReviewHistoryItem {
  id: string;
  reviewId: string;
  caseId: string;
  status: ReviewResult;
  deleted: boolean; // 是否是取消关联或评审被删除的：0-否，1-是
  notifier: string;
  createUser: string;
  createTime: number;
  content: string;
  userLogo: string;
  userName: string;
  contentText: string;
}

export interface ReviewerAndStatus {
  reviewerStatus: OptionItem[]; // 每个评审人最终的评审结果
  status: ReviewResult; // 用例评审最终结果
  caseId: string;
}

// 评审详情-用例列表项
export interface CaseReviewFunctionalCaseUserItem {
  caseId: string;
  reviewId: string;
  userId: string;
}

// 获取脑图请求参数
export interface CaseReviewMinderParams {
  projectId: string;
  moduleId: string;
  current?: number;
  reviewId: string;
  viewStatusFlag: boolean; // 我的评审结果
  sort?: Record<string, any>;
}

// 测试计划用例脑图
export interface CasePlanMinderParams {
  projectId: string;
  moduleId?: string;
  collectionId?: string;
  current?: number;
  planId: string;
  sort?: Record<string, any>;
}
