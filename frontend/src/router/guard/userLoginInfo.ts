import useUser from '@/hooks/useUser';
import { sleep } from '@/utils';
import { clearToken, hasToken, isLoginExpires } from '@/utils/auth';

import NProgress from 'nprogress'; // progress bar
import type { LocationQueryRaw, Router } from 'vue-router';

export default function setupUserLoginInfoGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    NProgress.start();
    const { isWhiteListPage } = useUser();
    if (isLoginExpires()) {
      clearToken();
    }
    if (to.name !== 'login' && hasToken(to.name as string)) {
      next();
    } else {
      if (to.name === 'login' || isWhiteListPage()) {
        next();
        return;
      }
      // 未登录的且访问非白名单内的地址都直接跳转至登录页，访问的页面地址缓存到 query 上
      next({
        name: 'login',
        query: {
          redirect: to.name,
          ...to.query,
        } as LocationQueryRaw,
      });
      await sleep(0);
      NProgress.done();
    }
  });
}
