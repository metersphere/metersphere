<template>
  <div v-for="item of requestBodyList" :key="item.value">
    <div v-if="getBodyDefinedCode(item.value) || getBodyCaseCode(item.value)" class="grid grid-cols-2 gap-[24px]">
      <div class="title">
        {{ item.title }}
      </div>
      <div class="title">
        {{ item.title }}
      </div>
    </div>
    <div
      v-if="getShowDiffer(item.value)"
      :class="`${getBodyCaseCode(item.value) && getBodyDefinedCode(item.value) ? '' : 'grid grid-cols-2 gap-[24px]'}`"
    >
      <div v-if="!getBodyDefinedCode(item.value) && getBodyCaseCode(item.value)" class="no-case-data h-full">
        {{ t('case.notSetData') }}
      </div>
      <template v-if="getShowDiffer(item.value)">
        <MsCodeEditor
          :model-value="getShowDiffer(item.value)"
          theme="vs"
          height="200px"
          :class="`${getBodyCaseCode(item.value) ? '' : 'no-case-data-bg'} w-full`"
          :language="getBodyCodeLanguage(item.value)"
          :show-full-screen="false"
          :show-theme-change="false"
          read-only
          is-adaptive
          :diff-mode="getDiffMode(item.value)"
          :original-value="getBodyDefinedCode(item.value)"
        >
          <template #rightTitle>
            <a-button
              type="outline"
              class="arco-btn-outline--secondary p-[0_8px]"
              size="mini"
              @click="copyScript(getBodyDefinedCode(item.value))"
            >
              <template #icon>
                <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
              </template>
            </a-button>
          </template>
        </MsCodeEditor>
      </template>
      <div v-if="!getBodyCaseCode(item.value) && getBodyDefinedCode(item.value)" class="no-case-data h-full">
        {{ t('case.notSetData') }}
      </div>
    </div>
    <div
      v-if="!getShowDiffer(item.value) && (getBodyDefinedCode(item.value) || getBodyCaseCode(item.value))"
      class="grid grid-cols-2 gap-[24px]"
    >
      <div
        v-if="!getBodyCaseCode(item.value) && !getBodyDefinedCode(item.value)"
        class="no-json-case-data no-case-data"
      >
        {{ t('case.notSetData') }}
      </div>
      <div
        v-if="!getBodyCaseCode(item.value) && !getBodyDefinedCode(item.value)"
        class="no-json-case-data no-case-data"
      >
        {{ t('case.notSetData') }}
      </div>
    </div>
  </div>
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

  const requestBodyList = ref([
    {
      value: RequestBodyFormat.XML,
      title: `${t('apiTestManagement.requestBody')}-XML`,
    },
    {
      value: RequestBodyFormat.JSON,
      title: `${t('apiTestManagement.requestBody')}-JSON`,
    },
  ]);
  const previewDefinedDetail = ref<RequestParam>(props.definedDetail);
  const previewCaseDetail = ref<RequestParam>(props.caseDetail);

  function getBodyCodeLanguage(bodyType: RequestBodyFormat) {
    if (bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (bodyType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  }

  function copyScript(val: string) {
    if (isSupported) {
      copy(val);
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  const getBodyCode = (body: ExecuteBody, bodyType: RequestBodyFormat) => {
    switch (bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return body.formDataBody?.formValues?.map((item: any) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.WWW_FORM:
        return body.wwwFormBody?.formValues?.map((item: any) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.RAW:
        return body.rawBody?.value;
      case RequestBodyFormat.JSON:
        return body?.jsonBody?.jsonValue;
      case RequestBodyFormat.XML:
        return body?.xmlBody?.value;
      default:
        return '';
    }
  };

  function getBodyDefinedCode(bodyType: RequestBodyFormat) {
    return getBodyCode(previewDefinedDetail.value?.body, bodyType);
  }
  function getBodyCaseCode(bodyType: RequestBodyFormat) {
    return getBodyCode(previewCaseDetail.value?.body, bodyType);
  }

  function getDiffMode(bodyType: RequestBodyFormat) {
    return (
      !!(getBodyDefinedCode(bodyType) && getBodyCaseCode(bodyType)) ||
      (!getBodyDefinedCode(bodyType) && !getBodyCaseCode(bodyType))
    );
  }

  function getShowDiffer(bodyType: RequestBodyFormat) {
    return getBodyCaseCode(bodyType) || getBodyDefinedCode(bodyType);
  }

  watchEffect(() => {
    if (props.definedDetail) {
      previewDefinedDetail.value = cloneDeep(props.definedDetail);
    }
    if (props.caseDetail) {
      previewCaseDetail.value = cloneDeep(props.caseDetail);
    }
  });
</script>

<style scoped lang="less">
  .no-case-data {
    height: 100% !important;
    border: 1px solid var(--color-border-2);
    border-radius: 4px;
    @apply flex items-center justify-center;
    &.no-json-case-data {
      min-height: 200px;
      @apply h-full;
    }
  }
  .no-case-data-bg {
    :deep(.view-line) {
      background: rgb(var(--success-1)) !important;
    }
  }
  .title {
    color: var(--color-text-1);
    @apply my-4;
  }
</style>
