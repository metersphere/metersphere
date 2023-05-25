<template>
  <div class="tab-bar-container">
    <a-affix ref="affixRef" :offset-top="offsetTop">
      <div class="tab-bar-box">
        <div class="tab-bar-scroll">
          <div class="tags-wrap">
            <tab-item v-for="(tag, index) in tabList" :key="tag.fullPath" :index="index" :item-data="tag" />
          </div>
        </div>
        <div class="tag-bar-operation"></div>
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
    position: relative;
    background-color: var(--color-bg-1);
    .tab-bar-box {
      display: flex;
      padding: 0 0 0 20px;
      border-bottom: 1px solid var(--color-border);
      background-color: var(--color-bg-1);
      .tab-bar-scroll {
        overflow: hidden;
        height: 32px;
        flex: 1;
        .tags-wrap {
          overflow-x: auto;
          padding: 4px 0;
          height: 48px;
          white-space: nowrap;
          :deep(.arco-tag) {
            display: inline-flex;
            align-items: center;
            margin-right: 6px;
            cursor: pointer;
            &:first-child {
              .arco-tag-close-btn {
                display: none;
              }
            }
          }
        }
      }
    }
    .tag-bar-operation {
      width: 100px;
      height: 32px;
    }
  }
</style>
