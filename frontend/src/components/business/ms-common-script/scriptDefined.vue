<template>
  <div v-if="props.showHeader && props.showType === 'commonScript'" class="w-full p-4 pb-0">
    <div class="flex items-center justify-between">
      <div>
        <!--        <MsTag class="!mr-2 cursor-pointer" theme="outline" @click="undoHandler">-->
        <!--          <template #icon><icon-undo class="mr-1 text-[16px] text-[var(&#45;&#45;color-text-4)]" /> </template>-->
        <!--          {{ t('project.commonScript.undo') }}</MsTag-->
        <!--        >-->
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
  <div v-if="props.showType === 'commonScript'" class="flex h-full">
    <div class="relative h-full w-full">
      <MsCodeEditor
        ref="codeEditorRef"
        v-model:model-value="code"
        title=""
        :width="expandMenu ? '100%' : '68%'"
        :height="props.scriptCodeEditorHeight || '460px'"
        theme="vs"
        :language="language"
        :read-only="props.disabled"
        :show-full-screen="false"
        :show-theme-change="false"
        @change="() => emit('change')"
      >
        <template #rightBox>
          <MsScriptMenu
            v-model:expand="expandMenu"
            v-model:languagesType="language"
            :disabled="props.disabled"
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
    v-model:model-value="executionResult"
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
    :script-language="language"
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

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { Language, LanguageEnum } from '@/components/pure/ms-code-editor/types';
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
      disabled?: boolean;
      enableRadioSelected?: boolean;
      showHeader?: boolean;
      scriptCodeEditorHeight?: string;
    }>(),
    {
      showHeader: true,
    }
  );
  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const projectId = ref<string>(appStore.currentProjectId);

  const language = defineModel<Language>('language', {
    required: true,
  });
  const executionResult = defineModel<string>('executionResult', {
    default: '',
  });
  const code = defineModel<string>('code', {
    required: true,
  });

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

  function insertHandler(_code: string) {
    codeEditorRef.value?.insertContent(_code);
  }

  function saveHandler(data: CommonScriptItem[]) {
    if (props.enableRadioSelected) {
      codeEditorRef.value?.insertContent(data[0].script);
    } else {
      let scriptStr = '';
      data.forEach((item) => {
        if (item.type === LanguageEnum.PYTHON) {
          scriptStr += `
# ${item.name}
${item.script}
`;
        } else {
          scriptStr += `
// ${item.name}
${item.script}
`;
        }
      });
      codeEditorRef.value?.insertContent(scriptStr);
      showInsertDrawer.value = false;
    }
  }

  function undoHandler() {
    codeEditorRef.value?.undo();
  }

  function clearCode() {
    code.value = '';
  }

  defineExpose({
    formatCoding,
    insertHandler,
    undoHandler,
    clearCode,
    executionResult,
  });
</script>

<style scoped lang="less"></style>
