<template>
  <MsDialog
    v-model:visible="visible"
    dialog-size="medium"
    title="project.member.addMember"
    ok-text="project.member.add"
    :confirm="confirmHandler"
    :close="closeHandler"
  >
    <div class="form">
      <a-form ref="memberFormRef" :model="form" size="large" layout="vertical">
        <a-form-item
          field="userIds"
          :label="t('project.member.member')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.member.selectMemberEmptyTip') }]"
        >
          <MsUserSelector
            v-model:value="form.userIds"
            :load-option-params="{ projectId: lastProjectId }"
            :type="UserRequestTypeEnum.PROJECT_PERMISSION_MEMBER"
            placeholder="project.member.selectMemberScope"
            disabled-key="memberFlag"
          />
        </a-form-item>
        <a-form-item
          field="roleIds"
          :label="t('project.member.tableColumnUserGroup')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.member.selectUserEmptyTip') }]"
        >
          <a-select v-model="form.roleIds" multiple allow-clear :placeholder="t('project.member.selectUserScope')">
            <a-option v-for="item of props.userGroupOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import { getProjectMemberOptions, addOrUpdateProjectMember } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import type {
    ProjectUserOption,
    ActionProjectMember,
    AddProjectMember,
  } from '@/models/projectManagement/projectAndPermission';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  const { t } = useI18n();
  const userStore = useUserStore();
  const lastProjectId = userStore.$state?.lastProjectId;

  const props = defineProps<{
    userGroupOptions: ProjectUserOption[];
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
  }>();

  const visible = ref<boolean>(false);

  const initFormValue: AddProjectMember = {
    roleIds: ['project_member'],
    userIds: [],
    projectId: lastProjectId as string,
  };

  const form = ref<AddProjectMember>({ ...initFormValue });

  const memberFormRef = ref<FormInstance | null>(null);

  const closeHandler = () => {
    memberFormRef.value?.resetFields();
    visible.value = false;
    form.value = { ...initFormValue };
  };

  // 添加项目成员
  const confirmHandler = async () => {
    await memberFormRef.value?.validate().then(async (error) => {
      if (!error) {
        try {
          await addOrUpdateProjectMember(form.value);
          Message.success(t('project.member.batchModalSuccess'));
          closeHandler();
          emits('success');
        } catch (e) {
          console.log(e);
        }
      } else {
        return false;
      }
    });
  };

  const memberList = ref<ProjectUserOption[]>([]);

  // 初始化项目成员
  const initProjectMemberOptions = async () => {
    try {
      if (lastProjectId) {
        const result = await getProjectMemberOptions(lastProjectId);
        memberList.value = result;
      }
    } catch (error) {
      console.log(error);
    }
  };

  watch(
    () => visible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  defineExpose({
    initProjectMemberOptions,
  });
</script>

<style scoped></style>
