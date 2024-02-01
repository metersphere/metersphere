// 支持添加机器人的平台类型
export type ProjectRobotPlatformCanEdit = 'DING_TALK' | 'LARK' | 'WE_COM' | 'CUSTOM';

// 机器人全平台类型
export type ProjectRobotPlatform = ProjectRobotPlatformCanEdit | 'IN_SITE' | 'MAIL';

// 钉钉机器人类型
export type ProjectRobotDingTalkType = 'CUSTOM' | 'ENTERPRISE';

export interface RobotCommon {
  name: string;
  platform: ProjectRobotPlatformCanEdit; // 机器人平台
  webhook: string;
  enable: boolean;
  description?: string;
}

export interface RobotAddParams extends RobotCommon {
  projectId: string;
  type?: ProjectRobotDingTalkType; // 钉钉机器人类型
  appKey?: string; // 钉钉机器人-ENTERPRISE-appKey
  appSecret?: string; // 钉钉机器人-ENTERPRISE-appSecret
}

export interface RobotEditParams extends RobotAddParams {
  id: string;
}

export interface RobotItem extends Omit<RobotEditParams, 'platform'> {
  platform: ProjectRobotPlatform;
  createUser: string;
  createTime: number;
  updateUser?: string;
  updateTime?: number;
}

// 消息配置机器人设置
export interface ProjectRobotConfig {
  robotId: string; // 消息机器人id
  robotName: string; // 消息机器人名称
  platform: ProjectRobotPlatform;
  type?: ProjectRobotDingTalkType; // 钉钉机器人类型
  dingType?: ProjectRobotDingTalkType; // 钉钉机器人类型
  enable: boolean; // 消息配置机器人是否开启
  template: string; // 消息配置机器人发送模板
  defaultTemplate: string; // 消息配置机器人默认发送模板
  useDefaultTemplate: boolean; // 消息配置机器人是否使用默认模板
  previewSubject?: string; // 消息配置机器人预览邮件标题
  previewTemplate?: string; // 消息配置机器人预览发送模板
  subject: string; // 消息模板配置的标题
  defaultSubject: string; // 消息模板配置的默认标题
  useDefaultSubject: boolean; // 消息模板是否使用默认标题
}

// 消息配置接收人
export interface Receiver {
  id: string;
  name: string;
}

export interface MessageTaskDetailDTO {
  event: string; // 消息配置场景
  eventName: string; // 消息配置场景名称
  receivers: Receiver[];
  projectRobotConfigMap: Record<string, ProjectRobotConfig>;
}

export interface MessageTaskTypeDTO {
  taskType: string; // 消息配置功能类型
  taskTypeName: string; // 消息配置功能名称
  messageTaskDetailDTOList: MessageTaskDetailDTO[];
}

export interface MessageItem {
  projectId: string; // 消息配置所在项目ID
  type: string; // 消息配置功能类型
  name: string; // 消息配置功能名称
  messageTaskTypeDTOList: MessageTaskTypeDTO[];
}

export interface SaveMessageTemplateParams {
  projectId: string; // 消息配置所在项目ID
  taskType: string; // 消息配置功能
  dingType?: string; // 钉钉机器人类型
  event: string; // 消息配置场景
  receiverIds: string[]; // 消息配置接收人id集合
  testId?: string; // 具体测试的ID
  robotId: string;
  enable: boolean;
  template: string; // 消息配置企业用户自定义的消息模板
  subject: string; // 消息配置企业用户自定义的邮件标题
  useDefaultTemplate: boolean; // 是否使用默认模板
  useDefaultSubject: boolean; // 是否使用默认邮件标题
}

export interface MessageTemplateDetail extends SaveMessageTemplateParams {
  taskTypeName: string; // 消息配置功能名称
  eventName: string; // 消息配置场景名称
  robotName: string; // 消息配置机器人名称
  platform: ProjectRobotPlatform;
  previewSubject?: string; // 消息配置机器人预览邮件标题
  previewTemplate?: string; // 消息配置机器人预览发送模板
  defaultTemplate: string; // 消息配置机器人默认发送模板
  defaultSubject: string; // 消息模板配置的默认标题
}

// 消息配置模板字段
export interface Field {
  id: string;
  name: string;
  fieldSource: string;
}
export interface FieldSource {
  id: string;
  name: string;
}

export interface FieldMap {
  fieldList: Field[]; // 消息配置模板字段列表
  fieldSourceList: FieldSource[]; // 消息配置模板字段来源列表
}
