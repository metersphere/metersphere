<template>
  <MsIcon
    v-if="innerAvatar === 'default' || innerAvatar === null"
    type="icon-icon_that_person"
    :size="props.size"
    class="text-[var(--color-text-4)]"
    :style="{
      fontSize: `${props.size}px !important`,
    }"
  />
  <a-avatar
    v-else-if="innerAvatar === 'word'"
    :size="props.size"
    class="bg-[rgb(var(--primary-1))] text-[rgb(var(--primary-6))]"
  >
    <slot>{{ props.word?.substring(0, 4) || userStore.name?.substring(0, 4) }}</slot>
  </a-avatar>
  <a-avatar v-else :image-url="innerAvatar" :size="props.size"></a-avatar>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import useUserStore from '@/store/modules/user/index';

  const userStore = useUserStore();

  const props = withDefaults(
    defineProps<{
      avatar?: 'default' | 'word' | string;
      size?: number;
      word?: string; // 用于显示文字头像
      isUser?: boolean; // 是否是登录用户头像
    }>(),
    {
      avatar: 'default',
      size: 40,
    }
  );

  const innerAvatar = ref(props.avatar);

  watchEffect(() => {
    if (props.isUser) {
      innerAvatar.value = userStore.avatar || 'default';
    } else {
      innerAvatar.value = props.avatar;
    }
  });
</script>

<style lang="less" scoped></style>
