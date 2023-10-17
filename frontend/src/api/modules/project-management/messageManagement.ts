import MSR from '@/api/http/index';
import {
  AddRobotUrl,
  DeleteRobotUrl,
  EnableRobotUrl,
  GetMessageDetailUrl,
  GetMessageFieldsUrl,
  GetMessageUrl,
  GetMessageUserListUrl,
  GetRobotUrl,
  RobotListUrl,
  SaveMessageUrl,
  UpdateRobotUrl,
} from '@/api/requrls/project-management/messageManagement';

import type {
  FieldMap,
  MessageItem,
  MessageTemplateDetail,
  Receiver,
  RobotAddParams,
  RobotEditParams,
  RobotItem,
  SaveMessageTemplateParams,
} from '@/models/projectManagement/message';

// 获取机器人列表
export function getRobotList(projectId: string) {
  return MSR.get<RobotItem[]>({ url: `${RobotListUrl}/${projectId}` });
}

// 获取机器人详情
export function getRobotDetail(robotId: string) {
  return MSR.get<RobotItem>({ url: GetRobotUrl, params: robotId });
}

// 添加机器人
export function addRobot(data: RobotAddParams) {
  return MSR.post({ url: AddRobotUrl, data });
}

// 更新机器人
export function updateRobot(data: RobotEditParams) {
  return MSR.post({ url: UpdateRobotUrl, data });
}

// 启用/禁用机器人
export function toggleRobot(id: string) {
  return MSR.get({ url: EnableRobotUrl, params: id });
}

// 删除机器人
export function deleteRobot(id: string) {
  return MSR.get({ url: DeleteRobotUrl, params: id });
}

// 获取消息配置列表
export function getMessageList({ projectId }: { projectId: string }) {
  return MSR.get<MessageItem[]>({ url: `${GetMessageUrl}/${projectId}` });
}

// 获取消息配置-用户列表
export function getMessageUserList({ projectId, keyword }: { projectId: string; keyword: string }) {
  return MSR.get<Receiver[]>({ url: `${GetMessageUserListUrl}/${projectId}`, params: { keyword } });
}

// 获取消息配置-字段
export function getMessageFields(projectId: string, taskType: string) {
  return MSR.get<FieldMap>({ url: `${GetMessageFieldsUrl}/${projectId}`, params: { taskType } });
}

// 获取消息配置详情
export function getMessageDetail(projectId: string, taskType: string, event: string, robotId: string) {
  return MSR.get<MessageTemplateDetail>({
    url: `${GetMessageDetailUrl}/${projectId}`,
    params: { taskType, event, robotId },
  });
}

// 保存消息配置
export function saveMessageConfig(data: SaveMessageTemplateParams) {
  return MSR.post({ url: SaveMessageUrl, data });
}
