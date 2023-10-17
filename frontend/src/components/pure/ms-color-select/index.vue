<template>
  <ColorPicker v-model:pureColor="innerPureColor" :z-index="1" picker-type="chrome" is-widget round-history />
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  import 'vue3-colorpicker/style.css';
  import { ColorPicker } from 'vue3-colorpicker';

  const props = defineProps<{
    pureColor: string;
  }>();

  const emit = defineEmits(['update:pureColor']);

  const innerPureColor = ref(props.pureColor || '#CF00FF');

  watch(
    () => props.pureColor,
    (val) => {
      innerPureColor.value = val;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => innerPureColor.value,
    (val) => {
      emit('update:pureColor', val);
    }
  );
</script>

<style lang="less">
  .color-cube {
    overflow: hidden;
    border-radius: var(--border-radius-small);
  }
  .vc-transparent {
    background-image: none !important;
  }
  .color-list {
    width: 100% !important;
  }
  .color-item:not(:last-child) {
    margin-right: 2px !important;
  }
</style>
