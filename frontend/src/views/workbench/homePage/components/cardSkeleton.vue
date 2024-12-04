<template>
  <a-skeleton :loading="props.showSkeleton" :animation="true">
    <div class="header-skeleton mb-[24px] flex justify-between">
      <div class="w-[20%]">
        <a-skeleton-line :rows="1" :line-height="24" />
      </div>
      <div class="w-[40%]">
        <a-skeleton-line :rows="1" :line-height="24" />
      </div>
    </div>

    <template v-if="!props.isMemberOverview">
      <div v-for="number in showLineNumber" :key="`out-${number}`" class="mb-[24px] flex flex-col gap-[12px]">
        <div
          v-for="index in skeletonLine"
          :key="`inner-${index}`"
          :class="`${index === skeletonLine ? 'w-[43%]' : 'w-full'}`"
        >
          <a-skeleton-line :rows="1" :line-height="16" />
        </div>
      </div>
    </template>

    <div class="content-skeleton">
      <a-skeleton-line :rows="1" :line-height="props.contentHeight" />
    </div>
  </a-skeleton>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      showSkeleton: boolean;
      skeletonLine?: number;
      showLineNumber?: number;
      isMemberOverview?: boolean;
      contentHeight?: number; // 内容区域高度
    }>(),
    {
      skeletonLine: 3,
      showLineNumber: 1,
      contentHeight: 166,
    }
  );
</script>

<style scoped lang="less">
  :deep(.content-skeleton) {
    .arco-skeleton-line-row {
      border-radius: 8px;
    }
  }
</style>
