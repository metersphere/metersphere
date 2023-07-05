<template>
  <a-modal
    v-model:visible="memberVisible"
    title-align="start"
    width="680px"
    :ok-text="t('organization.member.Save')"
    @ok="handleOK"
    @cancel="handleCancel"
  >
    <template #title> {{ t('organization.member.addMember') }} </template>
    <div class="form">
      <a-form :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
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
  import { ref, watchEffect, reactive } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { AddMemberForm } from '@/models/system/member';

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();
  const props = defineProps<{
    visible: boolean;
  }>();
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

  const memberVisible = ref<boolean>(props.visible);
  const form = reactive<AddMemberForm>({
    userGroups: '',
    members: [],
  });
  watchEffect(() => {
    memberVisible.value = props.visible;
  });

  const handleOK = () => {
    // eslint-disable-next-line no-use-before-define
    handleCancel();
  };

  const handleCancel = () => {
    emit('cancel');
  };
</script>

<style lang="less" scoped></style>
