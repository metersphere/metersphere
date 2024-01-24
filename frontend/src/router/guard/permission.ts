import usePermission from '@/hooks/usePermission';

import { NO_RESOURCE_ROUTE_NAME, WHITE_LIST } from '../constants';
import NProgress from 'nprogress'; // progress bar
import type { Router } from 'vue-router';

export default function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const Permission = usePermission();
    const permissionsAllow = Permission.accessRouter(to);

    const exist = WHITE_LIST.find((el) => el.name === to.name);

    if (exist || permissionsAllow) {
      next();
    } else next(NO_RESOURCE_ROUTE_NAME);
    NProgress.done();
  });
}
