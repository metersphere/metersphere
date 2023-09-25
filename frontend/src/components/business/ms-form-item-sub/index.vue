<template>
  <div class="mt-[4px] w-full text-[12px] text-[var(--color-text-4)]">
    {{ props.text }}
    <MsIcon
      v-if="props.showFillIcon"
      type="icon-icon_corner_right_up"
      class="cursor-pointer text-[rgb(var(--primary-6))]"
      @click="fillHeapByDefault"
    ></MsIcon>
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const props = withDefaults(
    defineProps<{
      text: string;
      showFillIcon?: boolean;
    }>(),
    {
      showFillIcon: true,
    }
  );
  const emit = defineEmits(['fill']);

  function fillHeapByDefault() {
    emit('fill');
  }
</script>

<style lang="less" scoped></style>
