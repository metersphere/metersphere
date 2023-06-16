import type { RouteRecordRaw } from 'vue-router';
import { REDIRECT_ROUTE_NAME } from '@/router/constants';

export const DEFAULT_LAYOUT = () => import('@/layout/default-layout.vue');
export const PAGE_LAYOUT = () => import('@/layout/page-layout.vue');

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
