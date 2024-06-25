import MSR from '@/api/http/index';

import { CommonList, TableQueryParams } from '@/models/common';

export interface MessageRecord {
  id: number;
  type: string;
  title: string;
  subTitle: string;
  avatar?: string;
  content: string;
  time: string;
  status: 0 | 1;
  messageType?: number;
}

export type MessageListType = MessageRecord[];

export function queryMessageList() {
  return MSR.post<MessageListType>({ url: '/api/message/list' });
}

interface MessageStatus {
  ids: number[];
}

export function setMessageStatus(data: MessageStatus) {
  return MSR.post<MessageListType>({ url: '/api/message/read', data });
}

export interface ChatRecord {
  id: number;
  username: string;
  content: string;
  time: string;
  isCollect: boolean;
}

export function queryChatList() {
  return MSR.post<ChatRecord[]>({ url: '/api/chat/list' });
}

interface MessageHistoryQueryParams extends TableQueryParams {
  id: number[];
  title: string;
  createTime: string;
  operator: string;
  operation: string;
}

interface historyQueryParams extends Partial<MessageHistoryQueryParams> {
  type?: string;
  receiver?: string;
  status?: string;
  resourceType?: string;
}

export interface MessageHistoryItem {
  id: number;
  type: string;
  receiver: string;
  subject: string;
  status: string;
  createTime: string;
  operator: string;
  operation: string;
  resourceId: string;
  resourceType: string;
  resourceName: string;
  content: string;
  organizationId: string;
  projectId: string;
}

export function queryMessageHistoryList(data: historyQueryParams) {
  return MSR.post<CommonList<MessageHistoryItem>>({ url: '/notification/list/all/page', data });
}

export interface OptionItem {
  id: string;
  name: string;
}

export function queryMessageHistoryCount(data: historyQueryParams) {
  return MSR.post<OptionItem[]>({ url: '/notification/count', data });
}

export function getMessageReadAll(resourceType?: string) {
  return MSR.get<number>({ url: '/notification/read/all', params: { resourceType } });
}
export function getMessageRead(id: number) {
  return MSR.get<number>({ url: `/notification/read/${id}` });
}

export function getMessageUnReadCount(projectId: string) {
  return MSR.get<number>({ url: '/notification/un-read', params: projectId }, { ignoreCancelToken: true });
}
