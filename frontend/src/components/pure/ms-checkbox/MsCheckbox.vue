<template>
  <a-checkbox :model-value="checked" :indeterminate="props.indeterminate" @change="handleChange"></a-checkbox>
</template>

<script setup lang="ts">
  import { ref, watchEffect } from 'vue';

  const props = defineProps<{
    value: boolean;
    indeterminate: boolean; // 半选用于树形子级别选择状态
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
