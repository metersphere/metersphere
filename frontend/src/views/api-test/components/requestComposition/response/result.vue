<template>
  <div v-show="props.requestResult?.responseResult.responseCode" class="h-full">
    <a-tabs v-model:active-key="activeTab" class="no-content border-b border-[var(--color-text-n8)]">
      <a-tab-pane v-for="item of responseCompositionTabList" :key="item.value" :title="item.label" />
    </a-tabs>
    <div class="response-container">
      <MsCodeEditor
        v-if="activeTab === ResponseComposition.BODY"
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
          <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="copyScript">
            <template #icon>
              <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
            </template>
          </a-button>
        </template>
      </MsCodeEditor>
      <MsCodeEditor
        v-else-if="activeTab === ResponseComposition.CONSOLE"
        :model-value="props.console?.trim()"
        :language="LanguageEnum.PLAINTEXT"
        theme="MS-text"
        height="100%"
        :show-full-screen="false"
        :show-theme-change="false"
        :show-language-change="false"
        :show-charset-change="false"
        read-only
      >
      </MsCodeEditor>
      <div
        v-else-if="
          activeTab === ResponseComposition.HEADER ||
          activeTab === ResponseComposition.REAL_REQUEST ||
          activeTab === ResponseComposition.EXTRACT
        "
        class="h-full rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]"
      >
        <pre class="response-header-pre">{{ getResponsePreContent(activeTab) }}</pre>
      </div>
      <MsBaseTable v-else-if="activeTab === 'ASSERTION'" v-bind="propsRes" v-on="propsEvent">
        <template #status="{ record }">
          <MsTag :type="record.status === 1 ? 'success' : 'danger'" theme="light">
            {{ record.status === 1 ? t('common.success') : t('common.fail') }}
          </MsTag>
        </template>
      </MsBaseTable>
    </div>
  </div>
  <a-empty
    v-show="!props.requestResult?.responseResult.responseCode"
    class="flex h-[150px] items-center gap-[16px] p-[16px]"
  >
    <template #image>
      <img :src="noDataSvg" class="!h-[60px] w-[78px]" />
    </template>
    <div class="flex items-center gap-[8px]">
      <div>{{ t('apiTestManagement.click') }}</div>
      <MsButton
        class="!mr-0"
        type="text"
        :disabled="props.isHttpProtocol && !props.requestUrl"
        @click="emit('execute')"
      >
        {{ props.isPriorityLocalExec ? t('apiTestDebug.localExec') : t('apiTestDebug.serverExec') }}
      </MsButton>
      <div>{{ t('apiTestManagement.getResponse') }}</div>
    </div>
  </a-empty>
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';
  import { ResponseComposition } from '@/enums/apiEnum';

  const props = defineProps<{
    requestResult?: RequestResult;
    console?: string;
    isPriorityLocalExec: boolean;
    requestUrl?: string;
    isHttpProtocol: boolean;
  }>();
  const emit = defineEmits(['execute']);

  const { t } = useI18n();

  const noDataSvg = `${import.meta.env.BASE_URL}images/noResponse.svg`;
  const responseCompositionTabList = [
    {
      label: t('apiTestDebug.responseBody'),
      value: ResponseComposition.BODY,
    },
    {
      label: t('apiTestDebug.responseHeader'),
      value: ResponseComposition.HEADER,
    },
    {
      label: t('apiTestDebug.realRequest'),
      value: ResponseComposition.REAL_REQUEST,
    },
    {
      label: t('apiTestDebug.console'),
      value: ResponseComposition.CONSOLE,
    },
    // {
    //   label: t('apiTestDebug.extract'),
    //   value: ResponseComposition.EXTRACT,
    // },
    // {
    //   label: t('apiTestDebug.assertion'),
    //   value: ResponseComposition.ASSERTION,
    // }, // TODO:断言暂时没加
  ];
  const activeTab = defineModel<ResponseComposition>('activeTab', {
    required: true,
    default: ResponseComposition.BODY,
  });

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

  const { copy, isSupported } = useClipboard();

  async function copyScript() {
    if (isSupported) {
      await copy(props.requestResult?.responseResult.body || '');
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  function getResponsePreContent(type: keyof typeof ResponseComposition) {
    switch (type) {
      case ResponseComposition.HEADER:
        return props.requestResult?.headers.trim();
      case ResponseComposition.REAL_REQUEST:
        return props.requestResult?.body
          ? `${t('apiTestDebug.requestUrl')}:\n${props.requestResult.url}\n${t('apiTestDebug.header')}:\n${
              props.requestResult.headers
            }\nBody:\n${props.requestResult.body.trim()}`
          : '';
      // case ResponseComposition.EXTRACT:
      //   return Object.keys(props.request.response.extract)
      //     .map((e) => `${e}: ${props.request.response.extract[e]}`)
      //     .join('\n'); // TODO:断言暂时没加
      default:
        return '';
    }
  }

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.content',
      dataIndex: 'content',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 80,
    },
    {
      title: '',
      dataIndex: 'desc',
      showTooltip: true,
    },
  ];
  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
    scroll: { x: '100%' },
    columns,
  });
  propsRes.value.data = [
    {
      id: new Date().getTime(),
      content: 'Response Code equals: 200',
      status: 1,
      desc: '',
    },
    {
      id: new Date().getTime(),
      content: '$.users[1].age REGEX: 31',
      status: 0,
      desc: `Value expected to match regexp '31', but it did not match: '30' match: '30'`,
    },
  ] as any;
</script>

<style lang="less" scoped>
  .response-container {
    margin-top: 8px;
    height: calc(100% - 48px);
    .response-header-pre {
      @apply h-full overflow-auto  bg-white;
      .ms-scroll-bar();

      padding: 8px 12px;
      border-radius: var(--border-radius-small);
    }
  }
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  :deep(.arco-tabs-tab) {
    @apply leading-none;
  }
</style>
