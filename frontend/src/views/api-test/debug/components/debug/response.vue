<template>
  <div class="flex h-full min-w-[300px] flex-col">
    <div class="flex flex-wrap items-center justify-between gap-[8px] bg-[var(--color-text-n9)] p-[8px_16px]">
      <div class="flex items-center">
        <template v-if="props.activeLayout === 'vertical'">
          <MsButton
            v-if="props.isExpanded"
            type="icon"
            class="!mr-0 !rounded-full bg-[rgb(var(--primary-1))]"
            @click="emit('changeExpand', false)"
          >
            <icon-down :size="12" />
          </MsButton>
          <MsButton
            v-else
            type="icon"
            status="secondary"
            class="!mr-0 !rounded-full"
            @click="emit('changeExpand', true)"
          >
            <icon-right :size="12" />
          </MsButton>
        </template>
        <div class="ml-[4px] mr-[24px] font-medium">{{ t('apiTestDebug.responseContent') }}</div>
        <a-radio-group
          v-model:model-value="innerLayout"
          type="button"
          size="small"
          @change="(val) => emit('changeLayout', val as Direction)"
        >
          <a-radio value="vertical">{{ t('apiTestDebug.vertical') }}</a-radio>
          <a-radio value="horizontal">{{ t('apiTestDebug.horizontal') }}</a-radio>
        </a-radio-group>
      </div>
      <div v-if="props.response.status" class="flex items-center justify-between gap-[24px]">
        <a-popover position="left" content-class="response-popover-content">
          <div class="text-[rgb(var(--danger-7))]">{{ props.response.status }}</div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.statusCode') }}</div>
              <div class="text-[rgb(var(--danger-7))]">{{ props.response.status }}</div>
            </div>
          </template>
        </a-popover>
        <a-popover position="left" content-class="w-[400px]">
          <div class="one-line-text text-[rgb(var(--success-7))]">{{ props.response.timing }} ms</div>
          <template #content>
            <div class="mb-[8px] flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseTime') }}</div>
              <div class="text-[rgb(var(--success-7))]">{{ props.response.timing }} ms</div>
            </div>
            <responseTimeLine :response-timing="$props.response.timingInfo" />
          </template>
        </a-popover>
        <a-popover position="left" content-class="response-popover-content">
          <div class="one-line-text text-[rgb(var(--success-7))]">{{ props.response.size }} bytes</div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseSize') }}</div>
              <div class="one-line-text text-[rgb(var(--success-7))]">{{ props.response.size }} bytes</div>
            </div>
          </template>
        </a-popover>
        <a-popover position="left" content-class="response-popover-content">
          <div class="text-[var(--color-text-1)]">{{ props.response.env }}</div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.runningEnv') }}</div>
              <div class="text-[var(--color-text-1)]">{{ props.response.env }}</div>
            </div>
          </template>
        </a-popover>
        <a-popover position="left" content-class="response-popover-content">
          <div class="text-[var(--color-text-1)]">{{ props.response.resource }}</div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.resourcePool') }}</div>
              <div class="text-[var(--color-text-1)]">{{ props.response.resource }}</div>
            </div>
          </template>
        </a-popover>
      </div>
    </div>
    <div class="h-[calc(100%-42px)] px-[16px] pb-[16px]">
      <a-tabs v-model:active-key="activeTab" class="no-content">
        <a-tab-pane v-for="item of responseTabList" :key="item.value" :title="item.label" />
      </a-tabs>
      <div class="response-container">
        <MsCodeEditor
          v-if="activeTab === ResponseComposition.BODY"
          :model-value="props.response.body"
          language="json"
          theme="vs"
          height="100%"
          :languages="['json', 'html', 'xml', 'plaintext']"
          :show-full-screen="false"
          :show-theme-change="false"
          show-language-change
          show-charset-change
          read-only
        >
          <template #title>
            <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="copyScript">
              <template #icon>
                <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
              </template>
            </a-button>
          </template>
        </MsCodeEditor>
        <div
          v-else-if="
            activeTab === 'HEADER' || activeTab === 'REAL_REQUEST' || activeTab === 'CONSOLE' || activeTab === 'EXTRACT'
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
  </div>
</template>

<script setup lang="ts">
  import { useClipboard, useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { Direction } from '@/components/pure/ms-split-box/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import responseTimeLine from '@/views/api-test/components/responseTimeLine.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ResponseTiming } from '@/models/apiTest/debug';
  import { ResponseComposition } from '@/enums/apiEnum';

  export interface Response {
    status: number;
    timing: number;
    size: number;
    env: string;
    resource: string;
    body: string;
    header: string;
    content: string;
    console: string;
    extract: Record<string, any>;
    timingInfo: ResponseTiming;
  }

  const props = defineProps<{
    activeTab: keyof typeof ResponseComposition;
    activeLayout: Direction;
    isExpanded: boolean;
    response: Response;
  }>();
  const emit = defineEmits<{
    (e: 'update:activeLayout', value: Direction): void;
    (e: 'update:activeTab', value: keyof typeof ResponseComposition): void;
    (e: 'changeExpand', value: boolean): void;
    (e: 'changeLayout', value: Direction): void;
  }>();

  const { t } = useI18n();

  const innerLayout = useVModel(props, 'activeLayout', emit);
  const activeTab = useVModel(props, 'activeTab', emit);

  const responseTabList = [
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
    {
      label: t('apiTestDebug.extract'),
      value: ResponseComposition.EXTRACT,
    },
    {
      label: t('apiTestDebug.assertion'),
      value: ResponseComposition.ASSERTION,
    },
  ];

  const { copy, isSupported } = useClipboard();

  function copyScript() {
    if (isSupported) {
      copy(props.response.body);
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  function getResponsePreContent(type: keyof typeof ResponseComposition) {
    switch (type) {
      case ResponseComposition.HEADER:
        return props.response.header.trim();
      case ResponseComposition.REAL_REQUEST:
        return props.response.content.trim();
      case ResponseComposition.CONSOLE:
        return props.response.console.trim();
      case ResponseComposition.EXTRACT:
        return Object.keys(props.response.extract)
          .map((e) => `${e}: ${props.response.extract[e]}`)
          .join('\n');
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

<style lang="less">
  .response-popover-content {
    padding: 4px 8px;
    .arco-popover-content {
      @apply mt-0;

      font-size: 14px;
      line-height: 22px;
    }
  }
</style>

<style lang="less" scoped>
  .response-container {
    margin-top: 16px;
    height: calc(100% - 66px);
    .response-header-pre {
      @apply h-full overflow-auto  bg-white;
      .ms-scroll-bar();

      padding: 12px;
      border-radius: var(--border-radius-small);
    }
  }
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
</style>
