<template>
  <div :class="`ms-button ms-button-${props.type} ms-button--${props.status}`" @click="clickHandler">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      type?: 'text' | 'icon' | 'button';
      status?: 'primary' | 'danger' | 'secondary';
    }>(),
    {
      type: 'text',
      status: 'primary',
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

    padding: 0 4px;
    font-size: 1rem;
    border-radius: var(--border-radius-mini);
    line-height: 22px;
  }
  .ms-button-text {
    @apply p-0;

    color: rgb(var(--primary-5));
  }
  .ms-button-icon {
    padding: 4px;
    color: var(--color-text-4);
    &:hover {
      color: rgb(var(--primary-5));
      background-color: rgb(var(--primary-9));
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
  .ms-button--secondary {
    color: var(--color-text-2);
    &:not(.ms-button-text):hover {
      background-color: var(--color-text-n8);
    }
  }
  .ms-button--primary {
    color: rgb(var(--primary-5));
    &:not(.ms-button-text):hover {
      background-color: rgb(var(--primary-9));
    }
  }
  .ms-button--danger {
    color: rgb(var(--danger-6));
    &:not(.ms-button-text):hover {
      color: rgb(var(--danger-6));
      background-color: rgb(var(--danger-1));
    }
  }
</style>
