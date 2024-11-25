<template>
  <a-skeleton v-if="props.showSkeleton" :loading="props.showSkeleton" :animation="true">
    <div class="flex gap-[16px]">
      <div class="flex-1">
        <a-skeleton-line :rows="1" :line-height="props.skeletonLine" />
      </div>
      <div class="flex-1">
        <a-skeleton-line :rows="1" :line-height="props.skeletonLine" />
      </div>
    </div>
    <div>
      <div v-for="index in props.showLineNumber" :key="index" class="mt-[16px] flex flex-col">
        <div class="flex gap-4">
          <div :style="{ width: `${index * 12}%` }">
            <a-skeleton-line :rows="1" :line-height="props.skeletonLine" />
          </div>
          <div :style="{ width: `calc(100% - ${index * 12}%)` }">
            <a-skeleton-line :rows="1" :line-height="props.skeletonLine" />
          </div>
        </div>
      </div>
    </div>
  </a-skeleton>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      showSkeleton: boolean;
      skeletonLine?: number;
      showLineNumber?: number;
    }>(),
    {
      skeletonLine: 24,
      showLineNumber: 7,
    }
  );
</script>

<style scoped></style>
