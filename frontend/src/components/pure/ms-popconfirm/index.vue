<template>
  <a-popover v-bind="attrs" :type="props.type" :popup-visible="currentVisible" class="w-[352px]" trigger="click">
    <template #content>
      <div class="flex flex-row flex-nowrap items-center">
        <slot name="icon">
          <MsIcon type="icon-icon_warning_filled" class="mr-[2px] text-xl text-[rgb(var(--danger-6))]" />
        </slot>
        <span class="ml-2 font-semibold">
          {{ props.title }}
        </span>
      </div>
      <div class="ml-8 mt-2 text-sm text-[var(--color-text-2)]">
        {{ props.subTitleTip }}
      </div>
      <div class="mt-4 flex flex-row flex-nowrap justify-end gap-2">
        <a-button type="secondary" size="mini" :disabled="props.loading" @click="handleCancel">
          {{ props.cancelText || t('common.cancel') }}
        </a-button>
        <a-button type="primary" size="mini" :loading="props.loading" @click="handleConfirm">
          {{ props.okText || t('common.remove') }}
        </a-button>
      </div>
    </template>

    <slot></slot>
  </a-popover>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { ref, useAttrs, watchEffect } from 'vue';

  export type types = 'error' | 'info' | 'success' | 'warning';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      title: string;
      subTitleTip: string;
      type: types;
      loading?: boolean;
      okText?: string;
      cancelText?: string;
      visible?: boolean;
    }>(),
    {
      type: 'warning',
    }
  );
  const emits = defineEmits<{
    (e: 'confirm'): void;
    (e: 'cancel'): void;
  }>();

  const currentVisible = ref(props.visible || false);

  const attrs = useAttrs();
  const handleConfirm = () => {
    emits('confirm');
  };

  const handleCancel = () => {
    emits('cancel');
  };

  watchEffect(() => {
    currentVisible.value = props.visible;
  });
</script>

<style scoped lang="less"></style>
