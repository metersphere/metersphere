import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';
import type { BreadcrumbItem } from '@/components/business/ms-breadcrumb/types';
import type { PageConfig, ThemeConfig, LoginConfig, PlatformConfig } from '@/models/setting/config';

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

export type CustomTheme = 'theme-default' | 'theme-green';
