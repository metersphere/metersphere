<template>
  <MsIcon v-if="props.avatar === 'default'" type="icon-icon_that_person" size="40" class="text-[var(--color-text-4)]" />
  <a-avatar v-else-if="props.avatar === 'word'" class="bg-[rgb(var(--primary-1))] text-[rgb(var(--primary-6))]">
    <slot></slot>
  </a-avatar>
  <a-avatar v-else :image-url="avatar"></a-avatar>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const props = defineProps<{
    avatar: 'default' | 'word' | string;
  }>();
</script>

<style lang="less" scoped></style>
