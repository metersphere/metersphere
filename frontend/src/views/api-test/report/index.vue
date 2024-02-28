<template>
  <!-- <MsCard class="mb-[16px]" :title="props.title" hide-back hide-footer auto-height no-content-padding no-bottom-radius>
    <a-tabs v-model:active-key="innerTab" class="no-content">
      <a-tab-pane v-for="item of tabList" :key="item.key" :title="item.title" />
    </a-tabs>
  </MsCard> -->
  <div></div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';

  const tabList = [
    {
      value: '',
    },
  ];
</script>

<style scoped></style>
