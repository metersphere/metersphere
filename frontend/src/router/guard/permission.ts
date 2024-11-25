import usePermission from '@/hooks/usePermission';
import useAppStore from '@/store/modules/app';

import { featureRouteMap, NO_RESOURCE_ROUTE_NAME, WHITE_LIST } from '../constants';
import NProgress from 'nprogress'; // progress bar
import type { Router } from 'vue-router';

export default function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const appStore = useAppStore();
    const Permission = usePermission();
    const permissionsAllow = Permission.accessRouter(to);
    // 如果是隐藏的模块，则跳转到无权限页面
    const moduleId = Object.keys(featureRouteMap).find((key) => (to.name as string)?.includes(key));
    if (moduleId && featureRouteMap[moduleId] && !appStore.currentMenuConfig.includes(featureRouteMap[moduleId])) {
      next({
        name: NO_RESOURCE_ROUTE_NAME,
      });
    }
    const exist = WHITE_LIST.find((el) => el.name === to.name);
    if (exist || permissionsAllow) {
      next();
    } else {
      next({
        name: NO_RESOURCE_ROUTE_NAME,
      });
    }
    NProgress.done();
  });
}
