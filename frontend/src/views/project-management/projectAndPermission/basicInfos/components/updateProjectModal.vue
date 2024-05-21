<template>
  <MsDialog
    v-model:visible="updateVisible"
    dialog-size="medium"
    title="project.basicInfo.updateProjectTitle"
    :close="closeHandler"
    :confirm="confirmHandler"
    :switch-props="{
      showSwitch: false,
    }"
  >
    <div class="form">
      <a-form ref="projectFormRef" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('project.basicInfo.projectName')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.basicInfo.projectNameTip') }]"
        >
          <a-input v-model="form.name" allow-clear :max-length="255" />
        </a-form-item>
        <a-form-item field="description" :label="t('common.desc')" asterisk-position="end">
          <a-textarea v-model="form.description" :max-length="1000" allow-clear auto-size />
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';

  import { updateProject } from '@/api/modules/project-management/basicInfo';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { ProjectBasicInfoModel, UpdateProject } from '@/models/projectManagement/basicInfo';

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentOrgId = computed(() => appStore.currentOrgId);

  const emits = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
  }>();

  const initForm = {
    organizationId: '',
    name: '',
    description: '',
    enable: false,
    moduleIds: [], // 模块设置
    id: '',
    userIds: [], // 成员数
  };
  const form = ref<UpdateProject>({ ...initForm });

  const updateVisible = ref<boolean>(false);
  const projectFormRef = ref<FormInstance | null>(null);

  const closeHandler = () => {
    projectFormRef.value?.resetFields();
    updateVisible.value = false;
    form.value = { ...initForm };
  };

  const confirmHandler = async () => {
    await projectFormRef.value?.validate().then(async (error) => {
      if (!error) {
        try {
          await updateProject(form.value);
          Message.success(t('project.basicInfo.updateContentTip'));
          emits('success');
          closeHandler();
        } catch (e) {
          console.log(e);
        }
      } else {
        return false;
      }
    });
  };

  const editProject = (projectItem: ProjectBasicInfoModel) => {
    const { id, name, description } = projectItem;
    form.value = {
      id,
      name,
      description,
      organizationId: currentOrgId.value,
    };
  };

  watch(
    () => updateVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  defineExpose({
    editProject,
  });
</script>

<style scoped></style>
