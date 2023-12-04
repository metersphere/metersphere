<template>
  <div class="mb-[16px] flex items-center justify-between">
    <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.localExecution') }}</div>
  </div>
  <div class="grid grid-cols-2 gap-[16px]">
    <div class="config-card">
      <div class="config-card-title">
        <div class="config-card-title-text">{{ t('ms.personal.apiLocalExecution') }}</div>
        <MsTag theme="outline" :type="tagMap[apiConfig.status].type" size="small" class="px-[4px]">
          {{ tagMap[apiConfig.status].text }}
        </MsTag>
      </div>
      <a-input
        v-model:model-value="apiConfig.url"
        :placeholder="t('ms.personal.apiLocalExecutionPlaceholder')"
        class="mb-[16px]"
      ></a-input>
      <div class="config-card-footer">
        <a-button
          type="outline"
          class="px-[8px]"
          size="mini"
          :disabled="apiConfig.url.trim() === ''"
          :loading="testApiLoading"
          @click="testApi"
        >
          {{ t('ms.personal.test') }}
        </a-button>
        <div class="flex items-center">
          <div class="mr-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
            {{ t('ms.personal.priorityLocalExec') }}
          </div>
          <a-switch
            v-model:model-value="apiConfig.isPriorityLocalExec"
            size="small"
            :disabled="apiConfig.status !== 1 || testApiLoading"
            :before-change="(val) => handleApiPriorityBeforeChange(val)"
          />
        </div>
      </div>
    </div>
    <div class="config-card">
      <div class="config-card-title">
        <div class="config-card-title-text">{{ t('ms.personal.uiLocalExecution') }}</div>
        <MsTag theme="outline" :type="tagMap[uiConfig.status].type" size="small" class="px-[4px]">
          {{ tagMap[uiConfig.status].text }}
        </MsTag>
      </div>
      <a-input
        v-model:model-value="uiConfig.url"
        :placeholder="t('ms.personal.uiLocalExecutionPlaceholder')"
        class="mb-[16px]"
      ></a-input>
      <div class="config-card-footer">
        <a-button
          type="outline"
          class="px-[8px]"
          size="mini"
          :disabled="uiConfig.url.trim() === ''"
          :loading="testUiLoading"
          @click="testUi"
        >
          {{ t('ms.personal.test') }}
        </a-button>
        <div class="flex items-center">
          <div class="mr-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
            {{ t('ms.personal.priorityLocalExec') }}
          </div>
          <a-switch
            v-model:model-value="uiConfig.isPriorityLocalExec"
            size="small"
            :disabled="uiConfig.status !== 1 || testUiLoading"
            :before-change="(val) => handleUiPriorityBeforeChange(val)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsTag, { TagType } from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  type Status = 0 | 1 | 2;
  interface TagMapItem {
    type: TagType;
    text: string;
  }
  const tagMap: Record<Status, TagMapItem> = {
    0: {
      type: 'default',
      text: t('ms.personal.unConfig'),
    },
    1: {
      type: 'success',
      text: t('ms.personal.testPass'),
    },
    2: {
      type: 'danger',
      text: t('ms.personal.testFail'),
    },
  };
  const testApiLoading = ref(false);
  const apiConfig = ref({
    url: '',
    status: 1 as Status,
    isPriorityLocalExec: false,
  });

  async function testApi() {
    try {
      testApiLoading.value = true;
      Message.success(t('ms.personal.testPass'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      testApiLoading.value = false;
    }
  }

  async function handleApiPriorityBeforeChange(val: string | number | boolean) {
    try {
      Message.success(val ? t('ms.personal.apiLocalExecutionOpen') : t('ms.personal.apiLocalExecutionClose'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  const testUiLoading = ref(false);
  const uiConfig = ref({
    url: '',
    status: 1 as Status,
    isPriorityLocalExec: false,
  });

  async function testUi() {
    try {
      testApiLoading.value = true;
      Message.success(t('ms.personal.testPass'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      testApiLoading.value = false;
    }
  }

  async function handleUiPriorityBeforeChange(val: string | number | boolean) {
    try {
      Message.success(val ? t('ms.personal.uiLocalExecutionOpen') : t('ms.personal.uiLocalExecutionClose'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }
</script>

<style lang="less" scoped>
  .config-card {
    padding: 16px;
    border: 2px solid white;
    border-radius: var(--border-radius-medium);
    background-color: var(--color-text-n9);
    box-shadow: 0 6px 15px 0 rgb(120 56 135 / 5%);
    .config-card-title {
      @apply flex items-center;

      gap: 8px;
      margin-bottom: 16px;
      .config-card-title-text {
        @apply font-medium;

        color: var(--color-text-1);
      }
    }
    .config-card-footer {
      @apply flex items-center justify-between;
    }
  }
</style>
