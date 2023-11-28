import { BugManagementRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const BugManagement: AppRouteRecordRaw = {
  path: '/bug-management',
  name: BugManagementRouteEnum.BUG_MANAGEMENT,
  redirect: '/bug-management/index',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.bugManagement',
    icon: 'icon-icon_defect',
    order: 2,
    hideChildrenInMenu: true,
  },
  children: [
    // 缺陷管理-首页
    {
      path: 'index',
      name: 'bugManagementIndex',
      component: () => import('@/views/bug-management/index.vue'),
      meta: {
        locale: 'bugManagement.index',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    // 缺陷管理-编辑缺陷
    {
      path: 'edit',
      name: 'bugManagementBugEdit',
      component: () => import('@/views/bug-management/edit.vue'),
      meta: {
        locale: 'bugManagement.editBug',
        roles: ['*'],
        breadcrumbs: [
          {
            name: 'bugManagementIndex',
            locale: 'bugManagement.index',
          },
          {
            name: 'bugManagementBugEdit',
            locale: 'bugManagement.editBug',
            editLocale: 'menu.settings.organization.templateFieldSetting',
          },
        ],
      },
    },
    // 回收站
    {
      path: 'recycle',
      name: 'bugManagementRecycle',
      component: () => import('@/views/bug-management/recycle.vue'),
      meta: {
        locale: 'bugManagement.recycle',
        roles: ['*'],
        isTopMenu: true,
      },
    },
  ],
};

export default BugManagement;
