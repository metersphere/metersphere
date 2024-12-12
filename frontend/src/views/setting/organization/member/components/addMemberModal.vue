<template>
  <a-modal
    v-model:visible="dialogVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('organization.member.Confirm')"
    :cancel-text="t('organization.member.Cancel')"
    unmount-on-close
    @close="handleCancel"
  >
    <template #title>
      {{
        type === 'add'
          ? t('organization.member.addMember')
          : t('organization.member.updateMember', { name: memberName })
      }}
    </template>
    <div class="form">
      <a-form ref="memberFormRef" :model="form" layout="vertical">
        <!-- 编辑项目 -->
        <a-form-item v-if="type === 'edit'" :label="t('organization.member.project')" asterisk-position="end">
          <MsUserSelector
            v-model:modelValue="form.projectIds"
            :load-option-params="{ organizationId: lastOrganizationId }"
            :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_PROJECT"
            placeholder="organization.member.selectProjectScope"
          />
        </a-form-item>
        <!-- 添加成员 -->
        <a-form-item
          v-else
          field="memberIds"
          :label="t('organization.member.member')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('organization.member.selectMemberEmptyTip') }]"
        >
          <MsUserSelector
            v-model:modelValue="form.memberIds"
            :load-option-params="{ organizationId: lastOrganizationId }"
            :type="UserRequestTypeEnum.SYSTEM_ORGANIZATION_MEMBER"
            placeholder="organization.member.selectMemberScope"
          />
        </a-form-item>
        <a-form-item
          field="userRoleIds"
          :label="t('organization.member.tableColunmUsergroup')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('organization.member.selectUserEmptyTip') }]"
        >
          <MsSelect
            v-model:model-value="form.userRoleIds"
            :options="props.userGroupOptions"
            :allow-search="true"
            allow-clear
            :search-keys="['name']"
            value-key="id"
            label-key="name"
            class="w-full"
            :multiple="true"
            :placeholder="t('organization.member.selectUserScope')"
          />
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
  import { ref, watch, watchEffect } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsSelect from '@/components/business/ms-select';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { addOrUpdate } from '@/api/modules/setting/member';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { LinkList, MemberItem } from '@/models/setting/member';

  const { t } = useI18n();
  const appStore = useAppStore();
  const lastOrganizationId = computed(() => appStore.currentOrgId);
  const props = defineProps<{
    visible: boolean;
    userGroupOptions: LinkList;
  }>();
  const dialogVisible = ref<boolean>(false);
  const title = ref<string>('');
  const type = ref<string>('');
  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'success'): void;
  }>();

  const confirmLoading = ref<boolean>(false);
  const memberFormRef = ref<FormInstance | null>(null);
  export interface InitFromType {
    organizationId?: string;
    userRoleIds: string[];
    memberIds: (string | number)[];
    projectIds: (string | number)[];
  }

  const initFormValue: InitFromType = {
    organizationId: lastOrganizationId.value,
    userRoleIds: ['org_member'],
    memberIds: [],
    projectIds: [],
  };
  const form = ref({ ...initFormValue });
  const handleCancel = () => {
    memberFormRef.value?.resetFields();
    form.value = { ...initFormValue };
    dialogVisible.value = false;
  };

  const memberName = ref<string>('');

  const edit = (record: MemberItem) => {
    const { userRoleIdNameMap, projectIdNameMap } = record;
    form.value.memberIds = [record.id as string];
    form.value.userRoleIds = (userRoleIdNameMap || []).map((item) => item.id);
    form.value.projectIds = (projectIdNameMap || []).map((item) => item.id);
    memberName.value = record.name;
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

  watchEffect(() => {
    dialogVisible.value = props.visible;
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
