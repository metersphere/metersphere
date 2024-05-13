<template>
  <!-- TODO 目前有一部分表格筛选在用 后边这个也要替换 暂时先修改了icon统一 -->
  <a-trigger v-model:popup-visible="innerVisible" trigger="click" @popup-visible-change="handleFilterHidden">
    <a-button
      type="text"
      :class="`${
        innerVisible ? '!bg-[rgb(var(--primary-1))]' : ''
      } arco-btn-text--secondary no-hover p-[8px_4px] text-[14px]
    `"
      size="mini"
      @click.stop="innerVisible = true"
    >
      <div :class="`${innerVisible ? 'filter-title' : ''} flex items-center pr-[2px] font-medium`">
        {{ t(props.title) }}
      </div>
      <svg-icon
        width="16px"
        height="16px"
        :name="innerVisible ? 'filter-icon-color' : 'filter-icon'"
        class="text-[12px] font-medium"
      />
    </a-button>
    <template #content>
      <div class="arco-table-filters-content">
        <div class="arco-table-filters-content-list">
          <div class="flex w-full items-center justify-start overflow-hidden px-[12px] py-[2px]">
            <a-checkbox-group
              v-if="props.mode === 'static' && props.list?.length"
              v-model:model-value="innerStatusFilters"
              direction="vertical"
              size="small"
            >
              <a-checkbox
                v-for="(item, index) of props.list"
                :key="item[props.valueKey || 'value']"
                :value="item[props.valueKey || 'value']"
              >
                <a-tooltip
                  :content="item[props.labelKey || 'text']"
                  :mouse-enter-delay="300"
                  :disabled="!item[props.labelKey || 'text']"
                >
                  <div class="one-line-text max-w-[120px]">
                    <slot name="item" :item="item" :index="index"></slot>
                  </div>
                </a-tooltip>
              </a-checkbox>
            </a-checkbox-group>
          </div>
          <div v-if="props.mode === 'remote'" class="w-[200px] p-[12px] pb-0">
            <MsUserSelector
              v-model="innerStatusFilters"
              :load-option-params="props.loadOptionParams"
              :type="props.type"
              :placeholder="props.placeholderText"
            />
          </div>
        </div>
        <div
          class="flex items-center border-t border-[var(--color-text-n8)] p-[12px]"
          :class="[props.mode === 'static' ? 'justify-between' : 'justify-end']"
        >
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

  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import type { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  export interface FilterListItem {
    [key: string]: any;
  }

  const props = withDefaults(
    defineProps<{
      mode?: 'static' | 'remote';
      visible: boolean;
      title: string;
      statusFilters: string[];
      list?: FilterListItem[];
      valueKey?: string;
      labelKey?: string;
      type?: UserRequestTypeEnum; // 加载选项的类型
      loadOptionParams?: Record<string, any>; // 请求下拉的参数
      placeholderText?: string;
    }>(),
    {
      mode: 'static',
    }
  );

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

<style scoped>
  .filter-title {
    border-radius: 4px;
    color: rgb(var(--primary-5));
    background: rgb(var(--primary-1)) content-box;
    .filter-icon {
      color: rgb(var(--primary-5)) !important;
    }
  }
</style>
