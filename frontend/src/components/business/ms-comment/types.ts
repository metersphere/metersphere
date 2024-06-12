export interface CommentUserInfo {
  id: string;
  name: string;
  email: string;
  avatar: string;
}

export interface CommentItem {
  id: string; // 评论id
  createUser: string;
  parentId: string; // 父级评论id
  notifier: string; // 通知人
  replyUser: string; // 回复人
  createTime: number;
  updateTime: number;
  content: string;
  commentUserInfos: CommentUserInfo[];
  childComments: CommentItem[];
  [key: string]: any;
}

// 仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
export type CommentEvent = 'COMMENT' | 'AT' | 'REPLY';
// 评论请求的时候是编辑还是新增
export type FetchType = 'ADD' | 'UPDATE';
export type CommentType = 'REPLY' | 'EDIT' | 'ADD';

export interface WriteCommentProps {
  id?: string; // 评论id
  parentId?: string; // 父级评论id
  event: CommentEvent; // 评论事件
  bugId?: string; // bug id
  caseId?: string; // 用例id
}
export interface CommentParams extends WriteCommentProps {
  content: string;
  replyUser?: string; // 回复人
  notifier?: string; // 通知人
  fetchType?: FetchType; // 发送后端请求类型 编辑还是新增
  commentType?: CommentType; // 评论类型
  uploadFileIds: string[]; // 评论上传文件
}
