import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';

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
  currentOrgId: string;
  currentProjectId: string;
  [key: string]: unknown;
}

export type CustomTheme = 'theme-default' | 'theme-green';
