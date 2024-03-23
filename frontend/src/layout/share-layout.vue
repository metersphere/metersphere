<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in" appear>
      <!-- transition内必须有且只有一个根元素，不然会导致二级路由的组件无法渲染 -->
      <div v-show="true" class="page-content">
        <!-- TODO 实验性组件，以后优化 -->
        <keep-alive>
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
      </div>
    </transition>
  </router-view>
</template>

<script lang="ts" setup>
  import MsCard from '@/components/pure/ms-card/index.vue';
</script>

<style lang="less" scoped>
  .page-content {
    @apply h-full overflow-y-auto;

    min-height: 500px;
  }
</style>
