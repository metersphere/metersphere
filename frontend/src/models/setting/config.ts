import { FileItem } from '@arco-design/web-vue';

import { Recordable } from '#/global';

// 基础信息配置
export interface BaseConfig {
  url: string;
  prometheusHost: string;
  fileMaxSize: string;
}

// 邮箱信息配置
export interface EmailConfig {
  host: string; // 主机
  port: string; // 端口
  account: string; // 账户
  from: string; // 发件人
  password: string; // 密码
  ssl: string;
  tsl: string;
  recipient: string; // 收件人
}

interface ParamItem {
  paramKey: string; // 参数的 key
  paramValue: string; // 参数的值
  type: string; // 参数类型，一般是 string
}

// 保存基础信息、邮箱信息接口入参
export type SaveInfoParams = ParamItem[];

// 测试邮箱连接接口入参
export interface TestEmailParams {
  'smtp.host': string;
  'smtp.port': string;
  'smtp.account': string;
  'smtp.password': string;
  'smtp.from': string;
  'smtp.ssl': string;
  'smtp.tsl': string;
  'smtp.recipient': string;
}

// 界面配置入参
export interface SavePageConfigParams {
  fileList: (File | undefined)[];
  request: (Recordable | undefined)[];
}

interface FileParamItem extends ParamItem {
  file: string;
  fileName: string;
}

// 页面配置返回参数
export type PageConfigReturns = FileParamItem[];

// 平台风格
export type Style = 'default' | 'custom' | 'follow';

// 主题
export type Theme = 'default' | 'custom';

// 主题配置对象
export interface ThemeConfig {
  style: Style;
  customStyle: string;
  theme: Theme;
  customTheme: string;
}

// 登录页配置对象
export interface LoginConfig {
  title: string;
  icon: (FileItem | never)[];
  loginLogo: (FileItem | never)[];
  loginImage: (FileItem | never)[];
  slogan: string;
}

//  平台配置对象
export interface PlatformConfig {
  logoPlatform: (FileItem | never)[];
  platformName: string;
  helpDoc: string;
}

//  界面配置对象
export interface PageConfig extends ThemeConfig, LoginConfig, PlatformConfig {}

export type PageConfigKeys = keyof PageConfig;

// 认证源类型
export type AuthType = 'CAS' | 'LDAP' | 'OAUTH2' | 'OIDC';

// 认证源配置列表项对象
export interface AuthItem {
  id: string;
  enable: boolean;
  createTime: number;
  updateTime: number;
  description: string;
  name: string;
  type: AuthType;
  configuration: string;
}

// 认证源配置对象
export type AuthConfig = Omit<AuthItem, 'id'>;

// 认证源配置表单对象
export interface AuthForm {
  id?: string;
  enable: boolean;
  description: string;
  name: string;
  type: AuthType;
  configuration: Recordable;
}

// 认证源配置接口入参
export type AuthParams = Omit<AuthForm, 'configuration'> & {
  configuration: string;
};

// 认证源配置详情对象
export type AuthDetail = AuthForm & Omit<AuthItem, 'configuration'>;

// 更新认证源状态入参
export interface UpdateAuthStatusParams {
  id: string;
  enable: boolean;
}
// ldap 连接配置
export interface LDAPConnectConfig {
  ldapUrl: string;
  ldapDn: string;
  ldapPassword: string;
}
// ldap登录配置
export interface LDAPConfig extends LDAPConnectConfig {
  username: string;
  password: string;
  ldapUserFilter: string;
  ldapUserOu: string;
  ldapUserMapping: string;
}
// 内存清理配置
export interface CleanupConfig {
  operationLog: string;
  operationHistory: string;
}
