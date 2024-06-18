<template>
  <div class="flex flex-row items-center justify-between">
    <slot name="left">
      <a-popover v-if="props.name" title="" position="bottom">
        <div class="flex">
          <div class="one-line-text mr-1 max-h-[32px] max-w-[300px] text-[var(--color-text-1)]">
            {{ props.name }}
          </div>
          <span class="text-[var(--color-text-4)]"> ({{ props.count }})</span>
        </div>
        <template #content>
          <div class="max-w-[400px] text-[14px] font-medium text-[var(--color-text-1)]">
            {{ props.name }}
            <span class="text-[var(--color-text-4)]">({{ props.count }})</span>
          </div>
        </template>
      </a-popover>
    </slot>
    <div class="flex flex-row gap-[12px]">
      <a-input-search
        v-model:modelValue="keyword"
        size="small"
        :placeholder="props.searchPlaceholder"
        class="w-[240px]"
        allow-clear
        @press-enter="emit('keywordSearch', keyword, filterResult)"
        @search="emit('keywordSearch', keyword, filterResult)"
        @clear="handleClear"
      ></a-input-search>
      <!-- <MsTag
        :type="visible ? 'primary' : 'default'"
        :theme="visible ? 'lightOutLine' : 'outline'"
        size="large"
        class="min-w-[64px] cursor-pointer"
        no-margin
        @click="handleOpenFilter"
      >
        <span :class="!visible ? 'text-[var(--color-text-4)]' : ''">
          <icon-filter class="text-[16px]" />
          <span class="ml-[4px]">
            <span v-if="filterCount">{{ filterCount }}</span>
            {{ t('common.filter') }}
          </span>
        </span>
      </MsTag> -->

      <slot name="right"></slot>
      <MsTag
        no-margin
        size="large"
        :tooltip-disabled="true"
        class="cursor-pointer"
        theme="outline"
        @click="handleRefresh"
      >
        <MsIcon class="text-[16px] text-[var(color-text-4)]" :size="32" type="icon-icon_reset_outlined" />
      </MsTag>
    </div>
  </div>
  <FilterForm
    v-show="visible"
    v-model:count="filterCount"
    :row-count="props.rowCount"
    :visible="visible"
    :config-list="props.filterConfigList"
    :custom-list="props.customFieldsConfigList"
    class="mt-[8px]"
    @on-search="handleFilter"
    @data-index-change="dataIndexChange"
    @reset="handleResetFilter"
  />
</template>

<script setup lang="ts">
  import { defineModel } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '../ms-tag/ms-tag.vue';
  import FilterForm from './FilterForm.vue';

  import { FilterFormItem, FilterResult } from './type';

  const props = defineProps<{
    rowCount: number;
    filterConfigList: FilterFormItem[]; // 系统字段
    customFieldsConfigList?: FilterFormItem[]; // 自定义字段
    searchPlaceholder?: string;
    name?: string;
    count?: number;
  }>();

  const emit = defineEmits<{
    (e: 'keywordSearch', value: string | undefined, combine: FilterResult): void; // keyword 搜索 TODO:可以去除，父组件通过 v-model:keyword 获取关键字
    (e: 'advSearch', value: FilterResult): void; // 高级搜索
    (e: 'dataIndexChange', value: string): void; // 高级搜索选项变更
    (e: 'refresh', value: FilterResult): void;
  }>();

  const keyword = defineModel<string>('keyword', { default: '' });
  const visible = ref(false);
  const filterCount = ref(0);
  const defaultFilterResult: FilterResult = { accordBelow: 'AND', combine: {} };
  const filterResult = ref<FilterResult>({ ...defaultFilterResult });

  const handleResetFilter = () => {
    filterResult.value = { ...defaultFilterResult };
    emit('advSearch', { ...defaultFilterResult });
  };

  const handleFilter = (filter: FilterResult) => {
    filterResult.value = filter;
    emit('advSearch', filter);
  };

  const handleRefresh = () => {
    emit('refresh', filterResult.value);
  };

  const dataIndexChange = (dataIndex: string) => {
    emit('dataIndexChange', dataIndex);
  };

  const handleClear = () => {
    keyword.value = '';
    emit('keywordSearch', '', filterResult.value);
  };

  const handleOpenFilter = () => {
    visible.value = !visible.value;
  };
</script>
