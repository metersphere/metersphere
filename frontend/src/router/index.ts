import { createRouter, createWebHashHistory } from 'vue-router';

import 'nprogress/nprogress.css';
import createRouteGuard from './guard';
import appRoutes from './routes';
import {
  INDEX_ROUTE,
  INVITE_ROUTE,
  NO_PROJECT,
  NO_RESOURCE,
  NOT_FOUND_RESOURCE,
  NOT_FOUND_ROUTE,
  REDIRECT_MAIN,
} from './routes/base';
import NProgress from 'nprogress'; // progress bar

NProgress.configure({ showSpinner: false }); // NProgress Configuration

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: 'login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    ...appRoutes,
    REDIRECT_MAIN,
    NOT_FOUND_ROUTE,
    NOT_FOUND_RESOURCE,
    INVITE_ROUTE,
    NO_PROJECT,
    NO_RESOURCE,
    INDEX_ROUTE,
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

createRouteGuard(router);

export default router;
