<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    :ok-text="t('system.userGroup.create')"
    @ok="handelOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.userGroup.createUserGroup') }} </template>
    <div class="form">
      <a-form :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.userGroup.userGroupName')"
          :rules="[{ required: true, message: t('system.userGroup.userGroupNameIsNotNone') }]"
        >
          <a-input v-model="form.name" :placeholder="t('system.userGroup.pleaseInputUserGroupName')" />
        </a-form-item>
        <a-form-item
          field="authScope"
          :label="t('system.userGroup.authScope')"
          :rules="[{ required: true, message: t('system.userGroup.authScopeIsNotNone') }]"
        >
          <a-select
            v-model="form.authScope"
            :virtual-list-props="{ height: 200 }"
            :placeholder="t('system.userGroup.pleaseSelectAuthScope')"
            :options="authOptions"
            :field-names="fieldNames"
            @search="handleSearch"
          >
            <template #label="{ data }">
              <span class="option-name"> {{ data.name }} </span>
            </template>
            <template #option="{ data }">
              <span class="option-name"> {{ data.name }} </span>
              <span class="option-email"> {{ `(${data.email})` }} </span>
            </template>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect } from 'vue';
  import { UserOption } from './type';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();

  const form = reactive({
    name: '',
    authScope: '',
  });
  const allOption: UserOption[] = [
    { id: 1, name: 'llb', email: 'name@163.com' },
    {
      id: 2,
      name: 'rubyliu',
      email: 'rubyliu@163.com',
    },
    {
      id: 3,
      name: 'jack',
      email: 'jack@163.com',
    },
  ];
  const authOptions = ref<UserOption[]>(allOption);

  const loading = ref(false);

  const fieldNames = { value: 'id', label: 'name' };
  const currentVisible = ref(props.visible);
  const handleSearch = (value: string) => {
    if (value) {
      loading.value = true;
      window.setTimeout(() => {
        authOptions.value = authOptions.value.filter((item) => item.name.includes(value));
        loading.value = false;
      }, 60);
    } else {
      authOptions.value = allOption;
    }
  };
  watchEffect(() => {
    currentVisible.value = props.visible;
  });
  const handleCancel = () => {
    emit('cancel');
  };

  const handelOk = () => {
    // eslint-disable-next-line no-console
    console.log('ok');
    handleCancel();
  };
</script>
