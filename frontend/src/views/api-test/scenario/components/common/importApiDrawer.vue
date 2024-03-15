<template>
  <MsDrawer v-model:visible="visible" :title="t('apiScenario.importSystemApi')" :width="1200">
    <div class="flex h-full flex-col overflow-hidden">
      <a-tabs v-model:active-key="activeKey">
        <a-tab-pane key="api" :title="t('apiScenario.api')" />
        <a-tab-pane key="case" :title="t('apiScenario.case')" />
        <a-tab-pane key="scenario" :title="t('apiScenario.scenario')" />
      </a-tabs>
      <div class="flex-1">
        <div class="flex">
          <div class="p-[16px]"></div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-[4px]">
          <div class="second-text">{{ t('apiScenario.sumSelected') }}</div>
          <div class="main-text">{{ totalSelected }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.api') }}</div>
          <div class="main-text">{{ selectedApis.length }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.case') }}</div>
          <div class="main-text">{{ selectedCases.length }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.scenario') }}</div>
          <div class="main-text">{{ selectedScenarios.length }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <MsButton v-show="totalSelected > 0" type="text" class="!mr-0 ml-[4px]" @click="clearAll">
            {{ t('common.clear') }}
          </MsButton>
        </div>
        <div class="flex items-center gap-[12px]">
          <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
          <a-button type="primary" @click="handleCopy">{{ t('common.copy') }}</a-button>
          <a-button type="primary" @click="handleQuote">{{ t('common.quote') }}</a-button>
        </div>
      </div>
    </template>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const emit = defineEmits<{
    (e: 'copy', data: any[]): void;
    (e: 'quote', data: any[]): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
  const activeKey = ref('api');

  const selectedApis = ref<any[]>([]);
  const selectedCases = ref<any[]>([]);
  const selectedScenarios = ref<any[]>([]);
  const totalSelected = computed(() => {
    return selectedApis.value.length + selectedCases.value.length + selectedScenarios.value.length;
  });

  function clearAll() {
    selectedApis.value = [];
    selectedCases.value = [];
    selectedScenarios.value = [];
  }

  function handleCancel() {
    clearAll();
    visible.value = false;
  }

  function handleCopy() {
    emit('copy', [...selectedApis.value, ...selectedCases.value, ...selectedScenarios.value]);
    handleCancel();
  }

  function handleQuote() {
    emit('quote', [...selectedApis.value, ...selectedCases.value, ...selectedScenarios.value]);
    handleCancel();
  }
</script>

<style lang="less" scoped>
  .second-text {
    color: var(--color-text-2);
  }
  .main-text {
    color: rgb(var(--primary-5));
  }
  .arco-tabs-content {
    @apply hidden;
  }
</style>
