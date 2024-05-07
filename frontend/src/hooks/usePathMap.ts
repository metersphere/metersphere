import { MENU_LEVEL, pathMap, PathMapItem, PathMapRoute } from '@/config/pathMap';
import router from '@/router';
import { findNodeByKey, mapTree, TreeNode } from '@/utils';

export default function usePathMap() {
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
      if (isValid && !e.hideInModule) {
        return typeof customNodeFn === 'function' ? customNodeFn(e) : e;
      }
      return null;
    });
  };

  /**
   * 根据路由的 key 进行路由跳转，自动携带配置的 routeQuery 和 传入的routeQuery
   * TODO: 权限校验待补充
   * @param key 路由的 key
   * @param routeQuery 路由的参数
   * @param openNewPage 是否在新页面打开
   */
  const jumpRouteByMapKey = (key: PathMapRoute, routeQuery?: Record<string, any>, openNewPage = false) => {
    const pathNode = findNodeByKey<PathMapItem>(pathMap, key as unknown as string);
    if (pathNode && (pathNode.route || key.includes('PROJECT_MANAGEMENT_TASK_CENTER'))) {
      if (openNewPage) {
        window.open(
          `${window.location.origin}#${router.resolve({ name: pathNode.route }).fullPath}?${new URLSearchParams({
            ...routeQuery,
            ...pathNode.routeQuery,
          }).toString()}`
        );
      } else {
        router.push({
          name: pathNode.route,
          query: {
            ...routeQuery,
            ...pathNode.routeQuery,
          },
        });
      }
    }
  };

  /**
   * 通过路由的 name 来获取它的菜单级别
   * @param name 路由的 name
   * @returns MENU_LEVEL
   */
  const getRouteLevelByKey = (name: PathMapRoute) => {
    const pathNode = findNodeByKey<PathMapItem>(pathMap, name, 'route');
    if (pathNode) {
      return pathNode.level;
    }
    return null;
  };

  /**
   * 通过 key 查找对应的 locale 路径
   */
  const findLocalePath = (targetKey: string) => {
    function getLocalePathArr(trees: PathMapItem[], path: string[] = []): string[] {
      for (let i = 0; i < trees.length; i++) {
        const node = trees[i];
        const newPathArr = [...path, node.locale];

        if (node.key === targetKey) {
          return newPathArr;
        }

        if (targetKey.startsWith(node.key) && node.children) {
          const result = getLocalePathArr(node.children, newPathArr);
          if (result) {
            return result;
          }
        }
      }

      return [];
    }
    return getLocalePathArr(pathMap);
  };

  return {
    getPathMapByLevel,
    jumpRouteByMapKey,
    getRouteLevelByKey,
    findLocalePath,
  };
}
