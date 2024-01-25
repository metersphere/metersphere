<template>
  <MsTabCard v-model:active-tab="activeTab" :title="t('system.config.parameterConfig')" :tab-list="tabList" />
  <baseConfig v-show="activeTab === 'baseConfig'" />
  <pageConfig v-if="isInitPageConfig" v-show="activeTab === 'pageConfig'" />
  <authConfig v-if="isInitAuthConfig" v-show="activeTab === 'authConfig'" ref="authConfigRef" />
  <memoryCleanup v-if="isInitMemoryCleanup" v-show="activeTab === 'memoryCleanup'" />
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
  import memoryCleanup from './components/memoryCleanup.vue';
  import pageConfig from './components/pageConfig.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';

  const { t } = useI18n();
  const route = useRoute();

  const activeTab = ref((route.query.tab as string) || 'baseConfig');
  const isInitPageConfig = ref(activeTab.value === 'pageConfig');
  const isInitAuthConfig = ref(activeTab.value === 'authConfig');
  const isInitMemoryCleanup = ref(activeTab.value === 'memoryCleanup');
  const authConfigRef = ref<AuthConfigInstance | null>();
  const tabList = ref([
    { key: 'baseConfig', title: t('system.config.baseConfig') },
    { key: 'pageConfig', title: t('system.config.pageConfig') },
    { key: 'authConfig', title: t('system.config.authConfig') },
    { key: 'memoryCleanup', title: t('system.config.memoryCleanup') },
  ]);

  watch(
    () => activeTab.value,
    (val) => {
      if (val === 'pageConfig' && !isInitPageConfig.value) {
        isInitPageConfig.value = true;
      } else if (val === 'authConfig' && !isInitAuthConfig.value) {
        isInitAuthConfig.value = true;
      } else if (val === 'memoryCleanup' && !isInitMemoryCleanup.value) {
        isInitMemoryCleanup.value = true;
      }
    },
    {
      immediate: true,
    }
  );
  const licenseStore = useLicenseStore();
  onMounted(() => {
    if (route.query.tab === 'authConfig' && route.query.id) {
      authConfigRef.value?.openAuthDetail(route.query.id as string);
    }
    if (!licenseStore.hasLicense()) {
      const excludes = ['baseConfig', 'memoryCleanup'];
      tabList.value = tabList.value.filter((item: any) => excludes.includes(item.key));
    }
  });
</script>

<style lang="less" scoped></style>
