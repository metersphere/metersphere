<template>
  <div v-if="props.showHeader && props.showType === 'commonScript'" class="w-full bg-[var(--color-bg-3)] p-4 pb-0">
    <div class="flex items-center justify-between">
      <div>
        <MsTag class="!mr-2 cursor-pointer" theme="outline" @click="undoHandler">
          <template #icon><icon-undo class="mr-1 text-[16px] text-[var(--color-text-4)]" /> </template>
          {{ t('project.commonScript.undo') }}</MsTag
        >
        <MsTag theme="outline" class="cursor-pointer" @click="clearCode">
          <template #icon>
            <icon-eraser class="mr-1 text-[16px] text-[var(--color-text-4)]" />
          </template>

          {{ t('project.commonScript.clear') }}</MsTag
        >
      </div>
      <MsTag class="cursor-pointer" theme="outline" @click="formatCoding">
        {{ t('project.commonScript.formatting') }}
      </MsTag>
    </div>
  </div>
  <div v-if="props.showType === 'commonScript'" class="flex bg-[var(--color-bg-3)]">
    <div class="relative w-full">
      <MsCodeEditor
        ref="codeEditorRef"
        v-model:model-value="innerCodeValue"
        title=""
        :width="expandMenu ? '100%' : '68%'"
        height="460px"
        theme="vs"
        :language="innerLanguagesType"
        :read-only="false"
        :show-full-screen="false"
        :show-theme-change="false"
      >
        <template #rightBox>
          <MsScriptMenu
            v-model:expand="expandMenu"
            v-model:languagesType="innerLanguagesType"
            @insert="insertHandler"
            @form-api-import="formApiImport"
            @insert-common-script="insertCommonScript"
          />
        </template>
      </MsCodeEditor>
    </div>
  </div>
  <MsCodeEditor
    v-else
    v-model:model-value="executionResultValue"
    title=""
    width="100%"
    height="calc(100vh - 155px)"
    theme="vs"
    language="PLAINTEXT"
    :read-only="false"
    :show-full-screen="false"
    :show-theme-change="false"
  />
  <InsertCommonScript
    v-model:visible="showInsertDrawer"
    :script-language="innerLanguagesType"
    :enable-radio-selected="props.enableRadioSelected"
    @save="saveHandler"
  />
  <FormApiImportDrawer
    v-model:visible="formApiExportVisible"
    v-model:project-id="projectId"
    :confirm-loading="confirmLoading"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { Language } from '@/components/pure/ms-code-editor/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import FormApiImportDrawer from './formApiImportDrawer.vue';
  import InsertCommonScript from './insertCommonScript.vue';
  import MsScriptMenu from './ms-script-menu.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { CommonScriptItem } from '@/models/projectManagement/commonScript';

  const appStore = useAppStore();

  const props = withDefaults(
    defineProps<{
      showType: 'commonScript' | 'executionResult'; // 执行类型
      language: Language;
      code: string;
      enableRadioSelected?: boolean;
      executionResult?: string; // 执行结果
      showHeader?: boolean;
    }>(),
    {
      showHeader: true,
    }
  );
  const emit = defineEmits<{
    (e: 'update:language', value: Language): void;
    (e: 'update:code', value: string): void;
  }>();

  const { t } = useI18n();

  const projectId = ref<string>(appStore.currentProjectId);

  const innerLanguagesType = useVModel(props, 'language', emit);
  const executionResultValue = useVModel(props, 'executionResult', emit);

  watch(
    () => innerLanguagesType.value,
    (val) => {
      emit('update:language', val);
    }
  );

  const innerCodeValue = useVModel(props, 'code', emit);

  watch(
    () => props.code,
    (val) => {
      innerCodeValue.value = val;
    }
  );

  const expandMenu = ref<boolean>(false);

  const showInsertDrawer = ref<boolean>(false);
  // 插入公共脚本
  function insertCommonScript() {
    showInsertDrawer.value = true;
  }

  const formApiExportVisible = ref<boolean>(false);
  // 从Api定义导入
  function formApiImport() {
    formApiExportVisible.value = true;
  }

  const codeEditorRef = ref<InstanceType<typeof MsCodeEditor>>();

  function formatCoding() {
    codeEditorRef.value?.format();
  }

  const confirmLoading = ref<boolean>(false);

  function insertHandler(code: string) {
    codeEditorRef.value?.insertContent(code);
  }

  function saveHandler(data: CommonScriptItem[]) {
    if (props.enableRadioSelected) {
      codeEditorRef.value?.insertContent(data[0].script);
    } else {
      let scriptStr = '';
      data.forEach((item) => {
        scriptStr += `
// ${item.name}
${item.script}
`;
      });
      codeEditorRef.value?.insertContent(scriptStr);
      showInsertDrawer.value = false;
    }
  }

  function undoHandler() {
    codeEditorRef.value?.undo();
  }

  function clearCode() {
    innerCodeValue.value = '';
  }

  defineExpose({
    formatCoding,
    insertHandler,
    undoHandler,
    clearCode,
    executionResultValue,
  });
</script>

<style scoped lang="less"></style>
