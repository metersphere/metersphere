<template>
  <PreTab v-model:config="params" layout="horizontal" />
</template>

<script lang="ts" setup>
  import PreTab from '@/views/api-test/components/requestComposition/precondition.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();

  // TODO:参数类型
  const params = computed<any>({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.preScript = value;
    },
    get: () => store.currentEnvDetailInfo.config.preScript || [],
  });
</script>

<style lang="less" scoped></style>
