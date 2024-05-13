<template>
  <a-trigger v-model:popup-visible="visible" trigger="click">
    <span class="cursor-pointer pr-[2px]" @click="visible = true">
      <svg-icon
        width="16px"
        height="16px"
        :name="visible ? 'filter-icon-color' : 'filter-icon'"
        class="text-[12px] font-medium"
      />
    </span>
    <template #content>
      <div class="arco-table-filters-content">
        <div class="arco-table-filters-content-list">
          <div class="arco-table-filters-content-wrap max-h-[300px] px-[12px] py-[4px]">
            <a-checkbox-group v-if="props.mode === 'static'" v-model="checkedList" size="mini" direction="vertical">
              <a-checkbox
                v-for="(item, index) of props.options"
                :key="item[props.valueKey || 'value']"
                :value="item[props.valueKey || 'value']"
              >
                <a-tooltip
                  :content="item[props.labelKey || 'label']"
                  :mouse-enter-delay="300"
                  :disabled="!item[props.labelKey || 'label']"
                >
                  <div class="one-line-text max-w-[120px]">
                    <slot name="item" :filter-item="item" :index="index">
                      <div class="one-line-text max-w-[120px]">{{ item[props.labelKey || 'label'] }}</div>
                    </slot>
                  </div>
                </a-tooltip>
              </a-checkbox>
            </a-checkbox-group>
          </div>
          <div v-if="props.mode === 'remote'" class="w-[200px] p-[12px] pb-[8px]">
            <MsUserSelector
              v-model="checkedList"
              :load-option-params="props.loadOptionParams"
              :type="props.type"
              :placeholder="props.placeholderText"
            />
          </div>
          <div
            :class="`${
              props.mode === 'static' ? 'justify-between' : 'justify-end'
            } arco-table-filters-bottom flex h-[38px] items-center`"
          >
            <a-button size="mini" type="secondary" @click="handleFilterReset">
              {{ t('common.reset') }}
            </a-button>
            <a-button size="mini" type="primary" @click="handleFilterSubmit()">
              {{ t('common.confirm') }}
            </a-button>
          </div>
        </div>
      </div>
    </template>
  </a-trigger>
</template>

<script lang="ts" setup>
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import type { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  export interface FilterListItem {
    [key: string]: any;
  }
  const props = withDefaults(
    defineProps<{
      options?: FilterListItem[];
      valueKey?: string;
      labelKey?: string;
      mode?: 'static' | 'remote';
      type?: UserRequestTypeEnum; // 加载选项的类型
      loadOptionParams?: Record<string, any>; // 请求下拉的参数
      placeholderText?: string;
    }>(),
    {
      mode: 'static',
    }
  );
  const emit = defineEmits<{
    (e: 'handleConfirm', value: (string | number)[] | string[] | undefined): void;
  }>();

  const visible = ref(false);

  const checkedList = ref<(string | number)[]>([]);

  const handleFilterReset = () => {
    checkedList.value = [];
    emit('handleConfirm', []);
    visible.value = false;
  };

  const handleFilterSubmit = () => {
    emit('handleConfirm', checkedList.value);
    visible.value = false;
  };
</script>

<style scoped lang="less">
  .arco-table-filters-content-wrap {
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
</style>
