<template>
  <div class="p-[24px]">
    <a-tabs v-model:active-key="activeKey" class="no-content">
      <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
    </a-tabs>
    <a-divider :margin="0" class="!mb-[16px]" />
    <RequestHeader v-if="activeKey === 'requestHeader'" v-model:params="headerParams" />
    <AllPrams v-if="activeKey === 'allParams'" v-model:params="AllParams" />
  </div>
</template>

<script lang="ts" setup>
  import AllPrams from './allParams/index.vue';
  import RequestHeader from './RequestHeader.vue';

  import { useI18n } from '@/hooks/useI18n';

  const activeKey = ref('requestHeader');
  const headerParams = ref<[]>([]);
  const AllParams = ref<[]>([]);
  const { t } = useI18n();

  const contentTabList = [
    {
      value: 'requestHeader',
      label: t('project.environmental.requestHeader'),
    },
    {
      value: 'allParams',
      label: t('project.environmental.allParams'),
    },
  ];
</script>

<style lang="less" scoped>
  .no-content {
    :deep(.arco-tabs-content) {
      padding-top: 0;
    }
  }
</style>
