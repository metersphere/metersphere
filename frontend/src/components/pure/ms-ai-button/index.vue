<template>
  <div class="ms-ai-button">
    <a-button
      :size="props.size"
      type="outline"
      class="!border-none !bg-[var(--color-text-fff)]"
      :class="props.size === 'large' ? 'h-[40px]' : ''"
      :disabled="props.disabled"
      @click="emit('click')"
    >
      <template v-if="!props.noIcon" #icon>
        <svg-icon :width="props.iconSize" :height="props.iconSize" name="ai" />
      </template>
      <div class="ms-ai-button-text" :class="props.size === 'large' ? 'text-[16px]' : ''">
        {{ props.text }}
      </div>
    </a-button>
  </div>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      text: string;
      size?: 'large' | 'small' | 'mini' | 'medium';
      iconSize?: string | number;
      noIcon?: boolean;
      disabled?: boolean;
    }>(),
    {
      size: 'medium',
      iconSize: '16px',
      noIcon: false,
    }
  );

  const emit = defineEmits<{
    (e: 'click'): void;
  }>();
</script>

<style lang="less" scoped>
  .ms-ai-button {
    position: relative;
    border-radius: 4px;
    &::before {
      position: absolute;
      top: -1px;
      right: -1px;
      bottom: -1px;
      left: -1px;
      border-radius: 4px;
      background: linear-gradient(93.67deg, #3370ff 0%, #783887 100%);
      content: '';
    }
    .ms-ai-button-text {
      margin-left: 4px;
      color: transparent;
      background-clip: text;
      background-image: linear-gradient(90deg, #3370ff 0%, #783887 100%);
    }
  }
</style>
