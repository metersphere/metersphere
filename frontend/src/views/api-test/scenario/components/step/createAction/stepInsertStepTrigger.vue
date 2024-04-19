<template>
  <div>
    <a-trigger
      trigger="click"
      class="arco-trigger-menu absolute"
      content-class="w-[160px]"
      position="br"
      @popup-visible-change="handleActionTriggerChange"
    >
      <MsButton
        :id="`trigger-${step.uniqueId}`"
        type="icon"
        class="ms-tree-node-extra__btn !mr-[4px]"
        @click="emit('click')"
      >
        <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
      </MsButton>
      <template #content>
        <createStepActions
          v-model:visible="innerStep.createActionsVisible"
          v-model:selected-keys="selectedKeys"
          v-model:steps="steps"
          v-model:step="innerStep"
          :create-step-action="activeCreateAction"
          position="br"
          :popup-translate="[-7, -10]"
          @other-create="(type, step) => emit('otherCreate', type, step, activeCreateAction)"
          @close="handleActionsClose"
          @add-done="emit('addDone', $event)"
        >
          <span></span>
        </createStepActions>
        <div class="arco-trigger-menu-inner">
          <div
            v-if="showAddChildStep"
            :class="[
              'arco-trigger-menu-item !mx-0 !w-full',
              activeCreateAction === 'inside' ? 'step-tree-active-action' : '',
            ]"
            @click="handleTriggerActionClick('inside')"
          >
            <icon-plus size="12" />
            {{ t('apiScenario.inside') }}
          </div>
          <div
            :class="[
              'arco-trigger-menu-item !mx-0 !w-full',
              activeCreateAction === 'before' ? 'step-tree-active-action' : '',
            ]"
            @click="handleTriggerActionClick('before')"
          >
            <icon-left size="12" />
            {{ t('apiScenario.before') }}
          </div>
          <div
            :class="[
              'arco-trigger-menu-item !mx-0 !w-full',
              activeCreateAction === 'after' ? 'step-tree-active-action' : '',
            ]"
            @click="handleTriggerActionClick('after')"
          >
            <icon-left size="12" />
            {{ t('apiScenario.after') }}
          </div>
        </div>
      </template>
    </a-trigger>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import createStepActions from './createStepActions.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { CreateStepAction, ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioAddStepActionType, ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

  const props = defineProps<{
    step: ScenarioStepItem;
  }>();
  const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'click'): void;
    (
      e: 'otherCreate',
      type:
        | ScenarioAddStepActionType.IMPORT_SYSTEM_API
        | ScenarioAddStepActionType.CUSTOM_API
        | ScenarioAddStepActionType.SCRIPT_OPERATION,
      step?: ScenarioStepItem,
      activeCreateAction?: CreateStepAction
    ): void;
    (e: 'addDone', newStep: ScenarioStepItem): void;
  }>();

  const { t } = useI18n();

  const steps = defineModel<ScenarioStepItem[]>('steps', {
    required: true,
  });
  const selectedKeys = defineModel<(string | number)[]>('selectedKeys', {
    required: true,
  });
  const innerStep = ref<ScenarioStepItem>(props.step);

  watch(
    () => props.step,
    (val) => {
      innerStep.value = val;
    }
  );

  const showAddChildStep = computed(() => {
    return (
      [
        ScenarioStepType.LOOP_CONTROLLER,
        ScenarioStepType.IF_CONTROLLER,
        ScenarioStepType.ONCE_ONLY_CONTROLLER,
      ].includes(innerStep.value.stepType) ||
      (innerStep.value.stepType === ScenarioStepType.API_SCENARIO &&
        innerStep.value.refType === ScenarioStepRefType.COPY)
    );
  });

  const activeCreateAction = ref<CreateStepAction>();
  function handleTriggerActionClick(action: CreateStepAction) {
    innerStep.value.createActionsVisible = true;
    activeCreateAction.value = action;
  }

  function handleActionTriggerChange(val: boolean) {
    if (!val) {
      activeCreateAction.value = undefined;
      innerStep.value.createActionsVisible = false;
      emit('close');
    }
  }

  function handleActionsClose() {
    activeCreateAction.value = undefined;
    innerStep.value.createActionsVisible = false;
    document.getElementById(`trigger-${innerStep.value.uniqueId}`)?.click();
  }
</script>

<style lang="less" scoped></style>
