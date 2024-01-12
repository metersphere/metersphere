<template>
  <div class="flex flex-row justify-between">
    <slot name="left"></slot>
    <div class="flex flex-row gap-[8px]">
      <a-input-search
        v-model="innerKeyword"
        size="small"
        :placeholder="props.searchPlaceholder"
        class="w-[240px]"
        allow-clear
        @press-enter="emit('keywordSearch', innerKeyword)"
        @search="emit('keywordSearch', innerKeyword)"
      ></a-input-search>
      <MsTag
        :type="visible ? 'primary' : 'default'"
        :theme="visible ? 'lightOutLine' : 'outline'"
        size="large"
        class="min-w-[64px] cursor-pointer"
        no-margin
      >
        <span :class="!visible ? 'text-[var(--color-text-4)]' : ''" @click="handleOpenFilter">
          <icon-filter class="text-[16px]" />
          <span class="ml-[4px]">
            <span v-if="filterCount">{{ filterCount }}</span>
            {{ t('common.filter') }}
          </span>
        </span>
      </MsTag>
      <MsTag no-margin size="large" class="cursor-pointer" theme="outline" @click="handleResetSearch">
        <MsIcon class="text-[var(color-text-4)]" :size="16" type="icon-icon_reset_outlined" />
      </MsTag>
      <slot name="right"></slot>
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
    @reset="emit('reset')"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '../ms-tag/ms-tag.vue';
  import FilterForm from './FilterForm.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { FilterFormItem, FilterResult } from './type';

  const props = defineProps<{
    rowCount: number;
    filterConfigList: FilterFormItem[]; // 系统字段
    customFieldsConfigList?: FilterFormItem[]; // 自定义字段
    searchPlaceholder?: string;
    keyword?: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:keyword', value: string): void;
    (e: 'keywordSearch', value: string | undefined): void; // innerKeyword 搜索
    (e: 'advSearch', value: FilterResult): void; // 高级搜索
    (e: 'dataIndexChange', value: string): void; // 高级搜索选项变更
    (e: 'reset'): void;
  }>();

  const { t } = useI18n();
  const innerKeyword = useVModel(props, 'keyword', emit);
  const visible = ref(false);
  const filterCount = ref(0);

  const handleResetSearch = () => {
    innerKeyword.value = '';
    emit('keywordSearch', '');
  };

  const handleFilter = (filter: FilterResult) => {
    emit('advSearch', filter);
  };

  const dataIndexChange = (dataIndex: string) => {
    emit('dataIndexChange', dataIndex);
  };

  const handleOpenFilter = () => {
    visible.value = !visible.value;
  };
</script>
