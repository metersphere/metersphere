<template>
  <a-input-password
    v-model="inputValue"
    :placeholder="placeholder"
    :default-visibility="true"
    :disabled="props.disabled"
    allow-clear
    @clear="clearHandler"
    @input="inputHandler"
  />
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  const props = defineProps<{
    placeholder?: string;
    value?: string;
    disabled?: boolean;
  }>();
  const inputValue = ref('');
  const emits = defineEmits<{
    (event: 'update:modelValue', value: string): void;
  }>();

  watch(
    () => inputValue.value,
    (val: string) => {
      emits('update:modelValue', val);
    }
  );

  function clearHandler() {
    inputValue.value = '';
    emits('update:modelValue', inputValue.value);
  }

  function inputHandler(value: string) {
    inputValue.value = value;
    emits('update:modelValue', inputValue.value);
  }
</script>

<style scoped></style>
