export type DingTalkInfo = Partial<{
  agentId: string;
  appKey: string;
  appSecret: string;
  callBack: string;
  enable: boolean;
  valid: boolean;
}>;

export type WeComInfo = Partial<{
  corpId: string;
  agentId: string;
  appSecret: string;
  callBack: string;
  enable: boolean;
  valid: boolean;
}>;

export interface EnableEditorRequest {
  enable: boolean;
}

export type PlatformSource = Partial<{
  platform: string;
  enable: boolean;
  valid: boolean;
}>;

export type PlatformConfigItem = Partial<{
  key: string;
  title: string;
  description: string;
  enable: boolean;
  valid: boolean;
  logo: string;
}>;

export type PlatformSourceList = PlatformSource[];
export type PlatformConfigList = PlatformConfigItem[];
