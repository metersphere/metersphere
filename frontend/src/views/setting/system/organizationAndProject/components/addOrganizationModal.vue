<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('system.organization.create')"
    unmount-on-close
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
            :type="UserRequesetTypeEnum.SYSTEM_ORGANIZATION_ADMIN"
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
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect, computed } from 'vue';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { createOrUpdateOrg } from '@/api/modules/setting/organizationAndProject';
  import { Message } from '@arco-design/web-vue';
  import { CreateOrUpdateSystemOrgParams } from '@/models/setting/system/orgAndProject';
  import { UserRequesetTypeEnum } from '@/components/business/ms-user-selector/utils';

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
      form.memberIds = props.currentOrganization.memberIds;
      form.description = props.currentOrganization.description;
    }
  });
  const isEdit = computed(() => !!props.currentOrganization?.id);
</script>
