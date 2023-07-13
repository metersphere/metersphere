<template>
  <icon-font
    :type="props.type"
    :size="props.size || 14"
    :class="props.color ? `text-[${props.color}]` : 'text-[var(--color-text-4)]'"
  />
</template>

<script lang="ts" setup>
  import { Icon } from '@arco-design/web-vue';
  import '@/assets/icon-font/iconfont';

  const props = defineProps<{
    type: string;
    size?: string | number;
    rotate?: number;
    spin?: boolean;
    color?: string;
  }>();

  const IconFont = Icon.addFromIconFontCn({
    extraProps: { ...props },
  });
</script>
