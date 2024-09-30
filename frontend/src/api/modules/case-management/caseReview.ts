import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';

import MSR from '@/api/http/index';
import {
  AddReviewModuleUrl,
  AddReviewUrl,
  AssociateReviewUrl,
  BatchChangeReviewerUrl,
  BatchDisassociateReviewCaseUrl,
  BatchReviewUrl,
  CopyReviewUrl,
  DeleteReviewModuleUrl,
  DeleteReviewUrl,
  DisassociateReviewCaseUrl,
  EditReviewUrl,
  FollowReviewUrl,
  GetAssociatedIdsUrl,
  GetCasePlanCollectionMinderUrl,
  GetCasePlanMinderUrl,
  getCaseReviewerListUrl,
  GetCaseReviewHistoryListUrl,
  GetCaseReviewMinderUrl,
  GetReviewDetailCasePageUrl,
  GetReviewDetailModuleCountUrl,
  GetReviewDetailModuleTreeUrl,
  GetReviewDetailUrl,
  GetReviewerAndStatusUrl,
  GetReviewListUrl,
  GetReviewModulesUrl,
  GetReviewUsersUrl,
  MinderReviewCaseUrl,
  MoveReviewModuleUrl,
  MoveReviewUrl,
  ReviewModuleCountUrl,
  SaveCaseReviewResultUrl,
  SortReviewDetailCaseUrl,
  SortReviewUrl,
  UpdateReviewModuleUrl,
} from '@/api/requrls/case-management/caseReview';

import {
  AssociateReviewCaseParams,
  BatchCancelReviewCaseParams,
  BatchChangeReviewerParams,
  BatchMoveReviewParams,
  BatchReviewCaseParams,
  CasePlanMinderParams,
  CaseReviewFunctionalCaseUserItem,
  CaseReviewMinderParams,
  CommitReviewResultParams,
  CopyReviewParams,
  CopyReviewResponse,
  FollowReviewParams,
  MinderReviewCaseParams,
  Review,
  ReviewCaseItem,
  ReviewDetailCaseListQueryParams,
  ReviewerAndStatus,
  ReviewHistoryItem,
  ReviewItem,
  ReviewListQueryParams,
  ReviewModule,
  ReviewModuleItem,
  ReviewUserItem,
  SortReviewCaseParams,
  SortReviewParams,
  UpdateReviewModuleParams,
  UpdateReviewParams,
} from '@/models/caseManagement/caseReview';
import { CommonList, MoveModules } from '@/models/common';

// 新增评审模块
export const addReviewModule = (data: ReviewModule) => {
  return MSR.post({ url: AddReviewModuleUrl, data });
};

// 更新评审模块
export const updateReviewModule = (data: UpdateReviewModuleParams) => {
  return MSR.post({ url: UpdateReviewModuleUrl, data });
};

// 移动评审模块
export const moveReviewModule = (data: MoveModules) => {
  return MSR.post({ url: MoveReviewModuleUrl, data });
};

// 获取评审模块树
export const getReviewModules = (projectId: string) => {
  return MSR.get<ReviewModuleItem[]>({ url: GetReviewModulesUrl, params: projectId });
};

// 删除评审模块
export const deleteReviewModule = (id: string) => {
  return MSR.get({ url: DeleteReviewModuleUrl, params: id });
};

// 评审模块树-统计用例数量
export const reviewModuleCount = (data: ReviewListQueryParams) => {
  return MSR.post({ url: ReviewModuleCountUrl, data });
};

// 新增评审
export const addReview = (data: Review) => {
  return MSR.post({ url: AddReviewUrl, data });
};

// 关联用例
export const associateReviewCase = (data: AssociateReviewCaseParams) => {
  return MSR.post({ url: AssociateReviewUrl, data });
};

// 复制评审
export const copyReview = (data: CopyReviewParams) => {
  return MSR.post<CopyReviewResponse>({ url: CopyReviewUrl, data });
};

// 编辑评审
export const editReview = (data: UpdateReviewParams) => {
  return MSR.post({ url: EditReviewUrl, data });
};

// 关注/取消关注评审
export const followReview = (data: FollowReviewParams) => {
  return MSR.post({ url: FollowReviewUrl, data });
};

// 移动评审
export const moveReview = (data: BatchMoveReviewParams) => {
  return MSR.post({ url: MoveReviewUrl, data });
};

// 评审拖拽排序
export const sortReview = (data: SortReviewParams) => {
  return MSR.post({ url: SortReviewUrl, data });
};

// 获取评审列表
export const getReviewList = (data: ReviewListQueryParams) => {
  return MSR.post<CommonList<ReviewItem>>({ url: GetReviewListUrl, data });
};

// 获取评审详情
export const getReviewDetail = (id: string) => {
  return MSR.get<ReviewItem>({ url: GetReviewDetailUrl, params: id });
};

// 获取评审人员列表
export const getReviewUsers = (projectId: string, keyword: string) => {
  return MSR.get<ReviewUserItem[]>({ url: `${GetReviewUsersUrl}/${projectId}`, params: { keyword } });
};

// 取消关联用例
export const disassociateReviewCase = (reviewId: string, caseId: string) => {
  return MSR.get<ReviewUserItem[]>({ url: `${DisassociateReviewCaseUrl}/${reviewId}/${caseId}` });
};

// 删除评审
export const deleteReview = (reviewId: string, projectId: string) => {
  return MSR.get<ReviewUserItem[]>({ url: `${DeleteReviewUrl}/${projectId}/${reviewId}` });
};

// 评审详情-获取用例列表
export const getReviewDetailCasePage = (data: ReviewDetailCaseListQueryParams) => {
  return MSR.post<CommonList<ReviewCaseItem>>({ url: GetReviewDetailCasePageUrl, data });
};

// 评审详情-用例拖拽排序
export const sortReviewDetailCase = (data: SortReviewCaseParams) => {
  return MSR.post({ url: SortReviewDetailCaseUrl, data });
};

// 评审详情-批量评审
export const batchReview = (data: BatchReviewCaseParams) => {
  return MSR.post({ url: BatchReviewUrl, data });
};

// 评审详情-批量修改评审人
export const batchChangeReviewer = (data: BatchChangeReviewerParams) => {
  return MSR.post({ url: BatchChangeReviewerUrl, data });
};

// 评审详情-批量取消关联用例
export const batchDisassociateReviewCase = (data: BatchCancelReviewCaseParams) => {
  return MSR.post({ url: BatchDisassociateReviewCaseUrl, data });
};

// 获取关联用例 id集合
export const getAssociatedIds = (reviewId: string) => {
  return MSR.get<string[]>({ url: `${GetAssociatedIdsUrl}/${reviewId}` });
};

// 评审详情-模块下用例数量统计
export const getReviewDetailModuleCount = (data: ReviewDetailCaseListQueryParams) => {
  return MSR.post({ url: GetReviewDetailModuleCountUrl, data });
};

// 评审详情-已关联用例模块树
export const getReviewDetailModuleTree = (reviewId: string) => {
  return MSR.get({ url: `${GetReviewDetailModuleTreeUrl}/${reviewId}` });
};

// 评审详情-获取用例评审历史
export const getCaseReviewHistoryList = (reviewId: string, caseId: string) => {
  return MSR.get<ReviewHistoryItem[]>({ url: `${GetCaseReviewHistoryListUrl}/${reviewId}/${caseId}` });
};

// 评审详情-提交用例评审结果
export const saveCaseReviewResult = (data: CommitReviewResultParams) => {
  return MSR.post({ url: SaveCaseReviewResultUrl, data });
};

// 评审详情-获取用例的评审人
export const getCaseReviewerList = (reviewId: string, caseId: string) => {
  return MSR.get<CaseReviewFunctionalCaseUserItem[]>({ url: `${getCaseReviewerListUrl}/${reviewId}/${caseId}` });
};

// 获取评审脑图
export function getCaseReviewMinder(data: CaseReviewMinderParams) {
  return MSR.post<CommonList<MinderJsonNode>>({ url: `${GetCaseReviewMinderUrl}`, data });
}

// 获取测试计划用例脑图
export function getCasePlanMinder(treeType: 'MODULE' | 'COLLECTION', data: CasePlanMinderParams) {
  if (treeType === 'COLLECTION') {
    return MSR.post<CommonList<MinderJsonNode>>({ url: `${GetCasePlanCollectionMinderUrl}`, data });
  }
  return MSR.post<CommonList<MinderJsonNode>>({ url: `${GetCasePlanMinderUrl}`, data });
}

// 脑图-获取用例评审最终结果和每个评审人最终的评审结果
export const getReviewerAndStatus = (reviewId: string, caseId: string) => {
  return MSR.get<ReviewerAndStatus>({ url: `${GetReviewerAndStatusUrl}/${reviewId}/${caseId}` });
};

// 评审详情-脑图评审用例
export const minderReviewCase = (data: MinderReviewCaseParams) => {
  return MSR.post({ url: MinderReviewCaseUrl, data });
};
