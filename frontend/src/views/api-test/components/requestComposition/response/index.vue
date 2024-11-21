<template>
  <div class="response flex h-full min-w-[300px] flex-col">
    <div :class="['response-head', activeLayout === 'vertical' ? 'border-t' : '']">
      <slot name="titleLeft">
        <div class="flex items-center justify-between">
          <template v-if="activeLayout === 'vertical'">
            <MsButton
              v-show="innerIsExpanded"
              type="icon"
              class="!mr-0 !rounded-full bg-[rgb(var(--primary-1))]"
              size="small"
              @click="changeExpand(false)"
            >
              <icon-down :size="8" />
            </MsButton>
            <MsButton
              v-show="!innerIsExpanded"
              type="icon"
              status="secondary"
              class="!mr-0 !rounded-full bg-[rgb(var(--primary-1))]"
              size="small"
              @click="changeExpand(true)"
            >
              <icon-right :size="8" />
            </MsButton>
          </template>
          <div v-if="props.isEdit && props.showResponseResultButton" class="ml-[4px] mr-[24px] flex items-center">
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
            v-model:model-value="activeLayout"
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
      <responseCodeTimeSize :request-result="props.requestResult" />
    </div>
    <a-spin
      v-show="innerIsExpanded"
      :loading="props.loading"
      :class="[isResponseModel ? 'h-[381px] w-full' : 'w-full flex-1 px-[16px] pb-[16px]']"
    >
      <edit
        v-if="props.isEdit && activeResponseType === 'content' && responseDefinition"
        v-model:response-definition="responseDefinition"
        :upload-temp-file-api="props.uploadTempFileApi"
        @change="handleResponseChange"
      />
      <result
        v-else-if="!props.isEdit || (props.isEdit && activeResponseType === 'result')"
        v-model:active-tab="activeTab"
        :loading="props.loading"
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
  import responseCodeTimeSize from './responseCodeTimeSize.vue';
  import result from './result.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';
  import { ResponseBodyFormat, ResponseComposition } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      isExpanded?: boolean;
      isPriorityLocalExec?: boolean;
      requestUrl?: string;
      isHttpProtocol?: boolean;
      requestResult?: RequestResult;
      console?: string;
      hideLayoutSwitch?: boolean; // 隐藏布局切换
      loading?: boolean;
      isEdit?: boolean; // 是否可编辑
      uploadTempFileApi?: (...args: any) => Promise<any>; // 上传临时文件接口
      isDefinition?: boolean;
      isResponseModel?: boolean;
      showEmpty?: boolean;
      showResponseResultButton?: boolean; // 展示执行结果按钮
    }>(),
    {
      isExpanded: true,
      hideLayoutSwitch: false,
      showEmpty: true,
    }
  );
  const emit = defineEmits<{
    (e: 'changeLayout', value: Direction): void;
    (e: 'change'): void;
    (e: 'execute', executeType: 'localExec' | 'serverExec'): void;
  }>();

  const { t } = useI18n();

  const activeLayout = defineModel<Direction>('activeLayout', {
    default: 'vertical',
  });
  const activeTab = defineModel<ResponseComposition>('activeTab', {
    required: true,
  });
  const responseDefinition = defineModel<ResponseItem[]>('responseDefinition', {
    default: [],
  });
  const innerIsExpanded = defineModel<boolean>('isExpanded', {
    default: true,
  });

  function changeExpand(isExpanded: boolean) {
    innerIsExpanded.value = isExpanded;
  }

  watch(
    () => activeLayout.value,
    (val) => {
      if (val === 'horizontal') {
        changeExpand(true);
      }
    }
  );

  watchEffect(() => {
    // 过滤无效数据后的有效响应数据；当接口导入时会存在部分字段为 null 的数据，需要设置默认值
    let hasInvalid = false;
    let validResponseDefinition: ResponseItem[] = [];
    if (responseDefinition.value.length > 0) {
      validResponseDefinition = responseDefinition.value.map((item, i) => {
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
            sendAsBody: false,
          };
          hasInvalid = true;
        }
        if (!item.body.jsonBody) {
          item.body.jsonBody = {
            jsonValue: '',
            enableJsonSchema: true,
            enableTransition: false,
            jsonSchemaTableData: [],
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
      responseDefinition.value = validResponseDefinition;
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

  defineExpose({
    setActiveResponse,
    changeExpand,
  });
</script>

<style lang="less" scoped>
  .response-head {
    @apply flex flex-wrap items-center justify-between border-b;

    padding: 11px 16px;
    border-color: var(--color-text-n8);
    gap: 8px;
  }
</style>
