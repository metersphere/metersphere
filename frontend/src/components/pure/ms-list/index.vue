<template>
  <a-list
    v-bind="props"
    ref="listRef"
    :data="data"
    :class="['ms-list', containerStatusClass, props.class]"
    @reach-bottom="handleReachBottom"
  >
    <template #item="{ item, index }">
      <slot name="item" :item="item" :index="index">
        <div
          :key="item.id"
          :class="[
            'ms-list-item',
            props.noHover ? 'ms-list-item--no-hover' : '',
            props.itemBorder ? 'ms-list-item--bordered' : '',
            props.itemClass,
            innerFocusItemKey === item[itemKeyField] ? 'ms-list-item--focus' : '',
            innerActiveItemKey === item[itemKeyField] ? props.activeItemClass : '',
          ]"
        >
          <div class="flex-1 overflow-x-hidden" @click="emit('itemClick', item, index)">
            <slot name="title" :item="item" :index="index"></slot>
          </div>
          <div class="flex items-center gap-[4px]">
            <icon-drag-dot-vertical v-if="props.draggable && !props.disabled" class="ms-list-drag-icon" />
            <div
              v-if="
                $slots['itemAction'] || (props.itemMoreActions && props.itemMoreActions.length > 0 && !props.disabled)
              "
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
            <slot name="itemRight" :item="item" :index="index"></slot>
          </div>
        </div>
      </slot>
      <div
        v-if="props.mode === 'remote' && index === props.data.length - 1"
        class="flex h-[32px] items-center justify-center"
      >
        <div v-if="noMoreData" class="text-[var(--color-text-4)]">{{ t('ms.timeline.noMoreData') }}</div>
        <a-spin v-else />
      </div>
    </template>
    <template v-if="$slots['empty'] || props.emptyText" #empty>
      <slot name="empty">
        <div
          class="rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[8px] text-[12px] leading-[16px] text-[var(--color-text-4)]"
        >
          {{ props.emptyText }}
        </div>
      </slot>
    </template>
  </a-list>
</template>

<script setup lang="ts">
  import { nextTick, Ref, ref, watch } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { useDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import useContainerShadow from '@/hooks/useContainerShadow';
  import { useI18n } from '@/hooks/useI18n';

  import type { VirtualListProps } from '@arco-design/web-vue/es/_components/virtual-list-v2/interface';

  const props = withDefaults(
    defineProps<{
      mode?: 'static' | 'remote'; // 静态数据或者远程数据
      data: Record<string, any>[];
      bordered?: boolean; // 是否显示边框
      activeItemKey?: string | number; // 当前选中的项的 key
      focusItemKey?: string | number; // 聚焦的项的 key
      itemMoreActions?: ActionsItem[]; // 每一项的更多操作
      itemKeyField?: string; // 唯一值 key 的字段名，默认为 key
      itemHeight?: number; // 每一项的高度
      emptyText?: string; // 空数据时的文案
      noMoreData?: boolean; // 远程模式下，是否没有更多数据
      noHover?: boolean; // 是否不显示列表项的 hover 效果
      itemBorder?: boolean; // 是否显示列表项的边框
      class?: string;
      itemClass?: string;
      activeItemClass?: string;
      draggable?: boolean;
      disabled?: boolean;
      virtualListProps?: VirtualListProps;
    }>(),
    {
      mode: 'static',
      itemKeyField: 'key',
      itemHeight: 20,
      bordered: false,
      draggable: false,
    }
  );

  const emit = defineEmits([
    'update:data',
    'update:focusItemKey',
    'update:activeItemKey',
    'itemClick',
    'close',
    'moreActionSelect',
    'moreActionsClose',
    'clickMore',
    'reachBottom',
  ]);

  const { t } = useI18n();

  const innerFocusItemKey = useVModel(props, 'focusItemKey', emit); // 聚焦的节点，一般用于在操作扩展按钮时，高亮当前节点，保持扩展按钮持续显示
  const innerActiveItemKey = useVModel(props, 'activeItemKey', emit); // 激活的节点，搭配activeItemClass使用，用于高亮当前节点
  const innerData = useVModel(props, 'data', emit);

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

  const { isInitListener, containerStatusClass, setContainer, initScrollListener } = useContainerShadow({
    overHeight: props.itemHeight,
    containerClassName: 'ms-list',
  });

  watch(
    props.data,
    () => {
      if (props.data.length > 0 && !isInitListener.value) {
        // 如果数据不为空，且没有初始化容器的滚动监听器
        if (props.draggable) {
          // 如果开启拖拽
          if (props.virtualListProps) {
            // 如果开启虚拟滚动，需要将拖拽的容器设置为虚拟滚动的容器
            useDraggable('.arco-list-content div div', innerData, {
              ghostClass: 'ms-list-ghost',
            });
          } else {
            // 否则直接设置为列表容器
            useDraggable('.arco-list-content', innerData, {
              ghostClass: 'ms-list-ghost',
            });
          }
        }
        nextTick(() => {
          const listContent = listRef.value?.$el.querySelector('.arco-list-content');
          setContainer(listContent);
          initScrollListener();
        });
      }
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
</script>

<style lang="less" scoped>
  .ms-list {
    .ms-container--shadow-y();
    :deep(.arco-list) {
      @apply rounded-none;
      .ms-list-item {
        @apply flex w-full cursor-pointer items-center justify-between;

        padding: 8px 4px;
        border-radius: var(--border-radius-small);
        &:hover {
          background-color: rgb(var(--primary-1));
          .ms-list-drag-icon {
            @apply visible;
          }
          .ms-list-item-actions {
            @apply visible;
          }
        }
        .ms-list-drag-icon {
          @apply invisible cursor-move;
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
        .arco-list-item-meta-content {
          @apply flex-1 overflow-hidden;
        }
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
    :deep(.arco-scrollbar),
    :deep(.arco-list),
    :deep(.arco-list-content-wrapper) {
      @apply h-full;
    }
    :deep(.arco-list-content) {
      .ms-scroll-bar();
    }
    :deep(.arco-list-item-main) {
      @apply overflow-hidden;
    }
  }
  .ms-list-ghost {
    opacity: 0.5;
  }
</style>
