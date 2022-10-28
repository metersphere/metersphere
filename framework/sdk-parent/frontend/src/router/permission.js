import router from "@/router" // 这里使用@，在不同的子应用中指向src
import NProgress from "nprogress"
import "nprogress/nprogress.css"

NProgress.configure({showSpinner: false}) // NProgress Configuration

const whiteList = ["/login"] // no redirect whitelist
let store = null;

router.beforeEach(async (to, from, next) => {
  // start progress bar
  NProgress.start();

  // determine whether the user has logged in
  if (store === null) {
    const {useUserStore} = await import('@/store');
    store = useUserStore()
  }
  const user = store.currentUser

  if (user && user.id) {
    if (to.path === '/login') {
      next();
      NProgress.done(); // hack: https://github.com/PanJiaChen/vue-element-admin/pull/2939
    } else {
      // const roles = user.roles.filter(r => r.id);
      // TODO 设置路由的权限
      next();
    }
  } else {
    /* has no token*/
    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next();
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login`);
      NProgress.done();
    }
  }
});

router.afterEach(() => {
  // finish progress bar
  NProgress.done()
})
