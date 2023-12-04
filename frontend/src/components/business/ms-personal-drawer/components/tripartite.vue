<template>
  <div class="flex h-full flex-col overflow-hidden">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.tripartite') }}</div>
    </div>
    <div class="platform-card-container">
      <div class="platform-card">
        <div class="mb-[16px] flex items-center">
          <a-image src="/plugin/image/jira?imagePath=static/jira.jpg" width="24"></a-image>
          <div class="ml-[8px] mr-[4px] font-medium text-[var(--color-text-1)]">JIRA</div>
          <a-tooltip :content="t('ms.personal.jiraTip')" position="right">
            <icon-exclamation-circle
              class="mr-[8px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
          <MsTag theme="light" :type="tagMap[jiraConfig.status].type" size="small" class="px-[4px]">
            {{ tagMap[jiraConfig.status].text }}
          </MsTag>
        </div>
        <a-form ref="jiraFormRef" :model="jiraConfig">
          <a-form-item :label="t('ms.personal.authType')">
            <a-radio-group v-model:model-value="jiraConfig.authType">
              <a-radio value="basic">Basic Auth</a-radio>
              <a-radio value="token">Bearer Token</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item :label="t('ms.personal.platformAccount')">
            <a-input
              v-model:model-value="jiraConfig.platformAccount"
              :placeholder="t('ms.personal.platformAccountPlaceholder', { type: 'JIRA' })"
              class="w-[312px]"
              allow-clear
              autocomplete="new-password"
            ></a-input>
          </a-form-item>
          <a-form-item :label="t('ms.personal.platformPsw')">
            <a-input-password
              v-model:model-value="jiraConfig.platformPsw"
              :placeholder="t('ms.personal.platformPswPlaceholder', { type: 'JIRA' })"
              class="mr-[8px] w-[312px]"
              allow-clear
              autocomplete="new-password"
            ></a-input-password>
            <a-button type="outline" :disabled="jiraConfig.platformAccount === '' || jiraConfig.platformPsw === ''">
              {{ t('ms.personal.valid') }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
      <div class="platform-card">
        <div class="mb-[16px] flex items-center">
          <a-image src="/plugin/image/jira?imagePath=static/jira.jpg" width="24"></a-image>
          <div class="ml-[8px] mr-[4px] font-medium text-[var(--color-text-1)]">
            {{ t('ms.personal.zendao') }}
          </div>
          <a-tooltip :content="t('ms.personal.zendaoTip')" position="right">
            <icon-exclamation-circle
              class="mr-[8px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
          <MsTag theme="light" :type="tagMap[zendaoConfig.status].type" size="small" class="px-[4px]">
            {{ tagMap[zendaoConfig.status].text }}
          </MsTag>
        </div>
        <a-form ref="zendaoFormRef" :model="zendaoConfig">
          <a-form-item :label="t('ms.personal.platformAccount')">
            <a-input
              v-model:model-value="zendaoConfig.platformAccount"
              :placeholder="t('ms.personal.platformAccountPlaceholder', { type: t('ms.personal.zendao') })"
              class="w-[312px]"
              allow-clear
              autocomplete="new-password"
            ></a-input>
          </a-form-item>
          <a-form-item :label="t('ms.personal.platformPsw')">
            <a-input-password
              v-model:model-value="zendaoConfig.platformPsw"
              :placeholder="t('ms.personal.platformPswPlaceholder', { type: t('ms.personal.zendao') })"
              class="mr-[8px] w-[312px]"
              allow-clear
              autocomplete="new-password"
            ></a-input-password>
            <a-button type="outline" :disabled="zendaoConfig.platformAccount === '' || zendaoConfig.platformPsw === ''">
              {{ t('ms.personal.valid') }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
      <div class="platform-card">
        <div class="mb-[16px] flex items-center">
          <a-image src="/plugin/image/jira?imagePath=static/jira.jpg" width="24"></a-image>
          <div class="ml-[8px] mr-[4px] font-medium text-[var(--color-text-1)]"> Azure DeVops </div>
          <a-tooltip :content="t('ms.personal.azureTip')" position="right">
            <icon-exclamation-circle
              class="mr-[8px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
          <MsTag theme="light" :type="tagMap[azureConfig.status].type" size="small" class="px-[4px]">
            {{ tagMap[azureConfig.status].text }}
          </MsTag>
        </div>
        <a-form ref="zendaoFormRef" :model="azureConfig">
          <a-form-item>
            <template #label>
              <div class="flex text-right leading-none"> Personal Access Tokens </div>
            </template>
            <a-input
              v-model:model-value="azureConfig.token"
              :placeholder="t('ms.personal.azurePlaceholder')"
              class="mr-[8px] w-[312px]"
              allow-clear
              autocomplete="new-password"
            ></a-input>
            <a-button type="outline" :disabled="azureConfig.token === ''">
              {{ t('ms.personal.valid') }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
      <div class="platform-card">
        <div class="mb-[16px] flex items-center">
          <a-image src="/plugin/image/jira?imagePath=static/jira.jpg" width="24"></a-image>
          <div class="ml-[8px] mr-[4px] font-medium text-[var(--color-text-1)]"> TAPD </div>
          <a-popover position="right">
            <template #content>
              <div class="bg-[var(--color-text-n9)] p-[12px]">
                <a-image src="/images/tapd-user.png" :width="385"></a-image>
              </div>
            </template>
            <icon-exclamation-circle
              class="mr-[8px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-popover>
          <MsTag theme="light" :type="tagMap[tapdConfig.status].type" size="small" class="px-[4px]">
            {{ tagMap[tapdConfig.status].text }}
          </MsTag>
        </div>
        <a-form ref="zendaoFormRef" :model="tapdConfig">
          <a-form-item :label="t('ms.personal.platformName')">
            <a-input
              v-model:model-value="tapdConfig.name"
              :placeholder="t('ms.personal.platformNamePlaceholder')"
              class="mr-[8px] w-[312px]"
              allow-clear
              autocomplete="new-password"
            />
            <a-button type="outline" :disabled="tapdConfig.name === ''">
              {{ t('ms.personal.valid') }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
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
      text: t('ms.personal.unValid'),
    },
    1: {
      type: 'success',
      text: t('ms.personal.validPass'),
    },
    2: {
      type: 'danger',
      text: t('ms.personal.validFail'),
    },
  };

  const jiraConfig = ref({
    status: 0 as Status,
    authType: 'basic',
    platformAccount: '',
    platformPsw: '',
  });

  const zendaoConfig = ref({
    status: 0 as Status,
    platformAccount: '',
    platformPsw: '',
  });

  const azureConfig = ref({
    status: 0 as Status,
    token: '',
  });

  const tapdConfig = ref({
    status: 0 as Status,
    name: '',
  });
</script>

<style lang="less" scoped>
  .platform-card-container {
    @apply flex flex-1 flex-wrap  overflow-auto;
    .ms-scroll-bar();

    padding: 16px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
    gap: 16px;
  }
  .platform-card {
    @apply w-full bg-white;

    padding: 16px;
    border-radius: var(--border-radius-small);
    :deep(.arco-form-item-label) {
      color: var(--color-text-4) !important;
    }
  }
</style>
