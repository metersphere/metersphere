export interface CommentUserInfo {
  id: string;
  name: string;
  email: string;
}

export interface CommentItem {
  id: string; // 评论id
  parentId?: string; // 父级评论id
  bugId: string; // bug id
  createUser: string; // 创建人
  updateTime: number; // 更新时间
  content: string;
  commentUserInfo: CommentUserInfo; // 评论人用户信息
  replyUser?: string; // 回复人
  notifier?: string; // 通知人
  childComments?: CommentItem[];
}

// 仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
export type commentEvent = 'COMMENT' | 'AT' | 'REPLAY';

export interface WriteCommentProps {
  id?: string; // 评论id
  parentId?: string; // 父级评论id
  event: commentEvent; // 评论事件
  bugId?: string; // bug id
  caseId?: string; // 用例id
}
export interface CommentParams extends WriteCommentProps {
  content: string;
  replyUser?: string; // 回复人
  notifiers?: string; // 通知人
}
