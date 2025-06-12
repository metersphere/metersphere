<template>
  <a-config-provider :locale="locale">
    <router-view />
  </a-config-provider>
</template>

<script lang="ts" setup>
  import { useEventListener, useWindowSize } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsSysUpgradeTip from '@/components/pure/ms-sys-upgrade-tip/index.vue';

  import { saveBaseUrl } from '@/api/modules/setting/config';
  import { GetPlatformIconUrl } from '@/api/requrls/setting/config';
  import useLocale from '@/locale/useLocale';
  import { WHITE_LIST } from '@/router/constants';
  import { useUserStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import useAIStore from '@/store/modules/setting/ai';
  import useLicenseStore from '@/store/modules/setting/license';
  import { getQueryVariable, getUrlParameterWidthRegExp } from '@/utils';
  import { setLoginExpires, setLongType, setToken } from '@/utils/auth';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';
  import { setFavicon, watchStyle, watchTheme } from '@/utils/theme';

  import { getLarkCallback, getLarkSuiteCallback, getPublicKeyRequest } from './api/modules/user';
  import { hasAnyPermission } from './utils/permission';
  import enUS from '@arco-design/web-vue/es/locale/lang/en-us';
  import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn';

  const aiStore = useAIStore();
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
  setFavicon(GetPlatformIconUrl);

  onBeforeMount(async () => {
    try {
      appStore.initSystemVersion(); // 初始化系统版本
      if (hasAnyPermission(['SYSTEM_PARAMETER_SETTING_BASE:READ'])) {
        appStore.initFileSizeLimit(); // 初始化文件大小限制
      }
      // 企业版才校验license
      if (appStore.getPackageType === 'enterprise') {
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

  // 获取公钥
  const getPublicKey = async () => {
    const publicKey = await getPublicKeyRequest();
    setLocalStorage('salt', publicKey);
  };

  onBeforeMount(async () => {
    await getPublicKey();
    if (WHITE_LIST.find((el) => window.location.hash.split('#')[1].includes(el.path)) === undefined) {
      const TOKEN = getQueryVariable('_token');
      const CSRF = getQueryVariable('_csrf');
      if (TOKEN !== null && TOKEN !== undefined && CSRF !== null && CSRF !== undefined) {
        setToken(window.atob(TOKEN), CSRF);
        setLoginExpires();
      }
      const state = ref('');
      const code = getQueryVariable('code');
      state.value = getQueryVariable('state') || '';
      if (state.value.split('#')[0] === 'fit2cloud-lark-qr') {
        try {
          appStore.setLoginLoading(true);
          const larkCallback = await getLarkCallback(code || '');
          userStore.qrCodeLogin(larkCallback);
          setLongType('LARK');
          setLoginExpires();
        } catch (err) {
          // eslint-disable-next-line no-console
          console.log(err);
        }
      }
      if (state.value.split('#')[0] === 'fit2cloud-lark-suite-qr') {
        try {
          appStore.setLoginLoading(true);
          const larkCallback = await getLarkSuiteCallback(code || '');
          userStore.qrCodeLogin(larkCallback);
          setLongType('LARK_SUITE');
          setLoginExpires();
        } catch (err) {
          // eslint-disable-next-line no-console
          console.log(err);
        }
      }
      await userStore.checkIsLogin();
      appStore.setLoginLoading(false);
    }
    if (getQueryVariable('code') && getQueryVariable('state')) {
      const currentUrl = window.location.href;
      const url = new URL(currentUrl);
      getUrlParameterWidthRegExp('code');
      getUrlParameterWidthRegExp('state');
      url.searchParams.delete('code');
      url.searchParams.delete('state');
      const newUrl = url.toString();
      // 或者在不刷新页面的情况下更新URL（比如使用 History API）
      window.history.replaceState({}, document.title, newUrl);
    }

    const { height } = useWindowSize();
    appStore.innerHeight = height.value;
    if (userStore.id) {
      userStore.initLocalConfig(); // 获取本地执行配置
      aiStore.getAISourceNameList();
    }

    // @desc: TODO待优化主要是为了拿到初始化配置的项目模块方便接下来过滤菜单权限 解决刷新菜单空白问题
    appStore.getProjectInfos();
  });

  function showUpdateMessage() {
    Message.clear();
    Message.warning({
      content: () => h(MsSysUpgradeTip),
      duration: 0,
      closable: false,
    });
  }

  onMounted(() => {
    window.onerror = (message) => {
      if (typeof message === 'string' && message.includes('Failed to fetch dynamically imported')) {
        showUpdateMessage();
      }
    };

    window.onunhandledrejection = (event: PromiseRejectionEvent) => {
      if (
        event &&
        event.reason &&
        event.reason.message &&
        event.reason.message.includes('Failed to fetch dynamically imported')
      ) {
        showUpdateMessage();
      }
    };
  });

  /** 屏幕大小改变时重新赋值innerHeight */
  useEventListener(window, 'resize', () => {
    const { height } = useWindowSize();
    appStore.innerHeight = height.value;
  });
</script>
