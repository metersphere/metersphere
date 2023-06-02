import { computed } from 'vue';
import { RouteRecordRaw, RouteRecordNormalized } from 'vue-router';
import usePermission from '@/hooks/usePermission';
import appClientMenus from '@/router/app-menus';
import { cloneDeep } from 'lodash-es';

/**
 * 获取菜单树
 * @returns
 */
export default function useMenuTree() {
  const permission = usePermission();
  const menuTree = computed(() => {
    const copyRouter = cloneDeep(appClientMenus) as RouteRecordNormalized[];
    copyRouter.sort((a: RouteRecordNormalized, b: RouteRecordNormalized) => {
      return (a.meta.order || 0) - (b.meta.order || 0);
    });
    function travel(_routes: RouteRecordRaw[], layer: number) {
      if (!_routes) return null;

      const collector = _routes.map((element) => {
        // 权限校验不通过
        if (!permission.accessRouter(element)) {
          return null;
        }

        // 叶子菜单
        if (element.meta?.hideChildrenInMenu || !element.children) {
          element.children = [];
          return element;
        }

        // 过滤隐藏的菜单
        element.children = element.children.filter((x) => x.meta?.hideInMenu !== true);

        // 解析子菜单
        const subItem = travel(element.children, layer + 1);

        if (subItem && subItem.length) {
          element.children = subItem as RouteRecordRaw[];
          return element;
        }
        // the else logic
        if (layer > 1) {
          element.children = subItem as RouteRecordRaw[];
          return element;
        }

        if (element.meta?.hideInMenu === false) {
          return element;
        }

        return null;
      });
      return collector.filter(Boolean);
    }
    return travel(copyRouter, 0);
  });

  return {
    menuTree,
  };
}
