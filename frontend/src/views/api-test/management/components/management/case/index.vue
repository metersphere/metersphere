<template>
  <div class="flex h-full flex-col">
    <div v-show="activeApiTab.id === 'all'" class="flex-1">
      <caseTable :active-module="props.activeModule" :protocol="props.protocol" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import caseTable from './caseTable.vue';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const props = defineProps<{
    activeModule: string;
    protocol: string;
  }>();

  const activeApiTab = defineModel<RequestParam>('activeApiTab', {
    required: true,
  });
</script>
