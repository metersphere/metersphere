<template>
  <a-modal
    v-model:visible="dialogVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('organization.member.Confirm')"
    :cancel-text="t('organization.member.Cancel')"
  >
    <template #title>
      {{ type === 'add' ? t('organization.member.addMember') : t('organization.member.updateMember') }}
    </template>
    <div class="form">
      <a-form ref="memberFormRef" :model="form" size="large" layout="vertical">
        <a-form-item v-if="type === 'edit'" :label="t('organization.member.project')" asterisk-position="end">
          <a-select
            v-model="form.projectIds"
            multiple
            :placeholder="t('organization.member.selectProjectScope')"
            allow-clear
          >
            <a-option v-for="item of props.projectList" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-else
          field="memberIds"
          :label="t('organization.member.member')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('organization.member.selectMemberEmptyTip') }]"
        >
          <a-select
            v-model="form.memberIds"
            multiple
            :placeholder="t('organization.member.selectMemberScope')"
            allow-clear
          >
            <a-option v-for="item of memberList" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          field="userRoleIds"
          :label="t('organization.member.tableColunmUsergroup')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('organization.member.selectUserEmptyTip') }]"
        >
          <a-select
            v-model="form.userRoleIds"
            multiple
            allow-clear
            :placeholder="t('organization.member.selectUserScope')"
          >
            <a-option v-for="item of props.userGroupOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" @click="handleCancel">{{ t('organization.member.Cancel') }}</a-button>
      <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleOK">
        {{ t('organization.member.Confirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { addOrUpdate, getUser } from '@/api/modules/setting/member';
  import { useUserStore } from '@/store';
  import type { AddorUpdateMemberModel, MemberItem, LinkList } from '@/models/setting/member';

  const { t } = useI18n();
  const userStore = useUserStore();
  const lastOrganizationId = userStore.$state?.lastOrganizationId as string;
  const props = defineProps<{
    visible: boolean;
    userGroupOptions: LinkList;
    projectList: LinkList;
  }>();
  const dialogVisible = ref<boolean>(false);
  const memberList = ref<LinkList>([]);
  const title = ref<string>('');
  const type = ref<string>('');
  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'success'): void;
  }>();

  const confirmLoading = ref<boolean>(false);
  const memberFormRef = ref<FormInstance | null>(null);
  const initFormValue = {
    organizationId: userStore.$state?.lastOrganizationId,
    userRoleIds: [],
    memberIds: [],
    projectIds: [],
  };
  const form = ref<AddorUpdateMemberModel>({ ...initFormValue });
  const handleCancel = () => {
    memberFormRef.value?.resetFields();
    form.value = { ...initFormValue };
    dialogVisible.value = false;
  };
  const edit = (record: MemberItem) => {
    const { userRoleIdNameMap, projectIdNameMap } = record;
    form.value.memberIds = [record.id as string];
    form.value.userRoleIds = (userRoleIdNameMap || []).map((item) => item.id);
    form.value.projectIds = (projectIdNameMap || []).map((item) => item.id);
  };
  const handleOK = () => {
    memberFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          confirmLoading.value = true;
          let params = {};
          const { organizationId, memberIds, userRoleIds, projectIds } = form.value;
          params = Object.assign(params, {
            organizationId,
            userRoleIds,
          });
          params =
            type.value === 'add'
              ? {
                  ...params,
                  memberIds,
                }
              : {
                  ...params,
                  projectIds,
                  memberId: memberIds?.join(),
                };
          await addOrUpdate(params, type.value);
          Message.success(
            type.value === 'add'
              ? t('organization.member.batchModalSuccess')
              : t('organization.member.batchUpdateSuccess')
          );
          handleCancel();
          emits('success');
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      } else {
        return false;
      }
    });
  };
  const getUserOptions = async () => {
    memberList.value = await getUser(lastOrganizationId);
  };
  watchEffect(() => {
    dialogVisible.value = props.visible;
    if (props.visible) getUserOptions();
  });
  watch(
    () => dialogVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );
  defineExpose({
    title,
    type,
    edit,
  });
</script>

<style lang="less" scoped></style>
