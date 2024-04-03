<template>
  <MsAssertion v-model:params="params" :show-extraction="false" :disabled="false" />
</template>

<script lang="ts" setup>
  import MsAssertion from '@/components/business/ms-assertion/index.vue';
  import { MsAssertionItem } from '@/components/business/ms-assertion/type';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();

  const params = computed({
    set: (value: any[]) => {
      store.currentEnvDetailInfo.config.assertionConfig.assertions = (value || []) as MsAssertionItem[];
    },
    get: () => (store.currentEnvDetailInfo.config.assertionConfig.assertions || []) as MsAssertionItem[],
  });
</script>

<style lang="less" scoped></style>
