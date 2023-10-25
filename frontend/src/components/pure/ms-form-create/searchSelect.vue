<template>
  <a-select
    v-model="selectValue"
    :placeholder="t(props.placeholder || 'common.pleaseSelect')"
    allow-search
    :multiple="props.multiple"
    @search="searchHandler"
  >
    <a-option v-for="opt of optionsList" :key="opt.value" :value="opt.value">{{ opt.label }}</a-option>
  </a-select>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { debounce } from 'lodash-es';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      optionMethod?: string; // 选项请求方法
      inputSearch?: boolean; // 是否支持远程检索
      modelValue: string[] | string | undefined; // 绑定值
      keyword?: string; // 级联搜索关键词
      formValue?: Record<string, any>; // 所有表单的值
      options?: { label: string; value: string }[];
      multiple?: boolean; // 是否多选
      placeholder?: string;
    }>(),
    {
      inputSearch: false,
      multiple: false,
    }
  );

  const emit = defineEmits(['update:model-value']);
  const selectValue = ref([]);

  const optionsList = ref<{ label: string; value: string }[]>([]);

  // 内部的关键词
  const innerKeyword = ref<string | undefined>('');
  async function getOptionsList() {
    if (props.inputSearch && props.optionMethod) {
      try {
        setTimeout(() => {
          // console.log('模拟请求');
          optionsList.value = [
            {
              value: '111',
              label: '测试测试测试111111',
            },
          ];
        }, 1000);
      } catch (error) {
        console.log(error);
      }
    }
  }
  const searchHandler = debounce(async (inputVal: string) => {
    innerKeyword.value = inputVal;
    getOptionsList();
  }, 300);

  watch(
    () => props.modelValue,
    (val) => {
      selectValue.value = val as any;
    }
  );
  watch(
    () => props.keyword,
    (val) => {
      if (val) {
        innerKeyword.value = val;
        getOptionsList();
      }
    }
  );

  watch(
    () => selectValue.value,
    (val) => {
      emit('update:model-value', val);
    }
  );

  watchEffect(() => {
    if (props.options) optionsList.value = props.options;
  });
</script>

<style scoped></style>
