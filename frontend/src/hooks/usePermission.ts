import { RouteLocationNormalized, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store';

/**
 * 用户权限
 * @returns 调用方法
 */
export default function usePermission() {
  const userStore = useUserStore();
  return {
    /**
     * 是否为允许访问的路由
     * @param route 路由信息
     * @returns 是否
     */
    accessRouter(route: RouteLocationNormalized | RouteRecordRaw) {
      return (
        route.meta?.requiresAuth === false ||
        !route.meta?.roles ||
        route.meta?.roles?.includes('*') ||
        route.meta?.roles?.includes(userStore.role)
      );
    },
    /**
     * 查找第一个允许访问的路由
     * @param _routers 路由数组
     * @param role 用户角色
     * @returns 路由信息 or null
     */
    findFirstPermissionRoute(_routers: any, role = 'admin') {
      const cloneRouters = [..._routers];
      while (cloneRouters.length) {
        const firstElement = cloneRouters.shift();
        if (
          firstElement?.meta?.roles?.find((el: string[]) => {
            return el.includes('*') || el.includes(role);
          })
        )
          return { name: firstElement.name };
        if (firstElement?.children) {
          cloneRouters.push(...firstElement.children);
        }
      }
      return null;
    },
    // You can add any rules you want
  };
}
