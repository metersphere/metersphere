import type { Router } from 'vue-router';
import { setRouteEmitter } from '@/utils/route-listener';
import setupUserLoginInfoGuard from './userLoginInfo';
import setupPermissionGuard from './permission';

function setupPageGuard(router: Router) {
  router.beforeEach(async (to) => {
    // 监听路由变化
    setRouteEmitter(to);
  });
}

export default function createRouteGuard(router: Router) {
  // 设置路由监听守卫
  setupPageGuard(router);
  // 设置用户登录校验守卫
  setupUserLoginInfoGuard(router);
  // 设置菜单权限守卫
  setupPermissionGuard(router);
}
