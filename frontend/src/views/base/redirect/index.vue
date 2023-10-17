<template>
  <div></div>
</template>

<script lang="ts" setup>
  import { useRoute, useRouter } from 'vue-router';

  const router = useRouter();
  const route = useRoute();

  const gotoPath = route.params.path as string;

  router.replace({ path: gotoPath });
</script>
