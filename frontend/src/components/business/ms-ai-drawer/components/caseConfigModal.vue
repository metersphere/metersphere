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
    unmount-on-close
    @before-ok="handleSave"
    @cancel="handleCancel"
    @close="activeTab = 'template'"
  >
    <MsTab v-model:active-key="activeTab" :content-tab-list="tabList" :show-badge="false">
      <template #template>
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('ms.ai.caseType') }}</div>
        <a-radio-group v-model:model-value="config.templateConfig.caseEditType" class="mb-[16px] w-full">
          <a-radio value="TEXT">{{ t('ms.ai.textDesc') }}</a-radio>
          <a-radio value="STEP">{{ t('ms.ai.stepDesc') }}</a-radio>
        </a-radio-group>
        <div class="flex items-center gap-[24px]">
          <a-checkbox v-model:model-value="config.templateConfig.caseName" disabled>
            {{ t('ms.ai.caseName') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.templateConfig.preCondition">
            {{ t('ms.ai.precondition') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.templateConfig.caseSteps" disabled>
            {{ t('ms.ai.caseStep') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.templateConfig.expectedResult" disabled>
            {{ t('ms.ai.expectResult') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.templateConfig.remark">{{ t('ms.ai.remark') }}</a-checkbox>
        </div>
      </template>
      <template #design>
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('ms.ai.generateScene') }}</div>
        <div class="flex items-center gap-[24px]">
          <a-checkbox v-model:model-value="config.designConfig.normal">{{ t('ms.ai.normalScene') }}</a-checkbox>
          <a-checkbox v-model:model-value="config.designConfig.abnormal">{{ t('ms.ai.abnormalScene') }}</a-checkbox>
        </div>
        <div class="mb-[8px] mt-[16px] text-[var(--color-text-1)]">{{ t('ms.ai.designMethod') }}</div>
        <div class="flex items-center gap-[24px]">
          <a-checkbox v-model:model-value="config.designConfig.equivalenceClassPartitioning">
            {{ t('ms.ai.equivalence') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.designConfig.boundaryValueAnalysis">
            {{ t('ms.ai.boundaryValue') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.designConfig.decisionTableTesting">
            {{ t('ms.ai.decision') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.designConfig.causeEffectGraphing">
            {{ t('ms.ai.caseEffect') }}
          </a-checkbox>
          <a-checkbox
            v-model:model-value="config.designConfig.scenarioMethod"
            @update:model-value="config.designConfig.scenarioMethodDescription = ''"
          >
            {{ t('ms.ai.scene') }}
          </a-checkbox>
          <a-checkbox v-model:model-value="config.designConfig.orthogonalExperimentMethod">
            {{ t('ms.ai.orthogonal') }}
          </a-checkbox>
        </div>
        <a-textarea
          v-if="config.designConfig.scenarioMethod"
          v-model:model-value="config.designConfig.scenarioMethodDescription"
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

  import { getAiConfig, saveAiConfig } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import { CaseAiChatConfig } from '@/models/ai';

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
  const config = ref<CaseAiChatConfig>({
    designConfig: {
      normal: true,
      abnormal: true,
      equivalenceClassPartitioning: true,
      boundaryValueAnalysis: true,
      decisionTableTesting: true,
      orthogonalExperimentMethod: true,
      causeEffectGraphing: true,
      scenarioMethod: true,
      scenarioMethodDescription: '',
    },
    templateConfig: {
      caseEditType: 'TEXT',
      caseName: true,
      preCondition: true,
      caseSteps: true,
      expectedResult: true,
      remark: true,
    },
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
      if (!config.value.designConfig) {
        config.value.designConfig = {
          normal: true,
          abnormal: true,
          equivalenceClassPartitioning: true,
          boundaryValueAnalysis: true,
          decisionTableTesting: true,
          orthogonalExperimentMethod: true,
          causeEffectGraphing: true,
          scenarioMethod: true,
          scenarioMethodDescription: '',
        };
      }
      if (!config.value.templateConfig) {
        config.value.templateConfig = {
          caseEditType: 'TEXT',
          caseName: true,
          preCondition: true,
          caseSteps: true,
          expectedResult: true,
          remark: true,
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
