<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in" appear>
      <!-- transition内必须有且只有一个根元素，不然会导致二级路由的组件无法渲染 -->
      <div
        v-show="true"
        class="page-content"
        :class="[
          licenseStore.expiredDuring && route.path.includes('/setting/system') ? 'h-[calc(100%-48px)]' : 'h-full',
        ]"
      >
        <!-- TODO 实验性组件，以后优化 -->
        <!-- <Suspense v-if="!route.meta.isCache">
          <component :is="Component" :key="route.fullPath" />
        </Suspense> -->
        <Suspense>
          <keep-alive :include="cacheStore.cacheViews">
            <component :is="Component" :key="route.name" />
          </keep-alive>
        </Suspense>
      </div>
    </transition>
  </router-view>
</template>

<script lang="ts" setup>
  import { useRoute, useRouter } from 'vue-router';

  import useCacheStore from '@/store/modules/cache/cache';
  import useLicenseStore from '@/store/modules/setting/license';

  const licenseStore = useLicenseStore();

  const cacheStore = useCacheStore();

  const router = useRouter();
  const route = useRoute();

  watch(
    () => route.name,
    (newName) => {
      const cachePath = cacheStore.cachePath.find((item) => item.cacheName === newName);
      if (cachePath && cachePath.type === 'ROUTE' && !cacheStore.cacheViews.includes(newName as string)) {
        cacheStore.setCache(cachePath.cacheName);
      }
    }
  );

  const handleRouteLeave = (to: any, from: any) => {
    if (to.name === from.name) {
      return;
    }
    // 选择了TAB离开的时候则清空TAB组件的缓存
    const isRemoveTab = cacheStore.cachePath.find(
      (item) => item.type === 'TAB' && cacheStore.cacheViews.includes(item.cacheName)
    );
    if (isRemoveTab) {
      cacheStore.removeCache(isRemoveTab.cacheName);
      return;
    }
    // 获取需要清理缓存的页面名称
    const namesToRemove = cacheStore.cacheViews.filter((name) => {
      const cachePath = cacheStore.cachePath.find((item) => item.cacheName === name);
      return cachePath && !(cachePath.toPathName || []).includes(to.name);
    });

    namesToRemove.forEach((name) => cacheStore.removeCache(name));
  };

  router.afterEach(handleRouteLeave);
</script>

<style lang="less" scoped>
  .page-content {
    min-height: 500px;
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
</style>
