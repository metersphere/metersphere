<template>
  <a-drawer
    v-model:visible="visible"
    :width="props.width"
    :footer="props.footer"
    :mask="props.mask"
    :class="props.mask ? '' : 'ms-drawer-no-mask'"
    @ok="handleOk"
    @cancel="handleCancel"
    @close="handleCancel"
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
    <slot>
      <MsDescription v-if="props.descriptions?.length > 0" :descriptions="props.descriptions"></MsDescription>
    </slot>
  </a-drawer>
</template>

<script setup lang="ts">
  import { ref, watch, defineAsyncComponent } from 'vue';
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
    [key: string]: any;
  }

  const props = withDefaults(defineProps<DrawerProps>(), {
    footer: true,
    mask: true,
  });
  const emit = defineEmits(['update:visible']);

  const visible = ref(props.visible);

  watch(
    () => props.visible,
    (val) => {
      visible.value = val;
    }
  );

  const handleOk = () => {
    visible.value = false;
  };
  const handleCancel = () => {
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
  .ms-drawer-no-mask {
    left: auto;
    .arco-drawer {
      box-shadow: -1px 0 4px 0 rgb(2 2 2 / 10%);
    }
  }
</style>
