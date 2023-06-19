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
          <a-select v-model="form.authScope" :placeholder="t('system.userGroup.pleaseSelectAuthScope')">
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

  const currentVisible = ref(props.visible);

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
