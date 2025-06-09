import MSR from '@/api/http/index';

import type { AiChatContentItem, AiChatListItem, AiChatPrams, AiChatResponse } from '@/models/ai';

import {
  addAiChatUrl,
  aiChatDeleteUrl,
  aiChatDetailUrl,
  aiChatListUrl,
  aiChatUpdateTitleUrl,
  aiChatUrl,
} from '../requrls/ai';

// AI 对话
export function aiChat(data: AiChatPrams) {
  return MSR.post({ url: aiChatUrl, data });
}

// 获取 AI 对话列表
export function getAiChatList() {
  return MSR.get<AiChatListItem[]>({ url: aiChatListUrl });
}

// AI 对话新增
export function addAiChat(data: AiChatPrams) {
  return MSR.post<AiChatResponse>({ url: addAiChatUrl, data });
}

// AI 对话删除
export function deleteAiChat(id: string) {
  return MSR.get({ url: `${aiChatDeleteUrl}/${id}` });
}

// 获取 AI 对话详情
export function getAiChatDetail(id: string) {
  return MSR.get<AiChatContentItem[]>({ url: `${aiChatDetailUrl}/${id}` });
}

// 更新 AI 对话标题
export function updateAiChatTitle(data: { id: string; title: string }) {
  return MSR.post({ url: aiChatUpdateTitleUrl, data });
}
