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
    collapsedLocale: 'menu.bugManagementShort',
    icon: 'icon-icon_defect',
    order: 7,
    roles: ['PROJECT_BUG:READ'],
    hideChildrenInMenu: true,
  },
  children: [
    // 缺陷管理-首页
    {
      path: 'index',
      name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
      component: () => import('@/views/bug-management/index.vue'),
      meta: {
        locale: 'bugManagement.index',
        roles: ['PROJECT_BUG:READ'],
        isTopMenu: true,
      },
    },
    // 缺陷管理-编辑缺陷
    {
      path: 'detail/:mode?',
      name: BugManagementRouteEnum.BUG_MANAGEMENT_DETAIL,
      component: () => import('@/views/bug-management/createAndEditBug.vue'),
      meta: {
        locale: 'bugManagement.editBug',
        roles: ['PROJECT_BUG:READ+ADD', 'PROJECT_BUG:READ+UPDATE'],
        breadcrumbs: [
          {
            name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
            locale: 'bugManagement.index',
          },
          {
            name: BugManagementRouteEnum.BUG_MANAGEMENT_DETAIL,
            locale: 'bugManagement.addBug',
            editTag: 'id',
            editLocale: 'bugManagement.editBug',
          },
        ],
      },
    },
    // 缺陷创建成功
    {
      path: 'create-success',
      name: BugManagementRouteEnum.BUG_MANAGEMENT_CREATE_SUCCESS,
      component: () => import('@/views/bug-management/createSuccess.vue'),
      meta: {
        locale: 'bugManagement.createBugSuccess',
        roles: ['PROJECT_BUG:READ+ADD', 'PROJECT_BUG:READ+UPDATE'],
      },
    },
    // 回收站
    {
      path: 'recycle',
      name: BugManagementRouteEnum.BUG_MANAGEMENT_RECYCLE,
      component: () => import('@/views/bug-management/recycle.vue'),
      meta: {
        locale: 'bugManagement.recycle.recycleBin',
        roles: ['PROJECT_BUG:READ'],
        isTopMenu: true,
      },
    },
  ],
};

export default BugManagement;
