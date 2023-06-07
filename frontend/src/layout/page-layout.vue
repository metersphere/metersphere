<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in" appear>
      <!-- transition内必须有且只有一个根元素，不然会导致二级路由的组件无法渲染 -->
      <div class="page-content">
        <component :is="Component" v-if="route.meta.ignoreCache" :key="route.fullPath" />
        <keep-alive v-else :include="cacheList">
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
      </div>
    </transition>
  </router-view>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useTabBarStore } from '@/store';

  const tabBarStore = useTabBarStore();

  const cacheList = computed(() => tabBarStore.getCacheList);
</script>

<style lang="less" scoped>
  .page-content {
    overflow: hidden;
    border-radius: 12px;
    background: #ffffff;
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
  }
</style>
