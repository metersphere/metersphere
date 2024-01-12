<template>
  <div class="flex h-full flex-col overflow-hidden">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.tripartite') }}</div>
    </div>
    <div class="platform-card-container">
      <div v-for="config of dynamicForm" :key="config.key" class="platform-card">
        <div class="mb-[16px] flex items-center">
          <a-image :src="`/plugin/image/${config.key}?imagePath=static/${config.key}.jpg`" width="24"></a-image>
          <div class="ml-[8px] mr-[4px] font-medium text-[var(--color-text-1)]">{{ config.key }}</div>
          <a-tooltip v-if="config.tooltip" :content="config.tooltip" position="right">
            <icon-exclamation-circle
              class="mr-[8px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
          <MsTag theme="light" :type="tagMap[config.status].type" size="small" class="px-[4px]">
            {{ tagMap[config.status].text }}
          </MsTag>
        </div>
        <MsFormCreate
          v-model:api="config.formModel"
          v-model:form-item="config.formItemList"
          :form-rule="config.formRules"
          :option="options"
        >
        </MsFormCreate>
        <a-button type="outline" :loading="config.validateLoading" @click="validate(config)">
          {{ t('ms.personal.valid') }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import MsTag, { TagType } from '@/components/pure/ms-tag/ms-tag.vue';

  import { getPlatform, getPlatformAccount, savePlatform, validatePlatform } from '@/api/modules/user/index';
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

  const dynamicForm = ref<any>({});
  const options = ref({
    resetBtn: false,
    submitBtn: false,
    on: false,
    form: {
      layout: 'vertical',
      labelAlign: 'left',
    },
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
    },
  });

  async function initPlatformAccountInfo() {
    try {
      const res = await getPlatformAccount();
      Object.keys(res).forEach((key) => {
        dynamicForm.value[key] = {
          key,
          status: 0,
          formModel: {},
          formRules: res[key].formItems,
          formItemList: [],
          tooltip: res[key].instructionsInfo,
          validateLoading: false,
        };
      });
    } catch (error) {
      console.log(error);
    }
  }

  async function initPlatformInfo() {
    try {
      const res = await getPlatform();
    } catch (error) {
      console.log(error);
    }
  }

  async function validate(config: any) {
    try {
      config.validateLoading = true;
      await validatePlatform(config.key, config.formModel.form);
      Message.success(t('ms.personal.validPass'));
      config.status = 1;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      config.status = 2;
    } finally {
      config.validateLoading = false;
    }
  }

  onBeforeMount(() => {
    initPlatformAccountInfo();
    initPlatformInfo();
  });
</script>

<style lang="less" scoped>
  .platform-card-container {
    @apply flex flex-wrap  overflow-auto;
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
