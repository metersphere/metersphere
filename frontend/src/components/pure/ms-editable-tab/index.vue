<template>
  <div class="tab-container">
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
    <div ref="tabNav" class="tab-nav">
      <div
        v-for="tab in props.tabs"
        :key="tab.id"
        class="tab"
        :class="{ active: innerActiveTab === tab.id }"
        @click="handleTabClick(tab)"
      >
        <div class="flex items-center">
          <slot name="label" :tab="tab">{{ tab.label }}</slot>
          <MsButton
            v-if="tab.closable"
            type="icon"
            status="secondary"
            class="tab-close-button"
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
        class="tab-button !mr-[8px]"
        :disabled="arrivedState.right"
        @click="scrollTabs('right')"
      >
        <MsIcon type="icon-icon_right_outlined" />
      </MsButton>
    </a-tooltip>
    <a-tooltip
      :content="t('ms.editableTab.limitTip', { max: props.limit })"
      :disabled="!props.limit || props.tabs.length >= props.limit"
    >
      <MsButton
        type="icon"
        status="secondary"
        class="tab-button !mr-[4px]"
        :disabled="!!props.limit && props.tabs.length >= props.limit"
        @click="addTab"
      >
        <MsIcon type="icon-icon_add_outlined" />
      </MsButton>
    </a-tooltip>
    <MsMoreAction v-if="props.moreActionList" :list="props.moreActionList">
      <MsButton type="icon" status="secondary" class="tab-button">
        <MsIcon type="icon-icon_more_outlined" />
      </MsButton>
    </MsMoreAction>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, ref, watch } from 'vue';
  import { useScroll, useVModel } from '@vueuse/core';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { useI18n } from '@/hooks/useI18n';

  import type { TabItem } from './types';

  const props = defineProps<{
    tabs: TabItem[];
    activeTab: string | number;
    moreActionList?: ActionsItem[];
    limit?: number; // 最多可打开的tab数量
  }>();
  const emit = defineEmits<{
    (e: 'update:activeTab', activeTab: string | number): void;
    (e: 'add'): void;
    (e: 'close', item: TabItem): void;
    (e: 'change', item: TabItem): void;
  }>();

  const { t } = useI18n();

  const innerActiveTab = useVModel(props, 'activeTab', emit);
  const tabNav = ref<HTMLElement | null>(null);
  const { arrivedState } = useScroll(tabNav);
  const isNotOverflow = computed(() => arrivedState.left && arrivedState.right); // 内容是否溢出，用于判断左右滑动按钮是否展示

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

  const scrollToActiveTab = () => {
    const activeTabDom = tabNav.value?.querySelector('.tab.active');
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

  watch(
    () => props.activeTab,
    (val) => {
      emit('change', props.tabs.find((item) => item.id === val) as TabItem);
    }
  );

  watch(props.tabs, () => {
    nextTick(() => {
      scrollToActiveTab();
    });
  });

  onMounted(() => {
    const resizeObserver = new ResizeObserver(() => {
      scrollToActiveTab();
    });
    resizeObserver.observe(tabNav.value as Element);
  });

  function addTab() {
    emit('add');
  }

  function close(item: TabItem) {
    emit('close', item);
  }

  function handleTabClick(item: TabItem) {
    emit('change', item);
    innerActiveTab.value = item.id;
    nextTick(() => {
      tabNav.value?.querySelector('.tab.active')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  }
</script>

<style lang="less" scoped>
  .tab-container {
    @apply flex items-center;

    height: 32px;
    .tab-nav {
      @apply relative flex  overflow-x-auto whitespace-nowrap;
      &::-webkit-scrollbar {
        width: 0; /* 宽度为0，隐藏垂直滚动条 */
        height: 0; /* 高度为0，隐藏水平滚动条 */
      }
      .tab {
        @apply flex cursor-pointer items-center;

        margin-right: 4px;
        padding: 5px 8px;
        border-radius: var(--border-radius-small);
        background-color: var(--color-text-n9);
        gap: 8px;
        &.active,
        &:hover {
          color: rgb(var(--primary-5));
          background-color: rgb(var(--primary-1));
          .tab-close-button {
            @apply visible;
          }
        }
        .tab-close-button {
          @apply invisible !rounded-full;

          margin-left: 4px !important;
        }
      }
    }
    .tab-button {
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
</style>
