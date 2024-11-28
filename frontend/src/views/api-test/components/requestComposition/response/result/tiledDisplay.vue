<template>
  <div class="resContent">
    <!-- 展开折叠列表 -->
    <div v-if="props.requestResult?.responseResult.responseCode" class="tiledList">
      <div v-for="item of props.menuList" :key="item.value" class="menu-list-wrapper">
        <div
          v-if="isShowContent(item.value)"
          :class="`${props.activeType === 'SubRequest' ? 'top-[90px]' : 'top-[38px]'} menu-list`"
        >
          <div class="flex items-center">
            <MsButton
              v-if="!expandIds.includes(item.value)"
              type="icon"
              class="!mr-2 !rounded-full bg-[rgb(var(--primary-1))]"
              size="small"
              @click="changeExpand(item.value)"
            >
              <icon-down :size="8" />
            </MsButton>
            <MsButton
              v-else
              type="icon"
              status="secondary"
              class="!mr-2 !rounded-full !bg-[var(--color-text-n8)]"
              size="small"
              @click="changeExpand(item.value)"
            >
              <icon-right :size="8" />
            </MsButton>
            <span class="menu-title">{{ item.label }}</span>
          </div>
        </div>
        <transition name="fade">
          <div v-show="!expandIds.includes(item.value) && isShowContent(item.value)" class="expandContent">
            <div v-if="item.value === ResponseComposition.BODY">
              <ResBody ref="resBodyRef" :request-result="props.requestResult" @copy="copyScript" />
            </div>
            <div v-if="!expandIds.includes(item.value) && item.value === ResponseComposition.CONSOLE">
              <ResConsole :console="props.console?.trim()" />
            </div>
            <div v-if="!expandIds.includes(item.value) && item.value === ResponseComposition.HEADER" class="">
              <ResValueScript :active-tab="item.value" :request-result="props.requestResult" />
            </div>
            <div v-if="!expandIds.includes(item.value) && item.value === ResponseComposition.REAL_REQUEST">
              <ResValueScript :active-tab="item.value" :request-result="props.requestResult" />
            </div>
            <div v-if="!expandIds.includes(item.value) && item.value === ResponseComposition.EXTRACT">
              <ExtractTable :request-result="props.requestResult" :scroll="{ x: '100%' }" />
            </div>
            <div v-if="!expandIds.includes(item.value) && item.value === ResponseComposition.ASSERTION">
              <ResAssertion :request-result="props.requestResult" :scroll="{ x: '100%' }" />
            </div>
          </div>
        </transition>
        <a-divider v-if="isShowContent(item.value)" type="dashed" :margin="0"></a-divider>
      </div>
    </div>
    <a-empty v-else class="flex h-[150px] items-center gap-[16px] p-[16px]">
      <template #image>
        <img :src="noDataSvg" class="!h-[60px] w-[78px]" />
      </template>
      <div class="flex items-center">
        {{ t('report.detail.api.noResponseContent') }}
      </div>
    </a-empty>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import ResAssertion from './assertionTable.vue';
  import ResBody from './body.vue';
  import ResConsole from './console.vue';
  import ResValueScript from './resValueScript.vue';
  import ExtractTable from '@/views/api-test/components/requestComposition/response/result/extractTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { RequestResult } from '@/models/apiTest/common';
  import { ResponseComposition } from '@/enums/apiEnum';

  const { t } = useI18n();

  const props = defineProps<{
    isExpanded?: boolean;
    requestResult?: RequestResult;
    console?: string;
    environmentName?: string;
    activeType: string; // 激活类型
    menuList: { label: string; value: keyof typeof ResponseComposition }[];
    reportId?: string;
  }>();

  const noDataSvg = `${import.meta.env.BASE_URL}images/noResponse.svg`;
  const expandIds = ref<string[]>([]);
  function changeExpand(value: string) {
    const isExpand = expandIds.value.indexOf(value) > -1;
    if (isExpand) {
      expandIds.value = expandIds.value.filter((item) => item !== value);
    } else {
      expandIds.value.push(value);
    }
  }
  const { copy, isSupported } = useClipboard({ legacy: true });

  const resBodyRef = ref();
  function copyScript() {
    const encodingFormatValue = resBodyRef.value[0].responseEditorRef.getEncodingCode();
    if (isSupported) {
      copy(encodingFormatValue || '');
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  const showBody = computed(() => props.requestResult?.responseResult.body);
  const showHeaders = computed(() => props.requestResult?.responseResult.headers);
  const showRealRequest = computed(
    () => props.requestResult?.responseResult?.headers.trim() || props.requestResult?.url || props.requestResult?.body
  );
  const showExtract = computed(() => (props.requestResult?.responseResult?.extractResults || []).length);

  function isShowContent(key: keyof typeof ResponseComposition) {
    switch (key) {
      case ResponseComposition.BODY:
        return showBody.value;
      case ResponseComposition.HEADER:
        return showHeaders.value;
      case ResponseComposition.REAL_REQUEST:
        return showRealRequest.value;
      case ResponseComposition.CONSOLE:
        return props?.console?.trim() && props.requestResult?.responseResult.responseCode;
      case ResponseComposition.EXTRACT:
        return showExtract.value;
      case ResponseComposition.ASSERTION:
        return (props.requestResult?.responseResult.assertions || []).length;
      default:
        break;
    }
  }
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
  .resContentWrapper {
    position: relative;
    border: 1px solid var(--color-text-n8);
    border-top: none;
    border-radius: 0 0 6px 6px;
    @apply mb-4  p-4;

    background-color: var(--color-text-fff);
    .resContent {
      height: 38px;
      border-radius: 6px;
    }
  }
  .tiledList {
    @apply px-4;
    .menu-list-wrapper {
      .menu-list {
        position: sticky;
        z-index: 999999;
        height: 40px;
        line-height: 40px;
        background: var(--color-text-fff);
        @apply flex items-start justify-between px-4;
        .menu-title {
          @apply font-medium;
        }
      }
      .expandContent {
        background: var(--color-text-n9);
      }
    }
  }
</style>
