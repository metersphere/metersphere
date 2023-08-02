<template>
  <ColorPicker v-model:pureColor="innerPureColor" :z-index="1" is-widget picker-type="chrome" round-history />
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { ColorPicker } from 'vue3-colorpicker';
  import 'vue3-colorpicker/style.css';

  const props = defineProps<{
    pureColor: string;
  }>();

  const emit = defineEmits(['update:pureColor']);

  const innerPureColor = ref(props.pureColor || '#CF00FF');

  watch(
    () => props.pureColor,
    (val) => {
      innerPureColor.value = val;
    }
  );

  watch(
    () => innerPureColor.value,
    (val) => {
      emit('update:pureColor', val);
    }
  );
</script>

<style lang="less"></style>
