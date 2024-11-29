<template>
  <a-spin class="!block" :class="props.autoHeight ? '' : 'min-h-[500px]'" :loading="props.loading" :size="28">
    <div
      ref="fullRef"
      :class="[
        'ms-card',
        'relative',
        'h-full',
        props.isFullscreen || isFullScreen ? 'ms-card--fullScreen' : '',
        props.noContentPadding ? 'ms-card--noContentPadding' : 'p-[16px]',
        props.noBottomRadius ? 'ms-card--noBottomRadius' : '',
        !props.hideFooter && !props.simple ? 'pb-[24px]' : '',
      ]"
    >
      <a-scrollbar v-if="!props.simple" :style="{ overflow: 'auto' }">
        <div class="ms-card-header" :style="props.headerMinWidth ? { minWidth: `${props.headerMinWidth}px` } : {}">
          <slot name="headerLeft">
            <div class="font-medium text-[var(--color-text-1)]">{{ props.title }}</div>
            <div class="text-[var(--color-text-4)]">{{ props.subTitle }}</div>
          </slot>
          <div class="ml-auto flex items-center">
            <slot name="headerRight"></slot>
            <div
              v-if="props.showFullScreen"
              class="cursor-pointer text-right !text-[var(--color-text-4)]"
              @click="toggleFullScreen"
            >
              <MsIcon v-if="isFullScreen" type="icon-icon_minify_outlined" />
              <MsIcon v-else type="icon-icon_magnify_outlined" />
              {{ t(isFullScreen ? 'common.offFullScreen' : 'common.fullScreen') }}
            </div>
          </div>
          <div v-if="$slots.subHeader" class="basis-full">
            <slot name="subHeader"></slot>
          </div>
        </div>
      </a-scrollbar>
      <div :class="{ 'px-[16px]': props.dividerHasPX }">
        <a-divider v-if="!props.simple && !props.hideDivider" class="mb-[16px] mt-0" />
      </div>
      <div class="ms-card-container">
        <a-scrollbar :class="['h-full', props.noContentPadding ? '' : 'pr-[5px]']" :style="getComputedContentStyle">
          <div class="relative h-full w-full" :style="{ minWidth: `${props.minWidth || 1000}px` }">
            <slot></slot>
          </div>
        </a-scrollbar>
      </div>
      <div
        v-if="!props.hideFooter && !props.simple"
        class="ms-card-footer"
        :style="{ width: props.isFullscreen || isFullScreen ? '100%' : `calc(100% - ${menuWidth + 16}px)` }"
      >
        <div class="ml-0 mr-auto">
          <slot name="footerLeft"></slot>
        </div>
        <slot name="footerRight">
          <div class="flex justify-end gap-[16px]">
            <a-button :disabled="props.loading" type="secondary" @click="back">
              {{ t('mscard.defaultCancelText') }}
            </a-button>
            <a-button
              v-if="!props.hideContinue && !props.isEdit"
              :loading="props.loading"
              type="secondary"
              @click="emit('saveAndContinue')"
            >
              {{ props.saveAndContinueText || t('mscard.defaultSaveAndContinueText') }}
            </a-button>
            <a-button :loading="props.loading" type="primary" @click="emit('save')">
              {{ props.saveText || t(props.isEdit ? 'mscard.defaultUpdate' : 'mscard.defaultConfirm') }}
            </a-button>
          </div>
        </slot>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { computed, watch } from 'vue';
  import { useRouter } from 'vue-router';

  import useFullScreen from '@/hooks/useFullScreen';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const props = withDefaults(
    defineProps<
      Partial<{
        simple: boolean; // 简单模式，没有标题和底部栏
        title: string; // 卡片标题
        subTitle: string; // 卡片副标题
        hideContinue: boolean; // 隐藏保存并继续创建按钮
        hideFooter: boolean; // 隐藏底部栏
        loading: boolean; // 卡片 loading 状态
        isEdit: boolean; // 是否编辑状态
        specialHeight: number; // 特殊高度，例如某些页面有面包屑，autoHeight 时无效
        hideBack: boolean; // 隐藏返回按钮
        autoHeight: boolean; // 内容区域高度是否自适应
        autoWidth: boolean; // 内容区域宽度是否自适应
        otherWidth: number; // 该宽度为卡片外部同级容器的宽度
        headerMinWidth: number; // 卡片头部最小宽度
        minWidth: number; // 卡片内容最小宽度
        hasBreadcrumb: boolean; // 是否有面包屑，如果有面包屑，高度需要减去面包屑的高度
        noContentPadding: boolean; // 内容区域是否有padding
        noBottomRadius?: boolean; // 底部是否有圆角
        isFullscreen?: boolean; // 是否全屏
        hideDivider?: boolean; // 是否隐藏分割线
        handleBack: () => void; // 自定义返回按钮触发事件
        dividerHasPX: boolean; // 分割线是否有左右padding;
        showFullScreen: boolean; // 是否显示全屏按钮
        saveText?: string; // 保存按钮文案
        saveAndContinueText?: string; // 保存并继续按钮文案
      }>
    >(),
    {
      simple: false,
      hideContinue: false,
      hideFooter: false,
      isEdit: false,
      specialHeight: 0,
      hideBack: false,
      autoHeight: false,
      autoWidth: false,
      hasBreadcrumb: false,
      noContentPadding: false,
      noBottomRadius: false,
      dividerHasPX: false,
    }
  );

  const emit = defineEmits(['saveAndContinue', 'save', 'toggleFullScreen']);

  const router = useRouter();
  const { t } = useI18n();

  const appStore = useAppStore();
  const menuWidth = computed(() => {
    return appStore.menuCollapse ? appStore.collapsedWidth : appStore.menuWidth;
  });

  // 用于全屏的容器 ref
  const fullRef = ref<HTMLElement | null>();
  const { isFullScreen, toggleFullScreen } = useFullScreen(fullRef);

  watch(
    () => isFullScreen.value,
    (val) => {
      emit('toggleFullScreen', val);
    }
  );

  const _specialHeight = props.hasBreadcrumb ? 32 + props.specialHeight : props.specialHeight; // 有面包屑的话，默认面包屑高度24+8间距

  // TODO：卡片高度调整，写上数值的注释
  const cardOverHeight = computed(() => {
    const contentPadding = 32; // 16+16 上下内容边距
    const navbarHeight = 56; // 顶部导航高度
    const layoutContentPaddingBottom = 16; // 卡片到底部距离
    if (isFullScreen.value) {
      return 106;
    }
    if (props.simple) {
      // 简单模式没有标题、没有底部
      return props.noContentPadding
        ? navbarHeight + layoutContentPaddingBottom + _specialHeight
        : navbarHeight + layoutContentPaddingBottom + contentPadding + _specialHeight;
    }
    if (props.hideFooter) {
      // 没有底部
      return props.noContentPadding ? 130 + _specialHeight : 168 + _specialHeight;
    }
    return 220 + _specialHeight;
  });

  const getComputedContentStyle = computed(() => {
    if (props.isFullscreen || isFullScreen.value || props.noContentPadding) {
      return {
        overflow: 'auto',
        width: 'auto',
        height: props.autoHeight ? 'auto' : `calc(100vh - ${cardOverHeight.value}px)`,
      };
    }
    const width = props.otherWidth
      ? `calc(100vw - ${menuWidth.value}px - ${props.otherWidth}px)`
      : `calc(100vw - ${menuWidth.value}px - 48px)`; // 48px 为卡片左右内边距 32+ 页面右侧内边距16
    return {
      overflow: 'auto',
      width: props.autoWidth ? 'auto' : width,
      height: props.autoHeight ? 'auto' : `calc(100vh - ${cardOverHeight.value}px)`,
    };
  });

  function back() {
    if (typeof props.handleBack === 'function') {
      props.handleBack();
    } else {
      router.back();
    }
  }
</script>

<style lang="less" scoped>
  .ms-card {
    @apply relative overflow-hidden;

    border-radius: var(--border-radius-large);
    background-color: var(--color-text-fff);
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
    &--noContentPadding {
      border-radius: var(--border-radius-large);
      .ms-card-header {
        padding: 16px;
      }
      .arco-divider {
        @apply mb-0;
      }
    }
    &--noBottomRadius {
      border-radius: var(--border-radius-large) var(--border-radius-large) 0 0;
    }
    .ms-card-header {
      @apply flex flex-wrap items-center;

      padding-bottom: 16px;
      .back-btn {
        @apply flex cursor-pointer items-center rounded-full;

        margin-right: 8px;
        width: 20px;
        height: 20px;
        border: 1px solid #ffffff;
        background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
        box-shadow: 0 0 7px rgb(15 0 78 / 9%);
        .arco-icon {
          color: rgb(var(--primary-5));
        }
      }
    }
    .ms-card-container {
      @apply relative;
    }
    .ms-card-footer {
      @apply fixed flex justify-between;

      right: 16px;
      bottom: 0;
      z-index: 100;
      padding: 24px;
      border-bottom: 0;
      background-color: var(--color-text-fff);
      box-shadow: var(--tw-ring-offset-shadow, 0 0 #00000000), var(--tw-ring-shadow, 0 0 #00000000), var(--tw-shadow);

      --tw-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
      --tw-shadow-colored: 0 -1px 4px var(--tw-shadow-color);
    }
  }
  .ms-card--fullScreen {
    border-radius: 0;
    .ms-card-footer {
      @apply left-0 right-0 w-full;
    }
  }
</style>
