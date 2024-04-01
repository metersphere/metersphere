<template>
  <div class="response flex h-full min-w-[300px] flex-col">
    <div :class="['response-head', props.isExpanded ? '' : 'border-t']">
      <slot name="titleLeft">
        <div class="flex items-center justify-between">
          <template v-if="props.activeLayout === 'vertical'">
            <MsButton
              v-if="props.isExpanded"
              type="icon"
              class="!mr-0 !rounded-full bg-[rgb(var(--primary-1))]"
              size="small"
              @click="emit('changeExpand', false)"
            >
              <icon-down :size="8" />
            </MsButton>
            <MsButton
              v-else
              type="icon"
              status="secondary"
              class="!mr-0 !rounded-full bg-[rgb(var(--primary-1))]"
              size="small"
              @click="emit('changeExpand', true)"
            >
              <icon-right :size="8" />
            </MsButton>
          </template>
          <div
            v-if="props.isEdit && props.requestResult?.responseResult?.responseCode"
            class="ml-[4px] flex items-center"
          >
            <MsButton
              type="text"
              :class="['font-medium', activeResponseType === 'content' ? '' : '!text-[var(--color-text-n4)]', '!mr-0']"
              @click="() => setActiveResponse('content')"
            >
              {{ t('apiTestDebug.responseContent') }}
            </MsButton>
            <a-divider direction="vertical" :margin="4"></a-divider>
            <MsButton
              type="text"
              :class="['font-medium', activeResponseType === 'result' ? '' : '!text-[var(--color-text-n4)]']"
              @click="() => setActiveResponse('result')"
            >
              {{ t('apiTestManagement.executeResult') }}
            </MsButton>
          </div>
          <div v-else class="ml-[4px] mr-[24px] font-medium">{{ t('apiTestDebug.responseContent') }}</div>
          <a-radio-group
            v-if="!props.hideLayoutSwitch"
            v-model:model-value="innerLayout"
            type="button"
            size="small"
            @change="(val) => emit('changeLayout', val as Direction)"
          >
            <a-radio value="vertical">{{ t('apiTestDebug.vertical') }}</a-radio>
            <a-radio value="horizontal">{{ t('apiTestDebug.horizontal') }}</a-radio>
          </a-radio-group>
        </div>
      </slot>
      <slot name="titleRight"></slot>
      <div
        v-if="props.requestResult?.responseResult?.responseCode"
        class="flex items-center justify-between gap-[24px] text-[14px]"
      >
        <a-popover position="left" content-class="response-popover-content">
          <div class="one-line-text max-w-[200px]" :style="{ color: statusCodeColor }">
            {{ props.requestResult.responseResult.responseCode }}
          </div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.statusCode') }}</div>
              <div :style="{ color: statusCodeColor }">
                {{ props.requestResult.responseResult.responseCode }}
              </div>
            </div>
          </template>
        </a-popover>
        <a-popover position="left" content-class="w-[400px]">
          <div class="one-line-text text-[rgb(var(--success-7))]"> {{ timingInfo?.responseTime }} ms </div>
          <template #content>
            <div class="mb-[8px] flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseTime') }}</div>
              <div class="text-[rgb(var(--success-7))]"> {{ timingInfo?.responseTime }} ms </div>
            </div>
            <responseTimeLine v-if="timingInfo" :response-timing="timingInfo" />
          </template>
        </a-popover>
        <a-popover position="left" content-class="response-popover-content">
          <div class="one-line-text text-[rgb(var(--success-7))]">
            {{ props.requestResult.responseResult.responseSize }} bytes
          </div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseSize') }}</div>
              <div class="one-line-text text-[rgb(var(--success-7))]">
                {{ props.requestResult.responseResult.responseSize }} bytes
              </div>
            </div>
          </template>
        </a-popover>
        <!-- <a-popover position="left" content-class="response-popover-content">
          <div class="text-[var(--color-text-1)]">{{ props.request.response.env }}</div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.runningEnv') }}</div>
              <div class="text-[var(--color-text-1)]">{{ props.request.response.env }}</div>
            </div>
          </template>
        </a-popover>
        <a-popover position="left" content-class="response-popover-content">
          <div class="text-[var(--color-text-1)]">{{ props.request.response.resource }}</div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.resourcePool') }}</div>
              <div class="text-[var(--color-text-1)]">{{ props.request.response.resource }}</div>
            </div>
          </template>
        </a-popover> -->
      </div>
    </div>
    <a-spin
      :loading="props.loading"
      :class="[isResponseModel ? 'h-[381px] w-full' : 'h-[calc(100%-35px)] w-full px-[18px] pb-[18px]']"
    >
      <edit
        v-if="props.isEdit && activeResponseType === 'content' && innerResponseDefinition"
        v-model:response-definition="innerResponseDefinition"
        :upload-temp-file-api="props.uploadTempFileApi"
        @change="handleResponseChange"
      />
      <result
        v-else-if="!props.isEdit || (props.isEdit && activeResponseType === 'result')"
        v-model:active-tab="innerActiveTab"
        :request-result="props.requestResult"
        :console="props.console"
        :is-http-protocol="props.isHttpProtocol"
        :is-priority-local-exec="props.isPriorityLocalExec"
        :request-url="props.requestUrl"
        :is-definition="props.isDefinition"
        :show-empty="props.showEmpty"
        @execute="emit('execute', props.isPriorityLocalExec ? 'localExec' : 'serverExec')"
      />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { Direction } from '@/components/pure/ms-split-box/index.vue';
  import edit, { ResponseItem } from './edit.vue';
  import result from './result.vue';
  import responseTimeLine from '@/views/api-test/components/responseTimeLine.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';
  import { ResponseBodyFormat, ResponseComposition } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      activeTab: ResponseComposition;
      isExpanded?: boolean;
      isPriorityLocalExec?: boolean;
      requestUrl?: string;
      isHttpProtocol?: boolean;
      activeLayout?: Direction;
      responseDefinition?: ResponseItem[];
      requestResult?: RequestResult;
      console?: string;
      hideLayoutSwitch?: boolean; // 隐藏布局切换
      loading?: boolean;
      isEdit?: boolean; // 是否可编辑
      uploadTempFileApi?: (...args) => Promise<any>; // 上传临时文件接口
      isDefinition?: boolean;
      isResponseModel?: boolean;
      showEmpty?: boolean;
    }>(),
    {
      isExpanded: true,
      activeLayout: 'vertical',
      hideLayoutSwitch: false,
      showEmpty: true,
    }
  );
  const emit = defineEmits<{
    (e: 'changeExpand', value: boolean): void;
    (e: 'changeLayout', value: Direction): void;
    (e: 'change'): void;
    (e: 'execute', executeType: 'localExec' | 'serverExec'): void;
  }>();

  const { t } = useI18n();

  const innerLayout = defineModel<Direction>('activeLayout', {
    default: 'vertical',
  });
  const innerActiveTab = defineModel<ResponseComposition>('activeTab', {
    required: true,
  });
  const innerResponseDefinition = defineModel<ResponseItem[]>('responseDefinition', {
    default: [],
  });
  // 响应时间信息
  const timingInfo = computed(() => {
    if (props.requestResult) {
      const {
        dnsLookupTime,
        downloadTime,
        latency,
        responseTime,
        socketInitTime,
        sslHandshakeTime,
        tcpHandshakeTime,
        transferStartTime,
      } = props.requestResult.responseResult;
      return {
        dnsLookupTime,
        tcpHandshakeTime,
        sslHandshakeTime,
        socketInitTime,
        latency,
        downloadTime,
        transferStartTime,
        responseTime,
      };
    }
    return null;
  });
  // 响应状态码对应颜色
  const statusCodeColor = computed(() => {
    if (props.requestResult) {
      const code = props.requestResult.responseResult.responseCode;
      if (code >= 200 && code < 300) {
        return 'rgb(var(--success-7)';
      }
      if (code >= 300 && code < 400) {
        return 'rgb(var(--warning-7)';
      }
      return 'rgb(var(--danger-7)';
    }
    return '';
  });
  watchEffect(() => {
    // 过滤无效数据后的有效响应数据；当接口导入时会存在部分字段为 null 的数据，需要设置默认值
    let hasInvalid = false;
    let validResponseDefinition: ResponseItem[] = [];
    if (props.responseDefinition && props.responseDefinition.length > 0) {
      validResponseDefinition = props.responseDefinition.map((item, i) => {
        // 某些字段在导入时接口返回 null，需要设置默认值
        if (!item.headers) {
          item.headers = [];
          hasInvalid = true;
        }
        if (!item.id) {
          item.id = new Date().getTime() + i;
          hasInvalid = true;
        }
        if (item.body.bodyType === ResponseBodyFormat.NONE) {
          item.body.bodyType = ResponseBodyFormat.RAW;
          hasInvalid = true;
        }
        if (!item.body.binaryBody) {
          item.body.binaryBody = {
            description: '',
            file: undefined,
          };
          hasInvalid = true;
        }
        if (!item.body.jsonBody) {
          item.body.jsonBody = {
            jsonValue: '',
            enableJsonSchema: false,
            enableTransition: false,
          };
          if (!item.body.xmlBody) {
            item.body.xmlBody = {
              value: '',
            };
          }
          if (!item.body.rawBody) {
            item.body.rawBody = {
              value: '',
            };
          }
          hasInvalid = true;
        }
        return item;
      });
    }
    if (hasInvalid) {
      innerResponseDefinition.value = validResponseDefinition;
    }
  });

  function handleResponseChange() {
    emit('change');
  }

  const activeResponseType = ref<'content' | 'result'>('content');

  function setActiveResponse(val: 'content' | 'result') {
    activeResponseType.value = val;
  }

  watch(
    () => props.requestResult,
    (requestResult) => {
      if (requestResult?.responseResult?.responseCode) {
        setActiveResponse('result');
      }
    }
  );
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
  .response-head {
    @apply flex flex-wrap items-center justify-between border-b;

    padding: 8px 16px;
    border-color: var(--color-text-n8);
    gap: 8px;
  }
</style>
