<template>
  <a-modal
    v-model:visible="dialogVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('organization.member.Save')"
    @ok="handleOK"
    @cancel="handleCancel"
  >
    <template #title> {{ t('organization.member.addMember') }} </template>
    <div class="form">
      <a-form :model="form" size="large" layout="vertical">
        <a-form-item
          v-model="form.members"
          field="members"
          :label="t('organization.member.member')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('organization.member.pleaseSelectMember') }]"
        >
          <a-select
            v-model="form.members"
            multiple
            :placeholder="t('organization.member.selectMemberScope')"
            allow-clear
          >
            <a-option v-for="item of memberList" :key="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-model="form.userGroups"
          field="userGroups"
          :label="t('organization.member.tableColunmUsergroup')"
          asterisk-position="end"
          :rules="[{ required: true }]"
        >
          <a-select v-model="form.userGroups">
            <a-option v-for="item of userGroupOptions" :key="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { AddMemberForm } from '@/models/setting/member';
  import { useDialog } from '@/hooks/useDialog';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    title?: string;
  }>();
  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'close'): void;
  }>();
  const { dialogVisible } = useDialog(props, emits);
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

  const memberList = ref([
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

  const form = reactive<AddMemberForm>({
    userGroups: '',
    members: [],
  });
  const handleCancel = () => {
    emits('close');
  };

  const handleOK = () => {
    handleCancel();
  };
</script>

<style lang="less" scoped></style>
@/models/setting/member
