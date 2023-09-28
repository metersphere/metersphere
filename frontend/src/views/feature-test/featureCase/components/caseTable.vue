<template>
  <div class="page-header h-[34px]">
    <div class="text-[var(--color-text-1)]"
      >{{ t('featureTest.featureCase.allCase') }}
      <span class="text-[var(--color-text-4)]"> ({{ allCaseCount }})</span></div
    >
    <div class="flex w-[80%] items-center justify-end">
      <a-select class="w-[240px]" :placeholder="t('featureTest.featureCase.versionPlaceholder')">
        <a-option v-for="version of versionOptions" :key="version.id" :value="version.id">{{ version.name }}</a-option>
      </a-select>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('featureTest.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
      ></a-input-search>
      <MsTag
        :type="isExpandFilter ? 'primary' : 'default'"
        :theme="isExpandFilter ? 'lightOutLine' : 'outline'"
        size="large"
        class="-mt-[3px] cursor-pointer"
      >
        <span :class="!isExpandFilter ? 'text-[var(--color-text-4)]' : ''" @click="isExpandFilterHandler"
          ><icon-filter class="mr-[4px]" :style="{ 'font-size': '16px' }" />{{
            t('featureTest.featureCase.filter')
          }}</span
        >
      </MsTag>
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type ml-[4px]">
        <a-radio value="list" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_view-list_outlined" /></a-radio>
        <a-radio value="xmind" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_mindnote_outlined" /></a-radio>
      </a-radio-group>
    </div>
  </div>
  <FilterPanel v-show="isExpandFilter"></FilterPanel>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import { useI18n } from '@/hooks/useI18n';
  import FilterPanel from '@/components/business/ms-filter-panel/searchForm.vue';

  const { t } = useI18n();
  const versionOptions = ref([
    {
      id: '1001',
      name: 'v_1.0',
    },
  ]);

  const keyword = ref<string>();

  const showType = ref<string>('list');

  const allCaseCount = ref<number>(100);

  const isExpandFilter = ref<boolean>(false);

  // 是否展开||折叠高级筛选
  const isExpandFilterHandler = () => {
    isExpandFilter.value = !isExpandFilter.value;
  };
</script>

<style scoped lang="less">
  .page-header {
    @apply flex items-center justify-between;
  }
  .filter-panel {
    background: var(--color-text-n9);
    @apply mt-1 rounded-md p-3;
    .condition-text {
      color: var(--color-text-2);
    }
  }
</style>
