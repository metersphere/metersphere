<template>
  <a-layout class="layout" :class="{ mobile: appStore.hideMenu }">
    <div v-if="navbar" class="layout-navbar z-[100]">
      <NavBar
        :is-preview="innerProps.isPreview"
        :hide-right="innerProps.hideRight"
        :logo="innerLogo"
        :name="innerName"
      />
    </div>
    <slot name="body">
      <a-layout>
        <a-layout>
          <a-layout-sider
            v-if="renderMenu && !innerProps.isPreview"
            v-show="!hideMenu"
            class="layout-sider z-[99]"
            breakpoint="xl"
            :collapsed="collapsed"
            :collapsible="true"
            :width="menuWidth"
            :collapsed-width="appStore.collapsedWidth"
            :style="{ paddingTop: navbar ? navbarHeight : '' }"
            :hide-trigger="true"
            @collapse="setCollapsed"
          >
            <div class="menu-wrapper">
              <MsMenu />
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
            <MsMenu />
          </a-drawer>
          <a-layout class="layout-content" :style="paddingStyle">
            <a-spin :loading="appStore.loading" :tip="appStore.loadingTip">
              <div class="layout-scroll-container">
                <MsBreadCrumb />
                <a-layout-content>
                  <slot name="page">
                    <!-- 授权到期在系统菜单展示提示 -->
                    <ExpireAlert v-if="isShowExpireTime" />
                    <PageLayout v-if="!props.isPreview" />
                  </slot>
                  <slot></slot>
                </a-layout-content>
                <Footer v-if="footer" />
              </div>
            </a-spin>
          </a-layout>
        </a-layout>
      </a-layout>
    </slot>
  </a-layout>
</template>

<script lang="ts" setup>
  import { computed, onMounted, provide, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import Footer from '@/components/pure/footer/index.vue';
  import NavBar from '@/components/pure/navbar/index.vue';
  import MsBreadCrumb from '@/components/business/ms-breadcrumb/index.vue';
  import ExpireAlert from '@/components/business/ms-expire-alert/index.vue';
  import MsMenu from '@/components/business/ms-menu/index.vue';
  import PageLayout from './page-layout.vue';

  import { GetTitleImgUrl } from '@/api/requrls/setting/config';
  import usePermission from '@/hooks/usePermission';
  import { useAppStore, useUserStore } from '@/store';

  interface Props {
    isPreview?: boolean;
    logo?: string;
    name?: string;
    singleLogo?: boolean;
    hideRight?: boolean;
  }
  const props = defineProps<Props>();

  const innerProps = ref<Props>(props);
  watch(
    () => props.logo,
    () => {
      innerProps.value = { ...props };
    }
  );

  watch(
    () => props.name,
    () => {
      innerProps.value = { ...props };
    }
  );

  const isInit = ref(false);
  const appStore = useAppStore();
  const userStore = useUserStore();
  const router = useRouter();
  const route = useRoute();
  const permission = usePermission();

  const innerLogo = computed(() => (props.isPreview && innerProps.value.logo ? innerProps.value.logo : GetTitleImgUrl));
  const innerName = computed(() => (props.isPreview ? innerProps.value.name : appStore.pageConfig.platformName));

  const navbarHeight = `56px`;
  const navbar = computed(() => appStore.navbar);
  const renderMenu = computed(() => appStore.menu);
  const hideMenu = computed(() => appStore.hideMenu);
  const footer = computed(() => appStore.footer);
  const menuWidth = computed(() => {
    return appStore.menuCollapse ? appStore.collapsedWidth : appStore.menuWidth;
  });
  const collapsed = computed(() => {
    return appStore.menuCollapse;
  });
  const paddingStyle = computed(() => {
    const paddingLeft = renderMenu.value && !hideMenu.value ? { paddingLeft: `${menuWidth.value}px` } : {};
    const paddingTop = navbar.value ? { paddingTop: navbarHeight } : {};
    return { ...paddingLeft, ...paddingTop };
  });
  const isShowExpireTime = computed(() => {
    return route.path.includes('/setting/system');
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
      @apply overflow-visible; // 防止展开收起按钮显示不全
    }
  }
  .menu-wrapper {
    @apply h-full overflow-visible; // 防止展开收起按钮显示不全
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
    :deep(.arco-menu-light:not(.arco-menu-collapsed)) {
      background-color: transparent !important;
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
    @apply box-content overflow-y-hidden;

    height: calc(100vh - 56px);
    background-color: var(--color-bg-3);
    transition: padding 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
    .arco-layout-content {
      overflow: hidden;
      padding: 0 16px 16px 0;
    }
  }
  .layout-scroll-container {
    @apply flex flex-col  overflow-auto;
    .ms-scroll-bar();

    gap: 8px;
    height: calc(100vh - 56px);
  }
  .arco-layout-sider-light {
    @apply bg-transparent;
  }
</style>
