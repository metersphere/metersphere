<template>
  <a-config-provider :locale="locale">
    <router-view />
    <global-setting />
  </a-config-provider>
</template>

<script lang="ts" setup>
  import { computed, onBeforeMount, onMounted } from 'vue';
  import enUS from '@arco-design/web-vue/es/locale/lang/en-us';
  import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn';
  import GlobalSetting from '@/components/pure/global-setting/index.vue';
  import useLocale from '@/locale/useLocale';
  import { saveBaseInfo, getBaseInfo } from '@/api/modules/setting/config';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import { watchStyle, watchTheme, setFavicon } from '@/utils/theme';
  import { GetPlatformIconUrl } from '@/api/requrls/setting/config';
  import { useUserStore } from '@/store';
  import { useRouter } from 'vue-router';

  const appStore = useAppStore();
  const userStore = useUserStore();
  const licenseStore = useLicenseStore();

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
  window.document.title = appStore.pageConfig.title;
  setFavicon(GetPlatformIconUrl);

  onBeforeMount(async () => {
    try {
      appStore.initSystemversion(); // 初始化系统版本
      appStore.initPageConfig(); // 初始化页面配置
      licenseStore.getValidateLicense(); // 初始化校验license
      // 项目初始化时需要获取基础设置信息，看当前站点 url是否为系统内置默认地址，如果是需要替换为当前项目部署的 url 地址
      const isInitUrl = getLocalStorage('isInitUrl'); // 是否已经初始化过 url
      if (isInitUrl === 'true') return;
      const res = await getBaseInfo();
      if (res.url === 'http://127.0.0.1:8081') {
        await saveBaseInfo([
          {
            paramKey: 'base.url',
            paramValue: window.location.origin,
            type: 'string',
          },
        ]);
        setLocalStorage('isInitUrl', 'true'); // 设置已经初始化过 url，避免重复初始化
      } else {
        setLocalStorage('isInitUrl', 'true'); // 设置已经初始化过 url，避免重复初始化
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  });
  onMounted(async () => {
    const res = await userStore.isLogin();
    if (!res) {
      const router = useRouter();
      router.push({ name: 'login' });
    }
  });
</script>
