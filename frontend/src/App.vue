<template>
  <a-config-provider :locale="locale">
    <router-view />
    <!-- <global-setting /> -->
  </a-config-provider>
</template>

<script lang="ts" setup>
  import { computed, onBeforeMount, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useEventListener, useWindowSize } from '@vueuse/core';

  import { getProjectInfo } from '@/api/modules/project-management/basicInfo';
  import { saveBaseUrl } from '@/api/modules/setting/config';
  import { GetPlatformIconUrl } from '@/api/requrls/setting/config';
  // import GlobalSetting from '@/components/pure/global-setting/index.vue';
  import useLocale from '@/locale/useLocale';
  import { NO_PROJECT_ROUTE_NAME, WHITE_LIST } from '@/router/constants';
  import { useUserStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';
  import { setFavicon, watchStyle, watchTheme } from '@/utils/theme';

  import { getPublicKeyRequest } from './api/modules/user';
  import { getFirstRouteNameByPermission } from './utils/permission';
  import enUS from '@arco-design/web-vue/es/locale/lang/en-us';
  import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn';

  const appStore = useAppStore();
  const userStore = useUserStore();
  const licenseStore = useLicenseStore();
  const router = useRouter();
  const route = useRoute();

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
      await appStore.initSystemVersion(); // 初始化系统版本
      // 企业版才校验license
      if (appStore.packageType === 'enterprise') {
        licenseStore.getValidateLicense();
      }
      if (licenseStore.hasLicense()) {
        appStore.initPageConfig(); // 初始化页面配置
      }
      // 项目初始化时需要获取基础设置信息，看当前站点 url是否为系统内置默认地址，如果是需要替换为当前项目部署的 url 地址
      const isInitUrl = getLocalStorage('isInitUrl'); // 是否已经初始化过 url
      if (isInitUrl === 'true') return;
      await saveBaseUrl(window.location.origin);
      setLocalStorage('isInitUrl', 'true'); // 设置已经初始化过 url，避免重复初始化
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  });
  const checkIsLogin = async () => {
    const isLogin = await userStore.isLogin();
    const isLoginPage = route.name === 'login';
    if (isLogin && appStore.currentProjectId && appStore.currentProjectId !== 'no_such_project') {
      // 当前为登陆状态，且已经选择了项目，初始化当前项目配置
      try {
        const res = await getProjectInfo(appStore.currentProjectId);
        if (res.deleted || !res.enable) {
          // 如果项目被删除或者被禁用，跳转到无项目页面
          router.push(NO_PROJECT_ROUTE_NAME);
          return;
        }
        appStore.setCurrentMenuConfig(res.moduleIds);
      } catch (err) {
        appStore.setCurrentMenuConfig([]);
        // eslint-disable-next-line no-console
        console.log(err);
      }
    }
    if (isLoginPage && isLogin) {
      // 当前页面为登录页面，且已经登录，跳转到首页
      const currentRouteName = getFirstRouteNameByPermission(router.getRoutes());
      router.push({ name: currentRouteName });
    }
  };
  // 获取公钥
  const getPublicKey = async () => {
    const publicKey = await getPublicKeyRequest();
    setLocalStorage('salt', publicKey);
  };

  onMounted(async () => {
    await getPublicKey();
    if (WHITE_LIST.find((el) => el.path === window.location.hash.split('#')[1]) === undefined) {
      await checkIsLogin();
    }
    const { height } = useWindowSize();
    appStore.innerHeight = height.value;
  });
  /** 屏幕大小改变时重新赋值innerHeight */
  useEventListener(window, 'resize', () => {
    const { height } = useWindowSize();
    appStore.innerHeight = height.value;
  });
</script>
