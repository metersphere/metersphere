<template>
  <div>
    <a-button @click="open">Modal</a-button>
  </div>
</template>

<script setup lang="ts">
  import useModal from '@/hooks/useModal';

  const { openModal } = useModal();

  function open() {
    openModal({
      mode: 'weak',
      type: 'info',
      content: '这是一个弱提示',
    });
  }
</script>

<style lang="less" scoped></style>
