<template>
  <a-trigger
    trigger="click"
    class="arco-trigger-menu absolute"
    content-class="w-[160px]"
    position="br"
    @popup-visible-change="handleActionTriggerChange"
  >
    <MsButton :id="step.stepId" type="icon" class="ms-tree-node-extra__btn !mr-[4px]" @click="emit('click')">
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
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { ScenarioStepItem } from '../stepTree.vue';
  import createStepActions from './createStepActions.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { CreateStepAction } from '@/models/apiTest/scenario';
  import { ScenarioAddStepActionType, ScenarioStepType } from '@/enums/apiEnum';

  const props = defineProps<{
    step: ScenarioStepItem;
  }>();
  const emit = defineEmits<{
    (e: 'close');
    (e: 'click');
    (
      e: 'otherCreate',
      type:
        | ScenarioAddStepActionType.IMPORT_SYSTEM_API
        | ScenarioAddStepActionType.CUSTOM_API
        | ScenarioAddStepActionType.SCRIPT_OPERATION,
      step?: ScenarioStepItem,
      activeCreateAction?: CreateStepAction
    );
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
    return [
      ScenarioStepType.LOOP_CONTROL,
      ScenarioStepType.CONDITION_CONTROL,
      ScenarioStepType.ONLY_ONCE_CONTROL,
      ScenarioStepType.COPY_SCENARIO,
    ].includes(innerStep.value.type);
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
    document.getElementById(innerStep.value.stepId.toString())?.click();
  }
</script>

<style lang="less" scoped></style>
