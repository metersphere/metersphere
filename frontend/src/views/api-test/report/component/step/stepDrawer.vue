<template>
  <MsDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawer"
    :width="960"
    :footer="false"
    :title="props.scenarioDetail?.name"
    show-full-screen
    :unmount-on-close="true"
  >
    <template #headerLeft>
      <ConditionStatus
        v-if="props.scenarioDetail?.stepType && showCondition.includes(props.scenarioDetail?.stepType)"
        class="ml-2"
        :status="props.scenarioDetail?.stepType"
      />
    </template>
    <div>
      <StepDetailContent
        mode="tiled"
        :show-type="props.showType"
        :step-item="props.scenarioDetail"
        :console="props.console"
        :is-definition="true"
        :environment-name="props.environmentName"
        :report-id="props.scenarioDetail?.reportId"
      />
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import ConditionStatus from '../conditionStatus.vue';
  import StepDetailContent from '@/views/api-test/components/requestComposition/response/result/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { ScenarioItemType } from '@/models/apiTest/report';
  import { ScenarioStepType } from '@/enums/apiEnum';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    stepId: string;
    activeStepIndex: number;
    scenarioDetail?: ScenarioItemType;
    showType: 'API' | 'CASE'; // 接口场景|用例
    console?: string; //  控制台
    environmentName?: string; // 环境名称
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const showCondition = ref<string[]>([
    ScenarioStepType.API,
    ScenarioStepType.API_CASE,
    ScenarioStepType.CUSTOM_REQUEST,
    ScenarioStepType.LOOP_CONTROLLER,
    ScenarioStepType.IF_CONTROLLER,
    ScenarioStepType.ONCE_ONLY_CONTROLLER,
  ]);
</script>

<style scoped lang="less">
  .scene-type {
    margin-left: 8px;
    padding: 0 2px;
    font-size: 12px;
    line-height: 16px;
    border: 1px solid rgb(var(--link-6));
    border-radius: 0 12px 12px 0;
    color: rgb(var(--link-6));
  }
  .resContentHeader {
    @apply flex justify-between;
  }
  :deep(.gray-td-bg) {
    td {
      background-color: var(--color-text-n9);
    }
  }
  :deep(.titleClass .arco-table-th-title) {
    @apply w-full;
  }
  :deep(.cellClassWrapper > .arco-table-cell) {
    padding: 0 !important;
    span {
      padding-left: 0 !important;
    }
  }
</style>
