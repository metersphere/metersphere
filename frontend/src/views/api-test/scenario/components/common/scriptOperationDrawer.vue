<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('apiScenario.scriptOperation')"
    :width="960"
    no-content-padding
    disabled-width-drag
    :footer="!props.detail"
    @close="handleClose"
    @cancel="handleDrawerCancel"
  >
    <div class="flex h-full flex-col">
      <div class="ml-[16px] mt-[10px]">
        <!-- <stepTypeVue v-if="props.step" :step="props.step" /> -->
        {{ t('apiScenario.scriptOperationName') }}
      </div>
      <div class="ml-[16px] mt-[3px] max-w-[70%]">
        <a-input
          v-model="scriptName"
          :placeholder="t('apiScenario.scriptOperationNamePlaceholder')"
          :max-length="255"
          size="small"
        />
      </div>
      <div class="mt-[10px] flex flex-1 gap-[8px]">
        <conditionContent v-if="visible" v-model:data="activeItem" :is-build-in="true" />
      </div>
      <div v-if="currentResponse?.console" class="p-[8px]">
        <div class="mb-[8px] font-medium text-[var(--color-text-1)]">{{ t('apiScenario.executionResult') }}</div>
        <loopPagination v-model:current-loop="currentLoop" :loop-total="loopTotal" />
        <div class="h-[300px] bg-[var(--color-text-n9)] p-[12px]">
          <pre class="response-header-pre">{{ currentResponse?.console }}</pre>
        </div>
      </div>
    </div>
    <template v-if="!props.detail" #footer>
      <a-button type="secondary" @click="handleDrawerCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="secondary" :disabled="!scriptName" @click="saveAndContinue">
        {{ t('common.saveAndContinue') }}
      </a-button>
      <a-button type="primary" :disabled="!scriptName" @click="save">
        {{ t('common.add') }}
      </a-button>
    </template>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import loopPagination from './loopPagination.vue';

  // import stepTypeVue from './stepType/stepType.vue';
  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteConditionProcessor, RequestResult } from '@/models/apiTest/common';
  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const conditionContent = defineAsyncComponent(() => import('@/views/api-test/components/condition/content.vue'));

  const props = defineProps<{
    detail?: ExecuteConditionProcessor;
    step?: ScenarioStepItem;
    name?: string;
    stepResponses?: Record<string | number, RequestResult[]>;
  }>();
  const emit = defineEmits<{
    (e: 'add', name: string, scriptProcessor: ExecuteConditionProcessor): void;
    (e: 'save', name: string, scriptProcessor: ExecuteConditionProcessor): void;
  }>();

  const defaultScript = {
    processorType: RequestConditionProcessor.SCRIPT,
    enableCommonScript: false,
    script: '',
    scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
    commonScriptInfo: {},
    polymorphicName: 'MsScriptElement',
  } as unknown as ExecuteConditionProcessor;
  const scriptName = ref('');
  const activeItem = ref<ExecuteConditionProcessor>(cloneDeep(defaultScript));

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const currentLoop = ref(1);
  const currentResponse = computed(() => {
    if (props.step?.uniqueId) {
      return props.stepResponses?.[props.step?.uniqueId]?.[currentLoop.value - 1];
    }
  });
  const loopTotal = computed(() => (props.step?.uniqueId && props.stepResponses?.[props.step?.uniqueId]?.length) || 0);

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        scriptName.value = props.detail ? props.name || '' : '';
        activeItem.value = cloneDeep(
          props.detail
            ? {
                ...props.detail,
                processorType: RequestConditionProcessor.SCRIPT,
                polymorphicName: 'MsScriptElement',
              }
            : defaultScript
        );
      }
    }
  );

  function handleDrawerCancel() {
    visible.value = false;
  }

  function saveAndContinue() {
    emit('add', scriptName.value, activeItem.value);
  }

  function save() {
    emit('add', scriptName.value, activeItem.value);
    visible.value = false;
  }

  function handleClose() {
    if (props.detail) {
      emit('save', scriptName.value, activeItem.value);
    }
    scriptName.value = '';
    activeItem.value = defaultScript as unknown as ExecuteConditionProcessor;
  }
</script>

<style lang="less" scoped>
  .response-header-pre {
    @apply h-full overflow-auto bg-white;
    .ms-scroll-bar();

    padding: 8px 12px;
    border-radius: var(--border-radius-small);
  }
</style>
