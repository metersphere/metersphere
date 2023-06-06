<template>
  <div class="tab-bar-container">
    <a-affix ref="affixRef" :offset-top="offsetTop">
      <div class="tab-bar-box pl-[20px]">
        <div class="tab-bar-scroll leading-[32px]">
          <div class="tags-wrap h-[48px] px-0 py-[4px]">
            <tab-item v-for="(tag, index) in tabList" :key="tag.fullPath" :index="index" :item-data="tag" />
          </div>
        </div>
        <div class="tag-bar-operation h-[32px] w-[100px]"></div>
      </div>
    </a-affix>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, onUnmounted } from 'vue';
  import type { RouteLocationNormalized } from 'vue-router';
  import { listenerRouteChange, removeRouteListener } from '@/utils/route-listener';
  import { useAppStore, useTabBarStore } from '@/store';
  import tabItem from './tab-item.vue';

  const appStore = useAppStore();
  const tabBarStore = useTabBarStore();

  const affixRef = ref();
  const tabList = computed(() => {
    return tabBarStore.getTabList;
  });
  const offsetTop = computed(() => {
    return appStore.navbar ? 60 : 0;
  });

  watch(
    () => appStore.navbar,
    () => {
      affixRef.value.updatePosition();
    }
  );
  listenerRouteChange((route: RouteLocationNormalized) => {
    if (!route.meta.noAffix && !tabList.value.some((tag) => tag.fullPath === route.fullPath)) {
      tabBarStore.updateTabList(route);
    }
  }, true);

  onUnmounted(() => {
    removeRouteListener();
  });
</script>

<style scoped lang="less">
  .tab-bar-container {
    @apply relative;

    background-color: var(--color-bg-1);
    .tab-bar-box {
      @apply flex p-0;

      border-bottom: 1px solid var(--color-border);
      background-color: var(--color-bg-1);
      .tab-bar-scroll {
        @apply flex-1 overflow-hidden;
        .tags-wrap {
          @apply overflow-x-auto whitespace-nowrap;
          :deep(.arco-tag) {
            @apply inline-flex cursor-pointer items-center;

            margin-right: 6px;
            &:first-child {
              .arco-tag-close-btn {
                @apply hidden;
              }
            }
          }
        }
      }
    }
  }
</style>
