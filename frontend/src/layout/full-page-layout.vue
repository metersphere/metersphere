<template>
  <a-layout class="layout">
    <router-view v-slot="{ Component, route }">
      <transition name="fade" mode="out-in" appear>
        <!-- transition内必须有且只有一个根元素，不然会导致二级路由的组件无法渲染 -->
        <div class="page-content">
          <component :is="Component" :key="route.fullPath" />
        </div>
      </transition>
    </router-view>
  </a-layout>
</template>

<script setup lang="ts"></script>

<style lang="less" scoped>
  .layout {
    @apply h-full w-full;
    .page-content {
      @apply overflow-y-auto;

      height: 100vh;
      background-color: var(--color-bg-3);
      .ms-scroll-bar();
    }
  }
</style>
