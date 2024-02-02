import { RouteLocationNormalized, RouteRecordRaw } from 'vue-router';
import { includes } from 'lodash-es';

import { firstLevelMenu } from '@/config/permission';
import { hasAnyPermission, topLevelMenuHasPermission } from '@/utils/permission';

/**
 * 用户权限
 * @returns 调用方法
 */
export default function usePermission() {
  return {
    /**
     * 是否为允许访问的路由
     * @param route 路由信息
     * @returns 是否
     */
    accessRouter(route: RouteLocationNormalized | RouteRecordRaw) {
      if (includes(firstLevelMenu, route.name)) {
        // 一级菜单: 创建项目时 被勾选的模块
        return topLevelMenuHasPermission(route);
      }
      return (
        route.meta?.requiresAuth === false ||
        !route.meta?.roles ||
        route.meta?.roles?.includes('*') ||
        hasAnyPermission(route.meta?.roles || [])
      );
    },
    // You can add any rules you want
  };
}
