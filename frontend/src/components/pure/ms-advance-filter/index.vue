<template>
  <div class="flex flex-row justify-between">
    <slot name="left"></slot>
    <div class="flex flex-row gap-[8px]">
      <a-input-search
        v-model="keyword"
        size="small"
        :placeholder="t('system.user.searchUser')"
        class="w-[240px]"
        allow-clear
        @press-enter="emit('keywordSearch', keyword)"
        @search="emit('keywordSearch', keyword)"
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
      <slot name="right"></slot>
      <MsTag no-margin size="large" class="cursor-pointer" theme="outline" @click="handleResetSearch">
        <MsIcon class="text-[var(color-text-4)]" :size="16" type="icon-icon_reset_outlined" />
      </MsTag>
    </div>
  </div>
  <FilterForm
    v-show="visible"
    v-model:count="filterCount"
    :row-count="props.rowCount"
    :visible="visible"
    :config-list="props.filterConfigList"
    class="mt-[8px]"
    @on-search="handleFilter"
    @data-index-change="dataIndexChange"
  />
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '../ms-tag/ms-tag.vue';
  import FilterForm from './FilterForm.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { FilterFormItem, FilterResult } from './type';

  const { t } = useI18n();
  const keyword = ref('');
  const props = defineProps<{
    rowCount: number;
    filterConfigList: FilterFormItem[];
  }>();
  const visible = ref(false);
  const filterCount = ref(0);
  const emit = defineEmits<{
    (e: 'keywordSearch', value: string): void; // keyword 搜索
    (e: 'advSearch', value: FilterResult): void; // 高级搜索
    (e: 'dataIndexChange', value: string): void; // 高级搜索选项变更
  }>();

  const handleResetSearch = () => {
    keyword.value = '';
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
