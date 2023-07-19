import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';
import type { BreadcrumbItem } from '@/components/bussiness/ms-breadcrumb/types';

export interface AppState {
  theme: string;
  colorWeak: boolean;
  navbar: boolean;
  menu: boolean;
  hideMenu: boolean;
  menuCollapse: boolean;
  footer: boolean;
  themeColor: string;
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
  [key: string]: unknown;
}

export type CustomTheme = 'theme-default' | 'theme-green';
