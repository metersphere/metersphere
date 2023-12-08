<template>
  <MsIcon
    v-if="props.avatar === 'default'"
    type="icon-icon_that_person"
    :size="props.size"
    class="text-[var(--color-text-4)]"
  />
  <a-avatar
    v-else-if="props.avatar === 'word'"
    :size="props.size"
    class="bg-[rgb(var(--primary-1))] text-[rgb(var(--primary-6))]"
  >
    <slot>{{ userStore.name?.substring(0, 4) }}</slot>
  </a-avatar>
  <a-avatar v-else :image-url="avatar" :size="props.size"></a-avatar>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import useUserStore from '@/store/modules/user/index';

  const userStore = useUserStore();

  const props = withDefaults(
    defineProps<{
      avatar?: 'default' | 'word' | string;
      size?: number;
    }>(),
    {
      avatar: 'default',
      size: 40,
    }
  );
</script>

<style lang="less" scoped></style>
