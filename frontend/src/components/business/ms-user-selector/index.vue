<template>
  <a-select
    :model-value="currentValue"
    :placeholder="t(props.placeholder || 'common.pleaseSelectMember')"
    multiple
    :value-key="props.valueKey"
    :disabled="props.disabled"
    allow-clear
    @change="change"
    @search="search"
  >
    <template #label="{ data }">
      <span class="text-[var(--color-text-1)]"> {{ data.value.name }} </span>
    </template>
    <a-option v-for="data in currentOptions" :key="data.id" :disabled="data.disabled" :value="data">
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

  const currentOptions = ref<MsUserSelectorOption[]>([]);
  const oldOptions = ref<MsUserSelectorOption[]>([]);

  const currentValue = computed(() => {
    return currentOptions.value.filter((item) => props.value.includes(item.id)) || [];
  });

  const change = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[]) => {
    const tmpArr = Array.isArray(value) ? value : [value];
    const { valueKey } = props;
    emit(
      'update:value',
      tmpArr.map((item) => item[valueKey])
    );
  };
  const loadList = async () => {
    try {
      const list = (await initOptionsFunc(props.type, props.loadOptionParams || {})) || [];
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
      currentOptions.value = [...list];
      oldOptions.value = [...list];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      currentOptions.value = [];
      oldOptions.value = [];
    }
  };

  const idInSelected = (id: string) => {
    return props.value.includes(id);
  };

  const search = async (value: string) => {
    let timeout = null;
    if (value) {
      timeout = window.setTimeout(() => {
        currentOptions.value = currentOptions.value.filter(
          (item) => item.name.includes(value) || item.email.includes(value) || idInSelected(item.id)
        );
      }, 60);
    } else {
      if (timeout) {
        window.clearTimeout(timeout);
      }
      currentOptions.value = [...oldOptions.value];
    }
  };

  onMounted(async () => {
    await loadList();
  });
</script>
