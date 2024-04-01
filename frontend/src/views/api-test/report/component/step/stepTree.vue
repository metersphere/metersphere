<template>
  <div class="flex h-full flex-col gap-[16px]">
    <a-spin class="w-full" :loading="loading">
      <!--  不做虚拟滚动  :virtual-list-props="{
          height: `calc(100vh - 454px)`,
          threshold: 20,
          fixedSize: true,
          buffer: 15,
        }" -->
      <MsTree
        ref="treeRef"
        v-model:selected-keys="selectedKeys"
        v-model:expanded-keys="innerExpandedKeys"
        v-model:data="steps"
        :expand-all="props.expandAll"
        :field-names="{ title: 'name', key: 'stepId', children: 'children' }"
        title-class="step-tree-node-title"
        node-highlight-class="step-tree-node-focus"
        action-on-node-click="expand"
        disabled-title-tooltip
        block-node
        @select="(selectedKeys, node) => handleStepSelect(selectedKeys, node as ScenarioItemType)"
        @expand="handleStepExpand"
        @more-actions-close="() => setFocusNodeKey('')"
      >
        <template #title="step">
          <div class="flex w-full items-center gap-[8px]">
            <div
              class="flex h-[16px] min-w-[16px] items-center justify-center rounded-full bg-[var(--color-text-brand)] px-[2px] !text-white"
            >
              {{ step.sort }}
            </div>
            <div class="step-node-content flex justify-between">
              <div class="flex flex-1 items-center">
                <!-- 步骤展开折叠按钮 -->
                <a-tooltip
                  v-if="step.children?.length > 0"
                  :content="
                    t(step.expanded ? 'apiScenario.collapseStepTip' : 'apiScenario.expandStepTip', {
                      count: step.children.length,
                    })
                  "
                >
                  <div class="flex cursor-pointer items-center gap-[2px] text-[var(--color-text-4)]">
                    <MsIcon
                      :type="step.expanded ? 'icon-icon_split_turn-down_arrow' : 'icon-icon_split-turn-down-left'"
                      :size="14"
                    />
                    <span class="mx-1"> {{ step.children?.length || 0 }}</span>
                  </div>
                </a-tooltip>
                <!-- 展开折叠控制器 -->
                <div v-if="getShowExpand(step)" class="mx-1" @click.stop="expandHandler(step)">
                  <span v-if="step.fold" class="collapsebtn flex items-center justify-center">
                    <icon-right class="text-[var(--color-text-4)]" :style="{ 'font-size': '12px' }" />
                  </span>
                  <span v-else class="expand flex items-center justify-center">
                    <icon-down class="text-[rgb(var(--primary-6))]" :style="{ 'font-size': '12px' }" />
                  </span>
                </div>
                <div v-if="props.showType === 'API' && showCondition.includes(step.stepType)" class="flex-shrink-0">
                  <ConditionStatus class="mx-1" :status="step.stepType || ''" />
                </div>

                <a-tooltip :content="step.name" position="tl">
                  <div class="step-name-container w-full flex-grow" @click.stop="showDetail(step)">
                    <div class="one-line-text mx-[4px] max-w-[150px] text-[var(--color-text-1)]">
                      {{ step.name }}
                    </div>
                  </div>
                </a-tooltip>
              </div>
              <div class="flex">
                <stepStatus :status="step.status || 'PENDING'" />
                <!-- 脚本报错 -->
                <a-popover position="left" content-class="response-popover-content">
                  <MsTag
                    v-if="step.scriptIdentifier"
                    type="primary"
                    theme="light"
                    :self-style="{
                      color: 'rgb(var(--primary-3))',
                      background: 'rgb(var(--primary-1))',
                    }"
                  >
                    <template #icon>
                      <MsIcon type="icon-icon_info_outlined" class="mx-1 !text-[rgb(var(--primary-3))]" size="16" />
                      <span class="!text-[rgb(var(--primary-3))]">{{ t('report.detail.api.scriptErrorTip') }}</span>
                    </template>
                  </MsTag>
                  <template #content>
                    <div class="max-w-[400px]">{{ step.scriptIdentifier }}</div>
                  </template>
                </a-popover>
                <div v-show="showStatus(step)" class="flex">
                  <span class="statusCode mx-2">
                    <div v-if="step.code" class="mr-2"> {{ t('report.detail.api.statusCode') }}</div>
                    <a-popover position="left" content-class="response-popover-content">
                      <div
                        v-if="step.code"
                        class="one-line-text max-w-[200px]"
                        :style="{ color: statusCodeColor(step.code) }"
                      >
                        {{ step.code || '-' }}
                      </div>
                      <template #content>
                        <div class="flex items-center gap-[8px] text-[14px]">
                          <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.statusCode') }}</div>
                          <div :style="{ color: statusCodeColor(step.code) }">
                            {{ step.code || '-' }}
                          </div>
                        </div>
                      </template>
                    </a-popover>
                  </span>

                  <span v-if="step.requestTime !== null" class="resTime">
                    {{ t('report.detail.api.responseTime') }}
                    <a-popover position="left" content-class="response-popover-content">
                      <span class="resTimeCount ml-2"
                        >{{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[0] : '-'
                        }}{{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[1] : 'ms' }}</span
                      >
                      <template #content>
                        <span v-if="step.requestTime !== null" class="resTime">
                          {{ t('report.detail.api.responseTime') }}
                          <span class="resTimeCount ml-2"
                            >{{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[0] : '-'
                            }}{{
                              step.requestTime !== null ? formatDuration(step.requestTime).split('-')[1] : 'ms'
                            }}</span
                          ></span
                        >
                      </template>
                    </a-popover></span
                  >
                  <span v-if="step.responseSize !== null" class="resSize">
                    {{ t('report.detail.api.responseSize') }}
                    <a-popover position="left" content-class="response-popover-content">
                      <span class="resTimeCount ml-2">{{ step.responseSize || 0 }} bytes</span>
                      <template #content>
                        <span class="resSize">
                          {{ t('report.detail.api.responseSize') }}
                          <span class="resTimeCount ml-2">{{ step.responseSize || 0 }} bytes</span></span
                        >
                      </template>
                    </a-popover></span
                  >
                </div>
              </div>
            </div>
            <div v-if="!step.fold" class="line"></div>
          </div>
          <!-- 折叠展开内容 v-if="showResContent(step)" -->
          <div v-if="showResContent(step)" class="foldContent mt-4 pl-2">
            <a-scrollbar
              :style="{
                overflow: 'auto',
                height: 'calc(100vh - 540px)',
                width: '100%',
              }"
            >
              <StepDetailContent
                :mode="props.activeType"
                :step-item="step"
                :console="props.console"
                :is-definition="true"
                :environment-name="props.environmentName"
                :show-type="props.showType"
                :is-response-model="true"
                :report-id="props?.reportId"
                :steps="steps"
              />
            </a-scrollbar>
          </div>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeExpandedData } from '@/components/business/ms-tree/types';
  import stepStatus from './stepStatus.vue';
  import StepDetailContent from '@/views/api-test/components/requestComposition/response/result/index.vue';
  import ConditionStatus from '@/views/api-test/report/component/conditionStatus.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { findNodeByKey, formatDuration, mapTree } from '@/utils';

  import type { ScenarioItemType } from '@/models/apiTest/report';
  import { ScenarioStepType } from '@/enums/apiEnum';

  const { t } = useI18n();
  const props = defineProps<{
    stepKeyword?: string;
    expandAll?: boolean;
    showType: 'API' | 'CASE';
    activeType: 'tiled' | 'tab';
    console?: string;
    environmentName?: string;
    reportId?: string;
    expandedKeys: (string | number)[];
  }>();
  const loading = ref(false);

  const treeRef = ref<InstanceType<typeof MsTree>>();

  const emit = defineEmits(['expand', 'detail']);

  const steps = defineModel<ScenarioItemType[]>('steps', {
    required: true,
  });
  const innerExpandedKeys = defineModel<(string | number)[]>('expandedKeys', {
    required: false,
  });

  /**
   * 处理步骤展开折叠
   */
  function handleStepExpand(data: MsTreeExpandedData) {
    const realStep = findNodeByKey<ScenarioItemType>(steps.value, data.node?.stepId, 'stepId');
    if (realStep) {
      realStep.expanded = !realStep.expanded;
    }
  }

  const selectedKeys = ref<(string | number)[]>([]);
  const focusStepKey = ref<string>('');

  function handleStepSelect(_selectedKeys: Array<string | number>, step: ScenarioItemType) {
    const offspringIds: string[] = [];
    mapTree(step.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    selectedKeys.value = [step.stepId, ...offspringIds];
  }

  function setFocusNodeKey(id: string) {
    focusStepKey.value = id || '';
  }

  function expandHandler(item: ScenarioItemType) {
    const realStep = findNodeByKey<ScenarioItemType>(steps.value, item.stepId, 'stepId');
    // TODO
    if (realStep) {
      realStep.fold = !realStep.fold;
    }
  }
  const showApiType = ref<string[]>([ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.CUSTOM_REQUEST]);
  const showCondition = ref<string[]>([
    ScenarioStepType.API,
    ScenarioStepType.API_CASE,
    ScenarioStepType.CUSTOM_REQUEST,
    ScenarioStepType.LOOP_CONTROLLER,
    ScenarioStepType.IF_CONTROLLER,
    ScenarioStepType.ONCE_ONLY_CONTROLLER,
  ]);
  function getShowExpand(item: ScenarioItemType) {
    if (props.showType === 'API') {
      return showApiType.value.includes(item.stepType) && props.activeType === 'tab';
    }
    return props.activeType === 'tab';
  }
  const activeItem = ref();
  function showDetail(item: ScenarioItemType) {
    if (props.activeType === 'tab') {
      return;
    }
    if (!showApiType.value.includes(item.stepType)) {
      return;
    }
    activeItem.value = item;
    emit('detail', activeItem.value);
  }

  // 响应状态码对应颜色
  function statusCodeColor(code: string) {
    if (code) {
      const resCode = Number(code);
      if (resCode >= 200 && resCode < 300) {
        return 'rgb(var(--success-7)';
      }
      if (resCode >= 300 && resCode < 400) {
        return 'rgb(var(--warning-7)';
      }
      return 'rgb(var(--danger-7)';
    }
    return '';
  }

  function showResContent(item: ScenarioItemType) {
    if (props.showType === 'API') {
      return showApiType.value.includes(item.stepType) && props.activeType === 'tab' && !item.fold;
    }
    return props.activeType === 'tab' && !item.fold;
  }

  function showStatus(item: ScenarioItemType) {
    if (showApiType.value.includes(item.stepType)) {
      return true;
    }
    return item.children && item.children.length > 0;
  }
</script>

<style scoped lang="less">
  // 循环生成树的左边距样式 TODO:transform性能更高以及保留步骤完整宽度，需要加横向滚动
  .loop-levels(@index, @max) when (@index <= @max) {
    :deep(.arco-tree-node[data-level='@{index}']) {
      margin-left: @index * 32px;
    }
    .loop-levels(@index + 1, @max); // 下个层级
  }
  .loop-levels(0, 99); // 最大层级
  :deep(.arco-tree-node) {
    padding: 0 8px;
    min-width: 1000px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-medium) !important;
    &:not(:first-child) {
      margin-top: 4px;
    }
    &:hover {
      background-color: white !important;
      .arco-tree-node-title {
        background-color: white !important;
      }
    }
    .arco-tree-node-title {
      @apply !cursor-pointer bg-white;

      padding: 12px 4px;
      &:hover {
        background-color: white !important;
      }
      .step-node-content {
        @apply flex w-full flex-1 items-center;

        gap: 8px;
        margin-right: 6px;
      }
      .step-name-container {
        @apply flex items-center;

        margin-right: 16px;
        &:hover {
          .edit-script-name-icon {
            @apply visible;
          }
        }
        .edit-script-name-icon {
          @apply invisible cursor-pointer;

          color: rgb(var(--primary-5));
        }
      }
      .arco-tree-node-title-text {
        @apply flex-1;
      }
    }
    .arco-tree-node-indent {
      @apply hidden;
    }
    .arco-tree-node-switcher {
      @apply hidden;
    }
    .arco-tree-node-drag-icon {
      @apply hidden;
    }
    .ms-tree-node-extra {
      gap: 4px;
      background-color: white !important;
    }
  }
  :deep(.arco-tree-node-selected) {
    .arco-tree-node-title {
      .step-tree-node-title {
        font-weight: 400;
        color: var(--color-text-1);
      }
    }
  }
  :deep(.step-tree-node-focus) {
    background-color: white !important;
    .arco-tree-node-title {
      background-color: white;
    }
  }
  .resTime,
  .resSize,
  .statusCode {
    margin-right: 8px;
    color: var(--color-text-4);
    @apply flex;
    .resTimeCount {
      color: rgb(var(--success-6));
    }
    .code {
      display: inline-block;
      max-width: 60px;
      text-overflow: ellipsis;
      word-break: keep-all;
      white-space: nowrap;
    }
  }
  :deep(.expand) {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: rgb(var(--primary-1));
  }
  :deep(.collapsebtn) {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: var(--color-text-n8) !important;
    @apply bg-white;
  }
  :deep(.arco-table-expand-btn) {
    width: 16px;
    height: 16px;
    border: none;
    border-radius: 50%;
    background: var(--color-text-n8) !important;
  }
  .resContentWrapper {
    border-top: 1px solid red;
    border-radius: 0 0 6px 6px;
    @apply mb-4 bg-white p-4;
    .resContent {
      height: 38px;
      border-radius: 6px;
    }
  }
  :deep(.ms-tree-container .ms-tree .arco-tree-node .arco-tree-node-title) {
    background: white;
  }
  :deep(.ms-tree-container .ms-tree .arco-tree-node-selected) {
    background: white;
  }
  .line {
    position: absolute;
    top: 48px;
    width: 100%;
    height: 1px;
    background: var(--color-text-n8);
  }
</style>
