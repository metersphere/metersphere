<template>
  <a-input
    v-model="inputValue"
    :max-length="250"
    :type="isShowPassword ? 'password' : 'text'"
    :placeholder="placeholder"
    style="width: 100%"
  >
    <template #suffix>
      <span v-if="!isShowPassword" @click="togglePasswordVisibility">
        <icon-eye />
      </span>
      <span v-else>
        <icon-eye-invisible @click="togglePasswordVisibility" />
      </span>
    </template>
  </a-input>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  defineProps<{
    placeholder?: string;
    value?: string;
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

  const isShowPassword = ref<boolean>(true);
  const togglePasswordVisibility = () => {
    isShowPassword.value = !isShowPassword.value;
  };
</script>

<style scoped></style>
