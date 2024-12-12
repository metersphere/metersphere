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
      <a-form ref="memberFormRef" :model="form" layout="vertical">
        <a-form-item
          field="userIds"
          :label="t('project.member.member')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.member.selectMemberEmptyTip') }]"
        >
          <MsUserSelector
            v-model="form.userIds"
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
          <MsSelect
            v-model:model-value="form.roleIds"
            :options="props.userGroupOptions"
            :allow-search="true"
            allow-clear
            :search-keys="['name']"
            value-key="id"
            label-key="name"
            class="w-full"
            :multiple="true"
            :placeholder="t('project.member.selectUserScope')"
          />
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { addOrUpdateProjectMember, getProjectMemberOptions } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { AddProjectMember, ProjectUserOption } from '@/models/projectManagement/projectAndPermission';

  const { t } = useI18n();
  const appStore = useAppStore();

  const lastProjectId = computed(() => appStore.currentProjectId);

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
    projectId: lastProjectId.value,
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
      if (lastProjectId.value) {
        const result = await getProjectMemberOptions(lastProjectId.value);
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
