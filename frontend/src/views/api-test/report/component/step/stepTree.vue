<template>
  <div class="flex h-full flex-col gap-[16px]">
    <a-spin class="w-full" :loading="loading">
      <MsTree
        ref="treeRef"
        v-model:selected-keys="selectedKeys"
        v-model:expanded-keys="innerExpandedKeys"
        v-model:focus-node-key="focusStepKey"
        v-model:data="steps"
        :expand-all="props.expandAll"
        :field-names="{ title: 'name', key: 'stepId', children: 'children' }"
        title-class="step-tree-node-title"
        node-highlight-class="step-tree-node-focus"
        :virtual-list-props="{
          height: 'calc(100vh - 200px)',
          threshold: 200,
          fixedSize: true,
          buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
          isStaticItemHeight: true,
          estimatedSize: 48,
        }"
        :animation="false"
        action-on-node-click="expand"
        disabled-title-tooltip
        block-node
        hide-switcher
        @select="(selectedKeys, node) => handleStepSelect(selectedKeys, node as ScenarioItemType)"
        @expand="handleStepExpand"
        @more-actions-close="() => setFocusNodeKey('')"
      >
        <template #title="step">
          <div class="flex flex-col" @click="handleStop($event, step)">
            <div class="flex w-full items-center gap-[8px]">
              <div
                class="flex h-[16px] min-w-[16px] items-center justify-center rounded-full bg-[var(--color-text-brand)] px-[2px] !text-white"
              >
                {{ step.index }}
              </div>
              <div class="step-node-content">
                <div class="flex max-w-[calc(100%-580px)] flex-1 items-center">
                  <!-- 步骤展开折叠按钮 -->
                  <a-tooltip
                    v-if="step.children?.length > 0"
                    :content="
                      t(step.expanded ? 'apiScenario.collapseStepTip' : 'apiScenario.expandStepTip', {
                        count: step.children.length,
                      })
                    "
                  >
                    <div
                      v-if="!(step.children && step.children.length && showApiType.includes(step.stepType))"
                      class="flex cursor-pointer items-center gap-[2px] text-[var(--color-text-4)]"
                    >
                      <MsIcon :type="'icon-icon_split_turn-down_arrow'" :size="14" />
                      <span class="mx-1"> {{ step.children?.length || 0 }}</span>
                    </div>
                  </a-tooltip>
                  <!-- 展开折叠控制器 -->
                  <div v-show="getShowExpand(step)" class="mx-1" @click.stop="expandHandler(step)">
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
                  <a-tooltip :content="step.name" :disabled="!step.name">
                    <div class="step-name-container" @click="showDetail($event, step)">
                      <div class="step-name-text one-line-text ml-[4px] font-normal">
                        {{ step.name }}
                      </div>
                    </div>
                  </a-tooltip>
                </div>
                <div class="flex flex-1 flex-nowrap justify-end">
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
                      <div class="max-w-[400px] break-words">{{ step.scriptIdentifier }}</div>
                    </template>
                  </a-popover>
                  <div v-show="showStatus(step)" class="flex">
                    <span class="statusCode">
                      <div v-show="step.code" class="mr-2"> {{ t('report.detail.api.statusCode') }}</div>
                      <a-popover position="left" content-class="response-popover-content">
                        <div
                          v-show="step.code"
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

                    <span v-show="step.requestTime !== null" class="resTime">
                      {{ t('report.detail.api.responseTime') }}
                      <a-popover position="left" content-class="response-popover-content">
                        <span class="resTimeCount ml-2">
                          {{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[0] : '-' }}
                          {{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[1] : 'ms' }}
                        </span>
                        <template #content>
                          <span v-show="step.requestTime !== null" class="resTime">
                            {{ t('report.detail.api.responseTime') }}
                            <span class="resTimeCount ml-2">
                              {{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[0] : '-' }}
                              {{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[1] : 'ms' }}
                            </span>
                          </span>
                        </template>
                      </a-popover>
                    </span>
                    <span v-show="step.responseSize !== null" class="resSize">
                      {{ t('report.detail.api.responseSize') }}
                      <a-popover position="left" content-class="response-popover-content">
                        <span class="resTimeCount ml-2">{{ step.responseSize || 0 }} bytes</span>
                        <template #content>
                          <span class="resSize">
                            {{ t('report.detail.api.responseSize') }}
                            <span class="resTimeCount ml-2">{{ step.responseSize || 0 }} bytes</span></span
                          >
                        </template>
                      </a-popover>
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="!step.fold" class="line"></div>
            </div>
            <!-- 折叠展开内容 -->
            <div v-if="showResContent(step)" class="foldContent mt-[18px] pl-2">
              <Suspense>
                <StepDetailContent
                  :mode="props.activeType"
                  :step-item="step"
                  :console="props.console"
                  :is-definition="true"
                  :show-type="props.showType"
                  :is-response-model="true"
                  :report-id="props?.reportId"
                  :steps="steps"
                  :get-report-step-detail="props.getReportStepDetail"
                  hide-response-time
                />
              </Suspense>
            </div>
          </div>
        </template>
        <template v-if="steps.length === 0" #empty>
          <div
            class="rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[8px] text-center text-[12px] leading-[16px] text-[var(--color-text-4)]"
          >
            {{ t('common.noData') }}
          </div>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import { MsTreeExpandedData } from '@/components/business/ms-tree/types';

  import { useI18n } from '@/hooks/useI18n';
  import { findNodeByKey, formatDuration, mapTree } from '@/utils';

  import type { ScenarioItemType } from '@/models/apiTest/report';
  import { ScenarioStepType } from '@/enums/apiEnum';

  const StepDetailContent = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/response/result/index.vue')
  );
  const stepStatus = defineAsyncComponent(() => import('./stepStatus.vue'));
  const ConditionStatus = defineAsyncComponent(() => import('@/views/api-test/report/component/conditionStatus.vue'));
  const MsTag = defineAsyncComponent(() => import('@/components/pure/ms-tag/ms-tag.vue'));
  const MsTree = defineAsyncComponent(() => import('@/components/business/ms-tree/index.vue'));

  const { t } = useI18n();
  const props = defineProps<{
    stepKeyword?: string;
    expandAll?: boolean;
    showType: 'API' | 'CASE';
    activeType: 'tiled' | 'tab';
    console?: string;
    reportId?: string;
    expandedKeys: (string | number)[];
    getReportStepDetail?: (...args: any) => Promise<any>; // 获取步骤的详情内容接口
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
  const showApiType = ref<string[]>([
    ScenarioStepType.API,
    ScenarioStepType.API_CASE,
    ScenarioStepType.CUSTOM_REQUEST,
    ScenarioStepType.SCRIPT,
    ScenarioStepType.TEST_PLAN_API_CASE,
  ]);

  /**
   * 处理步骤展开折叠
   */
  function handleStepExpand(data: MsTreeExpandedData) {
    const realStep = findNodeByKey<ScenarioItemType>(steps.value, data.node?.stepId, 'stepId');
    const isNotAllowExpand =
      data.node?.children && data.node?.children.length && showApiType.value.includes(data.node?.stepType);
    if (isNotAllowExpand && data.node && data.node.children?.length) {
      if (realStep) {
        realStep.stepChildren = cloneDeep(realStep.children);
        realStep.children = [];
      }
    } else if (realStep) {
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
    if (realStep) {
      const isNotAllowExpand =
        realStep.children && realStep?.children.length && showApiType.value.includes(realStep?.stepType);
      if (isNotAllowExpand) {
        realStep.stepChildren = cloneDeep(realStep.children);
        realStep.children = [];
      }
      realStep.fold = !realStep.fold;
    }
  }

  const showCondition = ref<string[]>([
    ScenarioStepType.API,
    ScenarioStepType.API_CASE,
    ScenarioStepType.CUSTOM_REQUEST,
    ScenarioStepType.LOOP_CONTROLLER,
    ScenarioStepType.IF_CONTROLLER,
    ScenarioStepType.ONCE_ONLY_CONTROLLER,
    ScenarioStepType.TEST_PLAN_API_CASE,
    ScenarioStepType.API_SCENARIO,
    ScenarioStepType.CONSTANT_TIMER,
    ScenarioStepType.SCRIPT,
  ]);
  function getShowExpand(item: ScenarioItemType) {
    if (props.showType === 'API') {
      return showApiType.value.includes(item.stepType) && props.activeType === 'tab';
    }
    return props.activeType === 'tab';
  }
  const activeItem = ref();
  function showDetail(event: Event, item: ScenarioItemType) {
    if (props.activeType === 'tab') {
      return;
    }
    if (!showApiType.value.includes(item.stepType)) {
      return;
    }
    const isNotAllowExpand = item.children && item?.children.length && showApiType.value.includes(item.stepType);
    if (isNotAllowExpand) {
      item.stepChildren = cloneDeep(item.children);
      item.children = [];
    }
    activeItem.value = cloneDeep(item);
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
    if (showApiType.value.includes(item.stepType) && item.status && item.status !== 'PENDING') {
      return true;
    }
    return item.children && item.children.length > 0 && item.status && item.status !== 'PENDING';
  }

  function handleStop(event: Event, step: ScenarioItemType) {
    if (step.children && step.children.length && showApiType.value.includes(step.stepType)) {
      event.stopPropagation();
    }
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
      background-color: var(--color-text-fff) !important;
      .arco-tree-node-title {
        background-color: var(--color-text-fff) !important;
      }
    }
    .arco-tree-node-title {
      @apply !cursor-pointer;

      padding: 8px 4px;
      background-color: var(--color-text-fff);
      &:hover {
        background-color: var(--color-text-fff) !important;
      }
      .step-node-content {
        width: calc(100% - 20px);
        gap: 8px;
        @apply flex items-center justify-between;
      }
      .step-name-container {
        @apply flex flex-1 items-center overflow-hidden;

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
      background-color: var(--color-text-n9) !important;
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
  :deep(.step-tree-node-title) {
    @apply w-full;
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
    background-color: var(--color-text-fff);
  }
  :deep(.arco-table-expand-btn) {
    width: 16px;
    height: 16px;
    border: none;
    border-radius: 50%;
    background: var(--color-text-n8) !important;
  }
  .resContentWrapper {
    border-radius: 0 0 6px 6px;
    @apply mb-4 p-4;

    background-color: var(--color-text-fff);
    .resContent {
      height: 38px;
      border-radius: 6px;
    }
  }
  :deep(.step-tree-node-focus) {
    background-color: var(--color-text-n9) !important;
    .arco-tree-node-title {
      background-color: var(--color-text-n9) !important;
    }
    .ms-tree-node-extra {
      @apply !visible !w-auto;
    }
  }
  .line {
    position: absolute;
    top: 48px;
    width: 100%;
    height: 1px;
    background: var(--color-text-n8);
  }
</style>
