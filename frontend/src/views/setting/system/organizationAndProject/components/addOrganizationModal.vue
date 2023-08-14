<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    :ok-text="t('system.organization.create')"
    unmount-on-close
    :on-before-ok="handleBeforeOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.organization.createOrganization') }} </template>
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
        <a-form-item
          field="name"
          required
          :label="t('system.organization.organizationName')"
          :rules="[{ required: true, message: t('system.organization.organizationNameRequired') }]"
        >
          <a-input v-model="form.name" :placeholder="t('system.organization.organizationNamePlaceholder')" />
        </a-form-item>
        <a-form-item field="name" :label="t('system.organization.organizationAdmin')">
          <MsUserSelector v-model:value="form.admin" placeholder="system.organization.organizationAdminPlaceholder" />
        </a-form-item>
        <a-form-item field="description" :label="t('system.organization.description')">
          <a-input v-model="form.description" :placeholder="t('system.organization.descriptionPlaceholder')" />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect } from 'vue';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import MsUserSelector from '@/components/bussiness/ms-user-selector/index.vue';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    organizationId?: string;
  }>();

  const formRef = ref<FormInstance>();

  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();

  const form = reactive({
    name: '',
    admin: [],
    description: '',
  });

  const currentVisible = ref(props.visible);

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
      return true;
    });
  };
</script>
