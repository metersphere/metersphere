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
    <div class="scenario-script-drawer-content">
      <MsTab
        v-model:active-key="activeTab"
        :content-tab-list="contentTabList"
        :get-text-func="() => ''"
        class="sticky-content no-content relative top-0 border-b px-[16px]"
      />
      <template v-if="activeTab === 'script'">
        <div class="ml-[16px] mt-[10px]">
          <!-- <stepTypeVue v-if="props.step" :step="props.step" /> -->
          {{ t('apiScenario.scriptOperationName') }}
        </div>
        <div class="ml-[16px] mt-[3px] max-w-[70%]">
          <a-input
            v-model="scriptName"
            :placeholder="t('apiScenario.scriptOperationNamePlaceholder')"
            :max-length="255"
            :disabled="isReadonly"
          />
        </div>
        <div class="mt-[10px] flex flex-1 gap-[8px]">
          <conditionContent
            v-if="visible"
            v-model:data="scriptVModel"
            condition-type="scenario"
            :disabled="isReadonly"
            :is-build-in="true"
            script-code-editor-height="100%"
            @change="unSaved = true"
          />
        </div>
        <!-- <div v-if="currentResponse?.console" class="p-[8px]">
          <div class="mb-[8px] font-medium text-[var(--color-text-1)]">{{ t('apiScenario.executionResult') }}</div>
          <loopPagination v-model:current-loop="currentLoop" :loop-total="loopTotal" />
          <div class="h-[300px] bg-[var(--color-text-n9)] p-[12px]">
            <pre class="response-header-pre">{{ currentResponse?.console }}</pre>
          </div>
        </div> -->
      </template>
      <div v-else-if="scriptVModel.children" class="p-[16px]">
        <assertion
          v-model:params="scriptVModel.children[0].assertionConfig.assertions"
          is-definition
          :disabled="isReadonly"
          :assertion-config="scriptVModel.children[0].assertionConfig"
        />
      </div>
      <response
        v-if="visible && currentResponse && currentResponse.responseResult.responseCode"
        ref="responseRef"
        v-model:active-layout="activeLayout"
        v-model:active-tab="responseActiveTab"
        class="response"
        :is-http-protocol="false"
        :is-priority-local-exec="isPriorityLocalExec"
        request-url=""
        :is-expanded="isVerticalExpanded"
        :request-result="currentResponse"
        :console="currentResponse?.console"
        :is-edit="false"
        is-definition
        hide-layout-switch
      >
        <template #titleRight>
          <loopPagination v-model:current-loop="currentLoop" :loop-total="loopTotal" />
        </template>
      </response>
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
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import assertion from '@/components/business/ms-assertion/index.vue';
  import loopPagination from './loopPagination.vue';

  // import stepTypeVue from './stepType/stepType.vue';
  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteConditionProcessor, RequestResult } from '@/models/apiTest/common';
  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  import { RequestConditionProcessor, ResponseComposition } from '@/enums/apiEnum';

  const conditionContent = defineAsyncComponent(() => import('@/views/api-test/components/condition/content.vue'));
  const response = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/response/index.vue')
  );

  const props = defineProps<{
    detail?: ExecuteConditionProcessor;
    step?: ScenarioStepItem;
    name?: string;
    stepResponses?: Record<string | number, RequestResult[]>;
  }>();
  const emit = defineEmits<{
    (e: 'add', name: string, scriptProcessor: ExecuteConditionProcessor): void;
    (e: 'save', name: string, scriptProcessor: ExecuteConditionProcessor, unSaved: boolean): void;
  }>();

  const defaultScript = {
    processorType: RequestConditionProcessor.SCRIPT,
    enableCommonScript: false,
    script: '',
    scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
    commonScriptInfo: {},
    polymorphicName: 'MsScriptElement',
    children: [
      {
        polymorphicName: 'MsCommonElement',
        assertionConfig: {
          assertions: [],
        },
      },
    ],
  } as unknown as ExecuteConditionProcessor;
  const scriptName = ref('');
  const scriptVModel = ref<ExecuteConditionProcessor>(cloneDeep(defaultScript));

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const isReadonly = computed(() => props.step?.isQuoteScenarioStep);
  const unSaved = ref(false);
  const currentLoop = ref(1);
  const currentResponse = computed(() => {
    if (props.step?.uniqueId) {
      return props.stepResponses?.[props.step?.uniqueId]?.[currentLoop.value - 1];
    }
  });
  const loopTotal = computed(() => (props.step?.uniqueId && props.stepResponses?.[props.step?.uniqueId]?.length) || 0);
  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const responseActiveTab = ref<ResponseComposition>(ResponseComposition.BODY);
  const isPriorityLocalExec = inject<Ref<boolean>>('isPriorityLocalExec');
  const isVerticalExpanded = computed(() => activeLayout.value === 'vertical');

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        scriptName.value = props.detail ? props.name || '' : '';
        scriptVModel.value = cloneDeep(
          props.detail
            ? {
                ...props.detail,
                processorType: RequestConditionProcessor.SCRIPT,
                polymorphicName: 'MsScriptElement',
                children: props.detail.children || defaultScript.children,
              }
            : defaultScript
        );
      }
    }
  );

  const activeTab = ref<'script' | 'assertion'>('script');
  const contentTabList = [
    {
      value: 'script',
      label: t('apiTestManagement.script'),
    },
    {
      value: 'assertion',
      label: t('apiTestDebug.assertion'),
    },
  ];

  function handleDrawerCancel() {
    visible.value = false;
  }

  function saveAndContinue() {
    emit('add', scriptName.value, scriptVModel.value);
  }

  function save() {
    emit('add', scriptName.value, scriptVModel.value);
    visible.value = false;
  }

  function handleClose() {
    if (props.detail) {
      emit('save', scriptName.value, scriptVModel.value, unSaved.value);
    }
    scriptName.value = '';
    scriptVModel.value = defaultScript as unknown as ExecuteConditionProcessor;
    activeTab.value = 'script';
  }
</script>

<style lang="less">
  .scenario-script-drawer-content {
    @apply flex h-full flex-col;
    .condition-content {
      @apply !border-none;
    }
  }
</style>

<style lang="less" scoped>
  :deep(.arco-tabs-tab:first-child) {
    margin-left: 0;
  }
  .response-header-pre {
    @apply h-full overflow-auto;

    padding: 8px 12px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    .ms-scroll-bar();
  }
  .sticky-content {
    @apply sticky overflow-visible;

    z-index: 101;
    background-color: var(--color-text-fff);
  }
</style>
