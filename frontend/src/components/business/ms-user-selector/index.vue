<template>
  <MsSelect
    v-model:model-value="currentValue"
    mode="remote"
    :options="[]"
    :placeholder="props.placeholder || 'common.pleaseSelectMember'"
    multiple
    :value-key="props.valueKey"
    :disabled="props.disabled"
    :at-least-one="props.atLeastOne"
    :label-key="props.firstLabelKey"
    :filter-option="false"
    allow-clear
    :loading="loading"
    :search-keys="[props.firstLabelKey, props.secondLabelKey]"
    :remote-func="loadList"
    :remote-extra-params="{ ...props.loadOptionParams, type: props.type }"
    :option-label-render="optionLabelRender"
    :should-calculate-max-tag="false"
  >
  </MsSelect>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import MsSelect from '@/components/business/ms-select/index';

  import initOptionsFunc, { UserRequestTypeEnum } from './utils';

  defineOptions({ name: 'MsUserSelector' });

  export interface MsUserSelectorOption {
    id: string;
    name: string;
    email: string;
    disabled?: boolean;
    [key: string]: string | number | boolean | undefined;
  }

  const props = withDefaults(
    defineProps<{
      disabled?: boolean; // 是否禁用
      disabledKey?: string; // 禁用的key
      valueKey?: string; // value的key
      placeholder?: string;
      firstLabelKey?: string; // 首要的的字段key
      secondLabelKey?: string; // 次要的字段key
      loadOptionParams?: Record<string, any>; // 加载选项的参数
      type?: UserRequestTypeEnum; // 加载选项的类型
      atLeastOne?: boolean; // 是否至少选择一个
    }>(),
    {
      disabled: false,
      disabledKey: 'disabled',
      valueKey: 'id',
      firstLabelKey: 'name',
      secondLabelKey: 'email',
      type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
      atLeastOne: false,
    }
  );

  const currentValue = defineModel<(string | number)[] | string>({ default: [] });
  const loading = ref(true);

  const loadList = async (params: Record<string, any>) => {
    try {
      loading.value = true;
      const { type, keyword, ...rest } = params;
      const list = (await initOptionsFunc(type, { keyword, ...rest })) || [];
      const { firstLabelKey, secondLabelKey, disabledKey, valueKey } = props;
      list.forEach((item: MsUserSelectorOption) => {
        if (firstLabelKey) {
          item.name = (item[firstLabelKey] as string) || '';
        }
        if (secondLabelKey) {
          item.email = (item[secondLabelKey] as string) || '';
        }
        if (disabledKey) {
          item.disabled = item[disabledKey] as boolean;
        }
        if (valueKey) {
          item.id = item[valueKey] as string;
        }
      });
      return list;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return [];
    } finally {
      loading.value = false;
    }
  };
  const optionLabelRender = (option: SelectOptionData) => {
    if (option.email !== '') {
      return `<span class='text-[var(--color-text-1)]'>${option.name}</span><span class='text-[var(--color-text-4)] ml-[4px]'>（${option.email}）</span>`;
    }
    return `<span class='text-[var(--color-text-1)]'>${option.name}</span>`;
  };
</script>
