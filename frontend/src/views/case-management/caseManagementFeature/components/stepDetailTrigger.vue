<template>
  <a-trigger trigger="click" position="bottom" :popup-translate="[0, 4]">
    <MsButton type="text" class="!mr-0">
      {{ t('system.orgTemplate.stepDetail') }}
    </MsButton>
    <template #content>
      <div class="step-detail-trigger-content">
        <StepDetail :step-list="getStepData()" is-disabled is-preview is-test-plan :is-disabled-test-plan="false" />
      </div>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import StepDetail from '@/views/case-management/caseManagementFeature/components/addStep.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    stepsText: string;
  }>();

  const { t } = useI18n();

  function getStepData() {
    if (props.stepsText) {
      return JSON.parse(props.stepsText).map((item: any) => {
        return {
          id: item.id,
          step: item.desc,
          expected: item.result,
          actualResult: item.actualResult,
          executeResult: item.executeResult,
        };
      });
    }
    return [];
  }
</script>

<style scoped lang="less">
  .step-detail-trigger-content {
    padding: 16px;
    width: 700px;
    border-radius: var(--border-radius-medium);
    background-color: var(--color-text-fff);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
  }
</style>
