<template>
  <template v-for="item in list" :key="item.stepId">
    <div
      :style="{
        'padding-left': `${16 * (item.level as number)}px`,
      }"
    >
      <div
        class="scenario-class cursor-pointer rounded-t-md px-8"
        :class="[...getBorderAndRadius(item), ...getBorderClass(item)]"
        @click="showDetail(item)"
      >
        <div class="flex h-[46px] items-center">
          <!-- 序号 -->
          <span class="index mr-2 text-[var(--color-text-4)]">{{ item.sort }}</span>
          <!-- 展开折叠控制器 -->
          <div v-if="getShowExpand(item)" class="mx-2">
            <span
              v-if="item.fold"
              class="collapsebtn flex items-center justify-center"
              @click.stop="expandHandler(item)"
            >
              <icon-right class="text-[var(--color-text-4)]" :style="{ 'font-size': '12px' }" />
            </span>
            <span v-else class="expand flex items-center justify-center" @click.stop="expandHandler(item)">
              <icon-down class="text-[rgb(var(--primary-6))]" :style="{ 'font-size': '12px' }" />
            </span>
          </div>

          <MsIcon
            v-if="props.showType === 'API'"
            type="icon-icon_split_turn-down_arrow"
            class="mx-[4px] text-[var(--color-text-4)]"
            size="16"
          />
          <!-- 场景count -->
          <span v-if="props.showType === 'API'" class="mr-2 text-[var(--color-text-4)]">{{
            (item.children || []).length
          }}</span>
          <!-- 循环控制器 -->
          <ConditionStatus v-if="props.showType === 'API'" :status="item.stepType || ''" />
          <a-popover position="left" content-class="response-popover-content">
            <div class="one-line-text max-w-[200px]">
              {{ item.name || '-' }}
            </div>
            <template #content>
              <div class="flex items-center gap-[8px] text-[14px]">
                <div class="max-w-[300px]">
                  {{ item.name || '-' }}
                </div>
              </div>
            </template>
          </a-popover>
        </div>
        <div class="flex">
          <MsTag class="cursor-pointer" :type="item.status === 'SUCCESS' ? 'success' : 'danger'" theme="light">
            {{ item.status === 'SUCCESS' ? t('report.detail.api.pass') : t('report.detail.api.resError') }}
          </MsTag>
          <span class="statusCode mx-2">
            <div class="mr-2"> {{ t('report.detail.api.statusCode') }}</div>
            <a-popover position="left" content-class="response-popover-content">
              <div class="one-line-text max-w-[200px]" :style="{ color: statusCodeColor(item.code) }">
                {{ item.code || '-' }}
              </div>
              <template #content>
                <div class="flex items-center gap-[8px] text-[14px]">
                  <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.statusCode') }}</div>
                  <div :style="{ color: statusCodeColor(item.code) }">
                    {{ item.code || '-' }}
                  </div>
                </div>
              </template>
            </a-popover>
          </span>
          <span class="resTime">
            {{ t('report.detail.api.responseTime') }}
            <span class="resTimeCount ml-2">{{ item.requestTime || 0 }}ms</span></span
          >
          <span class="resSize">
            {{ t('report.detail.api.responseSize') }}
            <span class="resTimeCount ml-2">{{ item.responseSize || 0 }} bytes</span></span
          >
        </div>
      </div>
    </div>
    <a-divider
      v-if="item.level === 0 && props.showType !== 'CASE'"
      :margin="0"
      class="!mb-4"
      :class="props.showType === 'API' ? '!mb-4' : '!mb-0'"
    ></a-divider>

    <!-- 响应内容开始 -->
    <div
      v-if="showResContent(item)"
      :style="{
        'padding-left': `${16 * (item.level as number)}px`,
      }"
    >
      <ResponseContent
        :detail-item="item"
        :show-type="props.showType"
        :console="props.console"
        :environment-name="props.environmentName"
      />
    </div>
    <!-- </div> -->
    <!-- 响应内容结束 -->
    <ScenarioItem
      v-if="'children' in item"
      :list="item.children"
      :active-type="props.activeType"
      :show-type="props.showType"
      :console="props.console"
      :environment-name="props.environmentName"
      @detail="showDetail"
    />
  </template>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import ConditionStatus from './conditionStatus.vue';
  import ResponseContent from './responseContent.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { ScenarioItemType } from '@/models/apiTest/report';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      showBorder?: boolean;
      hasBottomMargin?: boolean;
      list: ScenarioItemType[];
      activeType: string;
      showType: 'API' | 'CASE';
      console?: string; // 控制台
      environmentName?: string; // 环境
    }>(),
    {
      showBorder: true,
      hasBottomMargin: true,
    }
  );

  const emit = defineEmits(['expand', 'detail']);
  const activeItem = ref();
  function showDetail(item: ScenarioItemType) {
    if (props.activeType === 'tab') {
      return;
    }
    activeItem.value = item;
    emit('detail', activeItem.value);
  }
  const showApiType = ref<string[]>(['API', 'API_CASE', 'CUSTOM_API', 'LOOP_CONTROLLER']);

  async function expandHandler(item: ScenarioItemType) {
    item.fold = !item.fold;
  }

  function getBorderAndRadius(item: ScenarioItemType) {
    if (props.showType === 'API') {
      if (props.activeType === 'tab') {
        if (!item.fold && showApiType.value.includes(item.stepType)) {
          return ['rounded-b-none', 'mb-0'];
        }
        return ['mb-1', 'rounded-[4px]'];
      }
    } else {
      return ['mb-1', 'rounded-[4px]'];
    }
    return ['mb-1', 'rounded-[4px]'];
  }

  function getBorderClass(item: ScenarioItemType) {
    if (props.showType === 'API') {
      return item.level !== 0 ? ['border', 'border-solid', 'border-[var(--color-text-n8)]'] : [''];
    }
    return ['border', 'border-solid', 'border-[var(--color-text-n8)]'];
  }

  function getShowExpand(item: ScenarioItemType) {
    if (props.showType === 'API') {
      return item.level !== 0 && showApiType.value.includes(item.stepType) && props.activeType === 'tab';
    }
    return props.activeType === 'tab';
  }

  function showResContent(item: ScenarioItemType) {
    if (props.showType === 'API') {
      return showApiType.value.includes(item.stepType) && props.activeType === 'tab' && !item.fold;
    }
    return props.activeType === 'tab' && !item.fold;
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
</script>

<style scoped lang="less">
  .scenario-class {
    @apply flex items-center justify-between px-2;
    .index {
      width: 16px;
      height: 16px;
      line-height: 16px;
      border-radius: 50%;
      color: white;
      background: var(--color-text-brand);
      @apply inline-block text-center;
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
  .resContentWrapper {
    position: relative;
    border: 1px solid var(--color-text-n8);
    border-top: none;
    border-radius: 0 0 6px 6px;
    @apply mb-4 bg-white p-4;
    .resContent {
      height: 38px;
      border-radius: 6px;
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
  :deep(.no-content) {
    .arco-tabs-content {
      display: none;
    }
  }
  .ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
</style>
