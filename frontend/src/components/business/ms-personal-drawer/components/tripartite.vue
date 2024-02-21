<template>
  <a-spin :loading="loading" class="flex h-full flex-col overflow-hidden">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.tripartite') }}</div>
      <MsSelect
        v-model:model-value="currentOrg"
        :options="orgOptions"
        :loading="orgLoading"
        class="!w-[300px]"
        @change="handleOrgChange"
      />
    </div>
    <div class="platform-card-container">
      <div v-for="config of dynamicForm" :key="config.key" class="platform-card">
        <div class="mb-[16px] flex items-center">
          <a-image :src="`/plugin/image/${config.key}?imagePath=static/${config.key}.jpg`" width="24"></a-image>
          <div class="ml-[8px] mr-[4px] font-medium text-[var(--color-text-1)]">{{ config.pluginName }}</div>
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
  </a-spin>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import MsTag, { TagType } from '@/components/pure/ms-tag/ms-tag.vue';
  import MsSelect from '@/components/business/ms-select';

  import { getSystemOrgOption } from '@/api/modules/setting/organizationAndProject';
  import { getPlatform, getPlatformAccount, savePlatform, validatePlatform } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const appStore = useAppStore();
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
  const currentOrg = ref(appStore.currentOrgId);
  const orgOptions = ref([]);
  const orgLoading = ref(false);

  async function initOrgOptions() {
    try {
      orgLoading.value = true;
      const res = await getSystemOrgOption();
      orgOptions.value = res.map((e) => ({
        label: e.name,
        value: e.id,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      orgLoading.value = false;
    }
  }

  const loading = ref(false);
  async function initPlatformAccountInfo() {
    try {
      loading.value = true;
      dynamicForm.value = {};
      const res = await getPlatformAccount();
      // 动态生成插件表单
      Object.keys(res).forEach((key) => {
        dynamicForm.value[key] = {
          key,
          name: res[key].name,
          status: 0,
          formModel: {},
          formRules: res[key].formItems,
          formItemList: [],
          tooltip: res[key].instructionsInfo,
          validateLoading: false,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function initPlatformInfo() {
    try {
      loading.value = true;
      const res = await getPlatform(currentOrg.value);
      // 遍历插件表单
      Object.keys(dynamicForm.value).forEach((configKey: any) => {
        const config = dynamicForm.value[configKey].formModel.form;
        // 遍历插件表单的表单项并赋值
        Object.keys(config).forEach((key) => {
          const value = res[configKey][key];
          config[key] = value || config[key];
          dynamicForm.value[configKey].status = value !== undefined ? 1 : 0;
        });
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function validate(config: any) {
    try {
      config.validateLoading = true;
      const configForms: Record<string, any> = {};
      Object.keys(dynamicForm.value).forEach((key) => {
        configForms[key] = {
          ...dynamicForm.value[key].formModel.form,
        };
      });
      await validatePlatform(config.key, currentOrg.value, config.formModel.form);
      await savePlatform({
        [currentOrg.value]: configForms,
      });
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

  async function handleOrgChange() {
    await initPlatformAccountInfo();
    initPlatformInfo();
  }

  onBeforeMount(async () => {
    initOrgOptions();
    await initPlatformAccountInfo();
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
