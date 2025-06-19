<template>
  <MsTabCard v-model:active-tab="activeTab" :title="t('system.config.parameterConfig')" :tab-list="tabList" />
  <baseConfig v-if="activeTab === 'baseConfig'" v-show="activeTab === 'baseConfig'" />
  <qrCodeConfig v-if="activeTab === 'qrCodeConfig'" v-show="activeTab === 'qrCodeConfig'" />
  <pageConfig v-if="isInitPageConfig" v-show="activeTab === 'pageConfig'" />
  <authConfig v-if="isInitAuthConfig" v-show="activeTab === 'authConfig'" />
  <memoryCleanup v-if="isInitMemoryCleanup" v-show="activeTab === 'memoryCleanup'" />
  <modelConfigCard v-if="isInitModelConfig" v-show="activeTab === 'modelConfig'" />
</template>

<script setup lang="ts">
  /**
   * @description 系统设置-系统参数
   */
  import { useRoute } from 'vue-router';

  import MsTabCard from '@/components/pure/ms-tab-card/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';
  import { hasAnyPermission } from '@/utils/permission';

  // 异步组件加载
  const baseConfig = defineAsyncComponent(() => import('./components/baseConfig.vue'));
  const pageConfig = defineAsyncComponent(() => import('./components/pageConfig.vue'));
  const modelConfigCard = defineAsyncComponent(() => import('./components/modelConfigCard.vue'));
  const qrCodeConfig = defineAsyncComponent(() => import('./components/qrCodeConfig.vue'));
  const authConfig = defineAsyncComponent(() => import('./components/authConfig.vue'));
  const memoryCleanup = defineAsyncComponent(() => import('./components/memoryCleanup.vue'));

  const { t } = useI18n();
  const route = useRoute();

  const activeTab = ref((route.query.tab as string) || 'baseConfig');
  const isInitPageConfig = ref(activeTab.value === 'pageConfig');
  const isInitAuthConfig = ref(activeTab.value === 'authConfig');
  const isInitMemoryCleanup = ref(activeTab.value === 'memoryCleanup');
  const isInitQrCodeConfig = ref(activeTab.value === 'qrCodeConfig');
  const isInitModelConfig = ref(activeTab.value === 'modelConfig');
  const tabList = ref([
    { key: 'baseConfig', title: t('system.config.baseConfig'), permission: ['SYSTEM_PARAMETER_SETTING_BASE:READ'] },
    { key: 'pageConfig', title: t('system.config.pageConfig'), permission: ['SYSTEM_PARAMETER_SETTING_DISPLAY:READ'] },
    {
      key: 'modelConfig',
      title: t('system.config.modelConfig.modelConfigSet'),
      permission: ['SYSTEM_PARAMETER_SETTING_AI_MODEL:READ'],
    },
    {
      key: 'qrCodeConfig',
      title: t('system.config.qrCodeConfig'),
      permission: ['SYSTEM_PARAMETER_SETTING_QRCODE:READ'],
    },
    { key: 'authConfig', title: t('system.config.authConfig'), permission: ['SYSTEM_PARAMETER_SETTING_AUTH:READ'] },
    {
      key: 'memoryCleanup',
      title: t('system.config.memoryCleanup'),
      permission: ['SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN:READ'],
    },
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
      } else if (val === 'qrCodeConfig' && !isInitQrCodeConfig.value) {
        isInitQrCodeConfig.value = true;
      } else if (val === 'modelConfig' && !isInitModelConfig.value) {
        isInitModelConfig.value = true;
      }
    },
    {
      immediate: true,
    }
  );
  const licenseStore = useLicenseStore();

  async function getXpackTab() {
    await licenseStore.getValidateLicense();
    if (!licenseStore.hasLicense()) {
      const excludes = ['baseConfig', 'memoryCleanup', 'modelConfig'];
      tabList.value = tabList.value.filter((item: any) => excludes.includes(item.key));
    }
  }

  onBeforeMount(() => {
    getXpackTab();
    const firstHasPermissionTab = tabList.value.find((item: any) => hasAnyPermission(item.permission));
    activeTab.value = (route.query.tab as string) || firstHasPermissionTab?.key || 'baseConfig';
  });
</script>

<style lang="less" scoped></style>
