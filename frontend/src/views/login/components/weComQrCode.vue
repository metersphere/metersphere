<template>
  <div id="wecom-qr" class="wecom-qr" />
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import { getProjectInfo } from '@/api/modules/project-management/basicInfo';
  import { getWeComCallback, getWeComInfo } from '@/api/modules/user';
  import { useI18n } from '@/hooks/useI18n';
  import { NO_PROJECT_ROUTE_NAME, NO_RESOURCE_ROUTE_NAME } from '@/router/constants';
  import { useAppStore, useUserStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import { setLoginExpires } from '@/utils/auth';
  import { getFirstRouteNameByPermission, routerNameHasPermission } from '@/utils/permission';

  import * as ww from '@wecom/jssdk';
  import { WWLoginRedirectType, WWLoginType } from '@wecom/jssdk';

  const { t } = useI18n();

  const userStore = useUserStore();
  const appStore = useAppStore();
  const licenseStore = useLicenseStore();
  const router = useRouter();

  const wwLogin = ref({});

  const init = async () => {
    const data = await getWeComInfo();
    wwLogin.value = ww.createWWLoginPanel({
      el: '#wecom-qr',
      params: {
        login_type: WWLoginType.corpApp,
        appid: data.corpId ? data.corpId : '',
        agentid: data.agentId,
        redirect_uri: data.callBack ? data.callBack : '',
        state: 'fit2cloud-wecom-qr',
        redirect_type: WWLoginRedirectType.callback,
      },
      onCheckWeComLogin({ isWeComLogin }) {
        console.log(isWeComLogin);
      },
      async onLoginSuccess({ code }) {
        const weComCallback = getWeComCallback(code);
        userStore.qrCodeLogin(await weComCallback);
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
      },
      onLoginFail(err) {
        console.log(err);
      },
    });
  };

  init();
</script>

<style lang="less" scoped>
  .wecom-qr {
    margin-top: -20px;
  }
</style>
