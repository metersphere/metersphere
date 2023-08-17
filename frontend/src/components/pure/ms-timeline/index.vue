<template>
  <a-timeline v-if="mode === 'static'" class="ms-timeline">
    <a-timeline-item
      v-for="item of props.list"
      :key="item.id || item.label"
      :dot-color="item.dotColor || 'var(--color-text-input-border)'"
      :label="item.label"
      :line-type="item.lineType"
    >
      <slot name="content">
        <span class="timeline-text">{{ item.time }}</span>
      </slot>
    </a-timeline-item>
  </a-timeline>
  <a-timeline
    v-else
    :class="[
      'ms-timeline',
      isArrivedTop ? 'ms-timeline--hidden-top-shadow' : '',
      isArrivedBottom ? 'ms-timeline--hidden-bottom-shadow' : '',
    ]"
  >
    <a-list
      ref="listRef"
      :data="props.list"
      :virtual-list-props="{ height: props.maxHeight }"
      :bordered="false"
      @reach-bottom="handleReachBottom"
    >
      <template #item="{ item, index }">
        <div>
          <a-list-item :key="item.id">
            <a-timeline-item :dot-color="item.dotColor || 'var(--color-text-input-border)'" :line-type="item.lineType">
              <slot name="time" :item="item">
                <span class="timeline-text">{{ item.time }}</span>
              </slot>
              <slot name="content" :item="item">
                <div>{{ item.label }}</div>
              </slot>
            </a-timeline-item>
          </a-list-item>
          <div v-if="index === props.list.length - 1" class="flex h-[32px] items-center justify-center">
            <div v-if="noMoreData" class="text-[var(--color-text-4)]">{{ t('ms.timeline.noMoreData') }}</div>
            <a-spin v-else />
          </div>
        </div>
      </template>
    </a-list>
  </a-timeline>
</template>

<script setup lang="ts">
  import { nextTick, ref, Ref, watch, onBeforeUnmount } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

  import type { MsTimeLineListItem } from './types';

  const props = withDefaults(
    defineProps<{
      mode?: 'static' | 'remote'; // 静态数据或者远程数据
      list: MsTimeLineListItem[];
      noMoreData?: boolean;
      maxHeight?: string | number;
    }>(),
    {
      mode: 'static',
      noMoreData: false,
      maxHeight: 400,
    }
  );

  const emit = defineEmits(['reachBottom']); // reach-bottom是列表滚动触底事件

  const { t } = useI18n();
  const listRef: Ref = ref(null);
  const isArrivedTop = ref(true);
  const isArrivedBottom = ref(true);
  const isInitListener = ref(false);

  /**
   * 监听列表内容区域滚动，以切换顶部底部阴影
   * @param event 滚动事件
   */
  function listenScroll(event: Event) {
    if (event.target) {
      const listContent = event.target as HTMLElement;
      const { scrollTop, scrollHeight, clientHeight } = listContent;
      const scrollBottom = scrollHeight - clientHeight - scrollTop;
      isArrivedTop.value = scrollTop < 20;
      isArrivedBottom.value = scrollBottom < 40;
    }
  }

  watch(props.list, () => {
    if (props.list.length > 0 && !isInitListener.value) {
      nextTick(() => {
        const listContent = listRef.value?.$el.querySelector('.arco-list-content');
        isInitListener.value = true;
        listContent.addEventListener('scroll', listenScroll);
      });
    }
    nextTick(() => {
      // 为了在列表数据滚动加载时，能够正确判断是否滚动到底部，因为此时没有触发滚动事件，而在加载前触发了滚动到底部的判断
      const listContent = listRef.value?.$el.querySelector('.arco-list-content');
      const { scrollTop, scrollHeight, clientHeight } = listContent;
      isArrivedBottom.value = scrollHeight - clientHeight - scrollTop < 20;
    });
  });

  function handleReachBottom() {
    if (!props.noMoreData && props.list.length > 0) {
      emit('reachBottom');
    }
  }

  onBeforeUnmount(() => {
    const listContent = listRef.value?.$el.querySelector('.arco-list-content');
    if (listContent) {
      listContent.removeEventListener('scroll', listenScroll);
    }
  });
</script>

<style lang="less" scoped>
  .ms-timeline {
    box-shadow: inset 0 10px 6px -10px rgb(0 0 0 / 15%), inset 0 -10px 6px -10px rgb(0 0 0 / 15%);
    transition: box-shadow 0.1s cubic-bezier(0.165, 0.84, 0.44, 1);
    :deep(.arco-list) {
      border-radius: 0;
    }
    .arco-list-wrapper {
      .arco-list-item {
        @apply border-none !py-0;
      }
    }
    .arco-timeline-item {
      padding-bottom: 24px;
      min-height: auto;
      :deep(.arco-timeline-item-dot-line) {
        @apply !block;
      }
    }
    :deep(.arco-timeline-item-label) {
      font-size: 14px;
      color: var(--color-text-2);
    }
    .timeline-text {
      color: @color-text-5;
    }
  }
  .ms-timeline--hidden-top-shadow {
    box-shadow: inset 0 -10px 6px -10px rgb(0 0 0 / 15%);
  }
  .ms-timeline--hidden-bottom-shadow {
    box-shadow: inset 0 10px 6px -10px rgb(0 0 0 / 15%);
  }
</style>
