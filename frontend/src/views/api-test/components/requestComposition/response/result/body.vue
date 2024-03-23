<template>
  <MsCodeEditor
    ref="responseEditorRef"
    :model-value="props.requestResult?.responseResult.body"
    :language="responseLanguage"
    theme="vs"
    height="100%"
    :languages="[LanguageEnum.JSON, LanguageEnum.HTML, LanguageEnum.XML, LanguageEnum.PLAINTEXT]"
    :show-full-screen="false"
    :show-theme-change="false"
    show-language-change
    show-charset-change
    read-only
  >
    <template #rightTitle>
      <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="emits('copy')">
        <template #icon>
          <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
        </template>
      </a-button>
    </template>
  </MsCodeEditor>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { RequestResult } from '@/models/apiTest/common';

  const props = defineProps<{
    requestResult?: RequestResult;
    requestUrl?: string;
    isHttpProtocol?: boolean;
    isDefinition?: boolean;
  }>();

  const emits = defineEmits<{
    (e: 'copy'): void;
  }>();

  // 响应体语言类型
  const responseLanguage = computed(() => {
    if (props.requestResult) {
      const { contentType } = props.requestResult.responseResult;
      if (contentType.includes('json')) {
        return LanguageEnum.JSON;
      }
      if (contentType.includes('html')) {
        return LanguageEnum.HTML;
      }
      if (contentType.includes('xml')) {
        return LanguageEnum.XML;
      }
    }
    return LanguageEnum.PLAINTEXT;
  });
</script>

<style scoped></style>
