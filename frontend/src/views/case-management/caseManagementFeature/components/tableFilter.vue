<template>
  <a-trigger v-model:popup-visible="innerVisible" trigger="click" @popup-visible-change="handleFilterHidden">
    <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" size="small" @click.stop="innerVisible = true">
      <div class="font-medium">
        {{ t(props.title) }}
      </div>
      <icon-down :class="innerVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
    </a-button>
    <template #content>
      <div class="arco-table-filters-content">
        <div class="ml-[6px] flex w-full items-center justify-start overflow-hidden px-[6px] py-[2px]">
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
              <a-tooltip :content="item[props.labelKey || 'text']" :mouse-enter-delay="300">
                <div class="one-line-text">
                  <slot name="item" :item="item" :index="index"></slot>
                </div>
              </a-tooltip>
            </a-checkbox>
          </a-checkbox-group>
        </div>
        <div v-if="props.mode === 'remote'" class="w-[200px] p-4 pb-0">
          <MsUserSelector
            v-model="innerStatusFilters"
            :load-option-params="props.loadOptionParams"
            :type="props.type"
            :placeholder="props.placeholderText"
          />
        </div>
        <div class="flex items-center p-4" :class="[props.mode === 'static' ? 'justify-between' : 'justify-end']">
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

<style scoped></style>
