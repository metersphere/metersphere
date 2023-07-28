<template>
  <a-config-provider :locale="locale">
    <router-view />
    <global-setting />
  </a-config-provider>
</template>

<script lang="ts" setup>
  import { computed, onBeforeMount } from 'vue';
  import enUS from '@arco-design/web-vue/es/locale/lang/en-us';
  import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn';
  import GlobalSetting from '@/components/pure/global-setting/index.vue';
  import useLocale from '@/locale/useLocale';
  import { saveBaseInfo, getBaseInfo } from '@/api/modules/setting/config';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';

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

  // 项目初始化时需要获取基础设置信息，看当前站点 url是否为系统内置默认地址，如果是需要替换为当前项目部署的 url 地址
  onBeforeMount(async () => {
    try {
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
      }
    } catch (error) {
      console.log(error);
    }
  });
</script>
