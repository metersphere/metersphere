<template>
  <a-list
    v-bind="props"
    ref="listRef"
    :data="data"
    :class="['ms-list', listStatusClass]"
    @reach-bottom="handleReachBottom"
  >
    <template #item="{ item, index }">
      <slot name="item" :item="item" :index="index">
        <div
          :key="index"
          :class="[
            'ms-list-item',
            props.noHover ? 'ms-list-item--no-hover' : '',
            props.itemBorder ? 'ms-list-item--bordered' : '',
            innerFocusItemKey === item[itemKeyField] ? 'ms-list-item--focus' : '',
          ]"
          @click="emit('itemClick', item, index)"
        >
          <slot name="title" :item="item" :index="index"></slot>
          <div
            v-if="$slots['itemAction'] || (props.itemMoreActions && props.itemMoreActions.length > 0)"
            class="ms-list-item-actions"
          >
            <slot name="itemAction" :item="item" :index="index"></slot>
            <MsTableMoreAction
              v-if="props.itemMoreActions && props.itemMoreActions.length > 0"
              :list="props.itemMoreActions"
              trigger="click"
              @select="handleMoreActionSelect($event, item)"
              @close="handleMoreActionClose"
            >
              <MsButton type="icon" size="mini" class="ms-list-item-actions-btn" @click="handleClickMore(item)">
                <MsIcon type="icon-icon_more_outlined" size="14" class="text-[var(--color-text-4)]" />
              </MsButton>
            </MsTableMoreAction>
          </div>
          <div
            v-if="props.mode === 'remote' && index === props.data.length - 1"
            class="flex h-[32px] items-center justify-center"
          >
            <div v-if="noMoreData" class="text-[var(--color-text-4)]">{{ t('ms.timeline.noMoreData') }}</div>
            <a-spin v-else />
          </div>
        </div>
      </slot>
    </template>
    <template v-if="$slots['empty'] || props.emptyText" #empty>
      <slot name="empty">
        <div
          class="rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[8px] text-[12px] text-[var(--color-text-4)]"
        >
          {{ props.emptyText }}
        </div>
      </slot>
    </template>
  </a-list>
</template>

<script setup lang="ts">
  import { computed, nextTick, onBeforeUnmount, Ref, ref, watch } from 'vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { useI18n } from '@/hooks/useI18n';

  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  const props = withDefaults(
    defineProps<{
      mode?: 'static' | 'remote'; // 静态数据或者远程数据
      data: Record<string, any>[];
      focusItemKey?: string | number; // 聚焦的项的 key
      itemMoreActions?: ActionsItem[]; // 每一项的更多操作
      itemKeyField?: string; // 唯一值 key 的字段名，默认为 key
      itemHeight?: number; // 每一项的高度
      emptyText?: string; // 空数据时的文案
      noMoreData?: boolean; // 远程模式下，是否没有更多数据
      noHover?: boolean; // 是否不显示列表项的 hover 效果
      itemBorder?: boolean; // 是否显示列表项的边框
    }>(),
    {
      mode: 'static',
      itemKeyField: 'key',
      itemHeight: 20,
    }
  );

  const emit = defineEmits([
    'update:focusItemKey',
    'itemClick',
    'close',
    'moreActionSelect',
    'moreActionsClose',
    'clickMore',
    'reachBottom',
  ]);

  const { t } = useI18n();

  const innerFocusItemKey = ref(props.focusItemKey || ''); // 聚焦的节点，一般用于在操作扩展按钮时，高亮当前节点，保持扩展按钮持续显示

  watch(
    () => props.focusItemKey,
    (val) => {
      innerFocusItemKey.value = val || '';
    }
  );

  watch(
    () => innerFocusItemKey.value,
    (val) => {
      emit('update:focusItemKey', val);
    }
  );

  const popVisible = ref(false);

  function handleClickMore(item: Record<string, any>) {
    innerFocusItemKey.value = item[props.itemKeyField];
    emit('clickMore', item);
  }

  function handleMoreActionSelect(event: ActionsItem, item: Record<string, any>) {
    popVisible.value = true;
    innerFocusItemKey.value = item[props.itemKeyField];
    emit('moreActionSelect', event, item);
  }

  function handleMoreActionClose() {
    if (!popVisible.value) {
      innerFocusItemKey.value = '';
    }
    emit('moreActionsClose');
  }

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
      isArrivedTop.value = scrollTop < props.itemHeight;
      isArrivedBottom.value = scrollBottom < props.itemHeight;
    }
  }

  watch(
    props.data,
    () => {
      if (props.data.length > 0 && !isInitListener.value) {
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
        isArrivedBottom.value = scrollHeight - clientHeight - scrollTop < props.itemHeight;
      });
    },
    {
      immediate: true,
    }
  );

  function handleReachBottom() {
    if (props.mode === 'remote' && !props.noMoreData && props.data.length > 0) {
      emit('reachBottom');
    }
  }

  const listStatusClass = computed(() => {
    if (isArrivedTop.value && isArrivedBottom.value) {
      // 内容不足一屏，不展示阴影
      return 'ms-list-hidden-shadow';
    }
    if (isArrivedTop.value) {
      // 滚动到顶部，隐藏顶部阴影
      return 'ms-list--hidden-top-shadow';
    }
    if (isArrivedBottom.value) {
      // 滚动到底部，隐藏底部阴影
      return 'ms-list--hidden-bottom-shadow';
    }
    // 滚动到中间，展示两侧阴影
    return '';
  });

  onBeforeUnmount(() => {
    const listContent = listRef.value?.$el.querySelector('.arco-list-content');
    if (listContent) {
      listContent.removeEventListener('scroll', listenScroll);
    }
  });
</script>

<style lang="less" scoped>
  .ms-list {
    box-shadow: inset 0 10px 6px -10px rgb(0 0 0 / 15%), inset 0 -10px 6px -10px rgb(0 0 0 / 15%);
    transition: box-shadow 0.1s cubic-bezier(0.165, 0.84, 0.44, 1);
    :deep(.arco-list) {
      @apply rounded-none;
      .ms-list-item {
        @apply flex w-full cursor-pointer items-center justify-between;

        padding: 8px 4px;
        border-radius: var(--border-radius-small);
        &:hover {
          background-color: rgb(var(--primary-1));
          .ms-list-item-actions {
            @apply visible;
          }
        }
        .ms-list-item-actions {
          @apply invisible flex items-center justify-end;
          .ms-list-item-actions-btn {
            @apply !mr-0;

            padding: 4px;
            border-radius: var(--border-radius-mini);
          }
        }
      }
      .ms-list-item--no-hover {
        @apply cursor-auto;
        &:hover {
          background-color: transparent;
        }
      }
      .ms-list-item--bordered {
        border: 1px solid var(--color-text-n8);
      }
      .ms-list-item--focus {
        background-color: rgb(var(--primary-1));
        .ms-list-item-actions {
          @apply visible;
        }
      }
      .arco-list-item-meta {
        @apply p-0;
        .arco-list-item-meta-title:not(:last-child) {
          @apply mb-0;
        }
        .arco-list-item-meta-avatar {
          margin-right: 12px;
        }
      }
      .arco-list-item-action > li:not(:last-child) {
        margin-right: 4px;
      }
    }
  }
  .ms-list-hidden-shadow {
    box-shadow: none;
  }
  .ms-list--hidden-top-shadow {
    box-shadow: inset 0 -10px 6px -10px rgb(0 0 0 / 15%);
  }
  .ms-list--hidden-bottom-shadow {
    box-shadow: inset 0 10px 6px -10px rgb(0 0 0 / 15%);
  }
</style>
