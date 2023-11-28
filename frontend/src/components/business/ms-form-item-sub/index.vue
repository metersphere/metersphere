<template>
  <div class="mt-[4px] flex w-full items-center text-[12px] leading-[16px] text-[var(--color-text-4)]">
    {{ props.text }}
    <MsIcon
      v-if="props.showFillIcon"
      type="icon-icon_corner_right_up"
      class="cursor-pointer text-[rgb(var(--primary-6))]"
      @click="fillHeapByDefault"
    ></MsIcon>
    <MsIcon
      v-else-if="props.icon"
      :type="props.icon"
      class="cursor-pointer text-[rgb(var(--primary-6))]"
      @click="emit('iconClick')"
    ></MsIcon>
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const props = withDefaults(
    defineProps<{
      text: string;
      showFillIcon?: boolean;
      icon?: string;
      iconText?: string;
    }>(),
    {
      showFillIcon: true,
    }
  );
  const emit = defineEmits(['fill', 'iconClick']);

  function fillHeapByDefault() {
    emit('fill');
  }
</script>

<style lang="less" scoped></style>
