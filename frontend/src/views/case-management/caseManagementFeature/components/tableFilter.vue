<template>
  <a-trigger v-model:popup-visible="innerVisible" trigger="click" @popup-visible-change="handleFilterHidden">
    <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click.stop="innerVisible = true">
      <div class="font-medium">
        {{ t(props.title) }}
      </div>
      <icon-down :class="innerVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
    </a-button>
    <template #content>
      <div class="arco-table-filters-content max-w-[400px]">
        <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
          <a-checkbox-group v-model:model-value="innerStatusFilters" direction="vertical" size="small">
            <a-checkbox
              v-for="(item, index) of props.list"
              :key="item[props.valueKey || 'value']"
              :value="item[props.valueKey || 'value']"
            >
              <div class="one-line-text max-w-[300px]">
                <slot name="item" :item="item" :index="index"></slot>
              </div>
            </a-checkbox>
          </a-checkbox-group>
        </div>
        <div class="filter-button">
          <a-button size="mini" class="mr-[8px]" @click="resetFilter">
            {{ t('common.reset') }}
          </a-button>
          <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
            {{ t('system.orgTemplate.confirm') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    title: string;
    statusFilters: string[];
    list: any[];
    valueKey?: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'update:statusFilters', visible: boolean): void;
    (e: 'search'): void;
  }>();
  const innerVisible = useVModel(props, 'visible', emit);
  const innerStatusFilters = useVModel(props, 'statusFilters', emit);

  function handleFilterHidden(val: boolean) {
    innerVisible.value = val;
    if (!val) {
      emit('search');
    }
  }
  function resetFilter() {
    innerStatusFilters.value = [];
    innerVisible.value = false;
    emit('search');
  }
</script>

<style scoped></style>
