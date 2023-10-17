<template>
  <MsTabCard v-model:active-tab="activeTab" :title="t('system.config.parameterConfig')" :tab-list="tabList" />
  <baseConfig v-show="activeTab === 'baseConfig'" />
  <pageConfig v-if="isInitPageConfig" v-show="activeTab === 'pageConfig'" />
  <authConfig v-if="isInitAuthConfig" v-show="activeTab === 'authConfig'" ref="authConfigRef" />
</template>

<script setup lang="ts">
  /**
   * @description 系统设置-系统参数
   */
  import { onMounted, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';

  import MsTabCard from '@/components/pure/ms-tab-card/index.vue';
  import authConfig, { AuthConfigInstance } from './components/authConfig.vue';
  import baseConfig from './components/baseConfig.vue';
  import pageConfig from './components/pageConfig.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const route = useRoute();

  const activeTab = ref((route.query.tab as string) || 'baseConfig');
  const isInitPageConfig = ref(activeTab.value === 'pageConfig');
  const isInitAuthConfig = ref(activeTab.value === 'authConfig');
  const authConfigRef = ref<AuthConfigInstance | null>();
  const tabList = [
    { key: 'baseConfig', title: t('system.config.baseConfig') },
    { key: 'pageConfig', title: t('system.config.pageConfig') },
    { key: 'authConfig', title: t('system.config.authConfig') },
  ];

  watch(
    () => activeTab.value,
    (val) => {
      if (val === 'pageConfig' && !isInitPageConfig.value) {
        isInitPageConfig.value = true;
      } else if (val === 'authConfig' && !isInitAuthConfig.value) {
        isInitAuthConfig.value = true;
      }
    },
    {
      immediate: true,
    }
  );

  onMounted(() => {
    if (route.query.tab === 'authConfig' && route.query.id) {
      authConfigRef.value?.openAuthDetail(route.query.id as string);
    }
  });
</script>

<style lang="less" scoped></style>
