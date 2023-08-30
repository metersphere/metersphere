<template>
  <MsDialog
    v-model:visible="visible"
    dialog-size="medium"
    title="project.member.addMember"
    :close="closeHandler"
    :confirm="confirmFunHandler"
    ok-text="project.member.add"
  >
    <div class="form">
      <a-form ref="memberFormRef" :model="form" size="large" layout="vertical">
        <a-form-item
          field="memberIds"
          :label="t('project.member.member')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.member.selectMemberEmptyTip') }]"
        >
          <a-select v-model="form.memberIds" multiple :placeholder="t('project.member.selectMemberScope')" allow-clear>
            <a-option v-for="item of memberList" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          field="userRoleIds"
          :label="t('project.member.tableColumnUserGroup')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.member.selectUserEmptyTip') }]"
        >
          <a-select v-model="form.userRoleIds" multiple allow-clear :placeholder="t('project.member.selectUserScope')">
            <a-option v-for="item of userGroupOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import MsDialog from '@/components/pure/ms-dialog/index.vue';
  import { useI18n } from '@/hooks/useI18n';
  import { FormInstance, Message } from '@arco-design/web-vue';

  const { t } = useI18n();

  const visible = ref<boolean>(false);
  const initFormValue = {
    userRoleIds: [],
    memberIds: [],
  };
  const form = ref({ ...initFormValue });

  const memberList = ref([
    {
      id: '',
      name: '全部',
    },
  ]);
  const userGroupOptions = ref([
    {
      id: '',
      name: '全部',
    },
  ]);

  const memberFormRef = ref<FormInstance | null>(null);

  const confirmFunHandler = async () => {
    await memberFormRef.value?.validate().then(async (error) => {
      if (!error) {
        console.log(error);
      } else {
        return false;
      }
    });
  };

  const closeHandler = () => {
    memberFormRef.value?.resetFields();
    visible.value = false;
  };
</script>

<style scoped></style>
