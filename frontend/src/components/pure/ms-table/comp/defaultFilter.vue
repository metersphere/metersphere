<template>
  <a-trigger v-model:popup-visible="visible" trigger="click">
    <span class="cursor-pointer pr-[2px]" @click="visible = true">
      <svg-icon
        width="16px"
        height="16px"
        :name="visible ? 'filter-icon-color' : 'filter-icon'"
        class="text-[12px] font-medium text-[rgb(var(--danger-6))]"
      />
    </span>
    <template #content>
      <div class="arco-table-filters-content">
        <div class="arco-table-filters-content-list">
          <div class="max-h-[300px] overflow-y-auto px-[12px] py-[4px]">
            <a-checkbox-group v-model="checkedList" size="mini" direction="vertical">
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
          <div class="arco-table-filters-bottom">
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
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  export interface FilterListItem {
    [key: string]: any;
  }
  const props = defineProps<{
    options?: FilterListItem[];
    valueKey?: string;
    labelKey?: string;
  }>();
  const emit = defineEmits<{
    (e: 'handleConfirm', value: (string | number)[]): void;
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
