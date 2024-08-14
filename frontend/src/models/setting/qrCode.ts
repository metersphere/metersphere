export interface DingTalkInfo {
  agentId: string;
  appKey: string;
  appSecret: string;
  callBack: string;
  enable: boolean;
  valid: boolean;
}

export interface LarkInfo {
  agentId: string;
  appSecret: string;
  callBack: string;
  enable: boolean;
  valid: boolean;
}

export interface WeComInfo {
  corpId: string;
  agentId: string;
  appSecret: string;
  callBack: string;
  enable: boolean;
  valid: boolean;
}

export interface EnableEditorRequest {
  enable: boolean;
}

export interface PlatformSource {
  platform: string;
  enable: boolean;
  valid: boolean;
  hasConfig: boolean;
}

export interface PlatformConfigItem {
  key: string;
  title: string;
  description: string;
  enable: boolean;
  valid: boolean;
  logo: string;
  edit: boolean;
  hasConfig: boolean;
}

export type PlatformSourceList = PlatformSource[];
export type PlatformConfigList = PlatformConfigItem[];
