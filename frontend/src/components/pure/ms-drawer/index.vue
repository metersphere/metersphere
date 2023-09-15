<template>
  <a-drawer
    v-bind="props"
    v-model:visible="visible"
    :width="props.width"
    :footer="props.footer"
    :mask="props.mask"
    :class="[
      'ms-drawer',
      props.mask ? '' : 'ms-drawer-no-mask',
      props.noContentPadding ? 'ms-drawer-no-content-padding' : '',
    ]"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template #title>
      <slot name="title">
        <div class="flex w-full justify-between">
          {{ props.title }}
          <a-tag v-if="titleTag" :color="props.titleTagColor" class="ml-[8px] mr-auto">{{ props.titleTag }}</a-tag>
          <slot name="tbutton"></slot>
        </div>
      </slot>
    </template>
    <a-scrollbar class="overflow-y-auto" :style="{ height: `calc(100vh - ${contentExtraHeight}px)` }">
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
                {{
                  item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value
                }}
              </slot>
            </template>
          </MsDescription>
        </slot>
      </div>
    </a-scrollbar>
    <template #footer>
      <slot name="footer">
        <a-button :disabled="props.okLoading" @click="handleCancel">
          {{ t(props.cancelText || 'ms.drawer.cancel') }}
        </a-button>
        <a-button v-if="showContinue" type="secondary" :loading="props.okLoading" @click="handleContinue">
          {{ t(props.saveContinueText || 'ms.drawer.saveContinue') }}
        </a-button>
        <a-button type="primary" :loading="props.okLoading" @click="handleOk">
          {{ t(props.okText || 'ms.drawer.ok') }}
        </a-button>
      </slot>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
  import { ref, watch, defineAsyncComponent, computed } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

  import type { Description } from '@/components/pure/ms-description/index.vue';

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
    okText?: string;
    cancelText?: string;
    saveContinueText?: string;
    showContinue?: boolean;
    width: number;
    noContentPadding?: boolean; // 是否没有内容内边距
  }

  const props = withDefaults(defineProps<DrawerProps>(), {
    footer: true,
    mask: true,
    showSkeleton: false,
    showContinue: false,
  });
  const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'continue']);

  const { t } = useI18n();

  const visible = ref(props.visible);

  watch(
    () => props.visible,
    (val) => {
      visible.value = val;
    }
  );

  const contentExtraHeight = computed(() => {
    // 默认有页脚、内边距时的额外高度146，内边距 30，页脚 60
    return 146 - (props.noContentPadding ? 30 : 0) - (props.footer ? 0 : 60);
  });

  const handleContinue = () => {
    emit('continue');
  };

  const handleOk = () => {
    emit('confirm');
  };

  const handleCancel = () => {
    visible.value = false;
    emit('update:visible', false);
    emit('cancel');
  };

  const handleClose = () => {
    visible.value = false;
    emit('update:visible', false);
  };
</script>

<style lang="less">
  .arco-drawer {
    @apply bg-white;
    .arco-drawer-header {
      height: 56px;
      border-bottom: 1px solid var(--color-text-n8);
      .arco-drawer-title {
        @apply w-full;
      }
      .arco-drawer-close-btn {
        margin-left: 16px;
        color: var(--color-text-2);
      }
    }
    .arco-drawer-footer {
      border-bottom: 1px solid var(--color-text-n8);
    }
  }
  .ms-drawer {
    .ms-drawer-body {
      @apply h-full;
    }
    .arco-scrollbar-track-direction-vertical {
      right: -12px;
    }
  }
  .ms-drawer-no-mask {
    left: auto;
    .arco-drawer {
      box-shadow: -1px 0 4px 0 rgb(2 2 2 / 10%);
    }
  }
  .ms-drawer-no-content-padding {
    .arco-drawer-body {
      @apply p-0;
    }
  }
</style>
