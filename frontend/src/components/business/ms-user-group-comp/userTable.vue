<template>
  <div class="px-[16px]">
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
      <template v-if="hasAnyPermission(props.updatePermission || [])" #quickCreate>
        <MsConfirmUserSelector :ok-loading="okLoading" v-bind="userSelectorProps" @confirm="handleAddMember" />
      </template>
      <template v-if="hasAnyPermission(props.updatePermission || [])" #action="{ record }">
        <MsRemoveButton
          :title="t('system.userGroup.removeName', { name: characterLimit(record.name) })"
          :sub-title-tip="t('system.userGroup.removeTip')"
          :disabled="systemType === AuthScopeEnum.SYSTEM && record.userId === 'admin'"
          @ok="handleRemove(record)"
        />
      </template>
    </MsBaseTable>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';

  import {
    addOrgUserToUserGroup,
    addUserToUserGroup,
    deleteOrgUserFromUserGroup,
    deleteUserFromUserGroup,
    postOrgUserByUserGroup,
    postUserByUserGroup,
  } from '@/api/modules/setting/usergroup';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { characterLimit, formatPhoneNumber } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { CurrentUserGroupItem, UserTableItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';

  import { MsConfirmUserSelector } from '../ms-user-selector';
  import { UserRequestTypeEnum } from '../ms-user-selector/utils';

  const systemType = inject<AuthScopeEnum>('systemType');
  const { t } = useI18n();
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const okLoading = ref(false);
  const props = defineProps<{
    keyword: string;
    current: CurrentUserGroupItem;
    deletePermission?: string[];
    readPermission?: string[];
    updatePermission?: string[];
  }>();

  const userSelectorProps = computed(() => {
    if (systemType === AuthScopeEnum.SYSTEM) {
      return {
        type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
        loadOptionParams: {
          roleId: props.current.id,
        },
        disabledKey: 'exclude',
      };
    }
    return {
      type: UserRequestTypeEnum.ORGANIZATION_USER_GROUP,
      loadOptionParams: {
        roleId: props.current.id,
        organizationId: currentOrgId.value,
      },
      disabledKey: 'checkRoleFlag',
    };
  });

  const userGroupUserColumns: MsTableColumn = [
    {
      title: 'system.userGroup.name',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'system.userGroup.email',
      dataIndex: 'email',
      showTooltip: true,
    },
    {
      title: 'system.userGroup.phone',
      dataIndex: 'phone',
      showTooltip: true,
    },
    {
      title: 'system.userGroup.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 100,
    },
  ];

  const getRequestBySystemType = () => {
    if (systemType === AuthScopeEnum.SYSTEM) {
      return postUserByUserGroup;
    }

    return postOrgUserByUserGroup;
  };

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(
    getRequestBySystemType(),
    {
      columns: userGroupUserColumns,
      scroll: { x: '100%', minWidth: 700, y: '100%' },
      selectable: false,
      noDisable: true,
      showSetting: false,
      heightUsed: 288,
    },
    (record) => {
      return {
        ...record,
        phone: formatPhoneNumber(record.phone || ''),
      };
    }
  );

  const handlePermission = (permission: string[], cb: () => void) => {
    if (!hasAnyPermission(permission)) {
      return false;
    }
    cb();
  };
  const fetchData = async () => {
    handlePermission(props.readPermission || [], async () => {
      setKeyword(props.keyword);
      await loadList();
    });
  };
  const handleRemove = async (record: UserTableItem) => {
    handlePermission(props.updatePermission || [], async () => {
      try {
        if (systemType === AuthScopeEnum.SYSTEM) {
          await deleteUserFromUserGroup(record.id);
        } else if (systemType === AuthScopeEnum.ORGANIZATION) {
          await deleteOrgUserFromUserGroup({
            organizationId: currentOrgId.value,
            userRoleId: props.current.id,
            userIds: [record.id],
          });
        }
        await fetchData();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    });
  };

  /**
   * 添加成员
   * @param userIds 用户ids
   */
  const handleAddMember = async (userIds: string[], callback: (v: boolean) => void) => {
    try {
      okLoading.value = true;
      if (systemType === AuthScopeEnum.SYSTEM) {
        await addUserToUserGroup({ roleId: props.current.id, userIds });
      }
      if (systemType === AuthScopeEnum.ORGANIZATION) {
        await addOrgUserToUserGroup({
          userRoleId: props.current.id,
          userIds,
          organizationId: currentOrgId.value,
        });
      }
      Message.success(t('common.addSuccess'));
      await fetchData();
      callback(true);
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
      callback(false);
    } finally {
      okLoading.value = false;
    }
  };

  watchEffect(() => {
    if (props.current.id && currentOrgId.value) {
      if (systemType === AuthScopeEnum.SYSTEM) {
        setLoadListParams({ roleId: props.current.id });
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        setLoadListParams({ userRoleId: props.current.id, organizationId: currentOrgId.value });
      }
      // TODO 项目-用户组
      fetchData();
    }
  });
  defineExpose({
    fetchData,
  });
</script>
