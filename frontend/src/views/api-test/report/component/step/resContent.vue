<template>
  <MsCodeEditor
    v-model:model-value="innerValue"
    :show-theme-change="false"
    title=""
    width="100%"
    height="208px"
    :show-language-change="props.language ? true : false"
    theme="MS-text"
    :language="props.language"
    :show-charset-change="props.showCharsetChange"
    :read-only="false"
    :show-full-screen="false"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { CustomTheme, editorProps, Language, LanguageEnum, Theme } from '@/components/pure/ms-code-editor/types';

  const props = defineProps<{
    script: string;
    language?: Language;
    showCharsetChange: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:script', value: string): void;
  }>();
  const innerValue = useVModel(props, 'script', emit);
</script>

<style scoped></style>
