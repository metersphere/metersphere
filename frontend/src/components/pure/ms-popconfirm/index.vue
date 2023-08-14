<template>
  <a-popconfirm v-bind="attrs" :type="props.type" class="w-[352px]" @before-ok="handleConfirm">
    <template v-if="props.type === 'error'" #icon>
      <MsIcon type="icon-icon_warning_filled" class="mr-[2px] text-xl text-[rgb(var(--danger-6))]" />
    </template>
    <slot v-if="props.type !== 'error'" name="icon"></slot>
    <template #content>
      <span class="font-semibold">
        {{ props.title }}
      </span>
      <div class="py-2 text-sm leading-6 text-[var(--color-text-2)]">
        {{ props.subTitleTip }}
      </div>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script setup lang="ts">
  import { useAttrs } from 'vue';

  export type types = 'error' | 'info' | 'success' | 'warning';

  const props = withDefaults(
    defineProps<{
      title: string;
      subTitleTip: string;
      type: types;
    }>(),
    {
      type: 'warning',
    }
  );
  const emits = defineEmits<{
    (e: 'confirm'): void;
  }>();
  const attrs = useAttrs();
  const handleConfirm = () => {
    emits('confirm');
  };
</script>

<style scoped lang="less"></style>
