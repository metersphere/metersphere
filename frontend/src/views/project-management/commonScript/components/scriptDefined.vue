<template>
  <div class="w-full bg-[var(--color-bg-3)] p-4 pb-0">
    <div class="flex items-center justify-between">
      <div>
        <MsTag class="!mr-2" theme="outline">
          <template #icon><icon-undo class="mr-1 text-[16px] text-[var(--color-text-4)]" /> </template>
          {{ t('project.commonScript.undo') }}</MsTag
        >
        <MsTag theme="outline">
          <template #icon>
            <icon-eraser class="mr-1 text-[16px] text-[var(--color-text-4)]" />
          </template>

          {{ t('project.commonScript.clear') }}</MsTag
        >
      </div>
      <MsTag theme="outline">{{ t('project.commonScript.formatting') }}</MsTag>
    </div>
  </div>
  <div class="flex h-[calc(100vh-120px)] bg-[var(--color-bg-3)]">
    <div class="leftCodeEditor w-[70%]">
      <MsCodeEditor
        v-model:model-value="commonScriptValue"
        title=""
        width="100%"
        height="calc(100vh - 155px)"
        theme="MS-text"
        :read-only="false"
        :show-full-screen="false"
        :show-theme-change="false"
      />
    </div>
    <div class="rightCodeEditor mt-[24px] h-[calc(100vh-155px)] w-[calc(30%-12px)] bg-white">
      <div class="flex items-center justify-between p-3">
        <div class="flex items-center">
          <span v-if="expanded" class="collapsebtn mr-1 flex items-center justify-center" @click="expandedHandler">
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
      <div v-if="!expanded" class="p-[12px] pt-0">
        <div v-for="item of SCRIPT_MENU" :key="item.value" class="menuItem px-1 text-[12px]" @click="handleClick(item)">
          {{ item.title }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { CommonScriptMenu } from '@/models/projectManagement/commonScript';

  import { SCRIPT_MENU } from '../utils';

  const { t } = useI18n();

  const expanded = ref<boolean>(true);
  const language = ref('beanshell');
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

  function handleClick(menu: CommonScriptMenu) {}
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
