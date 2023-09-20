<template>
  <a-checkbox :model-value="checked" @change="handleChange"></a-checkbox>
</template>

<script setup lang="ts">
  import { ref, watchEffect } from 'vue';

  const props = defineProps<{
    value: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'change', value: boolean): void;
  }>();
  const checked = ref(false);
  const handleChange = (v: boolean | (string | number | boolean)[]) => {
    emit('change', v as boolean);
  };
  watchEffect(() => {
    checked.value = props.value;
  });
</script>
