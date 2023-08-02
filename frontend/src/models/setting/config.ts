import { Recordable } from '#/global';
import { FileItem } from '@arco-design/web-vue';

// 基础信息配置
export interface BaseConfig {
  url: string;
  prometheusHost: string;
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
  request: Recordable[];
}

interface FileParamItem extends ParamItem {
  file: string;
  fileName: string;
}

// 页面配置返回参数
export type PageConfigReturns = FileParamItem[];

// 主题配置对象

export interface ThemeConfig {
  style: string;
  customStyle: string;
  theme: string;
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
