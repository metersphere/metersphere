<template>
  <MsDialog
    v-model:visible="updateVisible"
    dialog-size="medium"
    title="project.basicInfo.updateProjectTitle"
    :close="closeHandler"
    :confirm="confirmHandler"
    :switch-props="{
      enable: isEnable,
      switchName: t('project.basicInfo.status'),
      switchTooltip: t('project.basicInfo.createTip'),
      showSwitch: true,
    }"
  >
    <div class="form">
      <a-form ref="memberFormRef" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('project.basicInfo.projectName')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.basicInfo.projectNameTip') }]"
        >
          <a-input v-model="form.name" allow-clear />
        </a-form-item>
        <a-form-item field="userRoleIds" :label="t('project.basicInfo.organization')" asterisk-position="end">
          <a-select
            v-model="form.userRoleIds"
            multiple
            allow-clear
            :placeholder="t('project.basicInfo.selectOrganization')"
          >
            <a-option v-for="item of userGroupOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="description" :label="t('project.basicInfo.Description')" asterisk-position="end">
          <a-textarea v-model="form.description" allow-clear auto-size />
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const initForm = {
    name: '',
    userRoleIds: [],
    description: '',
  };
  const form = ref({ ...initForm });

  const updateVisible = ref<boolean>(false);

  const isEnable = ref<boolean>(false);

  const confirmHandler = (enable: boolean | undefined) => {
    console.log(enable);
  };

  const closeHandler = () => {
    updateVisible.value = false;
  };

  const userGroupOptions = ref([
    {
      name: '',
      id: '',
    },
  ]);
</script>

<style scoped></style>
