<template>
  <a-config-provider :locale="locale">
    <router-view />
    <template #empty>
      <MsEmpty />
    </template>
    <!-- <global-setting /> -->
  </a-config-provider>
</template>

<script lang="ts" setup>
  import { computed, onBeforeMount, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import enUS from '@arco-design/web-vue/es/locale/lang/en-us';
  import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn';
  // import GlobalSetting from '@/components/pure/global-setting/index.vue';
  import useLocale from '@/locale/useLocale';
  import { useUserStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import { saveBaseInfo } from '@/api/modules/setting/config';
  import { GetPlatformIconUrl } from '@/api/requrls/setting/config';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';
  import { watchStyle, watchTheme, setFavicon } from '@/utils/theme';
  import { WorkbenchRouteEnum } from './enums/routeEnum';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';

  const appStore = useAppStore();
  const userStore = useUserStore();
  const licenseStore = useLicenseStore();
  const router = useRouter();

  const { currentLocale } = useLocale();
  const locale = computed(() => {
    switch (currentLocale.value) {
      case 'zh-CN':
        return zhCN;
      case 'en-US':
        return enUS;
      default:
        return zhCN;
    }
  });

  // 初始化平台风格和主题色
  watchStyle(appStore.pageConfig.style, appStore.pageConfig);
  watchTheme(appStore.pageConfig.theme, appStore.pageConfig);
  setFavicon(GetPlatformIconUrl);

  onBeforeMount(async () => {
    try {
      appStore.initSystemVersion(); // 初始化系统版本
      appStore.initPageConfig(); // 初始化页面配置
      licenseStore.getValidateLicense(); // 初始化校验license
      // 项目初始化时需要获取基础设置信息，看当前站点 url是否为系统内置默认地址，如果是需要替换为当前项目部署的 url 地址
      const isInitUrl = getLocalStorage('isInitUrl'); // 是否已经初始化过 url
      if (isInitUrl === 'true') return;
      await saveBaseInfo([
        {
          paramKey: 'base.url',
          paramValue: window.location.origin,
          type: 'string',
        },
      ]);
      setLocalStorage('isInitUrl', 'true'); // 设置已经初始化过 url，避免重复初始化
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  });
  const checkIsLogin = async () => {
    const isLogin = await userStore.isLogin();
    const isLoginPage = window.location.hash.indexOf('#/login') > -1;
    if (isLoginPage && isLogin) {
      // 当前页面为登录页面，且已经登录，跳转到首页
      router.push(WorkbenchRouteEnum.WORKBENCH);
    }
  };
  onMounted(async () => {
    await checkIsLogin();
  });
</script>
