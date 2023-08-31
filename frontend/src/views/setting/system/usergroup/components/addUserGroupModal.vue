<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    :ok-text="t('system.userGroup.create')"
    unmount-on-close
    @cancel="handleCancel(false)"
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
    <template #footer>
      <a-button type="secondary" :disabled="loading" @click="handleCancel(false)">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" :disabled="form.name.length === 0" @click="handleOK">
        {{ t('common.add') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect } from 'vue';
  import { UserGroupItem } from '@/models/setting/usergroup';
  import { Message, type FormInstance, type ValidatedError } from '@arco-design/web-vue';
  import { updateOrAddUserGroup } from '@/api/modules/setting/usergroup';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    list: UserGroupItem[];
  }>();

  const formRef = ref<FormInstance>();
  const loading = ref(false);

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
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
  const handleCancel = (shouldSearch: boolean) => {
    form.name = '';
    form.type = '';
    emit('cancel', shouldSearch);
  };

  const handleOK = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        const res = await updateOrAddUserGroup(form);
        if (res) {
          Message.success(t('system.userGroup.addUserGroupSuccess'));
          loading.value = false;
          handleCancel(true);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };
</script>
