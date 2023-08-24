<template>
  <div class="user-group-left">
    <a-input-search
      class="w-[252px]"
      :placeholder="t('system.userGroup.searchHolder')"
      @press-enter="enterData"
      @search="searchData"
    />
    <div class="mt-2 flex flex-col">
      <div class="flex h-[38px] items-center px-[8px] leading-[24px]">
        <div class="second-color"> {{ t('system.userGroup.global') }}</div>
      </div>
      <div>
        <div
          v-for="element in globalUserGroupList"
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
          <div class="flex grow flex-row">
            <div class="usergroup-title leading-[24px]">
              <span class="n1">{{ element.name }}</span>
              <span v-if="element.type" class="n4">（{{ t(`system.userGroup.${element.type}`) }}）</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <a-divider class="mt-2" />
    <div class="mt-2 flex flex-col">
      <AddOrUpdateUserGroupPopup
        :visible="addUserGroupVisible"
        :list="customUserGroupList"
        @cancel="handleAddUserGroupCancel"
      >
        <div class="flex h-[38px] items-center justify-between px-[8px] leading-[24px]">
          <div class="second-color"> {{ t('system.userGroup.custom') }}</div>
          <div class="primary-color"><icon-plus-circle-fill style="font-size: 20px" @click="addUserGroup" /></div>
        </div>
      </AddOrUpdateUserGroupPopup>
      <div>
        <div
          v-for="element in customUserGroupList"
          :key="element.id"
          class="flex h-[38px] items-center px-[8px]"
          :class="{ 'is-active': element.id === currentId }"
          @click="handleListItemClick(element)"
        >
          <AddOrUpdateUserGroupPopup
            :id="element.id"
            :visible="popVisible[element.id]"
            :default-name="popDefaultName"
            :list="customUserGroupList"
            @cancel="() => handlePopConfirmCancel(element.id)"
          >
            <div class="draglist-item flex grow flex-row justify-between">
              <div class="usergroup-title leading-[24px]">
                <span class="n1">{{ element.name }}</span>
                <span v-if="element.type" class="n4">（{{ t(`system.userGroup.${element.type}`) }}）</span>
              </div>
              <div v-if="element.id === currentId && !element.internal">
                <MsTableMoreAction :list="customAction" @select="(value) => handleMoreAction(value, element.id)" />
              </div>
            </div>
          </AddOrUpdateUserGroupPopup>
        </div>
      </div>
    </div>
  </div>
  <AddUserModal :visible="addUserVisible" @cancel="addUserVisible = false" />
</template>

<script lang="ts" setup>
  import { ref, onMounted, computed } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { CustomMoreActionItem, PopVisibleItem, RenameType, UserGroupItem } from '@/models/setting/usergroup';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddUserModal from './addUserModal.vue';
  import AddOrUpdateUserGroupPopup from './addOrUpdateUserGroupPopup.vue';
  import useModal from '@/hooks/useModal';
  import { Message } from '@arco-design/web-vue';
  import useUserGroupStore from '@/store/modules/setting/organization/usergroup';
  import { useAppStore } from '@/store';
  import { getOrgUserGroupList, updateOrAddOrgUserGroup, deleteOrgUserGroup } from '@/api/modules/setting/usergroup';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();

  const store = useUserGroupStore();
  const appStore = useAppStore();
  const { openModal } = useModal();
  // 请求loading
  const currentId = ref('');
  const addUserVisible = ref(false);
  const addUserGroupVisible = ref(false);
  // 修改用户组名字，权限范围
  const popVisible = ref<PopVisibleItem>({});
  // 用户组和权限范围的状态
  const popType = ref<RenameType>('rename');
  const popDefaultName = ref('');
  // 用户列表
  const userGroupList = ref<UserGroupItem[]>([]);
  const currentOrgId = computed(() => appStore.currentOrgId);

  const globalUserGroupList = computed(() => {
    return userGroupList.value.filter((ele) => ele.internal);
  });
  const customUserGroupList = computed(() => {
    return userGroupList.value.filter((ele) => !ele.internal);
  });

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
      const res = await getOrgUserGroupList(currentOrgId.value);
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
  // 新增用户组
  const addUserGroup = () => {
    addUserGroupVisible.value = true;
  };
  // 关闭创建用户组
  const handleAddUserGroupCancel = () => {
    addUserGroupVisible.value = false;
    initData();
  };
  // 点击更多操作
  const handleMoreAction = (item: ActionsItem, id: string) => {
    if (item.eventTag !== 'delete') {
      popType.value = item.eventTag as RenameType;
      const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
      popVisible.value = { ...popVisible.value, [id]: true };
      if (item.eventTag === 'rename') {
        popDefaultName.value = tmpObj.name;
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
            await deleteOrgUserGroup(id);
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
