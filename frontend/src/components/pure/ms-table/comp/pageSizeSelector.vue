<template>
  <div
    class="flex h-[32px] w-[289px] items-center gap-[4px] rounded-[4px] bg-[var(--color-text-n8)] text-[var(--color-text-2)]"
  >
    <div
      v-for="item in option"
      :key="item"
      class="flex h-[28px] flex-1 cursor-pointer items-center justify-center rounded-[4px]"
      :class="{
        'w-[75px] bg-[var(--color-bg-1)] text-[rgb(var(--primary-5))]': item === props.modelValue,
        'ml-[2px]': props.modelValue === 10,
        'mr-[2px]': props.modelValue === 50,
      }"
      @click="handleClick(item)"
    >
      {{ item }}
    </div>
  </div>
</template>

<script lang="ts" setup>
  const props = defineProps<{
    modelValue: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:modelValue', value: number): void;
    (e: 'pageSizeChange', value: number): void;
  }>();
  const option = [10, 20, 30, 40, 50];
  const handleClick = (value: number) => {
    emit('update:modelValue', value);
    emit('pageSizeChange', value);
  };
</script>
