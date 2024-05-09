<template>
  <div v-if="showImg || isPdf || isFile" :class="showType === 'text' ? '' : 'h-full'">
    <div class="mb-[8px] flex items-center gap-[16px]">
      <a-button type="outline" class="arco-btn-outline--secondary" size="mini" @click="handleDownload">
        {{ t('common.download') }}
      </a-button>
      <a-radio-group v-model:model-value="showType" type="button" size="small" :disabled="isFile">
        <a-radio v-if="isPdf" value="pdf">pdf</a-radio>
        <a-radio v-else value="image">{{ t('common.image') }}</a-radio>
        <a-radio value="text">{{ t('common.text') }}</a-radio>
      </a-radio-group>
    </div>
    <object
      v-if="isPdf && showType === 'pdf'"
      :data="imageUrl"
      type="application/pdf"
      width="100%"
      style="height: calc(100% - 30px)"
    ></object>
    <a-image v-else-if="showType === 'image'" :src="imageUrl"></a-image>
  </div>
  <MsCodeEditor
    v-show="(!showImg && !isPdf) || showType === 'text'"
    ref="responseEditorRef"
    :model-value="props.requestResult?.responseResult.body || ''"
    :language="responseLanguage"
    theme="vs"
    :height="showImg ? 'calc(100% - 26px)' : '100%'"
    :languages="[LanguageEnum.JSON, LanguageEnum.HTML, LanguageEnum.XML, LanguageEnum.PLAINTEXT]"
    :show-full-screen="false"
    :show-theme-change="false"
    show-language-change
    show-charset-change
    read-only
    is-adaptive
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
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { downloadFile } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import { downloadByteFile, downloadUrlFile } from '@/utils';

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

  const { t } = useI18n();

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

  const showImg = computed(() => {
    if (props.requestResult) {
      return props.requestResult.responseResult.contentType.includes('image');
    }
    return false;
  });
  const isFile = computed(() => {
    const { responseResult } = props.requestResult || {};
    return !!responseResult?.filePath;
  });
  const isPdf = computed(() => {
    if (props.requestResult) {
      return props.requestResult.responseResult.contentType === 'application/pdf';
    }
    return false;
  });
  const imageUrl = computed(() => {
    if (props.requestResult) {
      return `data:${props.requestResult?.responseResult.contentType};base64,${props.requestResult?.responseResult.imageUrl}`;
    }
    return '';
  });

  const showType = ref<'image' | 'pdf' | 'text'>('image');

  watchEffect(() => {
    if (props.requestResult) {
      if (showImg.value) {
        showType.value = 'image';
      } else if (isPdf.value) {
        showType.value = 'pdf';
      } else {
        showType.value = 'text';
      }
    }
  });

  async function handleDownload() {
    if (isPdf.value) {
      downloadUrlFile(imageUrl.value, 'response.pdf');
    } else if (imageUrl.value && !isFile.value) {
      downloadUrlFile(imageUrl.value, `response.${props.requestResult?.responseResult.contentType.split('/')[1]}`);
    } else {
      const res = await downloadFile(props.requestResult?.responseResult.filePath);
      const path = props.requestResult?.responseResult.filePath;
      const fileName = path?.substring(path.lastIndexOf('/') + 1);

      downloadByteFile(res, fileName || 'response.zip');
    }
  }
  const responseEditorRef = ref();

  defineExpose({
    responseEditorRef,
  });
</script>

<style scoped></style>
