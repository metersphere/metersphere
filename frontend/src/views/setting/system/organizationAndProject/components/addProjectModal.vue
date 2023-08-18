<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    class="ms-modal-form ms-modal-medium"
    :ok-text="isEdit ? t('common.update') : t('common.create')"
    unmount-on-close
    @cancel="handleCancel"
  >
    <template #title>
      <span v-if="isEdit">
        {{ t('system.project.updateProject') }}
        <span class="text-[var(--color-text-4)]">({{ props.currentProject?.name }})</span>
      </span>
      <span v-else>
        {{ t('system.project.create') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
        <a-form-item
          field="name"
          required
          :label="t('system.project.name')"
          :rules="[{ required: true, message: t('system.project.projectNameRequired') }]"
        >
          <a-input v-model="form.name" :placeholder="t('system.project.projectNamePlaceholder')" />
        </a-form-item>
        <a-form-item field="organizationId" :label="t('system.project.affiliatedOrg')">
          <a-input v-model="form.organizationId" :placeholder="t('system.project.affiliatedOrgPlaceholder')" />
        </a-form-item>
        <a-form-item field="userIds" :label="t('system.project.projectAdmin')">
          <MsUserSelector v-model:value="form.userIds" placeholder="system.project.projectAdminPlaceholder" />
        </a-form-item>
        <a-form-item field="description" :label="t('system.organization.description')">
          <a-input v-model="form.description" :placeholder="t('system.organization.descriptionPlaceholder')" />
        </a-form-item>
        <a-form-item field="enable" :label="t('system.organization.description')">
          <a-switch v-model="form.enable" :placeholder="t('system.organization.descriptionPlaceholder')" />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" @click="handleBeforeOk">
        {{ isEdit ? t('common.confirm') : t('common.create') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect, computed } from 'vue';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import MsUserSelector from '@/components/bussiness/ms-user-selector/index.vue';
  import { createOrUpdateOrg } from '@/api/modules/setting/system/organizationAndProject';
  import { Message } from '@arco-design/web-vue';
  import { CreateOrUpdateSystemProjectParams } from '@/models/setting/system/orgAndProject';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    currentProject?: CreateOrUpdateSystemProjectParams;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const isEdit = computed(() => !!props.currentProject?.id);

  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();

  const form = reactive<CreateOrUpdateSystemProjectParams>({
    name: '',
    userIds: [],
    organizationId: '',
    description: '',
    enable: true,
    module: [],
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
        loading.value = true;
        await createOrUpdateOrg({ id: props.currentProject?.id, ...form });
        Message.success(
          isEdit.value
            ? t('system.organization.updateOrganizationSuccess')
            : t('system.organization.createOrganizationSuccess')
        );
        handleCancel();
        return true;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
        return false;
      } finally {
        loading.value = false;
      }
    });
  };
  watchEffect(() => {
    if (props.currentProject) {
      form.name = props.currentProject.name;
      form.userIds = props.currentProject.userIds;
      form.description = props.currentProject.description;
      form.organizationId = props.currentProject.organizationId;
      form.enable = props.currentProject.enable;
    }
  });
</script>
