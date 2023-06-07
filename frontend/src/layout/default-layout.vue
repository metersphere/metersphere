<template>
  <a-layout class="layout arco-reset" :class="{ mobile: appStore.hideMenu }">
    <div v-if="navbar" class="layout-navbar z-[100]">
      <NavBar />
    </div>
    <a-layout>
      <a-layout>
        <a-layout-sider
          v-if="renderMenu"
          v-show="!hideMenu"
          class="layout-sider z-[99]"
          breakpoint="xl"
          :collapsed="collapsed"
          :collapsible="true"
          :width="menuWidth"
          :style="{ paddingTop: navbar ? navbarHeight : '' }"
          :hide-trigger="true"
          @collapse="setCollapsed"
        >
          <div class="menu-wrapper">
            <Menu />
          </div>
        </a-layout-sider>
        <a-drawer
          v-if="hideMenu"
          :visible="drawerVisible"
          placement="left"
          :footer="false"
          mask-closable
          :closable="false"
          @cancel="drawerCancel"
        >
          <Menu />
        </a-drawer>
        <a-layout class="layout-content" :style="paddingStyle">
          <TabBar v-if="appStore.tabBar" />
          <a-layout-content>
            <PageLayout />
          </a-layout-content>
          <Footer v-if="footer" />
        </a-layout>
      </a-layout>
    </a-layout>
  </a-layout>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, provide, onMounted } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useAppStore, useUserStore } from '@/store';
  import NavBar from '@/components/navbar/index.vue';
  import Menu from '@/components/menu/index.vue';
  import Footer from '@/components/footer/index.vue';
  import TabBar from '@/components/tab-bar/index.vue';
  import usePermission from '@/hooks/usePermission';
  import useResponsive from '@/hooks/useResponsive';
  import PageLayout from './page-layout.vue';

  const isInit = ref(false);
  const appStore = useAppStore();
  const userStore = useUserStore();
  const router = useRouter();
  const route = useRoute();
  const permission = usePermission();
  useResponsive(true);
  const navbarHeight = `56px`;
  const navbar = computed(() => appStore.navbar);
  const renderMenu = computed(() => appStore.menu && !appStore.topMenu);
  const hideMenu = computed(() => appStore.hideMenu);
  const footer = computed(() => appStore.footer);
  const menuWidth = computed(() => {
    return appStore.menuCollapse ? 48 : appStore.menuWidth;
  });
  const collapsed = computed(() => {
    return appStore.menuCollapse;
  });
  const paddingStyle = computed(() => {
    const paddingLeft = renderMenu.value && !hideMenu.value ? { paddingLeft: `${menuWidth.value}px` } : {};
    const paddingTop = navbar.value ? { paddingTop: navbarHeight } : {};
    return { ...paddingLeft, ...paddingTop };
  });
  const setCollapsed = (val: boolean) => {
    if (!isInit.value) return; // for page initialization menu state problem
    appStore.updateSettings({ menuCollapse: val });
  };
  watch(
    () => userStore.role,
    (roleValue) => {
      if (roleValue && !permission.accessRouter(route)) router.push({ name: 'notFound' });
    }
  );
  const drawerVisible = ref(false);
  const drawerCancel = () => {
    drawerVisible.value = false;
  };
  provide('toggleDrawerMenu', () => {
    drawerVisible.value = !drawerVisible.value;
  });
  onMounted(() => {
    isInit.value = true;
  });
</script>

<style scoped lang="less">
  @nav-size-height: 56px;
  @layout-max-width: 1100px;
  .layout {
    @apply h-full w-full;
  }
  .layout-navbar {
    @apply fixed left-0 top-0 w-full;

    height: @nav-size-height;
  }
  .layout-sider {
    @apply fixed left-0 top-0 h-full shadow-none;

    transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
    > :deep(.arco-layout-sider-children) {
      @apply overflow-y-hidden;
    }
  }
  .menu-wrapper {
    @apply h-full overflow-auto overflow-x-hidden;
    :deep(.arco-menu) {
      ::-webkit-scrollbar {
        width: 12px;
        height: 4px;
      }
      ::-webkit-scrollbar-thumb {
        @apply bg-clip-padding;

        border: 4px solid transparent;
        background-color: var(--color-bg-6);
      }
      ::-webkit-scrollbar-thumb:hover {
        background-color: var(--color-bg-6);
      }
    }
    :deep(.arco-menu-light) {
      background-color: var(--color-bg-3) !important;
      .arco-menu-item {
        :hover {
          background-color: var(--color-bg-6);
        }
        .arco-menu-selected {
          background-color: var(--color-bg-6);
          :hover {
            background-color: var(--color-bg-6);
          }
        }
      }
    }
  }
  .layout-content {
    @apply overflow-y-hidden;

    min-height: 100vh;
    background-color: var(--color-bg-3);
    transition: padding 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
    .arco-layout-content {
      padding: 16px 16px 0;
    }
  }
</style>
