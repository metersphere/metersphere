<template>
  <MsCard simple>
    <h1>BugManagement is waiting for development </h1>
    <ms-pagination size="small" :total="100" show-total show-jumper show-page-size />
  </MsCard>
</template>

<script setup>
  import MsPagination from '@/components/pure/ms-pagination/index';
  import MsCard from '@/components/pure/ms-card/index.vue';
</script>
