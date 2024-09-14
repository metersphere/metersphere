<template>
  <a-modal
    v-model:visible="currentVisible"
    :width="680"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :title="t('system.organization.addMember')"
    :mask-closable="false"
    unmount-on-close
    @cancel="handleCancel"
  >
    <div class="mb-[16px] flex justify-end">
      <a-input-search
        v-model="keyword"
        :placeholder="t('system.organization.searchByNameOrEmailPlaceholder')"
        allow-clear
        class="w-[240px]"
        @press-enter="initData"
        @search="initData"
        @clear="initData"
      />
    </div>
    <MsBaseTable
      v-bind="propsRes"
      :action-config="{
        baseAction: [],
        moreAction: [],
      }"
      v-on="propsEvent"
    ></MsBaseTable>
    <template #footer>
      <div class="flex justify-between">
        <div class="flex items-center gap-[8px]">
          <div class="text-nowrap">{{ t('project.member.tableColumnUserGroup') }}</div>
          <MsSelect
            v-model:model-value="userGroupIds"
            mode="static"
            allow-clear
            class="!w-[240px] text-start"
            :search-keys="['name']"
            value-key="id"
            label-key="name"
            :placeholder="t('project.member.selectUserScope')"
            v-bind="{
              options: currentUserGroupOptions,
              multiple: true,
            }"
          />
        </div>
        <div class="flex gap-[12px]">
          <a-button type="secondary" :loading="loading" @click="handleCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button
            type="primary"
            :loading="loading"
            :disabled="!userGroupIds.length || !tableSelected.length"
            @click="handleAddMember"
          >
            {{ t('common.add') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsSelect from '@/components/business/ms-select';

  import { getProjectUserGroup } from '@/api/modules/project-management/projectMember';
  import { getGlobalUserGroup, getOrganizationMemberListPage } from '@/api/modules/setting/member';
  import {
    addProjectMemberByOrg,
    addUserToOrgOrProject,
    getSystemMemberListPage,
  } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';

  import type { LinkList } from '@/models/setting/member';

  const props = defineProps<{
    isOrganization?: boolean; // 组织下的
    userGroupOptions?: LinkList;
    organizationId?: string;
    projectId?: string;
  }>();
  const emit = defineEmits<{
    (e: 'submit'): void;
  }>();
  const currentVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const { t } = useI18n();

  const columns = [
    {
      title: 'common.name',
      dataIndex: 'name',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'system.organization.email',
      dataIndex: 'email',
      showTooltip: true,
      width: 250,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, resetPagination } = useTable(
    !props.isOrganization ? getSystemMemberListPage : getOrganizationMemberListPage,
    {
      columns,
      scroll: { x: '100%' },
      size: 'mini',
      selectable: true,
      showSelectAll: true,
      showSelectorAll: false,
      rowSelectionDisabledConfig: {
        disabledKey: 'memberFlag',
      },
      paginationSize: 'mini',
    }
  );

  const keyword = ref<string>('');
  const currentUserGroupOptions = ref<LinkList>([]);
  const userGroupIds = ref<string[]>([]);
  const tableSelected = computed(() => [...propsRes.value.selectedKeys]);

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      sourceId: props.projectId ?? props.organizationId,
      projectId: props.projectId,
      organizationId: props.organizationId,
    });
    loadList();
  }
  const getUserGroupOptions = async () => {
    try {
      if (props.organizationId && !props.isOrganization) {
        // 系统-组织与项目-组织-成员用户组下拉
        currentUserGroupOptions.value = await getGlobalUserGroup(props.organizationId);
      } else if (props.projectId) {
        // 系统-组织与项目-项目-成员用户组下拉 和 组织-项目-成员用户组下拉
        currentUserGroupOptions.value = await getProjectUserGroup(props.projectId);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };
  watch(
    () => currentVisible.value,
    (value) => {
      if (value) {
        initData();
        if (!props.userGroupOptions) {
          getUserGroupOptions();
        } else {
          currentUserGroupOptions.value = props.userGroupOptions;
        }
        if (props.projectId) {
          userGroupIds.value = ['project_member'];
        } else if (props.organizationId) {
          userGroupIds.value = ['org_member'];
        }
      }
    }
  );

  const handleCancel = () => {
    currentVisible.value = false;
    keyword.value = '';
    userGroupIds.value = [];
    resetPagination();
    resetSelector();
  };

  const loading = ref(false);
  const handleAddMember = async () => {
    try {
      loading.value = true;
      if (!props.isOrganization) {
        // 系统-组织与项目
        await addUserToOrgOrProject({
          userRoleIds: userGroupIds.value,
          userIds: tableSelected.value,
          projectId: props.projectId,
          organizationId: props.organizationId,
        });
      } else {
        // 组织-项目
        await addProjectMemberByOrg({
          userRoleIds: userGroupIds.value,
          userIds: tableSelected.value,
          projectId: props.projectId,
        });
      }
      Message.success(t('system.organization.addSuccess'));
      emit('submit');
      handleCancel();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  };
</script>

<style lang="less" scoped>
  :deep(.ms-pagination) {
    gap: 8px;
    .ms-pagination-item-previous {
      margin-left: 0;
    }
  }
</style>
