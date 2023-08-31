<template>
  <a-input-search
    allow-clear
    class="w-[252px]"
    :placeholder="t('system.userGroup.searchHolder')"
    @press-enter="enterData"
    @search="searchData"
  />
  <div class="mt-2 flex flex-col">
    <div class="flex h-[38px] items-center justify-between px-[8px] leading-[24px]">
      <div class="text-[var(--color-text-input-border)]"> {{ t('system.userGroup.global') }}</div>
      <div class="cursor-pointer text-[rgb(var(--primary-5))]"
        ><icon-plus-circle-fill style="font-size: 20px" @click="addUserGroup"
      /></div>
    </div>
    <div>
      <div
        v-for="element in userGroupList"
        :key="element.id"
        class="flex h-[38px] cursor-pointer items-center"
        :class="{ 'bg-[rgb(var(--primary-1))]': element.id === currentId }"
        @click="handleListItemClick(element)"
      >
        <popconfirm
          :visible="popVisible[element.id]"
          :loading="popLoading[element.id]"
          :type="popType"
          :default-name="popDefaultName"
          :list="userGroupList"
          @cancel="() => handlePopConfirmCancel(element.id)"
          @submit="(value: CustomMoreActionItem) => handlePopConfirmSubmit(value,element.id)"
        >
          <div class="flex grow flex-row justify-between px-[8px]">
            <a-tooltip :content="element.name">
              <div class="flex flex-row flex-nowrap">
                <div class="one-line-text max-w-[156px] text-[var(--color-text-1)]">{{ element.name }}</div>
                <div v-if="element.type" class="text-[var(--color-text-4)]"
                  >（{{ t(`system.userGroup.${element.type}`) }}）</div
                >
              </div>
            </a-tooltip>
            <div v-if="element.id === currentId && !element.internal">
              <MsTableMoreAction :list="customAction" @select="(value) => handleMoreAction(value, element.id)" />
            </div>
          </div>
        </popconfirm>
      </div>
    </div>
  </div>
  <AddUserModal :visible="addUserVisible" @cancel="addUserVisible = false" />
  <AddUserGroupModal :list="userGroupList" :visible="addUserGroupVisible" @cancel="handleAddUserGroupModalCancel" />
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { CustomMoreActionItem, PopVisibleItem, RenameType, UserGroupItem } from '@/models/setting/usergroup';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddUserModal from './addUserModal.vue';
  import AddUserGroupModal from './addUserGroupModal.vue';
  import useModal from '@/hooks/useModal';
  import { Message } from '@arco-design/web-vue';
  import popconfirm from './popconfirm.vue';
  import useUserGroupStore from '@/store/modules/setting/system/usergroup';
  import { getUserGroupList, updateOrAddUserGroup, deleteUserGroup } from '@/api/modules/setting/usergroup';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();

  const store = useUserGroupStore();
  const { openModal } = useModal();
  // 请求loading
  const currentId = ref('');
  const addUserVisible = ref(false);
  const addUserGroupVisible = ref(false);
  // 修改用户组名字，权限范围
  const popVisible = ref<PopVisibleItem>({});
  const popLoading = ref<PopVisibleItem>({});
  // 用户组和权限范围的状态
  const popType = ref<RenameType>('rename');
  const popDefaultName = ref('');
  // 用户列表
  const userGroupList = ref<UserGroupItem[]>([]);

  const customAction: ActionsItem[] = [
    {
      label: 'system.userGroup.rename',
      danger: false,
      eventTag: 'rename',
    },
    {
      isDivider: true,
    },
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  // 点击用户组列表
  const handleListItemClick = (element: UserGroupItem) => {
    const { id, name, type, internal } = element;
    currentId.value = id;
    store.setInfo({
      currentName: name,
      currentTitle: type,
      currentId: id,
      currentType: type,
      currentInternal: internal,
    });
  };

  // 用户组数据初始化
  const initData = async () => {
    try {
      const res = await getUserGroupList();
      if (res.length > 0) {
        userGroupList.value = res;
        handleListItemClick(res[0]);
        // 弹窗赋值
        const tmpObj: PopVisibleItem = {};
        res.forEach((element) => {
          tmpObj[element.id] = false;
        });
        popVisible.value = { ...tmpObj };
        popLoading.value = { ...tmpObj };
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };
  // 新增用户组
  const addUserGroup = () => {
    // eslint-disable-next-line no-console
    addUserGroupVisible.value = true;
  };
  // 点击更多操作
  const handleMoreAction = (item: ActionsItem, id: string) => {
    if (item.eventTag !== 'delete') {
      popType.value = item.eventTag as RenameType;
      const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
      popVisible.value = { ...popVisible.value, [id]: true };
      if (item.eventTag === 'rename') {
        popDefaultName.value = tmpObj.name;
      } else {
        popDefaultName.value = tmpObj.scopeId;
      }
    } else {
      openModal({
        type: 'error',
        title: t('system.userGroup.isDeleteUserGroup', { name: characterLimit(store.currentName) }),
        content: t('system.userGroup.beforeDeleteUserGroup'),
        okText: t('system.userGroup.confirmDelete'),
        cancelText: t('system.userGroup.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            await deleteUserGroup(id);
            Message.success(t('system.user.deleteUserSuccess'));
            initData();
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    }
  };

  // 关闭confirm 弹窗
  const handlePopConfirmCancel = (id: string) => {
    popVisible.value = { ...popVisible.value, [id]: false };
  };
  // 修改用户组名字，权限范围
  const handlePopConfirmSubmit = async (item: CustomMoreActionItem, id: string) => {
    if (item.eventKey === 'rename') {
      // 修改用户组名字
      try {
        popLoading.value = { ...popLoading.value, [id]: true };
        const res = await updateOrAddUserGroup({ id, name: item.name });
        if (res) {
          initData();
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        popLoading.value = { ...popLoading.value, [id]: false };
      }
    } else {
      // 修改权限范围
      try {
        const res = await updateOrAddUserGroup({ id, scopeId: item.name });
        if (res) {
          initData();
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      }
    }
    popVisible.value = { ...popVisible.value, [id]: false };
    initData();
  };

  function enterData(eve: Event) {
    if (!(eve.target as HTMLInputElement).value) {
      initData();
      return;
    }
    const keyword = (eve.target as HTMLInputElement).value;
    const tmpArr = userGroupList.value.filter((ele) => ele.name.includes(keyword));
    userGroupList.value = tmpArr;
  }
  function searchData(value: string) {
    if (!value) {
      initData();
      return;
    }
    const keyword = value;
    const tmpArr = userGroupList.value.filter((ele) => ele.name.includes(keyword));
    userGroupList.value = tmpArr;
  }

  const handleAddUserGroupModalCancel = (shouldSearch: boolean) => {
    addUserGroupVisible.value = false;
    if (shouldSearch) {
      initData();
    }
  };

  onMounted(() => {
    initData();
  });
</script>
