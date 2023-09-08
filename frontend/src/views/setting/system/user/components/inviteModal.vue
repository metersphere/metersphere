<template>
  <a-modal
    v-model:visible="inviteVisible"
    :title="t('system.user.invite')"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
  >
    <a-form ref="inviteFormRef" class="rounded-[4px]" :model="emailForm" layout="vertical">
      <a-form-item
        field="emails"
        :label="t('system.user.inviteEmail')"
        :rules="[{ required: true, message: t('system.user.createUserEmailNotNull') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <MsTagsInput
          v-model:model-value="emailForm.emails"
          placeholder="system.user.inviteEmailPlaceholder"
          allow-clear
          unique-value
          retain-input-value
        />
      </a-form-item>
      <a-form-item class="mb-0" field="userGroup" :label="t('system.user.createUserUserGroup')">
        <a-select
          v-model="emailForm.userGroup"
          multiple
          :placeholder="t('system.user.createUserUserGroupPlaceholder')"
          allow-clear
        >
          <a-option
            v-for="item of userGroupOptions"
            :key="item.id"
            :tag-props="{ closable: emailForm.userGroup.length > 1 }"
            :value="item.id"
            :disabled="emailForm.userGroup.includes(item.id) && emailForm.userGroup.length === 1"
          >
            {{ item.name }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="inviteLoading" @click="cancelInvite">
        {{ t('system.user.inviteCancel') }}
      </a-button>
      <a-button type="primary" :loading="inviteLoading" @click="emailInvite">
        {{ t('system.user.inviteSendEmail') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { cloneDeep } from 'lodash-es';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import { inviteUser } from '@/api/modules/setting/user';

  import type { SystemRole } from '@/models/setting/user';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    userGroupOptions: SystemRole[];
  }>();

  const emit = defineEmits(['update:visible']);

  const inviteVisible = ref(false);
  const inviteLoading = ref(false);
  const inviteFormRef = ref<FormInstance | null>(null);
  const defaultInviteForm = {
    emails: [] as string[],
    userGroup: [] as string[],
  };
  const emailForm = ref(cloneDeep(defaultInviteForm));

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

  watch(
    () => props.userGroupOptions,
    (arr) => {
      if (arr.length) {
        emailForm.value.userGroup = arr.filter((e: SystemRole) => e.selected === true).map((e: SystemRole) => e.id);
      }
    }
  );

  function cancelInvite() {
    inviteVisible.value = false;
    inviteFormRef.value?.resetFields();
    emailForm.value.emails = [];
    emailForm.value.userGroup = props.userGroupOptions
      .filter((e: SystemRole) => e.selected === true)
      .map((e: SystemRole) => e.id);
  }

  function emailInvite() {
    inviteFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        try {
          inviteLoading.value = true;
          await inviteUser({
            inviteEmails: emailForm.value.emails,
            userRoleIds: emailForm.value.userGroup,
          });
          Message.success(t('system.user.inviteSuccess'));
          inviteVisible.value = false;
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
