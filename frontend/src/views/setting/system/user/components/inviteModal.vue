<template>
  <a-modal
    v-model:visible="inviteVisible"
    :title="t('system.user.invite')"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :loading="inviteLoading"
  >
    <a-form ref="inviteFormRef" class="rounded-[4px]" :model="emailForm" layout="vertical">
      <a-form-item
        field="emails"
        :label="t('system.user.inviteEmail')"
        :rules="[{ required: true, message: t('system.user.createUserEmailNotNull') }]"
      >
        <a-input-tag v-model="emailForm.emails" :placeholder="t('system.user.inviteEmailPlaceholder')" allow-clear />
      </a-form-item>
      <a-form-item class="mb-0" field="userGroup" :label="t('system.user.createUserUserGroup')">
        <a-select
          v-model="emailForm.userGroup"
          multiple
          :placeholder="t('system.user.createUserUserGroupPlaceholder')"
          allow-clear
        >
          <a-option v-for="item of userGroupOptions" :key="item.value">{{ item.label }}</a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" @click="cancelInvite">{{ t('system.user.inviteCancel') }}</a-button>
      <a-button type="primary" @click="emailInvite">
        {{ t('system.user.inviteSendEmail') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { cloneDeep } from 'lodash-es';
  import { useI18n } from '@/hooks/useI18n';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits(['update:visible']);

  const inviteVisible = ref(false);
  const inviteLoading = ref(false);
  const inviteFormRef = ref<FormInstance | null>(null);
  const defaultInviteForm = {
    emails: [],
    userGroup: [],
  };
  const emailForm = ref(cloneDeep(defaultInviteForm));
  const userGroupOptions = ref([
    {
      label: 'Beijing',
      value: 'Beijing',
    },
    {
      label: 'Shanghai',
      value: 'Shanghai',
    },
    {
      label: 'Guangzhou',
      value: 'Guangzhou',
    },
  ]);

  watch(
    () => props.visible,
    (val) => {
      inviteVisible.value = val;
    }
  );

  watch(
    () => inviteVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  function cancelInvite() {
    inviteVisible.value = false;
    inviteFormRef.value?.resetFields();
    emailForm.value = cloneDeep(defaultInviteForm);
  }

  function emailInvite() {
    inviteFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        try {
          inviteLoading.value = true;
          cancelInvite();
          Message.success(t('system.user.inviteSuccess'));
        } catch (error) {
          console.log(error);
        } finally {
          inviteLoading.value = false;
        }
      }
    });
  }
</script>

<style lang="less" scoped></style>
