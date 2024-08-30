<template>
  <div class="flex flex-row items-center justify-between">
    <slot name="left">
      <div class="flex">
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
        <slot name="nameRight"></slot>
      </div>
    </slot>
    <div class="flex flex-row gap-[12px]">
      <a-input-search
        v-if="!props.notShowInputSearch && !isAdvancedSearchMode"
        v-model:modelValue="keyword"
        size="small"
        :placeholder="props.searchPlaceholder"
        class="w-[240px]"
        allow-clear
        @press-enter="emit('keywordSearch', keyword, filterResult)"
        @search="emit('keywordSearch', keyword, filterResult)"
        @clear="handleClear"
      ></a-input-search>
      <a-button
        v-if="props.showFilter"
        type="outline"
        :class="`${isAdvancedSearchMode ? '' : 'arco-btn-outline--secondary'} p-[0_8px]`"
        @click="handleOpenFilter"
      >
        <template #icon>
          <MsIcon
            type="icon-icon_copy_outlined"
            :class="`${isAdvancedSearchMode ? 'text-[rgb(var(--primary-5))]' : 'text-[var(--color-text-4)]'}`"
          />
        </template>
        {{ t('common.filter') }}
      </a-button>

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
  <FilterDrawer
    v-model:visible="visible"
    :config-list="props.filterConfigList"
    :custom-list="props.customFieldsConfigList"
    @handle-filter="handleFilter"
  />
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '../ms-tag/ms-tag.vue';
  import FilterDrawer from './filterDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { FilterFormItem, FilterResult } from './type';

  const props = defineProps<{
    rowCount: number;
    filterConfigList: FilterFormItem[]; // 系统字段
    customFieldsConfigList?: FilterFormItem[]; // 自定义字段
    searchPlaceholder?: string;
    name?: string;
    count?: number;
    notShowInputSearch?: boolean;
    showFilter?: boolean; // 展示高级搜索按钮
  }>();

  const emit = defineEmits<{
    (e: 'keywordSearch', value: string | undefined, combine: FilterResult): void; // keyword 搜索 TODO:可以去除，父组件通过 v-model:keyword 获取关键字
    (e: 'advSearch', value: FilterResult): void; // 高级搜索
    (e: 'refresh', value: FilterResult): void;
  }>();

  const { t } = useI18n();

  const keyword = defineModel<string>('keyword', { default: '' });
  const visible = ref(false);
  const filterResult = ref<FilterResult>({ searchMode: 'AND', conditions: [] });

  const isAdvancedSearchMode = ref(false);
  const handleFilter = (filter: FilterResult) => {
    keyword.value = '';
    isAdvancedSearchMode.value = !!filter.conditions?.length;
    filterResult.value = filter;
    emit('advSearch', filter);
  };

  const handleRefresh = () => {
    emit('refresh', filterResult.value);
  };

  const handleClear = () => {
    keyword.value = '';
    emit('keywordSearch', '', filterResult.value);
  };

  const handleOpenFilter = () => {
    visible.value = !visible.value;
  };
</script>
