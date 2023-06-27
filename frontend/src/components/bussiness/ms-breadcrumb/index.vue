<template>
  <a-breadcrumb v-if="showBreadcrumb" class="z-10 mb-[-8px] mt-[8px]">
    <a-breadcrumb-item v-for="crumb of appStore.breadcrumbList" :key="crumb.name" @click="jumpTo(crumb.name)">
      {{ t(crumb.locale) }}
    </a-breadcrumb-item>
  </a-breadcrumb>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { useRouter, RouteRecordName } from 'vue-router';
  import { useAppStore } from '@/store';
  import { listenerRouteChange } from '@/utils/route-listener';
  import { useI18n } from '@/hooks/useI18n';

  const appStore = useAppStore();
  const { t } = useI18n();
  const router = useRouter();
  const showBreadcrumb = computed(() => {
    const b = appStore.getCurrentTopMenu.meta?.breadcrumbs;
    return b && b.length > 0;
  });

  /**
   * 监听路由变化，存储打开及选中的菜单
   */
  listenerRouteChange((newRoute) => {
    const { name } = newRoute;
    if (name === appStore.currentTopMenu.name) {
      appStore.setBreadcrumbList(appStore.currentTopMenu?.meta?.breadcrumbs);
    } else {
      appStore.setBreadcrumbList([]);
    }
  }, true);

  function jumpTo(name: RouteRecordName) {
    router.push({ name });
  }
</script>

<style lang="less">
  /** 面包屑 **/
  .arco-breadcrumb-item {
    @apply cursor-pointer;

    color: var(--color-text-4);
    &:hover {
      color: rgb(var(--primary-5));
    }
    &:disabled {
      color: var(--color-text-brand);
    }
    &:last-child {
      @apply cursor-auto;

      color: var(--color-text-2);
    }
  }
</style>
