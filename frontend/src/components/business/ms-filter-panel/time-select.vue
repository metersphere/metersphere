<template>
  <a-date-picker
    v-if="props.operationType !== 'between'"
    v-model:model-value="timeValue"
    class="w-[100%]"
    show-time
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
    :allow-clear="false"
    class="w-[100%]"
    format="YYYY-MM-DD HH:mm"
    :time-picker-props="{
      defaultValue: ['00:00:00', '00:00:00'],
    }"
    @change="changeHandler"
  ></a-range-picker>
</template>

<script setup lang="ts">
  import { ref, watch, watchEffect } from 'vue';

  import { CalendarValue } from '@arco-design/web-vue/es/date-picker/interface';

  type PickerType = 'between' | 'gt' | 'lt'; // 之间 | 大于 | 小于

  const props = defineProps<{
    modelValue: string[] | string; // 传入当前值
    operationType: PickerType; // 查询运算符条件类型
  }>();

  const emits = defineEmits(['updateTime']);

  const timeValue = ref<string>('');

  const timeRangeValue = ref<string[]>([]);

  const changeHandler = (value: Date | string | number | undefined | (CalendarValue | undefined)[] | undefined) => {
    emits('updateTime', value);
  };

  watchEffect(() => {
    if (props.operationType === 'between') {
      timeRangeValue.value = props.modelValue as string[];
    }
    timeValue.value = props.modelValue as string;
  });

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
