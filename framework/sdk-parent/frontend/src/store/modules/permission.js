import {constantRoutes, rolesRoutes} from '../../router';

const hasPermission = (roles, route) => {
  if (route.meta && route.meta.roles) {
    return roles.some(role => route.meta.roles.includes(role))
  } else {
    return true
  }
};

export const filterRolesRoutes = (routes, roles) => {
  const res = [];
  routes.forEach(route => {
    const tmp = {
      ...route
    }
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = filterRolesRoutes(tmp.children, roles)
      }
      res.push(tmp)
    }
  })
  return res;
};

export default {
  id: 'permission',
  state: () => ({
    routes: [],
    addRoutes: []
  }),
  actions: {
    setRoutes(routes) {
      this.addRoutes = routes;
      this.routes = constantRoutes.concat(routes);
    },
    generateRoutes(roles) {
      return new Promise((resolve, reject) => {
        let accessedRoutes;
        if (roles.includes('admin')) {
          // admin角色加载所有路由
          accessedRoutes = rolesRoutes || [];
        } else {
          // 其他角色加载对应角色的路由
          accessedRoutes = filterRolesRoutes(rolesRoutes, roles);
        }
        this.setRoutes(accessedRoutes);
        resolve(accessedRoutes);
      })
    }
  }
}

