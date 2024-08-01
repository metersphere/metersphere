<template>
  <template
    v-if="
      [RequestBodyFormat.JSON, RequestBodyFormat.RAW, RequestBodyFormat.XML].includes(
        previewDefinedDetail?.body?.bodyType
      )
    "
  >
    <MsCodeEditor
      v-if="previewDefinedDetail?.body?.bodyType === RequestBodyFormat.JSON"
      :model-value="bodyCaseCode"
      theme="vs"
      height="200px"
      :language="bodyCodeLanguage"
      :show-full-screen="false"
      :show-theme-change="false"
      read-only
      is-adaptive
      diff-mode
      :original-value="bodyDefinedCode"
    >
      <template #rightTitle>
        <a-button
          type="outline"
          class="arco-btn-outline--secondary p-[0_8px]"
          size="mini"
          @click="copyScript(bodyDefinedCode)"
        >
          <template #icon>
            <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
          </template>
        </a-button>
      </template>
    </MsCodeEditor>
  </template>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteBody } from '@/models/apiTest/common';
  import { RequestBodyFormat } from '@/enums/apiEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const MsCodeEditor = defineAsyncComponent(() => import('@/components/pure/ms-code-editor/index.vue'));

  const { copy, isSupported } = useClipboard({ legacy: true });
  const { t } = useI18n();

  const props = defineProps<{
    definedDetail: RequestParam;
    caseDetail: RequestParam;
  }>();

  const previewDefinedDetail = ref<RequestParam>(props.definedDetail);
  const previewCaseDetail = ref<RequestParam>(props.caseDetail);

  const bodyCodeLanguage = computed(() => {
    if (previewDefinedDetail.value?.body?.bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (previewDefinedDetail.value?.body?.bodyType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  function copyScript(val: string) {
    if (isSupported) {
      copy(val);
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  const getBodyCode = (body: ExecuteBody) => {
    switch (body?.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return body.formDataBody?.formValues?.map((item: any) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.WWW_FORM:
        return body.wwwFormBody?.formValues?.map((item: any) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.RAW:
        return body.rawBody?.value;
      case RequestBodyFormat.JSON:
        return body.jsonBody?.jsonValue;
      case RequestBodyFormat.XML:
        return body.xmlBody?.value;
      default:
        return '';
    }
  };

  // 接口定义Code
  const bodyDefinedCode = computed(() => getBodyCode(previewDefinedDetail.value?.body));

  // 用例Code
  const bodyCaseCode = computed(() => getBodyCode(previewCaseDetail.value?.body));

  watchEffect(() => {
    if (props.definedDetail) {
      previewDefinedDetail.value = cloneDeep(props.definedDetail);
    }
    if (props.caseDetail) {
      previewCaseDetail.value = cloneDeep(props.caseDetail);
    }
  });
</script>

<style scoped></style>
