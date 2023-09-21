<template>
  <a-select
    :model-value="currentValue"
    :placeholder="t(props.placeholder || 'common.pleaseSelectMember')"
    multiple
    :value-key="props.valueKey"
    :disabled="props.disabled"
    :filter-option="false"
    allow-clear
    @change="change"
    @search="debouncedSearch"
  >
    <template #label="{ data }">
      <span class="text-[var(--color-text-1)]"> {{ data.value.name }} </span>
    </template>
    <a-option v-for="data in allOptions" :key="data.id" :disabled="data.disabled" :value="data">
      <span :class="data.disabled ? 'text-[var(--color-text-4)]' : 'text-[var(--color-text-1)]'">
        {{ data.name }}
      </span>
      <span v-if="data.email" class="text-[var(--color-text-4)]"> {{ `(${data.email})` }} </span>
    </a-option>
  </a-select>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { ref, onMounted, computed } from 'vue';
  import initOptionsFunc, { UserRequesetTypeEnum } from './utils';
  import { debounce } from 'lodash-es';

  export interface MsUserSelectorOption {
    id: string;
    name: string;
    email: string;
    disabled?: boolean;
    [key: string]: string | number | boolean | undefined;
  }

  const props = withDefaults(
    defineProps<{
      value: string[] | string; // 选中的值
      disabled?: boolean; // 是否禁用
      disabledKey?: string; // 禁用的key
      valueKey?: string; // value的key
      placeholder?: string;
      firstLabelKey?: string; // 首要的的字段key
      secondLabelKey?: string; // 次要的字段key
      loadOptionParams?: Record<string, any>; // 加载选项的参数
      type?: UserRequesetTypeEnum; // 加载选项的类型
    }>(),
    {
      disabled: false,
      disabledKey: 'disabled',
      valueKey: 'id',
      firstLabelKey: 'name',
      secondLabelKey: 'email',
      type: UserRequesetTypeEnum.SYSTEM_USER_GROUP,
    }
  );

  const emit = defineEmits<{
    (e: 'update:value', value: string[]): void;
  }>();
  const { t } = useI18n();

  const allOptions = ref<MsUserSelectorOption[]>([]);
  const currentOptions = ref<MsUserSelectorOption[]>([]);
  const currentLoadParams = ref<Record<string, any>>(props.loadOptionParams || {});
  const loading = ref(false);

  const currentValue = computed(() => {
    return currentOptions.value.filter((item) => props.value.includes(item.id)) || [];
  });

  const loadList = async () => {
    try {
      loading.value = true;
      const list = (await initOptionsFunc(props.type, currentLoadParams.value || {})) || [];
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
      allOptions.value = [...list];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      allOptions.value = [];
    }
  };

  const debouncedSearch = async (value: string) => {
    if (!value) {
      currentLoadParams.value = {
        ...currentLoadParams.value,
        keyword: value,
      };
      await loadList();
    } else {
      const fn = debounce(
        () => {
          currentLoadParams.value = {
            ...currentLoadParams.value,
            keyword: value,
          };
          loadList();
        },
        300,
        { maxWait: 1000 }
      );
      fn();
    }
  };

  const change = (
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) => {
    const tmpArr = Array.isArray(value) ? value : [value];
    currentOptions.value = tmpArr;
    const { valueKey } = props;
    emit(
      'update:value',
      tmpArr.map((item) => item[valueKey])
    );
    debouncedSearch('');
  };

  onMounted(async () => {
    await loadList();
  });
</script>
