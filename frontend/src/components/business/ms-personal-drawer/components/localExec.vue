<template>
  <div class="mb-[16px] flex items-center justify-between">
    <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.localExecution') }}</div>
  </div>
  <a-spin class="w-full" :loading="loading">
    <div class="grid grid-cols-2 gap-[16px]">
      <div class="config-card">
        <div class="config-card-title">
          <div class="config-card-title-text">{{ t('ms.personal.apiLocalExecution') }}</div>
          <MsTag
            v-if="apiConfig.status !== 'none'"
            theme="outline"
            :type="apiStatus.type"
            size="small"
            class="px-[4px]"
          >
            {{ apiStatus.text }}
          </MsTag>
        </div>
        <a-input
          v-model:model-value="apiConfig.userUrl"
          :placeholder="t('ms.personal.apiLocalExecutionPlaceholder')"
          class="mb-[16px]"
          :max-length="255"
          @press-enter="testApi"
        ></a-input>
        <div class="config-card-footer">
          <div>
            <a-button type="outline" class="px-[8px]" size="mini" :loading="testApiLoading" @click="testApi">
              {{ t('ms.personal.test') }}
            </a-button>
            <a-button
              v-if="apiConfig.userUrl.trim()"
              type="outline"
              class="arco-btn-outline--secondary px-[8px]"
              style="margin-left: 10px"
              size="mini"
              @click="clearApi"
            >
              {{ t('common.clear') }}
            </a-button>
          </div>
          <div class="flex items-center">
            <div class="mr-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t('ms.personal.priorityLocalExec') }}
            </div>
            <a-switch
              v-model:model-value="apiConfig.enable"
              size="small"
              :disabled="apiConfig.id === '' || testApiLoading || apiConfig.userUrl.trim() === ''"
              :before-change="(val) => handleApiPriorityBeforeChange(val)"
              type="line"
            />
          </div>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsTag, { TagType } from '@/components/pure/ms-tag/ms-tag.vue';

  import {
    addLocalConfig,
    disableLocalConfig,
    enableLocalConfig,
    getLocalConfig,
    updateLocalConfig,
    validLocalConfig,
  } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';
  import { UserState } from '@/store/modules/user/types';

  import { LocalConfig } from '@/models/user';

  const { t } = useI18n();
  const userStore = useUserStore();

  type Status = 0 | 1 | 2 | 'none'; // 0 未配置 1 测试通过 2 测试失败
  interface TagMapItem {
    type: TagType;
    text: string;
  }
  interface Config {
    status: Status;
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
    none: {
      type: 'default',
      text: '',
    },
  };
  const loading = ref(false);
  const testApiLoading = ref(false);
  const apiConfig = ref<LocalConfig & Config>({
    id: '',
    userUrl: '',
    enable: false,
    type: 'API',
    status: 0,
  });
  const apiStatus = computed(() => {
    return tagMap[apiConfig.value.status];
  });

  function updateLocalConfigStore(partial: Partial<UserState>) {
    userStore.updateLocalConfig(partial);
  }

  function clearApi() {
    apiConfig.value.userUrl = '';
    if (apiConfig.value.id) {
      // 已经存在配置
      updateLocalConfig({
        id: apiConfig.value.id,
        userUrl: apiConfig.value.userUrl.trim(),
      });
      disableLocalConfig(apiConfig.value.id);
      apiConfig.value.enable = false;
      updateLocalConfigStore({
        hasLocalExec: false,
        isPriorityLocalExec: false,
        localExecuteUrl: apiConfig.value.userUrl.trim(),
      });
      Message.success(t('common.updateSuccess'));
    }
  }

  async function testApi() {
    try {
      testApiLoading.value = true;
      const res = await validLocalConfig(apiConfig.value.userUrl.trim());
      apiConfig.value.status = res === 'OK' ? 1 : 2;
      if (res) {
        // 检测通过才保存配置
        if (apiConfig.value.id) {
          // 已经存在配置
          await updateLocalConfig({
            id: apiConfig.value.id,
            userUrl: apiConfig.value.userUrl.trim(),
          });
        } else {
          const result = await addLocalConfig({
            type: 'API',
            userUrl: apiConfig.value.userUrl.trim(),
          });
          apiConfig.value.id = result.id;
        }
        updateLocalConfigStore({ hasLocalExec: true, localExecuteUrl: apiConfig.value.userUrl.trim() });
        Message.success(t('ms.personal.testPass'));
      } else {
        Message.error(t('ms.personal.testFail'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      apiConfig.value.status = 2;
    } finally {
      testApiLoading.value = false;
    }
  }

  async function handleApiPriorityBeforeChange(val: string | number | boolean) {
    try {
      if (val) {
        await enableLocalConfig(apiConfig.value.id);
      } else {
        await disableLocalConfig(apiConfig.value.id);
      }
      updateLocalConfigStore({ isPriorityLocalExec: val as boolean });
      Message.success(val ? t('ms.personal.apiLocalExecutionOpen') : t('ms.personal.apiLocalExecutionClose'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  onBeforeMount(async () => {
    try {
      loading.value = true;
      if (!userStore.id) {
        return;
      }
      const res = await getLocalConfig();
      if (Array.isArray(res)) {
        res.forEach((config) => {
          if (config.type === 'API') {
            apiConfig.value = {
              ...config,
              status: 'none',
            };
          }
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  });
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
