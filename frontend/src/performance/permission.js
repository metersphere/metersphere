import router from './components/router/router'
import {TokenKey} from '../common/constants';

const whiteList = ['/login']; // no redirect whitelist

export const permission = {
  inserted(el, binding) {
    const { value } = binding;
    const rolesString = localStorage.getItem("roles");
    const roles = rolesString.split(',');
    if (value && value instanceof Array && value.length > 0) {
      const permissionRoles = value;
      const hasPermission = roles.some(role => {
        return permissionRoles.includes(role)
      });
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`need roles! Like v-permission="['admin','editor']"`)
    }
  }
}

router.beforeEach(async (to, from, next) => {

  // determine whether the user has logged in
  const user = JSON.parse(localStorage.getItem(TokenKey));

  if (user) {
    if (to.path === '/login') {
      next({path: '/'});
    } else {
      // const roles = user.roles.filter(r => r.id);
      // TODO 设置路由的权限
      next()
    }
  } else {
    /* has no token*/

    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next()
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login`)
    }
  }
});

router.afterEach(() => {
  // finish progress bar
});
