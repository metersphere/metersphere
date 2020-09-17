import router from './components/common/router/router'
import {TokenKey} from '@/common/js/constants';
import {hasRolePermissions, hasRoles} from "@/common/js/utils";

const whiteList = ['/login']; // no redirect whitelist

export const permission = {
  inserted(el, binding) {
    checkRolePermission(el, binding, 'permission');
  }
};

export const roles = {
  inserted(el, binding) {
    checkRolePermission(el, binding, 'roles');
  }
};

function checkRolePermission(el, binding, type) {
  const {value} = binding;
  if (value && value instanceof Array && value.length > 0) {
    const permissionRoles = value;
    let hasPermission = false;
    if (type === 'roles') {
      hasPermission = hasRoles(...permissionRoles);
    } else if (type === 'permission') {
      hasPermission = hasRolePermissions(...permissionRoles);
    }
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el)
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
