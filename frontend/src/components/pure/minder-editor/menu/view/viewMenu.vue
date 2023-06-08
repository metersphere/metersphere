<template>
  <div class="menu-container">
    <mold v-if="moldEnable" :default-mold="props.defaultMold" @mold-change="handleMoldChange" />
    <arrange v-if="arrangeEnable" />
    <style-operation v-if="styleEnable" />
    <font-operation v-if="fontEnable" />
  </div>
</template>

<script lang="ts" name="viewMenu" setup>
  import mold from './mold.vue';
  import arrange from './arrange.vue';
  import styleOperation from './styleOperation.vue';
  import fontOperation from './fontOperation.vue';
  import { moleProps, viewMenuProps } from '../../props';

  const props = defineProps({ ...moleProps, ...viewMenuProps });

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
  }>();

  function handleMoldChange(data: number) {
    emit('moldChange', data);
  }
</script>

<style lang="less" scoped>
  .menu-container {
    height: 60px;
    i {
      @apply inline-block;

      width: 20px;
      height: 20px;
    }
  }
</style>
