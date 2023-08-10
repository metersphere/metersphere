<template>
  <a-select
    :value="value"
    :disabled="props.disabled"
    multiple
    :virtual-list-props="{ height: 200 }"
    :placeholder="props.placeholder ? t(props.placeholder) : t('common.pleaseSelect')"
    :options="userOptions"
    :field-names="fieldNames"
    @search="handleSearch"
    @change="handleChange"
  >
    <template #label="{ data }">
      <span class="option-name"> {{ data.name }} </span>
    </template>
    <template #option="{ data }">
      <span class="option-name"> {{ data.name }} </span>
      <span class="option-email"> {{ `(${data.email})` }} </span>
    </template>
  </a-select>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { ref, onMounted } from 'vue';
  import { getUserList } from '@/api/modules/setting/usergroup';

  export interface MsUserSelectorProps {
    value: string[];
    disabled?: boolean;
    placeholder?: string;
  }

  export interface UserItem {
    id: string;
    name: string;
    email: string;
  }

  const fieldNames = { value: 'id', label: 'name' };
  const { t } = useI18n();
  const props = defineProps<MsUserSelectorProps>();
  const emit = defineEmits<{
    (e: 'update:value', value: string[]): void;
  }>();
  const allOption = ref<UserItem[]>([]);
  const userOptions = ref<UserItem[]>([]);

  const initUserList = async () => {
    const res = await getUserList();
    allOption.value = res;
    userOptions.value = res;
  };

  const handleSearch = (value: string) => {
    if (value) {
      window.setTimeout(() => {
        userOptions.value = userOptions.value.filter((item) => item.name.includes(value));
      }, 60);
    } else {
      userOptions.value = allOption.value;
    }
  };

  const handleChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[]) => {
    emit('update:value', value as string[]);
  };

  onMounted(() => {
    initUserList();
  });
</script>
