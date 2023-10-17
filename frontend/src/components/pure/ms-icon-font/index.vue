<template>
  <icon-font :type="props.type" :size="props.size || 14" />
</template>

<script lang="ts" setup>
  import { Icon } from '@arco-design/web-vue';

  import '@/assets/icon-font/iconfont';

  const props = defineProps<{
    type: string;
    size?: string | number;
    rotate?: number;
    spin?: boolean;
  }>();

  const IconFont = Icon.addFromIconFontCn({
    extraProps: { ...props },
  });
</script>
