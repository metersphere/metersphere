<template>
  <ms-drawer
    :mask="false"
    :width="800"
    :visible="currentVisible"
    unmount-on-close
    :footer="false"
    class="ms-drawer-no-mask"
    :title="t('system.memberList')"
    @cancel="handleCancel"
  >
    <template #headerLeft>
      <div class="ml-[8px] flex flex-1 overflow-hidden text-[var(--color-text-4)]">
        (
        <a-tooltip :content="props.currentName">
          <div class="one-line-text">
            {{ props.currentName }}
          </div>
        </a-tooltip>
        )
      </div>
    </template>
    <div>
      <div class="flex flex-row justify-between">
        <a-button
          v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER']"
          class="mr-3"
          type="outline"
          @click="handleAddMember"
        >
          {{ t('system.organization.addMember') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('system.organization.searchUserPlaceholder')"
          class="w-[230px]"
          allow-clear
          @search="fetchData"
          @press-enter="fetchData"
          @clear="fetchData"
        ></a-input-search>
      </div>
      <ms-base-table class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
        <template #name="{ record }">
          <span>{{ record.name }}</span>
          <span v-if="record.adminFlag" class="ml-[4px] text-[var(--color-text-4)]">{{
            `(${t('common.admin')})`
          }}</span>
        </template>
        <template #userGroup="{ record }">
          <MsTagGroup
            v-if="!record.selectUserGroupVisible"
            :tag-list="record.userRoleList"
            type="primary"
            theme="outline"
            allow-edit
            show-table
            @click="handleTagClick(record)"
          />
          <MsSelect
            v-else
            v-model:model-value="record.userRoleList"
            :placeholder="t('system.user.createUserUserGroupPlaceholder')"
            :search-keys="['name']"
            :loading="record.selectUserGroupLoading"
            :disabled="record.selectUserGroupLoading"
            :fallback-option="(val) => ({
              label: (val as Record<string, any>).name,
              value: val,
            })"
            value-key="id"
            label-key="name"
            class="w-full max-w-[300px]"
            allow-clear
            v-bind="{
              options: userGroupOptions,
              multiple: true,
            }"
            :at-least-one="true"
            :object-value="true"
            @popup-visible-change="(value) => handleUserGroupChange(value, record)"
          >
          </MsSelect>
        </template>
        <template #operation="{ record }">
          <MsRemoveButton
            v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE_MEMBER']"
            :title="t('system.organization.removeName', { name: characterLimit(record.name) })"
            :sub-title-tip="props.organizationId ? t('system.organization.removeTip') : t('system.project.removeTip')"
            @ok="handleRemove(record)"
          />
        </template>
      </ms-base-table>
    </div>
  </ms-drawer>
  <AddUserModal
    v-model:visible="userVisible"
    :user-group-options="userGroupOptions"
    :project-id="props.projectId"
    :organization-id="props.organizationId"
    @submit="handleAddMemberSubmit"
  />
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import MsSelect from '@/components/business/ms-select';
  import AddUserModal from './addUserModal.vue';

  import { getProjectUserGroup, updateProjectMember } from '@/api/modules/project-management/projectMember';
  import { getGlobalUserGroup, updateSystemOrganizationMember } from '@/api/modules/setting/member';
  import {
    deleteUserFromOrgOrProject,
    postUserTableByOrgIdOrProjectId,
  } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { LinkList } from '@/models/setting/member';
  import type { UserListItem } from '@/models/setting/user';

  export interface projectDrawerProps {
    visible: boolean;
    organizationId?: string;
    projectId?: string;
    currentName: string;
  }
  const { t } = useI18n();
  const props = defineProps<projectDrawerProps>();
  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'requestFetchData'): void;
  }>();

  const currentVisible = ref(props.visible);

  const userVisible = ref(false);

  const keyword = ref('');

  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER',
      'SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE',
      'SYSTEM_ORGANIZATION_PROJECT:READ+DELETE',
    ])
  );

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.userName',
      slotName: 'name',
      dataIndex: 'nameTooltip',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.user.tableColumnUserGroup',
      dataIndex: 'userRoleList',
      slotName: 'userGroup',
      allowEditTag: true,
      isTag: true,
      width: 300,
    },
    {
      title: 'system.organization.email',
      dataIndex: 'email',
      width: 180,
      showTooltip: true,
    },
    {
      title: 'system.organization.phone',
      dataIndex: 'phone',
    },
    { title: hasOperationPermission.value ? 'system.organization.operation' : '', slotName: 'operation', width: 60 },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    postUserTableByOrgIdOrProjectId,
    {
      columns: projectColumn,
      scroll: { x: '100%' },
      heightUsed: 240,
      selectable: false,
      noDisable: false,
      pageSimple: true,
    },
    (record: any) => ({
      ...record,
      nameTooltip: record.name + (record.adminFlag ? `(${t('common.admin')})` : ''),
      selectUserGroupVisible: false,
      selectUserGroupLoading: false,
    })
  );

  const handleCancel = () => {
    keyword.value = '';
    emit('cancel');
  };

  const fetchData = async () => {
    if (props.organizationId) {
      setLoadListParams({ organizationId: props.organizationId, keyword: keyword.value });
    }
    if (props.projectId) {
      setLoadListParams({ projectId: props.projectId, keyword: keyword.value });
    }
    await loadList();
  };

  const userGroupOptions = ref<LinkList>([]);
  const getUserGroupOptions = async () => {
    try {
      if (props.organizationId) {
        userGroupOptions.value = await getGlobalUserGroup(props.organizationId);
      } else if (props.projectId) {
        userGroupOptions.value = await getProjectUserGroup(props.projectId);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };
  function handleTagClick(record: UserListItem & Record<string, any>) {
    if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE_MEMBER'])) {
      record.selectUserGroupVisible = true;
    }
  }
  async function handleUserGroupChange(val: boolean, record: UserListItem & Record<string, any>) {
    try {
      if (!val) {
        record.selectUserGroupLoading = true;
        if (props.organizationId) {
          await updateSystemOrganizationMember({
            organizationId: props.organizationId,
            memberId: record.id,
            userRoleIds: record.userRoleList.map((e) => e.id),
          });
        } else if (props.projectId) {
          await updateProjectMember({
            projectId: props.projectId,
            userId: record.id,
            roleIds: record.userRoleList.map((e) => e.id),
          });
        }
        Message.success(t('system.user.updateUserSuccess'));
        fetchData();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      record.selectUserGroupLoading = false;
    }
  }

  const handleAddMember = () => {
    userVisible.value = true;
  };
  const handleAddMemberSubmit = () => {
    fetchData();
    emit('requestFetchData');
  };

  const handleRemove = async (record: TableData) => {
    try {
      if (props.organizationId) {
        await deleteUserFromOrgOrProject(props.organizationId, record.id);
      }
      if (props.projectId) {
        await deleteUserFromOrgOrProject(props.projectId, record.id, false);
      }
      Message.success(t('common.removeSuccess'));
      fetchData();
      emit('requestFetchData');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  watch([() => props.projectId, () => props.organizationId, () => props.visible], () => {
    if (props.visible) {
      fetchData();
      getUserGroupOptions();
    }
  });
  watch(
    () => props.visible,
    (visible) => {
      currentVisible.value = visible;
    }
  );
</script>
