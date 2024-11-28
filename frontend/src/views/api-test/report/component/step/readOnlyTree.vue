<template>
  <div class="flex h-full flex-col gap-[16px]">
    <MsTree
      ref="treeRef"
      v-model:selected-keys="selectedKeys"
      v-model:expanded-keys="innerExpandedKeys"
      v-model:data="steps"
      :field-names="{ title: 'name', key: 'stepId', children: 'children' }"
      title-class="step-tree-node-title"
      node-highlight-class="step-tree-node-focus"
      :animation="false"
      action-on-node-click="expand"
      disabled-title-tooltip
      block-node
      hide-switcher
    >
      <template #title="step">
        <div class="flex flex-col">
          <div class="flex w-full items-center gap-[8px]">
            <div
              class="flex h-[16px] min-w-[16px] items-center justify-center rounded-full bg-[var(--color-text-brand)] px-[2px] !text-white"
            >
              {{ step.sort }}
            </div>
            <div class="step-node-content">
              <div class="flex flex-1 items-center">
                <!-- 步骤展开折叠按钮 -->
                <!-- <div
                    v-if="step.children && step.children.length"
                    class="flex cursor-pointer items-center gap-[2px] text-[var(--color-text-4)]"
                  >
                    <MsIcon :type="'icon-icon_split_turn-down_arrow'" :size="14" />
                    <span class="mx-1"> {{ step.children?.length || 0 }}</span>
                  </div> -->
                <!-- 展开折叠控制器 -->
                <div v-show="getShowExpand(step)" class="mx-1">
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
                <div
                  class="step-name-container"
                  :class="{
                    'w-full flex-grow': showApiType.includes(step.stepType),
                  }"
                >
                  <div class="mx-[4px] max-w-[400px] break-all text-[var(--color-text-1)]">
                    {{ step.name }}
                  </div>
                </div>
              </div>
              <div class="flex items-center">
                <stepStatus :status="step.status || 'PENDING'" />
                <!-- 脚本报错 -->
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
                <div v-show="showStatus(step)" class="flex">
                  <div class="mx-2 flex items-center">
                    <div v-show="step.code" class="mr-2 text-[var(--color-text-4)]">
                      {{ t('report.detail.api.statusCode') }}</div
                    >
                    <div
                      v-show="step.code"
                      class="max-w-[200px] break-all"
                      :style="{ color: statusCodeColor(step.code) }"
                    >
                      {{ step.code || '-' }}
                    </div>
                  </div>

                  <div v-show="step.requestTime !== null" class="mr-2 flex items-center text-[var(--color-text-4)]">
                    {{ t('report.detail.api.responseTime') }}
                    <span class="ml-2 text-[rgb(var(--success-6))]">
                      {{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[0] : '-' }}
                      {{ step.requestTime !== null ? formatDuration(step.requestTime).split('-')[1] : 'ms' }}
                    </span>
                  </div>
                  <div v-show="step.responseSize !== null" class="flex items-center text-[var(--color-text-4)]">
                    {{ t('report.detail.api.responseSize') }}
                    <span class="ml-2 text-[rgb(var(--success-6))]"> {{ step.responseSize || 0 }} bytes </span>
                  </div>
                </div>
              </div>
            </div>
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
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { formatDuration } from '@/utils';

  import type { ScenarioItemType } from '@/models/apiTest/report';
  import { ScenarioStepType } from '@/enums/apiEnum';

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
  }>();

  const treeRef = ref<InstanceType<typeof MsTree>>();

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

  const selectedKeys = ref<(string | number)[]>([]);

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

  function showStatus(item: ScenarioItemType) {
    if (showApiType.value.includes(item.stepType) && item.status && item.status !== 'PENDING') {
      return true;
    }
    return item.children && item.children.length > 0 && item.status && item.status !== 'PENDING';
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
      background-color: var(--color-text-fff) !important;
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
    background-color: var(--color-text-fff) !important;
    .arco-tree-node-title {
      background-color: var(--color-text-fff);
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
  :deep(.ms-tree-container .ms-tree .arco-tree-node .arco-tree-node-title) {
    background: var(--color-text-fff);
  }
  :deep(.ms-tree-container .ms-tree .arco-tree-node-selected) {
    background: var(--color-text-fff);
  }
  .line {
    position: absolute;
    top: 40px;
    width: 100%;
    height: 1px;
    background: var(--color-text-n8);
  }
  :deep(.step-tree-node-title) {
    width: 100%;
  }
</style>
