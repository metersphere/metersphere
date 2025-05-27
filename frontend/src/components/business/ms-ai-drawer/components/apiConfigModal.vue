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
    <a-checkbox-group v-model:model-value="config.generateScene">
      <a-checkbox value="normalScene">{{ t('ms.ai.normalScene') }}</a-checkbox>
      <a-checkbox value="abnormalScene">{{ t('ms.ai.abnormalScene') }}</a-checkbox>
    </a-checkbox-group>
    <div class="mb-[8px] mt-[16px] text-[var(--color-text-1)]">{{ t('ms.ai.caseContent') }}</div>
    <a-checkbox-group v-model:model-value="config.designMethod">
      <a-checkbox value="name">{{ t('ms.ai.caseName') }}</a-checkbox>
      <a-checkbox value="param">{{ t('ms.ai.requestParam') }}</a-checkbox>
      <a-checkbox value="prescript">{{ t('ms.ai.prescript') }}</a-checkbox>
      <a-checkbox value="postscript">{{ t('ms.ai.postscript') }}</a-checkbox>
      <a-checkbox value="assertion">{{ t('ms.ai.assertion') }}</a-checkbox>
    </a-checkbox-group>
  </a-modal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const config = ref({
    generateScene: ['normalScene', 'abnormalScene'],
    designMethod: ['equivalence', 'boundaryValue', 'decision', 'caseEffect', 'scene', 'orthogonal'],
  });
  const saveLoading = ref(false);

  function handleCancel() {
    visible.value = false;
  }

  async function handleSave() {
    saveLoading.value = true;
    try {
      Message.success(t('common.saveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error saving:', error);
    } finally {
      saveLoading.value = false;
      visible.value = false;
    }
  }
</script>

<style lang="less"></style>
