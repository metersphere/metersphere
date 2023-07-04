import type { Router } from 'vue-router';
import NProgress from 'nprogress'; // progress bar

import usePermission from '@/hooks/usePermission';
import { WHITE_LIST, NOT_FOUND } from '../constants';

export default function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const Permission = usePermission();
    const permissionsAllow = Permission.accessRouter(to);

    const exist = WHITE_LIST.find((el) => el.name === to.name);

    if (exist || permissionsAllow) {
      next();
    } else next(NOT_FOUND);
    NProgress.done();
  });
}
