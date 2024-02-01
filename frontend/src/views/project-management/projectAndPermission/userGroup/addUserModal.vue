<template>
  <a-modal
    v-model:visible="currentVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('system.organization.addMember')"
    unmount-on-close
    :mask-closable="false"
    @cancel="handleCancel(false)"
  >
    <template #title> {{ t('system.organization.addMember') }} </template>
    <div class="form">
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.organization.member')"
          :rules="[{ required: true, message: t('system.organization.addMemberRequired') }]"
        >
          <MsUserSelector
            v-model="form.name"
            :type="UserRequestTypeEnum.PROJECT_USER_GROUP"
            :load-option-params="{ projectId: props.projectId, userRoleId: props.userRoleId }"
            disabled-key="checkRoleFlag"
          />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel(false)">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" :disabled="form.name.length === 0" @click="handleAddMember">
        {{ t('common.add') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { onUnmounted, reactive, ref, watchEffect } from 'vue';
  import { type FormInstance, Message, type ValidatedError } from '@arco-design/web-vue';

  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { addUserToUserGroup } from '@/api/modules/project-management/usergroup';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    projectId: string;
    userRoleId: string;
  }>();

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
  }>();

  const currentVisible = ref(props.visible);
  const loading = ref(false);

  const form = reactive({
    name: [],
  });

  const formRef = ref<FormInstance>();

  watchEffect(() => {
    currentVisible.value = props.visible;
  });

  const handleCancel = (shouldSearch: boolean) => {
    form.name = [];
    emit('cancel', shouldSearch);
  };

  const handleAddMember = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        loading.value = false;
      }
      const { projectId, userRoleId } = props;
      try {
        loading.value = true;
        await addUserToUserGroup({ userIds: form.name, projectId, userRoleId });
        Message.success(t('system.organization.addSuccess'));
        handleCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };

  onUnmounted(() => {
    form.name = [];
    loading.value = false;
  });
</script>

<style lang="less" scoped>
  .option-name {
    color: var(--color-text-1);
  }
  .option-email {
    color: var(--color-text-4);
  }
</style>
