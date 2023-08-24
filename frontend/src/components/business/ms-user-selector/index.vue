<template>
  <a-select
    v-model="currentValue"
    :disabled="props.disabled"
    multiple
    :virtual-list-props="{ height: 200 }"
    :placeholder="props.placeholder ? t(props.placeholder) : t('common.pleaseSelectMember')"
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
  import { ref, onMounted, watch } from 'vue';
  import { getUserByOrganizationOrProject, getAllUser } from '@/api/modules/setting/organizationAndProject';

  export interface MsUserSelectorProps {
    value: string[];
    disabled?: boolean;
    placeholder?: string;
    type?: 'organization' | 'usergroup';
    sourceId?: string;
    disabledKey?: 'disabled' | 'memberFlag';
  }

  export interface UserItem {
    id: string;
    name: string;
    email: string;
    disabled?: boolean;
    memberFlag?: boolean;
  }

  const fieldNames = { value: 'id', label: 'name' };
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
      item.disabled = item[props.disabledKey];
    });
    allOption.value = [...res];
    userOptions.value = [...res];
  };

  const handleSearch = (value: string) => {
    if (value) {
      window.setTimeout(() => {
        userOptions.value = userOptions.value.filter(
          (item) => item.name.includes(value) || currentValue.value.includes(item.id)
        );
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
  watch(
    () => props.value,
    (value) => {
      currentValue.value = value;
    }
  );
</script>
