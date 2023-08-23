import { useRouter } from 'vue-router';
import { MENU_LEVEL, pathMap } from '@/config/pathMap';
import { TreeNode, findNodeByKey, mapTree } from '@/utils';
import { RouteEnum } from '@/enums/routeEnum';

export default function usePathMap() {
  const router = useRouter();
  /**
   * 根据菜单级别过滤映射树
   * @param level 菜单级别
   * @param customNodeFn 自定义过滤函数
   * @returns 过滤后的映射树
   */
  const getPathMapByLevel = <T>(
    level: (typeof MENU_LEVEL)[number],
    customNodeFn: (node: TreeNode<T>) => TreeNode<T> | null = (node) => node
  ) => {
    return mapTree(pathMap, (e) => {
      let isValid = true; // 默认是系统级别
      if (level === MENU_LEVEL[1]) {
        // 组织级别只展示组织、项目
        isValid = e.level !== MENU_LEVEL[0];
      } else if (level === MENU_LEVEL[2]) {
        // 项目级别只展示项目
        isValid = e.level !== MENU_LEVEL[0] && e.level !== MENU_LEVEL[1];
      }
      if (isValid) {
        return typeof customNodeFn === 'function' ? customNodeFn(e) : e;
      }
      return null;
    });
  };

  /**
   * 根据路由的 key 进行路由跳转，自动携带配置的 routeQuery 和 传入的routeQuery
   * @param key
   */
  const jumpRouteByMapKey = (key: typeof RouteEnum, routeQuery?: Record<string, any>) => {
    const pathNode = findNodeByKey(pathMap, key as unknown as string);
    if (pathNode) {
      router.push({
        name: pathNode?.route,
        query: {
          ...routeQuery,
          ...pathNode?.routeQuery,
        },
      });
    }
  };

  return {
    getPathMapByLevel,
    jumpRouteByMapKey,
  };
}
