<template>
  <MsAssertion v-model:params="params" />
</template>

<script lang="ts" setup>
  import MsAssertion from '@/components/business/ms-assertion/index.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();

  const params = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.assertions = value;
    },
    get: () => store.currentEnvDetailInfo.config.assertions || [],
  });
</script>

<style lang="less" scoped></style>
