<template>
  <a-breadcrumb v-if="showBreadcrumb" class="z-10 mb-[-8px] mt-[8px]">
    <a-breadcrumb-item v-for="crumb of appStore.breadcrumbList" :key="crumb.name" @click="jumpTo(crumb.name)">
      {{ isEdit ? t(crumb.editLocale || crumb.locale) : t(crumb.locale) }}
    </a-breadcrumb-item>
  </a-breadcrumb>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRouter, useRoute, RouteRecordName } from 'vue-router';
  import { useAppStore } from '@/store';
  import { listenerRouteChange } from '@/utils/route-listener';
  import { useI18n } from '@/hooks/useI18n';

  const appStore = useAppStore();
  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();
  const showBreadcrumb = computed(() => {
    const b = appStore.getCurrentTopMenu.meta?.breadcrumbs;
    return b && b.length > 0;
  });

  const isEdit = ref(false);

  /**
   * 监听路由变化，存储打开及选中的菜单
   */
  listenerRouteChange((newRoute) => {
    const { name } = newRoute;

    // 顶部菜单层级会全等
    if (name === appStore.currentTopMenu.name) {
      appStore.setBreadcrumbList(appStore.currentTopMenu?.meta?.breadcrumbs);
    } else if ((name as string).includes(appStore.currentTopMenu.name as string)) {
      // 顶部菜单内下钻的父子路由命名是包含关系，子路由会携带完整的父路由名称
      const currentRoute = router.currentRoute.value.matched[1].children.find((e) => e.name === name);
      const currentBreads = currentRoute?.meta?.breadcrumbs;
      appStore.setBreadcrumbList(currentBreads);
      // 下钻的三级路由一版都会区分编辑添加场景，根据场景展示不同的国际化路由信息
      const editTag = currentBreads && currentBreads[currentBreads.length - 1].editTag;
      setTimeout(() => {
        // 路由异步挂载，这里使用同步或者nextTick都取不到变化后的路由参数，所以使用定时器
        isEdit.value = editTag && route.query[editTag];
      }, 0);
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
      @apply cursor-auto font-normal;

      color: var(--color-text-2);
    }
  }
</style>
