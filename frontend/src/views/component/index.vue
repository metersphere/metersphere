<template>
  <div class="my-container">
    <div>流程编辑器</div>
    <FlowEditor />
    <a-divider />
    <div class="mb-5">ace编辑器</div>
    <AceEditor v-model:content="currentValue" :init="initAce" :theme="aceTheme" :lang="aceLang" />
    <div class="mt-10">
      <span>主题</span>
      <a-radio-group v-model="aceTheme">
        <a-radio value="github_dark">github_dark</a-radio>
        <a-radio value="github">github</a-radio>
        <a-radio value="chrome">chrome</a-radio>
      </a-radio-group>
    </div>
    <div class="mt-10">
      <span>语言</span>
      <a-radio-group v-model="aceLang">
        <a-radio value="javascript">javascript</a-radio>
        <a-radio value="xml">xml</a-radio>
        <a-radio value="json">json</a-radio>
        <a-radio value="html">html</a-radio>
        <a-radio value="java">java</a-radio>
      </a-radio-group>
    </div>
    <a-divider />
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import FlowEditor from '@/components/flow-editor/index.vue';
  import AceEditor, { LangType, ThemeType } from '@/components/ace-editor/index.vue';

  const currentValue = ref('');
  const initAce = (editor: any) => {
    // eslint-disable-next-line no-console
    console.log(editor.getOptions());
  };
  const aceTheme = ref<ThemeType>('github_dark');
  const aceLang = ref<LangType>('javascript');
</script>

<style>
  .my-container {
    padding: 16px 20px;
    padding-bottom: 0;
    height: 100vh;
    background-color: #ffffff;
  }
</style>
