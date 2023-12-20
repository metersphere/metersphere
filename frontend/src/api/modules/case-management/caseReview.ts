import MSR from '@/api/http/index';
import {
  AddReviewModuleUrl,
  AddReviewUrl,
  AssociateReviewUrl,
  CopyReviewUrl,
  DeleteReviewModuleUrl,
  EditReviewUrl,
  FollowReviewUrl,
  GetAssociatedIdsUrl,
  GetReviewDetailUrl,
  GetReviewListUrl,
  GetReviewModulesUrl,
  GetReviewUsersUrl,
  MoveReviewModuleUrl,
  MoveReviewUrl,
  SortReviewUrl,
  UpdateReviewModuleUrl,
} from '@/api/requrls/case-management/caseReview';

import {
  AssociateReviewCaseParams,
  BatchMoveReviewParams,
  FollowReviewParams,
  Review,
  ReviewItem,
  ReviewListQueryParams,
  ReviewModule,
  ReviewModuleItem,
  ReviewUserItem,
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

// 新增评审
export const addReview = (data: Review) => {
  return MSR.post({ url: AddReviewUrl, data });
};

// 关联用例
export const associateReviewCase = (data: AssociateReviewCaseParams) => {
  return MSR.post({ url: AssociateReviewUrl, data });
};

// 复制评审
export const copyReview = (data: Review) => {
  return MSR.post({ url: CopyReviewUrl, data });
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

// 获取评审人员列表
export const getAssociatedIds = (reviewId: string) => {
  return MSR.get<string[]>({ url: `${GetAssociatedIdsUrl}/${reviewId}` });
};
