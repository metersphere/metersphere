<template>
  <div class="absolute left-[50%] top-[50%] ml-[-95px] mt-[-121px] text-center">
    <a-result class="result" status="404" :subtitle="'not found'"> </a-result>
    <div class="operation-row">
      <a-button key="back" type="primary" @click="back"> back </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';

  import { getFirstRouteNameByPermission } from '@/utils/permission';

  const router = useRouter();
  const back = () => {
    router.push({ name: getFirstRouteNameByPermission(router.getRoutes()) });
  };
</script>
