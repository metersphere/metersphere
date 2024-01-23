<template>
  <PreTab v-model:params="params" layout="horizontal" />
</template>

<script lang="ts" setup>
  import PreTab from '@/views/api-test/debug/components/debug/precondition.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();

  const params = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.preScript = value;
    },
    get: () => store.currentEnvDetailInfo.config.preScript || [],
  });
</script>

<style lang="less" scoped></style>
