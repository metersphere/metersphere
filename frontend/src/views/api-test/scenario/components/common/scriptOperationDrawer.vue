<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('apiScenario.scriptOperation')"
    :width="960"
    no-content-padding
    disabled-width-drag
    @cancel="handleDrawerCancel"
  >
    <div class="ml-[16px] mt-[10px]">
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
    <div class="mt-[10px] flex h-[calc(100%-40px)] gap-[8px]">
      <conditionContent v-model:data="activeItem" :is-build-in="true" :is-format="true" />
    </div>
    <template #footer>
      <a-button type="secondary" @click="handleDrawerCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="secondary" @click="saveAndContinue">
        {{ t('common.saveAndContinue') }}
      </a-button>
      <a-button type="primary" @click="save">
        {{ t('common.add') }}
      </a-button>
    </template>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import conditionContent from '@/views/api-test/components/condition/content.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteConditionProcessor } from '@/models/apiTest/common';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const props = defineProps<{
    script?: ExecuteConditionProcessor;
    name?: string;
  }>();

  const defaultScript = {
    processorType: RequestConditionProcessor.SCRIPT,
    enableCommonScript: false,
    script: '',
    scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
    commonScriptInfo: {},
  } as ExecuteConditionProcessor;
  const scriptName = ref(props.name || '');
  const activeItem = ref(props.script || defaultScript);

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });

  const emit = defineEmits<{
    (e: 'save', name: string, scriptProcessor: ExecuteConditionProcessor): void;
  }>();

  function resetField() {
    scriptName.value = '';
    activeItem.value = {
      processorType: RequestConditionProcessor.SCRIPT,
      enableCommonScript: false,
      script: '',
      scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
      commonScriptInfo: {},
    } as ExecuteConditionProcessor;
  }

  function handleDrawerCancel() {
    resetField();
    visible.value = false;
  }

  function saveAndContinue() {
    emit('save', scriptName.value, activeItem.value);
    resetField();
  }

  function save() {
    emit('save', scriptName.value, activeItem.value);
    resetField();
    visible.value = false;
  }
</script>

<style lang="less" scoped></style>
