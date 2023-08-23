<template>
  <div :class="`ms-button ms-button--${props.type}`" @click="clickHandler">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      type?: 'text';
    }>(),
    {
      type: 'text',
    }
  );

  const emit = defineEmits(['click']);

  function clickHandler() {
    emit('click');
  }
</script>

<style lang="less" scoped>
  .ms-button {
    @apply flex cursor-pointer items-center align-middle;
    &:not(:last-child) {
      @apply mr-4;
    }

    font-size: 1rem;
    line-height: 22px;
    color: rgb(var(--primary-5));
  }
</style>
