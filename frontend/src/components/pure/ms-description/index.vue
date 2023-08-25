<template>
  <a-skeleton v-if="props.showSkeleton" :loading="props.showSkeleton" :animation="true">
    <a-space direction="vertical" class="w-[28%]" size="large">
      <a-skeleton-line :rows="props.skeletonLine" :line-height="24" />
    </a-space>
    <a-space direction="vertical" class="ml-[4%] w-[68%]" size="large">
      <a-skeleton-line :rows="props.skeletonLine" :line-height="24" />
    </a-space>
  </a-skeleton>
  <div v-else class="ms-description" size="large">
    <slot name="title"></slot>
    <div
      v-for="(item, index) of props.descriptions"
      :key="item.label"
      class="ms-description-item"
      :style="{ marginBottom: props.descriptions.length - index <= props.column ? '' : '16px' }"
    >
      <div class="ms-description-item-label">
        <slot name="item-label">{{ item.label }}</slot>
      </div>
      <div class="ms-description-item-value">
        <slot name="item-value">
          <template v-if="item.isTag">
            <a-tag
              v-for="tag of item.value"
              :key="tag"
              color="var(--color-text-n8)"
              class="mr-[8px] font-normal !text-[var(--color-text-1)]"
            >
              {{ tag }}
            </a-tag>
            <span v-show="Array.isArray(item.value) && item.value.length === 0">-</span>
          </template>
          <a-button v-else-if="item.isButton" type="text" @click="handleItemClick(item)">{{ item.value }}</a-button>
          <div v-else>
            <slot name="value" :item="item">
              {{ item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value }}
            </slot>
          </div>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  export interface Description {
    label: string;
    value: (string | number) | (string | number)[];
    key?: string;
    isTag?: boolean;
    isButton?: boolean;
    onClick?: () => void;
  }

  const props = withDefaults(
    defineProps<{
      showSkeleton?: boolean;
      skeletonLine?: number;
      column?: number;
      descriptions: Description[];
    }>(),
    {
      column: 1,
    }
  );

  function handleItemClick(item: Description) {
    if (typeof item.onClick === 'function') {
      item.onClick();
    }
  }
</script>

<style lang="less" scoped>
  .ms-description {
    @apply flex flex-wrap;
    .ms-description-item {
      @apply flex items-center;

      width: calc(100% / v-bind(column));
    }
    .ms-description-item-label {
      @apply whitespace-pre-wrap font-normal;

      padding-right: 16px;
      width: 120px;
      color: var(--color-text-3);
      word-wrap: break-word;
    }
    .ms-description-item-value {
      @apply !pr-0 align-top;
    }
  }
</style>
