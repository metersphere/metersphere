<template>
  <PostTab v-model:params="params" layout="horizontal" />
</template>

<script lang="ts" setup>
  import PostTab from '@/views/api-test/debug/components/debug/postcondition.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();

  const params = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.postScript = value;
    },
    get: () => store.currentEnvDetailInfo.config.postScript || [],
  });
</script>

<style lang="less" scoped></style>
