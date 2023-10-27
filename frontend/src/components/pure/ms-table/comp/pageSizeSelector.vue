<template>
  <div class="flex h-[32px] w-[289px] items-center gap-[4px]">
    <div
      v-for="item in option"
      :key="item"
      class="flex h-[28px] w-[49px] items-center justify-center"
      :class="{ 'bg-[var(--color-bg-1)] text-[var(--color-text-4)]': item !== props.modelValue }"
      @click="handleClick(item)"
      >{{ item }}</div
    >
  </div>
</template>

<script lang="ts" setup>
  const props = defineProps<{
    modelValue: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:modelValue', value: number): void;
  }>();
  const option = [10, 20, 30, 40, 50];
  const handleClick = (value: number) => {
    emit('update:modelValue', value);
  };
</script>
