import type { MsFileItem } from '@/components/pure/ms-upload/types';
import type { BreadcrumbItem } from '@/components/business/ms-breadcrumb/types';

import { EnvConfig, EnvironmentItem } from '@/models/projectManagement/environmental';
import type { LoginConfig, PageConfig, PlatformConfig, ThemeConfig } from '@/models/setting/config';
import { ProjectListItem } from '@/models/setting/project';

import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';

export interface AppState {
  colorWeak: boolean;
  navbar: boolean;
  menu: boolean;
  hideMenu: boolean;
  menuCollapse: boolean; // 菜单是否折叠
  footer: boolean;
  menuWidth: number;
  collapsedWidth: number; // 菜单折叠宽度
  globalSettings: boolean;
  device: string;
  tabBar: boolean;
  serverMenu: RouteRecordNormalized[];
  loading: boolean;
  loadingTip: string;
  loginLoading: boolean;
  topMenus: RouteRecordRaw[];
  currentTopMenu: RouteRecordRaw;
  breadcrumbList: BreadcrumbItem[];
  currentOrgId: string;
  currentProjectId: string;
  pageSize: number;
  showPageSize: boolean;
  showTotal: boolean;
  showJumper: boolean;
  hideOnSinglePage: boolean;
  version: string;
  defaultThemeConfig: ThemeConfig;
  defaultLoginConfig: LoginConfig;
  defaultPlatformConfig: PlatformConfig;
  pageConfig: PageConfig;
  innerHeight: number;
  currentMenuConfig: string[];
  packageType: string;
  projectList: ProjectListItem[];
  orgList: { id: string; name: string }[];
  envList: EnvironmentItem[];
  currentEnvConfig?: EnvConfig; // 当前环境配置信息
  fileMaxSize: number; // 文件上传最大限制
  isDarkTheme: boolean; // 是否暗黑模式
}

export interface UploadFileTaskState {
  isBackstageUpload: boolean; // 是否后台上传，后台上传会展示全局提示类型的进度提示
  isHideMessage: boolean; // 后台上传时展示的消息提示在点击关闭后无需再弹
  fileList: MsFileItem[]; // 文件总队列，包含已经上传的历史记录（不做持久化存储，刷新丢失）
  uploadFunc?: (params: any) => Promise<any>; // 上传文件时，自定义上传方法
  requestParams?: Record<string, any>; // 上传文件时，额外的请求参数
  eachTaskQueue: MsFileItem[]; // 每次添加的上传队列，用于展示每次任务的进度使用
  uploadQueue: MsFileItem[]; // 上传队列，每个文件上传完成后会从队列中移除，初始值为上传队列的副本
  singleProgress: number; // 单个上传文件的上传进度，非总进度，是每个文件在上传时的模拟进度
  timer: NodeJS.Timer | null; // 上传进度定时器
  finishedTime: number | null; // 任务完成时间
}

export interface AsyncTaskState {
  uploadFileTask: UploadFileTaskState;
}
