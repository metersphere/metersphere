// 支持添加机器人的平台类型
export type ProjectRobotPlatformCanEdit = 'DING_TALK' | 'LARK' | 'WE_COM' | 'CUSTOM';

// 机器人全平台类型
export type ProjectRobotPlatform = ProjectRobotPlatformCanEdit | 'IN_SITE' | 'MAIL';

// 钉钉机器人类型
export type ProjectRobotDingTalkType = 'CUSTOM' | 'ENTERPRISE';

export interface RobotCommon {
  name: string;
  platform: ProjectRobotPlatformCanEdit;
  webhook: string;
  enable: boolean;
  description?: string;
}

export interface RobotAddParams extends RobotCommon {
  projectId: string;
  type?: ProjectRobotDingTalkType;
  appKey?: string;
  appSecret?: string;
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
