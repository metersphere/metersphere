<template>
  <a-modal
    v-model:visible="currentVisible"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('system.organization.create')"
    unmount-on-close
    title-align="start"
    @cancel="handleCancel(false)"
  >
    <template #title>
      <span v-if="isEdit">
        {{ t('system.organization.updateOrganization') }}
        <a-tooltip :content="props.currentOrganization?.name" position="right">
          <span class="font-normal text-[var(--color-text-4)]"
            >({{ characterLimit(props.currentOrganization?.name) }})</span
          >
        </a-tooltip>
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
          asterisk-position="end"
          :label="t('system.organization.organizationName')"
          :rules="[
            { required: true, message: t('system.organization.organizationNameRequired') },
            { maxLength: 255, message: t('common.nameIsTooLang') },
          ]"
        >
          <a-input
            v-model="form.name"
            :max-length="255"
            show-word-limit
            :placeholder="t('system.organization.organizationNamePlaceholder')"
          />
        </a-form-item>
        <a-form-item
          field="userIds"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.organization.organizationAdminRequired') }]"
          :label="t('system.organization.organizationAdmin')"
        >
          <MsUserSelector
            v-model="form.userIds"
            placeholder="system.organization.organizationAdminPlaceholder"
            :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_ADMIN"
          />
        </a-form-item>
        <a-form-item field="description" :label="t('common.desc')">
          <a-textarea
            v-model="form.description"
            allow-clear
            :placeholder="t('system.organization.descriptionPlaceholder')"
            :auto-size="{ minRows: 1 }"
            style="resize: vertical"
            :max-length="1000"
          />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :disabled="loading" @click="handleCancel(false)">
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
  import { useUserStore } from '@/store';
  import { characterLimit } from '@/utils';

  import { CreateOrUpdateSystemOrgParams } from '@/models/setting/system/orgAndProject';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const props = defineProps<{
    currentOrganization?: CreateOrUpdateSystemOrgParams;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const userStore = useUserStore();

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
  }>();

  const form = reactive<{ name: string; userIds: string[]; description: string }>({
    name: '',
    userIds: userStore.id ? [userStore.id] : [],
    description: '',
  });

  const currentVisible = defineModel<boolean>('visible', { default: false });

  const handleCancel = (shouldSearch = false) => {
    emit('cancel', shouldSearch);
    formRef.value?.resetFields();
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
        handleCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
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
