<template>
  <a-date-picker
    v-if="props.operationType !== 'between'"
    v-model:model-value="timeValue"
    class="w-[100%]"
    show-time
    allow-clear
    :time-picker-props="{ defaultValue: '00:00:00' }"
    format="YYYY-MM-DD HH:mm:ss"
    position="br"
    @change="changeHandler"
  />
  <a-range-picker
    v-else
    v-model:model-value="timeRangeValue"
    position="br"
    show-time
    class="w-[100%]"
    format="YYYY-MM-DD HH:mm"
    :time-picker-props="{
      defaultValue: ['00:00:00', '00:00:00'],
    }"
    @change="changeHandler"
  ></a-range-picker>
</template>

<script setup lang="ts">
  import { CalendarValue } from '@arco-design/web-vue/es/date-picker/interface';
  import { ref, watch } from 'vue';

  type PickerType = 'between' | 'gt' | 'lt'; // 之间 | 大于 | 小于

  const props = defineProps<{
    modelValue: [] | string; // 传入当前值
    operationType: PickerType; // 查询运算符条件类型
  }>();

  const emits = defineEmits(['updateTime']);

  const timeValue = ref<string>('');

  const timeRangeValue = ref([]);

  const changeHandler = (value: Date | string | number | undefined | (CalendarValue | undefined)[] | undefined) => {
    emits('updateTime', value);
  };

  watch(
    () => props.modelValue,
    (val) => {
      if (!val) {
        if (props.operationType === 'between') timeRangeValue.value = [];
        timeValue.value = '';
      }
    }
  );
</script>

<style scoped></style>
