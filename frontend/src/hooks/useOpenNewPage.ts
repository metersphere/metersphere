import { RouteRecordName, useRouter } from 'vue-router';

import { useAppStore } from '@/store';

/**
 * 打开新页面
 * @param name 路由名
 * @param query 路由参数
 */
export default function useOpenNewPage() {
  const appStore = useAppStore();
  const router = useRouter();

  function openNewPage(name: RouteRecordName | undefined, query = {}) {
    const queryParams = new URLSearchParams(query).toString();
    window.open(
      `${window.location.origin}#${router.resolve({ name }).fullPath}?orgId=${appStore.currentOrgId}&projectId=${
        appStore.currentProjectId
      }&${queryParams}`
    );
  }

  return {
    openNewPage,
  };
}
