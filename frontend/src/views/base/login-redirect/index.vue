<template>
  <a-spin class="w-full" />
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import { getProjectInfo } from '@/api/modules/project-management/project';
  import { getLarkCallback, getLarkSuiteCallback } from '@/api/modules/user';
  import { useI18n } from '@/hooks/useI18n';
  import { NO_PROJECT_ROUTE_NAME, NO_RESOURCE_ROUTE_NAME } from '@/router/constants';
  import { useAppStore, useUserStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import { setLoginExpires } from '@/utils/auth';
  import { getFirstRouteNameByPermission, routerNameHasPermission } from '@/utils/permission';

  const router = useRouter();
  const { t } = useI18n();
  const userStore = useUserStore();
  const appStore = useAppStore();
  const licenseStore = useLicenseStore();

  async function loginConfig() {
    const redirectUrl = new URL(window.location.href);
    // 获取参数
    const params = new URLSearchParams(redirectUrl.search);
    // 获取特定参数的值，例如 "id"
    const code = params.get('code');
    const state = params.get('state');
    let larkCallback;
    if (state === 'fit2cloud-lark-qr') {
      larkCallback = await getLarkCallback(code || '');
    } else {
      larkCallback = await getLarkSuiteCallback(code || '');
    }
    userStore.qrCodeLogin(await larkCallback);
    Message.success(t('login.form.login.success'));
    const { redirect, ...othersQuery } = router.currentRoute.value.query;
    const redirectHasPermission =
      redirect &&
      ![NO_RESOURCE_ROUTE_NAME, NO_PROJECT_ROUTE_NAME].includes(redirect as string) &&
      routerNameHasPermission(redirect as string, router.getRoutes());
    const currentRouteName = getFirstRouteNameByPermission(router.getRoutes());
    const [res] = await Promise.all([getProjectInfo(appStore.currentProjectId), licenseStore.getValidateLicense()]); // 登录前校验 license 避免进入页面后无license状态
    if (!res || res.deleted) {
      router.push({
        name: NO_PROJECT_ROUTE_NAME,
      });
    }
    if (res) {
      appStore.setCurrentMenuConfig(res?.moduleIds || []);
    }
    setLoginExpires();
    router.push({
      name: redirectHasPermission ? (redirect as string) : currentRouteName,
      query: {
        ...othersQuery,
        orgId: appStore.currentOrgId,
        pId: appStore.currentProjectId,
      },
    });
  }

  onBeforeMount(async () => {
    loginConfig();
  });
</script>
