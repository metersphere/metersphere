import { NO_PROJECT_ROUTE_NAME, NO_RESOURCE_ROUTE_NAME, REDIRECT_ROUTE_NAME } from '@/router/constants';

import type { RouteRecordRaw } from 'vue-router';

export const DEFAULT_LAYOUT = () => import('@/layout/default-layout.vue');
export const PAGE_LAYOUT = () => import('@/layout/page-layout.vue');
export const NO_PERMISSION_LAYOUT = () => import('@/layout/no-permission-layout.vue');
export const SHARE_LAYOUT = () => import('@/layout/share-layout.vue');
export const FULL_PAGE_LAYOUT = () => import('@/layout/full-page-layout.vue');

export const INDEX_ROUTE: RouteRecordRaw = {
  path: '/index',
  name: 'metersphereIndex',
  component: NO_PERMISSION_LAYOUT,
  meta: {
    hideInMenu: true,
    roles: ['*'],
    requiresAuth: true,
  },
};

export const REDIRECT_MAIN: RouteRecordRaw = {
  path: '/redirect',
  name: 'redirectWrapper',
  component: DEFAULT_LAYOUT,
  meta: {
    hideInMenu: true,
  },
  children: [
    {
      path: '/redirect/:path',
      name: REDIRECT_ROUTE_NAME,
      component: () => import('@/views/base/redirect/index.vue'),
      meta: {
        hideInMenu: true,
      },
    },
  ],
};

export const NOT_FOUND_ROUTE: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'notFound',
  component: () => import('@/views/base/not-found/index.vue'),
};

export const INVITE_ROUTE: RouteRecordRaw = {
  path: '/invite',
  name: 'invite',
  component: () => import('@/views/base/invite/index.vue'),
  meta: {
    hideInMenu: true,
  },
};

export const NO_RESOURCE: RouteRecordRaw = {
  path: '/no-resource',
  name: NO_RESOURCE_ROUTE_NAME,
  component: () => import('@/views/base/no-resource/index.vue'),
  meta: {
    hideInMenu: true,
  },
};

export const NO_PROJECT: RouteRecordRaw = {
  path: '/no-project',
  name: NO_PROJECT_ROUTE_NAME,
  component: () => import('@/views/base/no-project/index.vue'),
  meta: {
    hideInMenu: true,
  },
};

export const NOT_FOUND_RESOURCE: RouteRecordRaw = {
  path: '/notResourceScreen',
  name: 'notResourceScreen',
  component: () => import('@/views/base/not-resource-screen/not-resource-screen.vue'),
};
