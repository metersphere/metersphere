<template>
  <div class="flex h-[62px] flex-row items-end gap-[8px] text-[var(--color-text-1)]">
    <div>
      <div class="mb-[8px]">{{ t('ms.assertion.statusCode') }}</div>
      <a-select
        v-model="selectValue"
        class="w-[157px]"
        @change="
          emit('change', {
            statusCode: statusCode,
            selectValue: selectValue,
          })
        "
      >
        <a-option v-for="item in statusCodeOptions" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-option>
      </a-select>
    </div>
    <a-input-number
      v-if="showInput"
      v-model:modelValue="statusCode"
      hide-button
      class="w-[157px]"
      @change="
        emit('change', {
          statusCode: statusCode,
          selectValue: selectValue,
        })
      "
    />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    value: {
      statusCode: number;
      selectValue: string;
    };
  }>();

  const emit = defineEmits(['change']);
  const selectValue = ref<string>('');
  const statusCode = ref<number>(200);
  const showInput = computed(() => selectValue.value !== 'none' && selectValue.value !== '');

  const { t } = useI18n();
  watchEffect(() => {
    selectValue.value = props.value.selectValue;
    statusCode.value = props.value.statusCode;
  });
</script>

<style lang="less" scoped></style>
