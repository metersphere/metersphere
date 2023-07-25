<template>
  <a-skeleton v-if="props.showSkeleton" :loading="props.showSkeleton" :animation="true">
    <a-space direction="vertical" class="w-[28%]" size="large">
      <a-skeleton-line :rows="props.skeletonLine" :line-height="24" />
    </a-space>
    <a-space direction="vertical" class="ml-[4%] w-[68%]" size="large">
      <a-skeleton-line :rows="props.skeletonLine" :line-height="24" />
    </a-space>
  </a-skeleton>
  <a-descriptions v-else :data="(props.descriptions as unknown as DescData[])" size="large" :column="1">
    <a-descriptions-item v-for="item of props.descriptions" :key="item.label" :label="item.label">
      <template v-if="item.isTag">
        <a-tag
          v-for="tag of item.value"
          :key="tag"
          color="var(--color-text-n8)"
          class="mr-[8px] font-normal !text-[var(--color-text-1)]"
        >
          {{ tag }}
        </a-tag>
      </template>
      <a-button v-else-if="item.isButton" type="text" @click="handleItemClick(item)">{{ item.value }}</a-button>
      <div v-else>
        {{ item.value }}
      </div>
    </a-descriptions-item>
  </a-descriptions>
</template>

<script setup lang="ts">
  import type { DescData } from '@arco-design/web-vue';

  export interface Description {
    label: string;
    value: (string | number) | (string | number)[];
    isTag?: boolean;
    isButton?: boolean;
    onClick?: () => void;
  }

  const props = defineProps<{
    showSkeleton?: boolean;
    skeletonLine?: number;
    descriptions: Description[];
  }>();

  function handleItemClick(item: Description) {
    if (typeof item.onClick === 'function') {
      item.onClick();
    }
  }
</script>

<style lang="less" scoped>
  .arco-descriptions-item-label {
    @apply whitespace-pre-wrap font-normal;
  }
  .arco-descriptions-item-label-block {
    padding-right: 16px !important;
    word-wrap: break-word;
    width: 120px;
  }
  .arco-descriptions-item-value-block {
    @apply !pr-0 align-top;
  }
</style>
