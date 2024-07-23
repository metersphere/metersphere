<template>
  <div id="ding-talk-qr" class="ding-talk-qrName" />
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
  import { useScriptTag } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import { getProjectInfo } from '@/api/modules/project-management/basicInfo';
  import { getDingCallback, getDingInfo } from '@/api/modules/user';
  import { useI18n } from '@/hooks/useI18n';
  import { NO_PROJECT_ROUTE_NAME, NO_RESOURCE_ROUTE_NAME } from '@/router/constants';
  import { useAppStore, useUserStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import { setLoginExpires, setLongType } from '@/utils/auth';
  import { getFirstRouteNameByPermission, routerNameHasPermission } from '@/utils/permission';

  const { t } = useI18n();

  const userStore = useUserStore();
  const appStore = useAppStore();
  const licenseStore = useLicenseStore();
  const router = useRouter();

  const { load } = useScriptTag('https://g.alicdn.com/dingding/h5-dingtalk-login/0.21.0/ddlogin.js');

  const initActive = async () => {
    const data = await getDingInfo();
    await load(true);
    const url = encodeURIComponent(window.location.origin);
    window.DTFrameLogin(
      {
        id: 'ding-talk-qr',
        width: 300,
        height: 300,
      },
      {
        redirect_uri: url,
        client_id: data.appKey ? data.appKey : '',
        scope: 'openid',
        response_type: 'code',
        state: 'fit2cloud-ding-qr',
        prompt: 'consent',
      },
      async (loginResult) => {
        const { authCode } = loginResult;
        const dingCallback = getDingCallback(authCode);
        userStore.qrCodeLogin(await dingCallback);
        setLongType('DING_TALK');
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
        // 也可以在不跳转页面的情况下，使用code进行授权
      },
      (errorMsg) => {
        // 这里一般需要展示登录失败的具体原因,可以使用toast等轻提示
        Message.error(`errorMsg of errorCbk: ${errorMsg}`);
      }
    );
  };
  onMounted(() => {
    initActive();
  });
</script>

<style lang="less" scoped>
  .ding-talk-qrName {
    width: 300px;
    height: 300px;
  }
</style>
