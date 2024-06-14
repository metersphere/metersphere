<template>
  <a-popover
    v-if="
      [
        ScenarioStepType.API,
        ScenarioStepType.API_CASE,
        ScenarioStepType.SCRIPT,
        ScenarioStepType.CUSTOM_REQUEST,
      ].includes(step.stepType) &&
      props.finalExecuteStatus &&
      [ScenarioExecuteStatus.SUCCESS, ScenarioExecuteStatus.FAILED, ScenarioExecuteStatus.FAKE_ERROR].includes(
        props.finalExecuteStatus
      )
    "
    position="lt"
    content-class="scenario-step-response-popover"
    @popup-visible-change="emit('visibleChange', $event, props.step)"
  >
    <executeStatus :status="props.finalExecuteStatus" size="small" class="ml-[4px]" />
    <template #content>
      <div class="flex h-full flex-col">
        <loopPagination v-model:current-loop="currentLoop" :loop-total="loopTotal" />
        <div class="flex-1 overflow-y-hidden">
          <div v-if="step.stepType === ScenarioStepType.SCRIPT" class="flex h-full flex-col p-[8px]">
            <div class="mb-[8px] flex gap-[8px] text-[14px] font-medium text-[var(--color-text-1)]">
              {{ t('apiScenario.executionResult') }}
              <div class="one-line-text text-[var(--color-text-4)]">({{ step.name }})</div>
            </div>
            <div class="flex-1 bg-[var(--color-text-n9)] p-[12px]">
              <pre class="response-header-pre">{{ currentResponse?.console }}</pre>
            </div>
          </div>
          <div v-else class="response-result">
            <responseResult
              :active-tab="ResponseComposition.BODY"
              :request-result="currentResponse"
              :console="currentResponse?.console"
              :show-empty="false"
              :is-edit="false"
              is-definition
            >
              <template #titleLeft>
                <div class="flex items-center text-[14px]">
                  <div class="font-medium text-[var(--color-text-1)]">{{ t('apiScenario.response') }}</div>
                  <a-tooltip :content="props.step.name">
                    <div class="one-line-text">({{ props.step.name }})</div>
                  </a-tooltip>
                </div>
              </template>
            </responseResult>
          </div>
        </div>
      </div>
    </template>
  </a-popover>
  <executeStatus
    v-else-if="step.executeStatus"
    :status="props.finalExecuteStatus"
    :extra-text="getExecuteStatusExtraText(step)"
    size="small"
    class="ml-[4px]"
  />
</template>

<script lang="ts" setup>
  import executeStatus from './executeStatus.vue';
  import loopPagination from './loopPagination.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';
  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  import {
    ResponseComposition,
    ScenarioExecuteStatus,
    ScenarioStepLoopTypeEnum,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  const responseResult = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/response/index.vue')
  );

  const props = defineProps<{
    step: ScenarioStepItem;
    stepResponses: Record<string | number, Array<RequestResult>>;
    finalExecuteStatus?: ScenarioExecuteStatus;
  }>();
  const emit = defineEmits(['visibleChange']);

  const { t } = useI18n();

  const currentLoop = ref(1);
  const currentResponse = computed(() => props.stepResponses?.[props.step.uniqueId]?.[currentLoop.value - 1]);
  const loopTotal = computed(() => props.stepResponses?.[props.step.uniqueId]?.length || 0);

  function getExecuteStatusExtraText(step: ScenarioStepItem) {
    if (
      step.stepType === ScenarioStepType.LOOP_CONTROLLER &&
      step.config.loopType === ScenarioStepLoopTypeEnum.LOOP_COUNT &&
      step.config.msCountController &&
      !Number.isNaN(step.config.msCountController.loops) &&
      Number(step.config.msCountController.loops) > 0
    ) {
      // 循环控制器展示当前执行次数/总次数
      const firstHasResultChild = step.children?.find((child) => {
        return (
          [ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.CUSTOM_REQUEST].includes(step.stepType) ||
          child.stepType === ScenarioStepType.SCRIPT
        );
      });
      return firstHasResultChild && props.stepResponses[firstHasResultChild.uniqueId]
        ? `${props.stepResponses[firstHasResultChild.uniqueId].length}/${step.config.msCountController.loops}`
        : undefined;
    }
    return undefined;
  }
</script>

<style lang="less">
  .scenario-step-response-popover {
    width: 540px;
    height: 500px;
    .arco-popover-content {
      @apply h-full;
      .response-header-pre {
        @apply h-full overflow-auto bg-white;
        .ms-scroll-bar();

        padding: 8px 12px;
        border-radius: var(--border-radius-small);
      }
      .response-result {
        @apply h-full overflow-auto bg-white;
        .ms-scroll-bar();
        .response {
          .response-head {
            background-color: var(--color-text-n9);
          }

          border: 1px solid var(--color-text-n8);
          border-radius: var(--border-radius-small);
          .arco-spin {
            padding: 0;
            .response-container {
              padding: 0 16px 14px;
            }
          }
        }
      }
    }
  }
</style>
