<template>
  <a-modal
    v-model:visible="inviteVisible"
    :title="t('system.user.invite')"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    @close="cancelInvite"
  >
    <a-form ref="inviteFormRef" class="overflow-hidden rounded-[4px]" :model="emailForm" layout="vertical">
      <a-form-item
        id="emailInviteInput"
        field="emails"
        :label="t('system.user.inviteEmail')"
        :rules="[{ required: true, message: t('system.user.createUserEmailNotNull') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <MsTagsInput
          v-model:model-value="emailForm.emails"
          placeholder="system.user.inviteEmailPlaceholder"
          tags-duplicate-text="system.user.inviteEmailRepeat"
          :input-validator="validateInputEmailTag"
          allow-clear
          unique-value
          retain-input-value
        />
      </a-form-item>
      <a-form-item class="mb-0" field="userGroup" :label="t('system.user.createUserUserGroup')">
        <a-select v-model="emailForm.userGroup" multiple :placeholder="t('system.user.createUserUserGroupPlaceholder')">
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
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { inviteMember } from '@/api/modules/project-management/projectMember';
  import { inviteOrgMember } from '@/api/modules/setting/member';
  import { inviteUser } from '@/api/modules/setting/user';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { validateEmail } from '@/utils/validate';

  import { ProjectUserOption } from '@/models/projectManagement/projectAndPermission';
  import type { SystemRole } from '@/models/setting/user';

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      visible: boolean;
      userGroupOptions: (SystemRole | ProjectUserOption)[];
      range?: 'system' | 'organization' | 'project';
    }>(),
    {
      range: 'system',
    }
  );

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
        if (props.range === 'system') {
          emailForm.value.userGroup = (arr as SystemRole[]).filter((e) => e.selected === true).map((e) => e.id);
        } else if (props.range === 'project') {
          emailForm.value.userGroup = ['project_member'];
        } else if (props.range === 'organization') {
          emailForm.value.userGroup = ['org_member'];
        }
      }
    }
  );

  function validateInputEmailTag(value: string) {
    if (validateEmail(value)) {
      return true;
    }
    Message.warning(t('system.config.email.emailErrTip'));
    return false;
  }

  function cancelInvite() {
    inviteVisible.value = false;
    inviteFormRef.value?.resetFields();
    emailForm.value.emails = [];
    if (props.range === 'system') {
      emailForm.value.userGroup = (props.userGroupOptions as SystemRole[])
        .filter((e) => e.selected === true)
        .map((e) => e.id);
    } else if (props.range === 'project') {
      emailForm.value.userGroup = ['project_member'];
    } else if (props.range === 'organization') {
      emailForm.value.userGroup = ['org_member'];
    }
  }

  function handleInviteError(error: any) {
    if (error?.messageDetail) {
      try {
        const errEmails = JSON.parse(error.messageDetail);
        if (Array.isArray(errEmails)) {
          const inputEmails = document.getElementById('emailInviteInput')?.querySelectorAll('.arco-input-tag-tag');
          inputEmails?.forEach((input) => {
            if (errEmails.includes(input.textContent)) {
              input.setAttribute('style', 'border-color: rgb(var(--danger-6))');
            }
          });
        }
      } catch (_error) {
        // eslint-disable-next-line no-console
        console.log(_error);
      }
    }
  }

  function emailInvite() {
    inviteFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        try {
          inviteLoading.value = true;
          if (props.range === 'project') {
            await inviteMember({
              inviteEmails: emailForm.value.emails,
              userRoleIds: emailForm.value.userGroup,
              projectId: appStore.currentProjectId,
              organizationId: appStore.currentOrgId,
            });
          } else if (props.range === 'organization') {
            await inviteOrgMember({
              inviteEmails: emailForm.value.emails,
              userRoleIds: emailForm.value.userGroup,
              organizationId: appStore.currentOrgId,
            });
          } else {
            await inviteUser({
              inviteEmails: emailForm.value.emails,
              userRoleIds: emailForm.value.userGroup,
            });
          }
          Message.success(t('system.user.inviteSuccess'));
          inviteVisible.value = false;
        } catch (error) {
          // eslint-disable-next-line no-console
          handleInviteError(error);
        } finally {
          inviteLoading.value = false;
        }
      }
    });
  }
</script>

<style lang="less" scoped></style>
