import router from './components/common/router/router';
import {TokenKey} from '@/common/js/constants';
import {
  enableModules,
  hasLicense,
  hasPermissions,
  hasRoles
} from "@/common/js/utils";
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css'; // progress bar style
const whiteList = ['/login']; // no redirect whitelist

NProgress.configure({showSpinner: false}); // NProgress Configuration

function checkLicense(el, binding, type) {
  let v = hasLicense();

  if (!v) {
    el.parentNode && el.parentNode.removeChild(el);
  }
}

function checkRolePermission(el, binding, type) {
  const {value} = binding;
  if (value && value instanceof Array && value.length > 0) {
    const permissionRoles = value;
    let hasPermission = false;
    if (type === 'roles') {
      hasPermission = hasRoles(...permissionRoles);
    } else if (type === 'permission') {
      hasPermission = hasPermissions(...permissionRoles);
    }
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
}

function checkModule(el, binding) {
  const {value} = binding;
  if (value && value instanceof Array && value.length > 0) {
    let v = enableModules(...value);
    if (!v) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
}

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

export const xpack = {
  inserted(el, binding) {
    checkLicense(el, binding);
  }
};

export const modules = {
  inserted(el, binding) {
    checkModule(el, binding);
  }
};

router.beforeEach(async (to, from, next) => {
  // start progress bar
  NProgress.start();

  // determine whether the user has logged in
  const user = JSON.parse(localStorage.getItem(TokenKey));

  if (user) {
    if (to.path === '/login') {
      next({path: '/'});
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
  NProgress.done();
});
