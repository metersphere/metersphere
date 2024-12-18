<template>
  <a-drawer
    v-bind="props"
    v-model:visible="visible"
    :width="fullScreen?.isFullScreen ? '100%' : drawerWidth"
    :footer="props.footer"
    :mask="props.mask"
    :popup-container="props.popupContainer"
    :class="[
      'ms-drawer',
      props.mask ? '' : 'ms-drawer-no-mask',
      props.noContentPadding ? 'ms-drawer-no-content-padding' : '',
      props.noTitle ? 'ms-drawer-no-title' : '',
    ]"
    :on-before-cancel="handleBeforeCancel"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template #title>
      <div class="flex items-center justify-between gap-[4px]">
        <slot name="title">
          <div class="flex flex-1 items-center justify-between overflow-hidden">
            <div class="flex flex-1 items-center overflow-hidden">
              <a-tooltip :disabled="!props.title" :content="props.title">
                <span class="one-line-text max-w-[300px]"> {{ props.title }}</span>
              </a-tooltip>

              <slot name="headerLeft"></slot>
              <a-tag v-if="titleTag" :color="props.titleTagColor" class="ml-[8px] mr-auto">
                {{ props.titleTag }}
              </a-tag>
            </div>
            <slot name="tbutton"></slot>
          </div>
        </slot>
        <div class="ms-drawer-right-operation-button">
          <MsButton
            v-if="props.showFullScreen"
            type="icon"
            status="secondary"
            class="ms-drawer-fullscreen-btn"
            @click="fullScreen?.toggleFullScreen"
          >
            <MsIcon
              :type="fullScreen?.isFullScreen ? 'icon-icon_off_screen' : 'icon-icon_full_screen_one'"
              class="ms-drawer-fullscreen-btn-icon"
              size="14"
            />
            {{ fullScreen?.isFullScreen ? t('common.offFullScreen') : t('common.fullScreen') }}
          </MsButton>
        </div>
      </div>
    </template>
    <div
      v-if="!props.disabledWidthDrag && typeof drawerWidth === 'number' && !fullScreen?.isFullScreen"
      class="handle"
      @mousedown="startResize"
    >
      <icon-drag-dot-vertical class="absolute left-[-3px] top-[50%] w-[14px]" size="14" />
    </div>
    <div class="ms-drawer-body">
      <slot>
        <MsDescription
          v-if="props.descriptions && props.descriptions.length > 0"
          :descriptions="props.descriptions"
          :show-skeleton="props.showSkeleton"
          :skeleton-line="10"
        >
          <template #value="{ item }">
            <slot name="descValue" :item="item">
              <a-tooltip
                :content="`${item.value}`"
                :disabled="item.value === undefined || item.value === null || item.value?.toString() === ''"
              >
                <div>
                  {{
                    item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value
                  }}
                </div>
              </a-tooltip>
            </slot>
          </template>
        </MsDescription>
      </slot>
    </div>
    <template #footer>
      <slot name="footer">
        <div class="flex items-center justify-between">
          <slot name="footerLeft"></slot>
          <div class="ml-auto flex gap-[12px]">
            <a-button :disabled="props.okLoading" @click="handleCancel">
              {{ t(props.cancelText || 'ms.drawer.cancel') }}
            </a-button>
            <a-button
              v-if="showContinue"
              v-permission="props.okPermission || []"
              type="secondary"
              :loading="props.okLoading"
              :disabled="okDisabled"
              @click="handleContinue"
            >
              {{ t(props.saveContinueText || 'ms.drawer.saveContinue') }}
            </a-button>
            <a-button
              v-permission="props.okPermission || []"
              type="primary"
              :disabled="okDisabled"
              :loading="props.okLoading"
              @click="handleOk"
            >
              {{ t(props.okText || 'ms.drawer.ok') }}
            </a-button>
          </div>
        </div>
      </slot>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
  import { defineAsyncComponent, ref, watch } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { Description } from '@/components/pure/ms-description/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import useFullScreen, { UseFullScreen } from '@/hooks/useFullScreen';
  import { useI18n } from '@/hooks/useI18n';
  import { getMaxZIndexLayer } from '@/utils/dom';
  // 懒加载描述组件
  const MsDescription = defineAsyncComponent(() => import('@/components/pure/ms-description/index.vue'));

  interface DrawerProps {
    visible: boolean;
    title?: string | undefined;
    titleTag?: string;
    titleTagColor?: string;
    descriptions?: Description[];
    footer?: boolean;
    mask?: boolean; // 是否显示遮罩
    showSkeleton?: boolean; // 是否显示骨架屏
    okLoading?: boolean;
    okDisabled?: boolean;
    okPermission?: string[]; // 确认按钮权限
    okText?: string;
    cancelText?: string;
    saveContinueText?: string;
    showContinue?: boolean;
    width: string | number; // 抽屉宽度，为数值时才可拖拽改变宽度
    noContentPadding?: boolean; // 是否没有内容内边距
    popupContainer?: string;
    disabledWidthDrag?: boolean; // 是否禁止拖拽宽度
    closable?: boolean; // 是否显示右上角的关闭按钮
    noTitle?: boolean; // 是否不显示标题栏
    drawerStyle?: Record<string, string>; // 抽屉样式
    showFullScreen?: boolean; // 是否显示全屏按钮
    maskClosable?: boolean; // 点击遮罩是否关闭
    unmountOnClose?: boolean; // 关闭时销毁组件
    handleBeforeCancel?: () => boolean;
  }

  const props = withDefaults(defineProps<DrawerProps>(), {
    footer: true,
    mask: true,
    closable: true,
    showSkeleton: false,
    showContinue: false,
    popupContainer: 'body',
    disabledWidthDrag: false,
    showFullScreen: false,
    maskClosable: true,
    unmountOnClose: false,
    okPermission: () => [], // 确认按钮权限
  });
  const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'continue', 'close']);

  const { t } = useI18n();

  const visible = ref(props.visible);
  const fullScreen = ref<UseFullScreen>();

  watch(
    () => props.visible,
    (val) => {
      visible.value = val;
    }
  );

  const handleContinue = () => {
    emit('continue');
  };

  const handleOk = () => {
    emit('confirm');
  };

  const handleCancel = () => {
    fullScreen.value?.exitFullscreen();
    visible.value = false;
    emit('update:visible', false);
    emit('cancel');
  };

  const handleClose = () => {
    fullScreen.value?.exitFullscreen();
    visible.value = false;
    emit('update:visible', false);
    emit('close');
  };

  // 关闭抽屉时进行拦截
  const handleBeforeCancel = () => {
    return props.handleBeforeCancel ? props.handleBeforeCancel() : true;
  };

  const resizing = ref(false); // 是否正在拖拽
  const drawerWidth = ref(props.width); // 抽屉初始宽度

  /**
   * 鼠标单击开始监听拖拽移动
   */
  const startResize = (event: MouseEvent) => {
    if (typeof drawerWidth.value === 'number') {
      resizing.value = true;
      const startX = event.clientX;
      const initialWidth = drawerWidth.value;

      // 计算鼠标移动距离
      const handleMouseMove = (_event: MouseEvent) => {
        if (resizing.value) {
          const newWidth = initialWidth + (startX - _event.clientX); // 新的宽度等于当前抽屉宽度+鼠标移动的距离
          if (
            typeof props.width === 'number' &&
            newWidth >= (props.width || 480) &&
            newWidth <= window.innerWidth * 0.9
          ) {
            // 最大最小宽度限制，最小宽度为传入的width或480，最大宽度为视图窗口宽度的90%
            drawerWidth.value = newWidth;
          }
        }
      };

      // 松开鼠标按键，拖拽结束
      const handleMouseUp = () => {
        if (resizing.value) {
          // 如果当前是在拖拽，则重置拖拽状态，且移除鼠标监听事件
          resizing.value = false;
          window.removeEventListener('mousemove', handleMouseMove);
          window.removeEventListener('mouseup', handleMouseUp);
        }
      };

      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
    }
  };

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        nextTick(() => {
          const topDrawer = getMaxZIndexLayer('.ms-drawer');
          fullScreen.value = useFullScreen(topDrawer?.querySelector('.arco-drawer'));
        });
      }
    },
    { immediate: true }
  );
</script>

<style lang="less" scoped>
  .arco-scrollbar {
    @apply h-full;
  }
</style>

<style lang="less">
  .arco-drawer {
    max-width: 100vw;
    background-color: var(--color-text-fff);
    .arco-drawer-header {
      display: flex;
      height: 56px;
      border-bottom: 1px solid var(--color-text-n8);
      .arco-drawer-title {
        flex: 1;
        overflow: hidden;
        line-height: 24px;
        .ms-drawer-right-operation-button {
          .ms-button-icon,
          .ms-drawer-fullscreen-btn {
            border-radius: var(--border-radius-small);
            color: var(--color-text-1);
            .arco-icon,
            .ms-drawer-fullscreen-btn-icon {
              margin-right: 8px;
              color: var(--color-text-1);
            }
            &:hover {
              color: rgb(var(--primary-5));
              .arco-icon {
                color: rgb(var(--primary-5));
              }
            }
          }
        }
      }
      .arco-drawer-close-btn {
        @apply flex items-center;

        margin-left: 16px;
        color: var(--color-text-2);
      }
    }
    .arco-drawer-footer {
      border-color: var(--color-text-n8);
      border-bottom: 1px solid var(--color-text-n8);
    }
  }
  .ms-drawer {
    .arco-drawer-body {
      @apply overflow-hidden;
    }
    .ms-drawer-body {
      @apply h-full w-full overflow-auto;
      .ms-scroll-bar();

      min-width: 448px; // 480 - 左右边距 32
      min-height: 500px;
    }
    .arco-scrollbar-track-direction-vertical {
      right: -12px;
    }
  }
  .ms-drawer-no-title {
    .arco-drawer-header {
      @apply hidden;
    }
  }
  .ms-drawer-no-mask {
    left: auto;
    .arco-drawer {
      box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    }
  }
  .ms-drawer-no-content-padding {
    .arco-drawer-body {
      @apply p-0;
    }
  }
  .handle {
    @apply absolute left-0 top-0 flex h-full items-center;

    z-index: 200;
    width: 8px;
    background-color: var(--color-text-n8);
    cursor: col-resize;
  }
</style>
