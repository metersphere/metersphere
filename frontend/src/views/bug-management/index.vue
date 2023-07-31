<template>
  <div>BugManagement is waiting for development </div>
  <div class="continer">
    <ms-pagination
      v-modal:page-size="pageSize"
      size="small"
      :total="100"
      show-total
      show-jumper
      show-more
      show-page-size
    />
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import MsPagination from '@/components/pure/ms-pagination/index';

  const pageSize = ref(10);
</script>

<style lang="less" scoped>
  .continer {
    margin-top: 200px;
    padding: 50px;
    width: 100%;
    height: 500px;
    background-color: #ffffff;
  }
</style>
