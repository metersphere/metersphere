<template>
  <a-select
    v-model="currentValue"
    :disabled="props.disabled"
    multiple
    :placeholder="props.placeholder ? t(props.placeholder) : t('common.pleaseSelectMember')"
    value-key="id"
    @search="handleSearch"
    @change="handleChange"
  >
    <template #label="{ data }">
      <span class="option-name"> {{ data.value.name }} </span>
    </template>
    <a-option v-for="data in userOptions" :key="data.id" :disabled="(data.disabled as boolean)" :value="data">
      <span class="option-name"> {{ data.name }} </span>
      <span class="option-email"> {{ `(${data.email})` }} </span>
    </a-option>
  </a-select>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { ref, onMounted, watch } from 'vue';
  import { getUserByOrganizationOrProject, getAllUser } from '@/api/modules/setting/organizationAndProject';

  export interface MsUserSelectorProps {
    value: string[];
    disabled?: boolean;
    placeholder?: string;
    type?: 'organization' | 'usergroup';
    sourceId?: string;
    disabledKey?: string;
  }

  export interface UserItem {
    id: string;
    name: string;
    email: string;
    [key: string]: boolean | string;
  }

  const { t } = useI18n();
  const props = withDefaults(defineProps<MsUserSelectorProps>(), {
    disabled: false,
    type: 'usergroup',
    disabledKey: 'disabled',
  });
  const emit = defineEmits<{
    (e: 'update:value', value: string[]): void;
  }>();
  const currentValue = ref(props.value);

  const allOption = ref<UserItem[]>([]);
  const userOptions = ref<UserItem[]>([]);

  const initUserList = async () => {
    let res: UserItem[] = [];
    if (props.type === 'organization') {
      if (!props.sourceId) {
        return;
      }
      res = await getUserByOrganizationOrProject(props.sourceId);
    } else {
      res = await getAllUser();
    }
    res.forEach((item) => {
      item.disabled = item[props.disabledKey as string];
    });
    allOption.value = [...res];
    userOptions.value = [...res];
  };

  const handleSearch = (value: string) => {
    let timmer = null;
    if (value) {
      timmer = window.setTimeout(() => {
        userOptions.value = userOptions.value.filter(
          (item) => item.name.includes(value) || currentValue.value.includes(item.id)
        );
      }, 60);
    } else {
      if (timmer) window.clearTimeout(timmer);
      userOptions.value = allOption.value;
    }
  };

  const handleChange = (value: string | number | Record<string, any> | (string | number | Record<string, any>)[]) => {
    emit('update:value', value as string[]);
  };

  onMounted(() => {
    initUserList();
  });
  watch(
    () => props.value,
    (value) => {
      currentValue.value = value;
    }
  );
</script>
