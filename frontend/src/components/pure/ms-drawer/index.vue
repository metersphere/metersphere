<template>
  <a-drawer
    v-model:visible="visible"
    :width="props.width"
    :footer="props.footer"
    :mask="props.mask"
    :class="['ms-drawer', props.mask ? '' : 'ms-drawer-no-mask']"
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
    <a-scrollbar
      :style="{
        overflowY: 'auto',
        height: 'calc(100vh - 146px)',
      }"
    >
      <slot>
        <MsDescription
          v-if="props.descriptions && props.descriptions.length > 0"
          :descriptions="props.descriptions"
          :show-skeleton="props.showSkeleton"
          :skeleton-line="10"
        ></MsDescription>
      </slot>
    </a-scrollbar>
    <template #footer>
      <slot name="footer">
        <a-button :disabled="props.okLoading" @click="handleCancel">
          {{ t(props.cancelText || 'ms.drawer.cancel') }}
        </a-button>
        <a-button type="primary" :loading="props.okLoading" @click="handleOk">
          {{ t(props.okText || 'ms.drawer.ok') }}
        </a-button>
      </slot>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
  import { ref, watch, defineAsyncComponent } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

  import type { Description } from '@/components/pure/ms-description/index.vue';

  // 懒加载描述组件
  const MsDescription = defineAsyncComponent(() => import('@/components/pure/ms-description/index.vue'));

  interface DrawerProps {
    visible: boolean;
    title: string | undefined;
    titleTag?: string;
    titleTagColor?: string;
    descriptions?: Description[];
    footer?: boolean;
    mask?: boolean;
    showSkeleton?: boolean;
    okLoading?: boolean;
    okText?: string;
    cancelText?: string;
    width: number;
  }

  const props = withDefaults(defineProps<DrawerProps>(), {
    footer: true,
    mask: true,
    showSkeleton: false,
  });
  const emit = defineEmits(['update:visible', 'confirm', 'cancel']);

  const { t } = useI18n();

  const visible = ref(props.visible);

  watch(
    () => props.visible,
    (val) => {
      visible.value = val;
    }
  );

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
</style>
