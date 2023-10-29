<template>
  <a-select
    v-model:model-value="selectValue"
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

  import { getPluginOptions } from '@/api/modules/setting/pluginManger';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useFormCreateStore from '@/store/modules/form-create/form-create';

  import type { OptionsParams } from '@/models/setting/plugin';
  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

  const appStore = useAppStore();

  const attrs = useAttrs();
  const formCreateStore = useFormCreateStore();

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
  const selectValue = ref<string[] | string>();

  const optionsList = ref<{ label: string; value: string }[]>([]);

  const params = ref<OptionsParams>();

  async function getLinksItem() {
    const { formKey } = attrs;
    const formRulesList = formCreateStore.formRuleMap.get(formKey as FormCreateKeyEnum[keyof FormCreateKeyEnum]);
    if (formRulesList && props.optionMethod) {
      params.value = {
        pluginId: props.keyword as string,
        organizationId: appStore.currentOrgId,
        projectConfig: formRulesList,
        optionMethod: props.optionMethod,
      };
      // 请求参数
      // console.log(selectValue.value, props.keyword, props.modelValue);
      try {
        const res = await getPluginOptions(params.value);
        optionsList.value = res.map((item) => {
          return {
            label: item.text,
            value: item.value,
          };
        });
      } catch (error) {
        console.log(error);
      }
    }
  }
  // 内部的关键词
  const innerKeyword = ref<string | undefined>('');
  const searchHandler = debounce((inputVal: string) => {
    innerKeyword.value = inputVal;
  }, 300);

  watch(
    () => props.modelValue,
    (val) => {
      if (val) {
        selectValue.value = val as any;
      }
    },
    { immediate: true }
  );

  watch(
    () => props.keyword,
    (val) => {
      if (val) {
        innerKeyword.value = val;
        if (props.inputSearch && props.optionMethod) {
          getLinksItem();
        }
      }
    }
  );

  watch(
    () => selectValue.value,
    async (val) => {
      selectValue.value = val;
      await emit('update:model-value', val);
    }
  );

  watchEffect(() => {
    if (props.options) optionsList.value = props.options;
  });
</script>

<style scoped></style>
