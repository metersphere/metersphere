import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';
import type { BreadcrumbItem } from '@/components/business/ms-breadcrumb/types';
import type { PageConfig, ThemeConfig, LoginConfig, PlatformConfig } from '@/models/setting/config';
import type { MsFileItem } from '@/components/pure/ms-upload/types';

export interface AppState {
  colorWeak: boolean;
  navbar: boolean;
  menu: boolean;
  hideMenu: boolean;
  menuCollapse: boolean;
  footer: boolean;
  menuWidth: number;
  globalSettings: boolean;
  device: string;
  tabBar: boolean;
  serverMenu: RouteRecordNormalized[];
  loading: boolean;
  loadingTip: string;
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
}

export interface UploadFileTaskState {
  isBackstageUpload: boolean; // 是否后台上传，后台上传会展示全局提示类型的进度提示
  isHideMessage: boolean; // 后台上传时展示的消息提示在点击关闭后无需再弹
  fileList: MsFileItem[]; // 文件总队列，包含已经上传的历史记录（不做持久化存储，刷新丢失）
  eachTaskQueue: MsFileItem[]; // 每次添加的上传队列，用于展示每次任务的进度使用
  uploadQueue: MsFileItem[]; // 上传队列，每个文件上传完成后会从队列中移除，初始值为上传队列的副本
  singleProgress: number; // 单个上传文件的上传进度，非总进度，是每个文件在上传时的模拟进度
  timer: NodeJS.Timer | null; // 上传进度定时器
  finishedTime: number | null; // 任务完成时间
}

export interface AsyncTaskState {
  uploadFileTask: UploadFileTaskState;
}
