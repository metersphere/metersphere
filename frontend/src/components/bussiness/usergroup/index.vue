<template>
  <div class="user-group-left">
    <a-input-search
      v-model="searchKey"
      class="w-[252px]"
      :placeholder="t('system.userGroup.searchHolder')"
      @press-enter="searchData"
    />
    <div class="mt-2 flex flex-col">
      <div class="flex h-[38px] items-center justify-between px-[8px] leading-[24px]">
        <div class="second-color"> {{ t('system.userGroup.global') }}</div>
        <div class="primary-color"><icon-plus-circle-fill style="font-size: 20px" @click="addUserGroup" /></div>
      </div>
      <div>
        <div
          v-for="element in userGroupList"
          :key="element.id"
          :class="{
            'flex': true,
            'h-[38px]': true,
            'items-center': true,
            'px-[8px]': true,
            'is-active': element.id === currentId,
          }"
          @click="handleListItemClick(element)"
        >
          <popconfirm
            :visible="popVisible[element.id]"
            :type="popType"
            :default-name="popDefaultName"
            :list="userGroupList"
            position="bl"
            @cancel="() => handlePopConfirmCancel(element.id)"
            @submit="(value: CustomMoreActionItem) => handlePopConfirmSubmit(value,element.id)"
          >
            <div class="draglist-item flex grow flex-row justify-between">
              <div class="usergroup-title leading-[24px]">
                <span class="n1">{{ element.name }}</span>
                <span v-if="element.type" class="n4">（{{ t(`system.userGroup.${element.type}`) }}）</span>
              </div>
              <div v-if="element.id === currentId">
                <MsTableMoreAction :list="customAction" @select="(value) => handleMoreAction(value, element.id)" />
              </div>
            </div>
          </popconfirm>
        </div>
      </div>
    </div>
  </div>
  <AddUserModal :visible="addUserVisible" @cancel="addUserVisible = false" />
  <AddUserGroupModal :visible="addUserGroupVisible" @cancel="addUserGroupVisible = false" />
  <DeleteUserGroupModal :visible="deleteUserGroupVisible" @cancel="deleteUserGroupVisible = false" />
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { CustomMoreActionItem, PopVisibleItem, RenameType, UserGroupItem } from './type';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddUserModal from './addUserModal.vue';
  import AddUserGroupModal from './addUserGroupModal.vue';
  import DeleteUserGroupModal from './deleteUserGroupModal.vue';
  import popconfirm from './popconfirm.vue';
  import useUserGroupStore from '@/store/modules/system/usergroup';
  import { getUserGroupList, updateOrAddUserGroup } from '@/api/modules/system/usergroup';

  const { t } = useI18n();

  const searchKey = ref('');
  const store = useUserGroupStore();
  // 请求loading
  const currentId = ref('');
  const addUserVisible = ref(false);
  const addUserGroupVisible = ref(false);
  const deleteUserGroupVisible = ref(false);
  // 修改用户组名字，权限范围
  const popVisible = ref<PopVisibleItem>({});
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
      label: 'system.userGroup.changeAuthScope',
      danger: false,
      eventTag: 'changeAuthScope',
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
      // 删除用户组
      deleteUserGroupVisible.value = true;
    }
  };

  // 点击用户组列表
  const handleListItemClick = (element: UserGroupItem) => {
    const { id, name, type } = element;
    currentId.value = id;
    store.setInfo({ currentName: name, currentTitle: type });
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
        popVisible.value = tmpObj;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };
  // 关闭confirm 弹窗
  const handlePopConfirmCancel = (id: string) => {
    popVisible.value = { ...popVisible.value, [id]: false };
  };
  // 修改用户组名字，权限范围
  const handlePopConfirmSubmit = async (item: CustomMoreActionItem, id: string) => {
    popVisible.value = { ...popVisible.value, [id]: false };
    if (item.eventKey === 'rename') {
      // 修改用户组名字
      try {
        const res = await updateOrAddUserGroup({ id, name: item.name });
        if (res) {
          initData();
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
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
    initData();
  };

  function searchData(eve: Event) {
    if (!(eve.target as HTMLInputElement).value) {
      initData();
      return;
    }
    const keyword = (eve.target as HTMLInputElement).value;
    const tmpArr = userGroupList.value.filter((ele) => ele.name.includes(keyword));
    userGroupList.value = tmpArr;
  }
  onMounted(() => {
    initData();
  });
</script>

<style scoped lang="less">
  .primary-color {
    color: rgb(var(--primary-5));
  }
  .n1 {
    color: var(--color-text-1);
  }
  .n4 {
    color: var(--color-text-4);
  }
  .second-color {
    color: var(--color-text-input-border);
  }
  .handle {
    cursor: move;
    opacity: 0.3;
  }
  .is-active {
    background-color: rgb(var(--primary-1));
  }
  .custom-empty {
    padding: 8px;
    font-size: 12px;
    font-family: 'PingFang SC';
    font-weight: 400;
    border-radius: 4px;
    color: #8f959e;
    background: #f7f9fc;
    font-style: normal;
    line-height: 20px;
    overflow-wrap: break-word;
  }
  .button-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 24px;
    height: 24px;
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-9));
  }
  .usergroup-title {
    color: var(--color-text-1);
  }
</style>
