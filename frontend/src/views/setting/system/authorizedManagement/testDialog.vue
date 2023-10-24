<template>
  <MsDialog
    v-model:visible="visible"
    dialog-size="medium"
    :title="title"
    :disabled-ok="disabledOk"
    :close="closeHandler"
    :show-switch="true"
    :switch-props="{
      enable: true,
      switchName: '插件状态',
      switchTooltip: 'aaaaaaaaa',
      showSwitch: true,
    }"
    :confirm="confirmFunHandler"
  >
    <div class="form">
      <a-form ref="memberFormRef" :model="form" size="large" layout="vertical">
        <a-form-item
          field="projectIds"
          :label="t('organization.member.project')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('organization.member.selectMemberEmptyTip') }]"
        >
          <a-input v-model="form.projectIds" :placeholder="t('organization.member.selectProjectScope')" allow-clear>
          </a-input>
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';

  import { addOrUpdate } from '@/api/modules/setting/serviceIntegration';
  import { useI18n } from '@/hooks/useI18n';

  import { FormInstance, ValidatedError } from '@arco-design/web-vue/es/form';

  const { t } = useI18n();

  const title = ref('我是标题');
  const visible = ref(false);
  const disabledOk = ref<boolean>(false);
  const initFormValue = {
    organizationId: '',
    userRoleIds: [],
    memberIds: [],
    projectIds: '',
  };
  const form = ref({ ...initFormValue });
  const isEnable = ref<boolean>(false);
  const memberFormRef = ref<FormInstance | null>(null);
  const closeHandler = () => {
    memberFormRef.value?.resetFields();
    form.value = { ...initFormValue };
    visible.value = false;
  };

  const confirmFunHandler = async (enable: boolean | undefined) => {
    await memberFormRef.value?.validate().then(async (res) => {
      if (!res) {
        try {
          await addOrUpdate({ enable }, 'add');
          Message.success(t('organization.member.batchModalSuccess'));
        } catch (error) {
          console.log(error);
        }
      } else {
        return false;
      }
    });
  };

  defineExpose({
    isEnable,
  });
</script>

<style scoped></style>
