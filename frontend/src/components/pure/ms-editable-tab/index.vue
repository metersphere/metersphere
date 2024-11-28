<template>
  <div class="ms-editable-tab-container">
    <a-tooltip v-if="!isNotOverflow" :content="t('ms.editableTab.arrivedLeft')" :disabled="!arrivedState.left">
      <MsButton
        type="icon"
        status="secondary"
        class="tab-button !mr-[4px]"
        :disabled="arrivedState.left"
        @click="scrollTabs('left')"
      >
        <MsIcon type="icon-icon_left_outlined" />
      </MsButton>
    </a-tooltip>
    <div ref="tabNav" class="ms-editable-tab-nav">
      <div
        v-for="tab in tabs"
        :key="tab.id"
        class="ms-editable-tab"
        :class="{ active: activeTab?.id === tab.id }"
        @click="handleTabClick(tab)"
      >
        <div :draggable="!!tab.draggable" class="flex items-center">
          <slot name="label" :tab="tab">
            <a-tooltip :content="tab.label" :mouse-enter-delay="500">
              <div class="one-line-text flex max-w-[144px] items-center">
                {{ tab.label }}
              </div>
            </a-tooltip>
          </slot>
          <div v-if="tab.unSaved" class="ml-[8px] h-[8px] w-[8px] rounded-full bg-[rgb(var(--primary-5))]"></div>
          <MsButton
            v-if="props.atLeastOne ? tabs.length > 1 && tab.closable : tab.closable !== false"
            type="icon"
            status="secondary"
            class="ms-editable-tab-close-button"
            @click="() => close(tab)"
          >
            <MsIcon type="icon-icon_close_outlined" size="12" />
          </MsButton>
        </div>
      </div>
    </div>
    <a-tooltip v-if="!isNotOverflow" :content="t('ms.editableTab.arrivedRight')" :disabled="!arrivedState.right">
      <MsButton
        type="icon"
        status="secondary"
        class="ms-editable-tab-button !mr-[8px]"
        :disabled="arrivedState.right"
        @click="scrollTabs('right')"
      >
        <MsIcon type="icon-icon_right_outlined" />
      </MsButton>
    </a-tooltip>
    <a-tooltip
      v-if="!props.readonly && showAdd"
      :content="t('ms.editableTab.limitTip', { max: props.limit })"
      :disabled="!props.limit || tabs.length >= props.limit"
    >
      <MsButton
        type="icon"
        status="secondary"
        class="ms-editable-tab-button !mr-[4px]"
        :disabled="!!props.limit && tabs.length >= props.limit"
        @click="addTab"
      >
        <MsIcon type="icon-icon_add_outlined" />
      </MsButton>
    </a-tooltip>
    <MsMoreAction
      v-if="!props.hideMoreAction && !props.readonly && mergedMoreActionList.length > 0"
      :list="mergedMoreActionList"
      @select="handleMoreActionSelect"
    >
      <MsButton type="icon" status="secondary" class="ms-editable-tab-button">
        <MsIcon type="icon-icon_more_outlined" />
      </MsButton>
    </MsMoreAction>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, ref, watch } from 'vue';
  import { useScroll } from '@vueuse/core';
  import { useDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';

  import type { TabItem } from './types';

  const props = withDefaults(
    defineProps<{
      moreActionList?: ActionsItem[];
      showAdd?: boolean; // 是否展示增加tab按钮
      limit?: number; // 最多可打开的tab数量
      atLeastOne?: boolean; // 是否至少保留一个tab
      hideMoreAction?: boolean; // 是否隐藏更多操作
      readonly?: boolean; // 是否只读
    }>(),
    {
      showAdd: true,
    }
  );
  const emit = defineEmits<{
    (e: 'add'): void;
    (e: 'close', item: TabItem): void;
    (e: 'change', item: TabItem): void;
    (e: 'moreActionSelect', item: ActionsItem): void;
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();

  const activeTab = defineModel<TabItem | undefined>('activeTab', {
    default: undefined,
  });
  const tabs = defineModel<TabItem[]>('tabs', {
    required: true,
  });
  const tabNav = ref<HTMLElement>();
  const { arrivedState } = useScroll(tabNav);
  const isNotOverflow = computed(() => arrivedState.left && arrivedState.right); // 内容是否溢出，用于判断左右滑动按钮是否展示

  /**
   * 滚动tab
   */
  const scrollTabs = (direction: 'left' | 'right') => {
    if (tabNav.value) {
      const tabNavWidth = tabNav.value?.clientWidth || 0;
      const tabNavScrollWidth = tabNav.value?.scrollWidth || 0;

      if (direction === 'left') {
        tabNav.value.scrollTo({
          left: tabNav.value.scrollLeft - tabNavWidth - 80,
          behavior: 'smooth',
        });
      } else if (tabNavScrollWidth > tabNav.value.scrollLeft + tabNavWidth - 80) {
        tabNav.value.scrollTo({
          left: tabNav.value.scrollLeft + tabNavWidth,
          behavior: 'smooth',
        });
      }
    }
  };

  /**
   * 滚动到当前激活的tab
   */
  const scrollToActiveTab = () => {
    const activeTabDom = tabNav.value?.querySelector('.ms-editable-tab.active');
    if (activeTabDom) {
      const tabRect = activeTabDom.getBoundingClientRect();
      const navRect = tabNav.value?.getBoundingClientRect();
      if (tabRect.left < navRect!.left) {
        scrollTabs('left');
      } else if (tabRect.right > navRect!.right) {
        scrollTabs('right');
      }
    }
  };

  const defaultMoreActionList = [
    {
      eventTag: 'closeAll',
      label: t('ms.editableTab.closeAll'),
    },
    {
      eventTag: 'closeOther',
      label: t('ms.editableTab.closeOther'),
    },
  ];
  const mergedMoreActionList = computed(() => {
    const dl = props.atLeastOne
      ? defaultMoreActionList.filter((e) => e.eventTag !== 'closeAll')
      : defaultMoreActionList;
    if (tabs.value.filter((item) => item.closable === true).length === 0) {
      return props.moreActionList ? [...props.moreActionList] : [];
    }
    return props.moreActionList ? [...dl, ...props.moreActionList] : dl;
  });

  /**
   * 监听激活的tab变化，滚动到激活的tab
   */
  watch(
    () => activeTab.value,
    () => {
      useDraggable('.ms-editable-tab-nav', tabs, {
        ghostClass: 'ms-editable-tab-ghost',
      });
      nextTick(() => {
        scrollToActiveTab();
      });
    }
  );

  onMounted(() => {
    const resizeObserver = new ResizeObserver(() => {
      scrollToActiveTab();
    });
    resizeObserver.observe(tabNav.value as Element);
  });

  function addTab() {
    emit('add');
  }

  /**
   * 关闭一个tab
   */
  function closeOneTab(item: TabItem) {
    const index = tabs.value.findIndex((e) => e.id === item.id);
    tabs.value.splice(index, 1);
    if (activeTab.value?.id === item.id && tabs.value[0]) {
      [activeTab.value] = tabs.value;
      emit('change', tabs.value[0]);
    }
  }

  /**
   * 关闭tab前处理
   */
  function close(item: TabItem) {
    if (item.unSaved) {
      openModal({
        title: t('common.tip'),
        content: t('ms.editableTab.closeTabTip'),
        type: 'warning',
        hideCancel: false,
        onBeforeOk: async () => {
          closeOneTab(item);
          emit('close', item);
        },
      });
    } else {
      closeOneTab(item);
      emit('close', item);
    }
  }

  function handleTabClick(item: TabItem) {
    if (activeTab.value?.id !== item.id) {
      activeTab.value = item;
      nextTick(() => {
        tabNav.value?.querySelector('.tab.active')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      });
      emit('change', item);
    }
  }

  /**
   * 执行更多操作
   */
  function executeAction(event: ActionsItem) {
    switch (event.eventTag) {
      case 'closeAll':
        tabs.value = tabs.value.filter((item) => item.closable === false);
        [activeTab.value] = tabs.value;
        emit('change', activeTab.value);
        break;
      case 'closeOther':
        tabs.value = tabs.value.filter((item) => item.id === activeTab.value?.id || item.closable === false);
        break;
      default:
        emit('moreActionSelect', event);
        break;
    }
  }

  /**
   * 处理更多操作选择
   */
  function handleMoreActionSelect(event: ActionsItem) {
    if (
      (event.eventTag === 'closeAll' && tabs.value.some((item) => item.unSaved)) ||
      (event.eventTag === 'closeOther' && tabs.value.some((item) => item.unSaved && item.id !== activeTab.value?.id))
    ) {
      openModal({
        title: t('common.tip'),
        content: t('ms.editableTab.batchCloseTabTip'),
        type: 'warning',
        hideCancel: false,
        onBeforeOk: async () => {
          executeAction(event);
        },
      });
      return;
    }
    executeAction(event);
  }
</script>

<style lang="less" scoped>
  .ms-editable-tab-container {
    @apply flex items-center;

    height: 32px;
    .ms-editable-tab-nav {
      @apply relative flex  overflow-x-auto whitespace-nowrap;
      &::-webkit-scrollbar {
        width: 0; /* 宽度为0，隐藏垂直滚动条 */
        height: 0; /* 高度为0，隐藏水平滚动条 */
      }
      .ms-editable-tab {
        @apply flex cursor-pointer items-center;

        margin-right: 4px;
        padding: 5px 8px;
        border-radius: var(--border-radius-small);
        background-color: var(--color-text-n9);
        gap: 8px;
        &.active,
        &:hover {
          color: rgb(var(--primary-5));
          background-color: rgb(var(--primary-2));
          .ms-editable-tab-close-button {
            @apply visible;
          }
        }
        .ms-editable-tab-close-button {
          @apply invisible !rounded-full;

          margin-left: 4px !important;
        }
      }
    }
    .ms-editable-tab-button {
      padding: 8px;
      &:not([disabled='true']) {
        padding: 8px;
        color: var(--color-text-4);
        &:hover {
          color: var(--color-text-1);
        }
      }
    }
  }
  .ms-editable-tab-ghost {
    opacity: 0.5;
  }
</style>
