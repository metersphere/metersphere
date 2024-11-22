<template>
  <a-layout class="layout-content">
    <router-view v-slot="{ Component, route }">
      <transition name="fade" mode="out-in" appear>
        <!-- transition内必须有且只有一个根元素，不然会导致二级路由的组件无法渲染 -->
        <div v-show="true" class="page-content">
          <!-- TODO 实验性组件，以后优化 -->
          <keep-alive>
            <Suspense>
              <component :is="Component" :key="route.fullPath">
                <template #header="{ projectName }">
                  <div class="mb-[12px] flex w-full items-center px-[16px]">
                    <a-space>
                      <div class="one-line-text flex max-w-[145px] items-center">
                        <img
                          :src="pageConfig.logoPlatform[0]?.url || defaultPlatformLogo"
                          class="mr-[4px] h-[34px] w-[32px]"
                        />
                        <a-tooltip :content="pageConfig.platformName">
                          <div
                            class="one-line-text font-['Helvetica_Neue'] text-[16px] font-bold text-[rgb(var(--primary-5))]"
                          >
                            {{ pageConfig.platformName }}
                          </div>
                        </a-tooltip>
                      </div>
                      <a-tooltip :mouse-enter-delay="300" :content="projectName">
                        <div class="one-line-text max-w-[300px]"> {{ projectName }}</div>
                      </a-tooltip>
                    </a-space>
                  </div>
                </template>
              </component>
            </Suspense>
          </keep-alive>
        </div>
      </transition>
    </router-view>
  </a-layout>
</template>

<script lang="ts" setup>
  import useAppStore from '@/store/modules/app';

  const defaultPlatformLogo = `${import.meta.env.BASE_URL}images/MeterSphere-logo.svg`;

  const appStore = useAppStore();

  const pageConfig = ref({ ...appStore.pageConfig });
</script>

<style lang="less" scoped>
  .layout-content {
    width: 100%;
    height: 100%;
    @apply box-content overflow-y-hidden;

    height: 100vh;
    background-color: var(--color-bg-3);
    transition: padding 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
    .arco-layout-content {
      overflow: hidden;
      padding: 0 16px 16px 0;
    }
    .page-content {
      min-height: 500px;
      background: var(--color-text-n9);
      @apply h-full w-full overflow-y-auto p-4;
      .ms-scroll-bar();
    }
  }
  :deep(.arco-scrollbar-container) {
    width: 100% !important;
  }
</style>
