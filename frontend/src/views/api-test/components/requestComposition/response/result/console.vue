<template>
  <MsCodeEditor
    :model-value="props.console?.trim() || ''"
    :language="LanguageEnum.PLAINTEXT"
    theme="MS-text"
    height="100%"
    :show-full-screen="false"
    :show-theme-change="false"
    :show-language-change="false"
    :show-charset-change="false"
    read-only
    is-adaptive
  >
  </MsCodeEditor>
</template>

<script setup lang="ts">
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';

  const props = defineProps<{
    console?: string;
  }>();
</script>

<style scoped></style>
