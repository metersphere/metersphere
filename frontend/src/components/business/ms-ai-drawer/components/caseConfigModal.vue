<template>
  <a-modal
    v-model:visible="visible"
    :title="t('common.config')"
    title-align="start"
    body-class="ms-ai-config-modal p-0"
    :width="800"
    :cancel-button-props="{ disabled: saveLoading }"
    :ok-loading="saveLoading"
    :ok-text="t('common.save')"
    @before-ok="handleSave"
    @cancel="handleCancel"
  >
    <MsTab v-model:active-key="activeTab" :content-tab-list="tabList" :show-badge="false">
      <template #template>
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('ms.ai.caseType') }}</div>
        <a-radio-group v-model:model-value="config.caseType" class="mb-[16px] w-full">
          <a-radio value="text">{{ t('ms.ai.textDesc') }}</a-radio>
          <a-radio value="step">{{ t('ms.ai.stepDesc') }}</a-radio>
        </a-radio-group>
        <a-checkbox-group v-model:model-value="config.template">
          <a-checkbox value="name">{{ t('ms.ai.caseName') }}</a-checkbox>
          <a-checkbox value="precondition">{{ t('ms.ai.precondition') }}</a-checkbox>
          <a-checkbox value="step">{{ t('ms.ai.caseStep') }}</a-checkbox>
          <a-checkbox value="result">{{ t('ms.ai.expectResult') }}</a-checkbox>
          <a-checkbox value="remark">{{ t('ms.ai.remark') }}</a-checkbox>
        </a-checkbox-group>
      </template>
      <template #design>
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('ms.ai.generateScene') }}</div>
        <a-checkbox-group v-model:model-value="config.generateScene">
          <a-checkbox value="normalScene">{{ t('ms.ai.normalScene') }}</a-checkbox>
          <a-checkbox value="abnormalScene">{{ t('ms.ai.abnormalScene') }}</a-checkbox>
        </a-checkbox-group>
        <div class="mb-[8px] mt-[16px] text-[var(--color-text-1)]">{{ t('ms.ai.designMethod') }}</div>
        <a-checkbox-group v-model:model-value="config.designMethod" @change="handleDesignMethodChange">
          <a-checkbox value="equivalence">{{ t('ms.ai.equivalence') }}</a-checkbox>
          <a-checkbox value="boundaryValue">{{ t('ms.ai.boundaryValue') }}</a-checkbox>
          <a-checkbox value="decision">{{ t('ms.ai.decision') }}</a-checkbox>
          <a-checkbox value="caseEffect">{{ t('ms.ai.caseEffect') }}</a-checkbox>
          <a-checkbox value="scene">{{ t('ms.ai.scene') }}</a-checkbox>
          <a-checkbox value="orthogonal">{{ t('ms.ai.orthogonal') }}</a-checkbox>
        </a-checkbox-group>
        <a-textarea
          v-if="config.designMethod.includes('scene')"
          v-model:model-value="config.scene"
          class="mt-[16px]"
          :placeholder="t('ms.ai.sceneTip')"
          :max-length="150"
          show-word-limit
        ></a-textarea>
      </template>
    </MsTab>
  </a-modal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsTab from '@/components/pure/ms-tab/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const activeTab = ref('template');
  const tabList = [
    {
      value: 'template',
      label: t('ms.ai.caseTemplate'),
    },
    {
      value: 'design',
      label: t('ms.ai.designMethod'),
    },
  ];
  const config = ref({
    caseType: 'text',
    template: ['name', 'precondition', 'step', 'result', 'remark'],
    generateScene: ['normalScene', 'abnormalScene'],
    designMethod: ['equivalence', 'boundaryValue', 'decision', 'caseEffect', 'scene', 'orthogonal'],
    scene: '',
  });
  const saveLoading = ref(false);

  function handleDesignMethodChange() {
    if (!config.value.designMethod.includes('scene')) {
      config.value.scene = '';
    }
  }

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

<style lang="less">
  .ms-ai-config-modal {
    .arco-tabs-nav::before {
      height: 1px;
    }
    .arco-tabs-tab {
      &:first-child {
        margin-left: 0;
      }
    }
    .arco-tabs-tab-active {
      font-weight: 400;
    }
    .arco-tabs-nav-ink {
      height: 1px;
    }
  }
</style>
