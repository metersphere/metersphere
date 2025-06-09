<template>
  <a-modal
    v-model:visible="visible"
    :title="t('common.config')"
    title-align="start"
    body-class="p-0"
    :width="800"
    :cancel-button-props="{ disabled: saveLoading }"
    :ok-loading="saveLoading"
    :ok-text="t('common.save')"
    @before-ok="handleSave"
    @cancel="handleCancel"
  >
    <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('ms.ai.generateScene') }}</div>
    <div class="flex items-center gap-[24px]">
      <a-checkbox v-model:model-value="config.normal">{{ t('ms.ai.normalScene') }}</a-checkbox>
      <a-checkbox v-model:model-value="config.abnormal">{{ t('ms.ai.abnormalScene') }}</a-checkbox>
    </div>
    <div class="mb-[8px] mt-[16px] text-[var(--color-text-1)]">{{ t('ms.ai.caseContent') }}</div>
    <div class="flex items-center gap-[24px]">
      <a-checkbox v-model:model-value="config.caseName" disabled>{{ t('ms.ai.caseName') }}</a-checkbox>
      <a-checkbox v-model:model-value="config.requestParams" disabled>{{ t('ms.ai.requestParam') }}</a-checkbox>
      <a-checkbox v-model:model-value="config.preScript">{{ t('ms.ai.prescript') }}</a-checkbox>
      <a-checkbox v-model:model-value="config.postScript">{{ t('ms.ai.postscript') }}</a-checkbox>
      <a-checkbox v-model:model-value="config.assertion">{{ t('ms.ai.assertion') }}</a-checkbox>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import { getAiConfig, saveAiConfig } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';

  import { ApiAiChatConfig } from '@/models/ai';

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const config = ref<ApiAiChatConfig>({
    normal: true,
    abnormal: true,
    caseName: true,
    requestParams: true,
    preScript: true,
    postScript: true,
    assertion: true,
  });
  const saveLoading = ref(false);

  function handleCancel() {
    visible.value = false;
  }

  async function handleSave() {
    saveLoading.value = true;
    try {
      await saveAiConfig(config.value);
      Message.success(t('common.saveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error saving:', error);
    } finally {
      saveLoading.value = false;
      visible.value = false;
    }
  }

  async function initConfig() {
    saveLoading.value = true;
    try {
      config.value = await getAiConfig();
      if (!config.value) {
        config.value = {
          normal: true,
          abnormal: true,
          caseName: true,
          requestParams: true,
          preScript: true,
          postScript: true,
          assertion: true,
        };
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      saveLoading.value = false;
    }
  }

  watch(
    visible,
    (newVal) => {
      if (newVal) {
        initConfig();
      }
    },
    { immediate: true }
  );
</script>

<style lang="less"></style>
