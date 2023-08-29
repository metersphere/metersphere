<template>
  <a-modal
    v-model:visible="currentVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('system.organization.addMember')"
    unmount-on-close
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.organization.addMember') }} </template>
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.organization.member')"
          :rules="[{ required: true, message: t('system.organization.addMemberRequired') }]"
        >
          <MsUserSelector
            v-model:value="form.name"
            :type="UserRequesetTypeEnum.SYSTEM_ORGANIZATION"
            :load-option-params="{ sourceId: props.organizationId || props.projectId }"
          />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" :disabled="form.name.length === 0" @click="handleAddMember">
        {{ t('common.add') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect, onUnmounted } from 'vue';
  import { addUserToOrgOrProject } from '@/api/modules/setting/organizationAndProject';
  import { Message, type FormInstance, type ValidatedError } from '@arco-design/web-vue';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequesetTypeEnum } from '@/components/business/ms-user-selector/utils';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    organizationId?: string;
    projectId?: string;
  }>();

  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'submit'): void;
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

  const handleCancel = () => {
    form.name = [];
    emit('cancel');
  };

  const handleAddMember = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        loading.value = false;
      }
      const { organizationId, projectId } = props;
      try {
        loading.value = true;
        await addUserToOrgOrProject({ userIds: form.name, organizationId, projectId });
        Message.success(t('system.organization.addSuccess'));
        handleCancel();
        emit('submit');
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
@/api/modules/setting/organizationAndProject
