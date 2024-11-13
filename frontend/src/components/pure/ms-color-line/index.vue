<template>
  <a-popover position="bottom" content-class="ms-color-line-popper">
    <div class="color-bar" :style="{ borderRadius: props.radius }">
      <template v-for="(item, index) in colorData">
        <div v-if="item.percentage > 0" :key="index" :style="getStyle(item, index)"></div>
      </template>
    </div>
    <template #content>
      <slot name="popoverContent"></slot>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      colorData: Array<{ percentage: number; color: string }>;
      height: string;
      radius?: string;
    }>(),
    {
      radius: 'var(--border-radius-large)',
    }
  );

  const getStyle = (item: { percentage: number; color: string }, index: number) => {
    return {
      width: `${item.percentage}%`,
      backgroundColor: item.color,
      height: props.height,
      display: 'inline-block',
      borderRight: index === props.colorData.length - 1 ? 'none' : '1px solid white', // 1px间隔
    };
  };
</script>

<style lang="less">
  .ms-color-line-popper {
    padding: 16px;
    .arco-popover-title {
      @apply hidden;
    }
    .arco-popover-content {
      @apply mt-0;
    }
  }
</style>

<style lang="less" scoped>
  .color-bar {
    @apply flex w-full  overflow-hidden;

    background-color: var(--color-text-n8);
  }
  .color-bar div {
    box-sizing: border-box;
  }
</style>
