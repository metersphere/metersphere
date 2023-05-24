import type { Router, RouteRecordNormalized } from 'vue-router';
import NProgress from 'nprogress'; // progress bar

import usePermission from '@/hooks/usePermission';
import { useAppStore } from '@/store';
import { WHITE_LIST, NOT_FOUND } from '../constants';

export default function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const appStore = useAppStore();
    const Permission = usePermission();
    const permissionsAllow = Permission.accessRouter(to);
    // 针对来自服务端的菜单配置进行处理
    if (!appStore.appAsyncMenus.length && !WHITE_LIST.find((el) => el.name === to.name)) {
      await appStore.fetchServerMenuConfig();
    }
    const serverMenuConfig = [...appStore.appAsyncMenus, ...WHITE_LIST];

    let exist = false;
    while (serverMenuConfig.length && !exist) {
      const element = serverMenuConfig.shift();
      if (element?.name === to.name) exist = true;

      if (element?.children) {
        serverMenuConfig.push(...(element.children as unknown as RouteRecordNormalized[]));
      }
    }
    if (exist && permissionsAllow) {
      next();
    } else next(NOT_FOUND);
    NProgress.done();
  });
}
