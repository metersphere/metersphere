<template>
  <div
    class="scenario-class cursor-pointer"
    :class="[
      props.showBorder ? 'border border-solid border-[var(--color-text-n8)]' : '',
      props.hasBottomMargin ? 'mb-1' : '',
    ]"
  >
    <div class="flex h-[46px] items-center">
      <!-- 序号 -->
      <span class="index text-[var(--color-text-4)]">{{ props.item.sort }}</span>
      <MsIcon type="icon-icon_split_turn-down_arrow" class="mx-[4px] text-[var(--color-text-4)]" size="16" />
      <!-- 场景count -->
      <span class="mr-2 text-[var(--color-text-4)]">8</span>
      <!-- 循环控制器 -->
      <ConditionStatus :status="props.item.stepType" />
      <span class="ml-2">{{ props.item.name }}</span>
    </div>
    <div>
      <MsTag class="cursor-pointer" :type="props.item.status === 'SUCCESS' ? 'success' : 'danger'" theme="light">
        通过
      </MsTag>
      <span class="statusCode"
        >状态码 <span class="code">{{ props.item.code }}</span></span
      >
      <span class="resTime"
        >响应时间 <span class="resTimeCount">{{ props.item.requestTime }}ms</span></span
      >
      <span class="resSize"
        >响应大小 <span class="resTimeCount">{{ props.item.responseSize }} bytes</span></span
      >
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import ConditionStatus from './conditionStatus.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { ScenarioItemType } from '@/models/apiTest/report';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      showBorder?: boolean;
      hasBottomMargin?: boolean;
      item: ScenarioItemType;
    }>(),
    {
      showBorder: true,
      hasBottomMargin: true,
    }
  );
</script>

<style scoped lang="less">
  .scenario-class {
    border-radius: 4px;
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
      .resTimeCount {
        color: rgb(var(--success-6));
      }
    }
  }
</style>
