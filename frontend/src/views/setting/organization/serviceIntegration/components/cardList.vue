<template>
  <div ref="containerRef" class="infinite-list-wrapper">
    <a-scrollbar
      :style="{
        overflow: 'auto',
        height: wrapperHeight + 'px',
      }"
    >
      <div class="list h-[`${wrapperHeight}px`] grid grid-cols-3 gap-x-4 gap-y-4">
        <div v-for="(item, index) in data" :key="index" :style="{ height: itemHeight + 'px' }">
          <slot :item="item" :index="index">
            {{ item }}
          </slot>
        </div>
      </div>
    </a-scrollbar>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watchEffect } from 'vue';

  const props = withDefaults(
    defineProps<{
      dataSource?: any[];
      cols?: number;
      wrapperHeight?: number;
    }>(),
    {
      cols: 3,
      wrapperHeight: 420,
    }
  );
  const col = ref();

  watchEffect(() => {
    col.value = props.cols;
  });

  const data = ref<any[]>([...(props.dataSource || [])]);
  const itemHeight = computed(() => {
    return ((props.wrapperHeight as number) - ((props.cols as number) - 1) * 16) / props.cols;
  });
</script>

<style lang="less" scoped></style>
