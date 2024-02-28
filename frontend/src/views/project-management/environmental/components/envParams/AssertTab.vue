<template>
  <MsAssertion v-model:params="params" />
</template>

<script lang="ts" setup>
  import MsAssertion from '@/components/business/ms-assertion/index.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();

  const params = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.assertionConfig = value;
    },
    get: () => store.currentEnvDetailInfo.config.assertionConfig || [],
  });
</script>

<style lang="less" scoped></style>
