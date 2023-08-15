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
          <MsUserSelector
            v-model:value="form.memberIds"
            placeholder="system.organization.organizationAdminPlaceholder"
          />
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
  import { createOrUpdateOrg } from '@/api/modules/setting/system/organizationAndProject';
  import { Message } from '@arco-design/web-vue';
  import { CreateOrUpdateSystemOrgParams } from '@/models/setting/system/orgAndProject';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    currentOrganization?: CreateOrUpdateSystemOrgParams;
  }>();

  const formRef = ref<FormInstance>();

  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();

  const form = reactive<{ name: string; memberIds: string[]; description: string }>({
    name: '',
    memberIds: [],
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
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return false;
      }
      try {
        await createOrUpdateOrg({ id: props.currentOrganization?.id, ...form });
        Message.success(
          props.currentOrganization?.id
            ? t('system.organization.updateOrganizationSuccess')
            : t('system.organization.createOrganizationSuccess')
        );
        handleCancel();
        return true;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
        return false;
      }
    });
  };
  watchEffect(() => {
    if (props.currentOrganization) {
      form.name = props.currentOrganization.name;
      form.memberIds = props.currentOrganization.memberIds;
      form.description = props.currentOrganization.description;
    }
  });
</script>
