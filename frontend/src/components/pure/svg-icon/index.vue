<script setup lang="ts">
  import { computed } from 'vue';

  interface Props {
    name: string;
    color?: string;
    width?: string;
    height?: string;
  }
  const props = withDefaults(defineProps<Props>(), {
    name: '',
    color: '#000',
    width: '16px',
    height: '16px',
  });
  const iconName = computed(() => `#icon-${props.name}`);
</script>

<template>
  <svg class="c-icon" aria-hidden="true">
    <use :xlink:href="iconName" :fill="color" />
  </svg>
</template>

<style scoped lang="less">
  .c-icon {
    @apply relative;

    width: v-bind(width);
    height: v-bind(height);
  }
</style>
