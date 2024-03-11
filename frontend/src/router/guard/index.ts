import { AxiosCanceler } from '@/api/http/axiosCancel';
import { MENU_LEVEL, type PathMapRoute } from '@/config/pathMap';
import usePathMap from '@/hooks/usePathMap';
import useAppStore from '@/store/modules/app';
import { setRouteEmitter } from '@/utils/route-listener';

import setupPermissionGuard from './permission';
import setupUserLoginInfoGuard from './userLoginInfo';
import type { Router } from 'vue-router';

function setupPageGuard(router: Router) {
  const axiosCanceler = new AxiosCanceler();
  const { getRouteLevelByKey } = usePathMap();
  router.beforeEach((to, from, next) => {
    // 监听路由变化
    setRouteEmitter(to);
    // 取消上个路由未完成的请求（不包含设置了ignoreCancelToken的请求）
    axiosCanceler.removeAllPending();
    const appStore = useAppStore();
    const urlOrgId = to.query.orgId;
    const urlProjectId = to.query.pId;
    // 如果访问页面的时候携带了项目 ID 或组织 ID，则将页面上的组织 ID和项目 ID设置为当前选中的组织和项目
    if (urlOrgId) {
      appStore.setCurrentOrgId(urlOrgId as string);
    }
    if (urlProjectId) {
      appStore.setCurrentProjectId(urlProjectId as string);
    }
    switch (getRouteLevelByKey(to.name as PathMapRoute)) {
      case MENU_LEVEL[1]: // 组织级别的页面，需要给页面携带上组织 ID
        if (urlOrgId === undefined) {
          to.query = {
            ...to.query,
            orgId: appStore.currentOrgId,
          };
          next(to);
          return;
        }
        break;
      case MENU_LEVEL[2]: // 项目级别的页面，需要给页面携带上组织 ID和项目 ID
        if (urlOrgId === undefined && urlProjectId === undefined) {
          to.query = {
            ...to.query,
            orgId: appStore.currentOrgId,
            pId: appStore.currentProjectId,
          };

          next(to);
          return;
        }
        break;
      case MENU_LEVEL[0]: // 系统级别的页面，无需携带组织ID和项目ID
      default:
        break;
    }
    next();
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
