<template>
  <PostTab v-model:config="params" layout="horizontal" />
</template>

<script lang="ts" setup>
  import PostTab from '@/views/api-test/components/requestComposition/postcondition.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();

  // TODO: 参数类型
  const params = computed<any>({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.postScript = value;
    },
    get: () => store.currentEnvDetailInfo.config.postScript || [],
  });
</script>

<style lang="less" scoped></style>
