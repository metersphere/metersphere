import { RouteRecordName, useRouter } from 'vue-router';

import { useAppStore } from '@/store';
import { getGenerateId } from '@/utils';

/**
 * 打开新页面
 * @param name 路由名
 * @param query 路由参数
 */
export default function useOpenNewPage() {
  const appStore = useAppStore();
  const router = useRouter();

  function openNewPage(name: RouteRecordName | undefined, query: Record<string, any> = {}) {
    const pId = query.pId || appStore.currentProjectId;
    if (pId) {
      // 如果传入参数指定了项目 id，则使用传入的项目 id
      delete query.pId;
    }
    const orgId = query.orgId || appStore.currentOrgId;
    if (orgId) {
      // 如果传入参数指定了组织 id，则使用传入的组织 id
      delete query.orgId;
    }
    const queryParams = new URLSearchParams(query).toString();
    window.open(
      `${window.location.origin}#${router.resolve({ name }).fullPath}?orgId=${orgId}&pId=${pId}&${queryParams}`,
      '_blank'
    );
  }

  function openNewPageWidthSingleParam(
    name: RouteRecordName | undefined,
    param: string,
    query: Record<string, any> = {}
  ) {
    const pId = query.pId || appStore.currentProjectId;
    if (pId) {
      // 如果传入参数指定了项目 id，则使用传入的项目 id
      delete query.pId;
    }
    const queryParams = new URLSearchParams(query).toString();
    window.open(
      `${window.location.origin}#${router.resolve({ name }).fullPath}/${param}?orgId=${
        appStore.currentOrgId
      }&pId=${pId}&${queryParams}`,
      '_blank'
    );
  }

  function openNewPageWithParams(
    name: RouteRecordName | undefined,
    query: Record<string, any> = {},
    params: Record<string, any> = {}
  ) {
    const pId = query.pId || appStore.currentProjectId;
    if (pId) {
      // 如果传入参数指定了项目 id，则使用传入的项目 id
      delete query.pId;
    }
    const orgId = query.orgId || appStore.currentOrgId;
    if (orgId) {
      // 如果传入参数指定了组织 id，则使用传入的组织 id
      delete query.orgId;
    }
    const queryParams = new URLSearchParams(query).toString();
    const newTab = window.open(
      `${window.location.origin}#${router.resolve({ name }).fullPath}?orgId=${orgId}&pId=${pId}&${queryParams}`,
      '_blank'
    );

    // 等待新标签页加载完成后发送消息
    if (newTab) {
      let timeInterval: NodeJS.Timeout;
      const eventId = `${name as string}-${getGenerateId()}`;
      newTab.onload = () => {
        timeInterval = setInterval(() => {
          newTab.postMessage(
            JSON.stringify({
              eventId,
              data: params,
            }),
            window.location.origin
          );
        }, 100);
      };
      window.addEventListener('message', (event) => {
        if (event.origin !== window.location.origin) {
          return;
        }
        if (event.data === eventId) {
          clearInterval(timeInterval);
        }
      });
    }
  }

  return {
    openNewPage,
    openNewPageWidthSingleParam,
    openNewPageWithParams,
  };
}
