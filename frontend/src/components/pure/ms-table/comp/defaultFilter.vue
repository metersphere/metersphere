<template>
  <a-trigger v-model:popup-visible="visible" trigger="click">
    <a-button type="text" @click="visible = true">
      <template #icon>
        <icon-filter class="text-[var(--color-text-4)]" />
      </template>
    </a-button>
    <template #content>
      <div class="arco-table-filters-content">
        <div class="max-h-[300px] overflow-y-auto px-[12px] py-[4px]">
          <a-checkbox-group v-if="props.multiple" v-model="checkedList" size="mini" direction="vertical">
            <a-checkbox v-for="item in props.options" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-checkbox>
          </a-checkbox-group>
          <a-radio-group v-else v-model="checkedValue" size="mini" direction="vertical">
            <a-radio v-for="item in props.options" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-radio>
          </a-radio-group>
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
    </template>
  </a-trigger>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    multiple: boolean;
    options?: { label: string; value: string | number }[];
  }>();
  const emit = defineEmits<{
    (e: 'handleConfirm', value: (string | number)[]): void;
  }>();

  const visible = ref(false);

  const checkedList = ref<(string | number)[]>([]);
  const checkedValue = ref<string | number>('');

  const handleFilterReset = () => {
    if (props.multiple) {
      checkedList.value = [];
    } else {
      checkedValue.value = '';
    }
    emit('handleConfirm', []);
    visible.value = false;
  };

  const handleFilterSubmit = () => {
    if (props.multiple) {
      emit('handleConfirm', checkedList.value);
    } else {
      emit('handleConfirm', checkedValue.value ? [checkedValue.value] : []);
    }
    visible.value = false;
  };
</script>
