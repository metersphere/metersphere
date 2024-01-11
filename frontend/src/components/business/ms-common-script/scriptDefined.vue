<template>
  <div v-if="props.showType === 'commonScript'" class="w-full bg-[var(--color-bg-3)] p-4 pb-0">
    <div class="flex items-center justify-between">
      <div>
        <MsTag class="!mr-2 cursor-pointer" theme="outline">
          <template #icon><icon-undo class="mr-1 text-[16px] text-[var(--color-text-4)]" /> </template>
          {{ t('project.commonScript.undo') }}</MsTag
        >
        <MsTag theme="outline" class="cursor-pointer">
          <template #icon>
            <icon-eraser class="mr-1 text-[16px] text-[var(--color-text-4)]" />
          </template>

          {{ t('project.commonScript.clear') }}</MsTag
        >
      </div>
      <MsTag class="cursor-pointer" theme="outline" @click="formatCoding">{{
        t('project.commonScript.formatting')
      }}</MsTag>
    </div>
  </div>
  <div v-if="props.showType === 'commonScript'" class="flex bg-[var(--color-bg-3)]">
    <div class="w-full">
      <MsCodeEditor
        ref="codeEditorRef"
        v-model:model-value="commonScriptValue"
        title=""
        :width="expanded ? '100%' : '68%'"
        height="460px"
        theme="MS-text"
        :read-only="false"
        :show-full-screen="false"
        :show-theme-change="false"
      >
        <template #rightBox>
          <div v-if="!expanded" class="w-[32%] min-w-[25%] bg-white p-3 pl-0">
            <div class="mb-2 flex items-center justify-between">
              <div class="flex items-center">
                <span
                  v-if="expanded"
                  class="collapsebtn mr-1 flex items-center justify-center"
                  @click="expandedHandler"
                >
                  <icon-right class="text-[12px] text-[var(--color-text-4)]" />
                </span>
                <span v-else class="expand mr-1 flex items-center justify-center" @click="expandedHandler">
                  <icon-down class="text-[12px] text-[rgb(var(--primary-6))]" />
                </span>
                <div class="font-medium">{{ t('project.commonScript.codeSnippet') }}</div>
              </div>

              <a-select v-model="language" class="max-w-[50%]" :placeholder="t('project.commonScript.pleaseSelected')">
                <a-option v-for="item of languages" :key="item.value">{{ item.text }}</a-option>
              </a-select>
            </div>
            <div class="p-[12px] pt-0">
              <div v-for="item of SCRIPT_MENU" :key="item.value" class="menuItem px-1" @click="handleClick(item)">
                {{ item.title }}
              </div>
            </div>
          </div>
          <span
            v-if="expanded"
            class="collapsebtn absolute right-2 z-10 mr-1 flex items-center justify-center"
            @click="expandedHandler"
          >
            <icon-right class="text-[12px] text-[var(--color-text-4)]" />
          </span>
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
    theme="MS-text"
    :read-only="false"
    :show-full-screen="false"
    :show-theme-change="false"
  />
  <InsertCommonScript v-model:visible="showInsertDrawer" />
  <FormApiImportDrawer
    v-model:visible="formApiExportVisible"
    :confirm-loading="confirmLoading"
    :project-id="currentProjectId"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import FormApiImportDrawer from './formApiImportDrawer.vue';
  import InsertCommonScript from './insertCommonScript.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { CommonScriptMenu } from '@/models/projectManagement/commonScript';

  import { getCodeTemplate, type Languages, SCRIPT_MENU } from './utils';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    showType: 'commonScript' | 'executionResult'; // 执行类型
  }>();

  const { t } = useI18n();
  const executionResultValue = ref('');

  const expanded = ref<boolean>(false);
  const language = ref<Languages>('beanshell');
  const commonScriptValue = ref('');

  const languages = [
    { text: 'beanshell', value: 'beanshell' },
    { text: 'python', value: 'python' },
    { text: 'groovy', value: 'groovy' },
    { text: 'javascript', value: 'javascript' },
  ];

  function expandedHandler() {
    expanded.value = !expanded.value;
  }

  const showInsertDrawer = ref<boolean>(false);
  // 插入公共脚本
  function getCustomFunction() {
    showInsertDrawer.value = true;
  }

  const formApiExportVisible = ref<boolean>(false);
  // 从Api定义导入
  function getApiExport() {
    formApiExportVisible.value = true;
  }

  function _handleCommand(command) {
    switch (command) {
      // 自定义代码片段
      case 'custom_function':
        getCustomFunction();
        return '';
      // 从API定义导入
      case 'api_definition':
        getApiExport();
        return '';
      // 新API测试[JSON]
      case 'new_api_request': {
        // requestObj为空则生产默认模版
        const headers = new Map();
        headers.set('Content-type', 'application/json');
        return getCodeTemplate(language.value, { requestHeaders: headers });
      }
      default:
        return '';
    }
  }

  const codeEditorRef = ref();

  function formatCoding() {
    codeEditorRef.value.format(commonScriptValue.value);
  }

  function handleCodeTemplate(code: string) {
    codeEditorRef.value.insertContent(code);
  }

  function handleClick(obj: CommonScriptMenu) {
    let code = '';
    if (obj.command) {
      code = _handleCommand(obj.command);
      if (!code) {
        return;
      }
    } else {
      if (language.value !== 'beanshell' && language.value !== 'groovy') {
        if (
          obj.title === t('api_test.request.processor.code_add_report_length') ||
          obj.title === t('api_test.request.processor.code_hide_report_length')
        ) {
          Message.warning(`${t('commons.no_corresponding')} ${language.value} ${t('commons.code_template')}！`);
          return;
        }
      }
      code = obj.value;
    }
    handleCodeTemplate(code);
  }

  const confirmLoading = ref<boolean>(false);
</script>

<style scoped lang="less">
  .collapsebtn {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: var(--color-text-n8) !important;
    @apply cursor-pointer bg-white;
  }
  .expand {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: rgb(var(--primary-1));
    @apply cursor-pointer;
  }
  .menuItem {
    height: 24px;
    line-height: 24px;
    color: rgb(var(--primary-5));
    @apply cursor-pointer;
  }
</style>
