<template>
  <a-modal
    v-model:visible="currentVisible"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('system.organization.create')"
    unmount-on-close
    title-align="start"
    @cancel="handleCancel"
  >
    <template #title>
      <span v-if="isEdit">
        {{ t('system.organization.updateOrganization') }}
        <span class="text-[var(--color-text-4)]">({{ props.currentOrganization?.name }})</span>
      </span>
      <span v-else>
        {{ t('system.organization.createOrganization') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
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
            v-model="form.userIds"
            placeholder="system.organization.organizationAdminPlaceholder"
            :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_ADMIN"
          />
        </a-form-item>
        <a-form-item field="description" :label="t('system.organization.description')">
          <a-input v-model="form.description" :placeholder="t('system.organization.descriptionPlaceholder')" />
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
  import { computed, reactive, ref, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { createOrUpdateOrg } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';

  import { CreateOrUpdateSystemOrgParams } from '@/models/setting/system/orgAndProject';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    currentOrganization?: CreateOrUpdateSystemOrgParams;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);

  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'submit'): void;
  }>();

  const form = reactive<{ name: string; userIds: string[]; description: string }>({
    name: '',
    userIds: [],
    description: '',
  });

  const currentVisible = ref(props.visible);

  watchEffect(() => {
    currentVisible.value = props.visible;
  });
  const handleCancel = () => {
    emit('cancel');
  };

  const handleSubmit = () => {
    handleCancel();
    emit('submit');
  };

  const handleBeforeOk = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return false;
      }
      try {
        loading.value = true;
        await createOrUpdateOrg({ id: props.currentOrganization?.id, ...form });
        Message.success(
          props.currentOrganization?.id
            ? t('system.organization.updateOrganizationSuccess')
            : t('system.organization.createOrganizationSuccess')
        );
        handleSubmit();
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
    if (props.currentOrganization) {
      form.name = props.currentOrganization.name;
      form.userIds = props.currentOrganization.userIds;
      form.description = props.currentOrganization.description;
    }
  });
  const isEdit = computed(() => !!props.currentOrganization?.id);
</script>
