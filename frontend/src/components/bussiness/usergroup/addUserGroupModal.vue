<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    :ok-text="t('system.userGroup.create')"
    :loading="props.loading"
    unmount-on-close
    :on-before-ok="handleBeforeOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.userGroup.createUserGroup') }} </template>
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
        <a-form-item
          field="name"
          required
          :label="t('system.userGroup.userGroupName')"
          :rules="[
            { required: true, message: t('system.userGroup.userGroupNameIsNotNone') },
            { validator: validateName },
          ]"
        >
          <a-input v-model="form.name" :placeholder="t('system.userGroup.pleaseInputUserGroupName')" />
        </a-form-item>
        <a-form-item
          field="type"
          :label="t('system.userGroup.authScope')"
          :rules="[{ required: true, message: t('system.userGroup.authScopeIsNotNone') }]"
        >
          <a-select v-model="form.type" :placeholder="t('system.userGroup.pleaseSelectAuthScope')">
            <a-option value="SYSTEM">{{ t('system.userGroup.SYSTEM') }}</a-option>
            <a-option value="ORGANIZATION">{{ t('system.userGroup.ORGANIZATION') }}</a-option>
            <a-option value="PROJECT">{{ t('system.userGroup.PROJECT') }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect } from 'vue';
  import { UserGroupItem } from './type';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    list: UserGroupItem[];
    loading: boolean;
  }>();

  const formRef = ref<FormInstance>();

  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'submit', value: Partial<UserGroupItem>): void;
  }>();

  const form = reactive({
    name: '',
    type: '',
  });

  const currentVisible = ref(props.visible);

  const validateName = (value: string, callback: (error?: string) => void) => {
    if (value !== '') {
      const isExist = props.list.some((item) => item.name === value);
      if (isExist) {
        callback(t('system.userGroup.userGroupNameIsExist', { name: value }));
      }
      callback();
    }
  };

  watchEffect(() => {
    currentVisible.value = props.visible;
  });
  const handleCancel = () => {
    emit('cancel');
  };

  const handleBeforeOk = () => {
    formRef.value?.validate((errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return false;
      }
      emit('submit', form);
      return true;
    });
  };
</script>
