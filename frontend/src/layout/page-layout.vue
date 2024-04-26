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
        <Suspense v-if="!route.meta.isCache">
          <component :is="Component" :key="route.fullPath" />
        </Suspense>
        <keep-alive v-else>
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
      </div>
    </transition>
  </router-view>
</template>

<script lang="ts" setup>
  import useLicenseStore from '@/store/modules/setting/license';

  const licenseStore = useLicenseStore();
</script>

<style lang="less" scoped>
  .page-content {
    min-height: 500px;
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
</style>
