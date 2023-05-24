import MSR from '@/api/http/index';

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
