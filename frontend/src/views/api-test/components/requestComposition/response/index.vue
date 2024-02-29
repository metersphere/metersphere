<template>
  <div class="flex h-full min-w-[300px] flex-col">
    <div :class="['response-head', props.isExpanded ? '' : 'border-t']">
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
            class="!mr-0 !rounded-full"
            size="small"
            @click="emit('changeExpand', true)"
          >
            <icon-right :size="8" />
          </MsButton>
        </template>
        <div
          v-if="props.isEdit && props.response.requestResults[0]?.responseResult?.responseCode"
          class="ml-[4px] flex items-center"
        >
          <MsButton
            type="text"
            :class="['font-medium', activeResponseType === 'content' ? '' : '!text-[var(--color-text-n4)]']"
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
      <div
        v-if="props.response.requestResults[0]?.responseResult?.responseCode"
        class="flex items-center justify-between gap-[24px]"
      >
        <a-popover position="left" content-class="response-popover-content">
          <div :style="{ color: statusCodeColor }">
            {{ props.response.requestResults[0].responseResult.responseCode }}
          </div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.statusCode') }}</div>
              <div :style="{ color: statusCodeColor }">
                {{ props.response.requestResults[0].responseResult.responseCode }}
              </div>
            </div>
          </template>
        </a-popover>
        <a-popover position="left" content-class="w-[400px]">
          <div class="one-line-text text-[rgb(var(--success-7))]">
            {{ props.response.requestResults[0].responseResult.responseTime }} ms
          </div>
          <template #content>
            <div class="mb-[8px] flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseTime') }}</div>
              <div class="text-[rgb(var(--success-7))]">
                {{ props.response.requestResults[0].responseResult.responseTime }} ms
              </div>
            </div>
            <responseTimeLine :response-timing="timingInfo" />
          </template>
        </a-popover>
        <a-popover position="left" content-class="response-popover-content">
          <div class="one-line-text text-[rgb(var(--success-7))]">
            {{ props.response.requestResults[0].responseResult.responseSize }} bytes
          </div>
          <template #content>
            <div class="flex items-center gap-[8px] text-[14px]">
              <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseSize') }}</div>
              <div class="one-line-text text-[rgb(var(--success-7))]">
                {{ props.response.requestResults[0].responseResult.responseSize }} bytes
              </div>
            </div>
          </template>
        </a-popover>
        <!-- <a-popover position="left" content-class="response-popover-content">
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
        </a-popover> -->
      </div>
    </div>
    <a-spin :loading="props.loading" class="h-[calc(100%-42px)] w-full px-[18px] pb-[18px]">
      <div v-if="props.isEdit" class="my-[8px] w-full">
        <MsEditableTab
          v-model:active-tab="activeResponse"
          v-model:tabs="responseTabs"
          at-least-one
          hide-more-action
          @add="addResponseTab"
        >
          <template #label="{ tab }">
            <div class="response-tab">
              <div v-if="tab.isDefault" class="response-tab-default-icon"></div>
              {{ tab.label }}({{ tab.code }})
              <MsMoreAction
                :list="
                  tab.isDefault
                    ? tabMoreActionList.filter((e) => e.eventTag !== 'setDefault' && e.eventTag !== 'delete')
                    : tabMoreActionList
                "
                class="response-more-action"
                @select="(e) => handleMoreActionSelect(e, tab as ResponseItem)"
              />
              <popConfirm
                v-model:visible="tab.showRenamePopConfirm"
                mode="tabRename"
                :field-config="{ field: tab.label }"
                :all-names="responseTabs.map((e) => e.label)"
                @rename-finish="(val) => (tab.label = val)"
              >
                <span :id="`renameSpan${tab.id}`" class="relative"></span>
              </popConfirm>
              <a-popconfirm
                v-model:popup-visible="tab.showPopConfirm"
                position="bottom"
                content-class="w-[300px]"
                :ok-text="t('common.confirm')"
                :popup-offset="20"
                @ok="() => handleDeleteResponseTab(tab.id)"
              >
                <template #icon>
                  <icon-exclamation-circle-fill class="!text-[rgb(var(--danger-6))]" />
                </template>
                <template #content>
                  <div class="font-semibold text-[var(--color-text-1)]">
                    {{ t('apiTestManagement.confirmDelete', { name: tab.label }) }}
                  </div>
                </template>
                <div class="relative"></div>
              </a-popconfirm>
            </div>
          </template>
        </MsEditableTab>
      </div>
      <result
        v-if="!props.isEdit || (props.isEdit && activeResponseType === 'result')"
        v-model:activeTab="activeTab"
        :response="props.response"
        :request="props.request"
      />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import type { Direction } from '@/components/pure/ms-split-box/index.vue';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import result from './result.vue';
  import popConfirm from '@/views/api-test/components/popConfirm.vue';
  import responseTimeLine from '@/views/api-test/components/responseTimeLine.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ResponseResult } from '@/models/apiTest/common';
  import { ResponseComposition } from '@/enums/apiEnum';

  import type { RequestParam } from '../index.vue';

  const props = withDefaults(
    defineProps<{
      activeTab: keyof typeof ResponseComposition;
      activeLayout?: Direction;
      isExpanded: boolean;
      response: ResponseResult;
      request?: RequestParam;
      hideLayoutSwitch?: boolean; // 隐藏布局切换
      loading?: boolean;
      isEdit?: boolean; // 是否可编辑
    }>(),
    {
      activeLayout: 'vertical',
      hideLayoutSwitch: false,
    }
  );
  const emit = defineEmits<{
    (e: 'update:activeLayout', value: Direction): void;
    (e: 'update:activeTab', value: keyof typeof ResponseComposition): void;
    (e: 'changeExpand', value: boolean): void;
    (e: 'changeLayout', value: Direction): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const innerLayout = useVModel(props, 'activeLayout', emit);
  const activeTab = useVModel(props, 'activeTab', emit);
  // 响应时间信息
  const timingInfo = computed(() => {
    const {
      dnsLookupTime,
      downloadTime,
      latency,
      responseTime,
      socketInitTime,
      sslHandshakeTime,
      tcpHandshakeTime,
      transferStartTime,
    } = props.response.requestResults[0].responseResult;
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
  });
  // 响应状态码对应颜色
  const statusCodeColor = computed(() => {
    const code = props.response.requestResults[0].responseResult.responseCode;
    if (code >= 200 && code < 300) {
      return 'rgb(var(--success-7)';
    }
    if (code >= 300 && code < 400) {
      return 'rgb(var(--warning-7)';
    }
    return 'rgb(var(--danger-7)';
  });

  /** 响应内容编辑状态逻辑 */

  export interface ResponseItem extends TabItem {
    isDefault?: boolean; // 是否是默认tab
    code: number; // 状态码
    showPopConfirm?: boolean; // 是否显示确认弹窗
    showRenamePopConfirm?: boolean; // 是否显示重命名确认弹窗
  }

  const activeResponseType = ref<'content' | 'result'>('content');

  function setActiveResponse(val: 'content' | 'result') {
    activeResponseType.value = val;
  }

  const responseTabs = ref<ResponseItem[]>([
    {
      id: new Date().getTime(),
      label: t('apiTestManagement.response'),
      closable: false,
      code: 200,
      isDefault: true,
      showPopConfirm: false,
      showRenamePopConfirm: false,
    },
  ]);
  const activeResponse = ref<ResponseItem>(responseTabs.value[0]);

  function addResponseTab(defaultProps?: Partial<ResponseItem>) {
    responseTabs.value.push({
      label: t('apiTestManagement.response', { count: responseTabs.value.length + 1 }),
      code: 200,
      ...defaultProps,
      id: new Date().getTime(),
      isDefault: false,
      showPopConfirm: false,
      showRenamePopConfirm: false,
    });
    activeResponse.value = responseTabs.value[responseTabs.value.length - 1];
    emit('change');
  }

  const tabMoreActionList: ActionsItem[] = [
    {
      label: t('apiTestManagement.setDefault'),
      eventTag: 'setDefault',
    },
    {
      label: t('common.rename'),
      eventTag: 'rename',
    },
    {
      label: t('common.copy'),
      eventTag: 'copy',
    },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      eventTag: 'delete',
      danger: true,
    },
  ];
  const renameValue = ref('');

  function handleMoreActionSelect(e: ActionsItem, _tab: ResponseItem) {
    switch (e.eventTag) {
      case 'setDefault':
        responseTabs.value = responseTabs.value.map((tab) => {
          tab.isDefault = _tab.id === tab.id;
          return tab;
        });
        break;
      case 'rename':
        renameValue.value = _tab.label || '';
        document.querySelector(`#renameSpan${_tab.id}`)?.dispatchEvent(new Event('click'));
        break;
      case 'copy':
        addResponseTab({ ..._tab, label: `${_tab.label}-Copy` });
        break;
      case 'delete':
        _tab.showPopConfirm = true;
        break;
      default:
        break;
    }
  }

  function handleDeleteResponseTab(id: number | string) {
    responseTabs.value = responseTabs.value.filter((tab) => tab.id !== id);
    if (id === activeResponse.value.id) {
      [activeResponse.value] = responseTabs.value;
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
</style>

<style lang="less" scoped>
  .response-head {
    @apply flex flex-wrap items-center justify-between border-b;

    padding: 8px 16px;
    border-color: var(--color-text-n8);
    gap: 8px;
  }
  .response-tab {
    @apply flex items-center;
    .response-tab-default-icon {
      @apply rounded-full;

      margin-right: 4px;
      width: 16px;
      height: 16px;
      background: url('@/assets/svg/icons/default.svg') no-repeat;
      background-size: contain;
      box-shadow: 0 0 7px 0 rgb(15 0 78 / 9%);
    }
    :deep(.response-more-action) {
      margin-left: 4px;
      .more-icon-btn {
        @apply invisible;
      }
    }
    &:hover {
      :deep(.response-more-action) {
        .more-icon-btn {
          @apply visible;
        }
      }
    }
  }
</style>
