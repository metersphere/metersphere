<template>
  <a-input-search
    class="w-[252px]"
    :placeholder="t('system.userGroup.searchHolder')"
    allow-clear
    @press-enter="enterData"
    @search="searchData"
  />
  <div class="mt-2 flex flex-col">
    <div class="flex h-[38px] items-center px-[8px] leading-[24px]">
      <div class="text-[var(--color-text-input-border)]"> {{ t('system.userGroup.global') }}</div>
    </div>
    <div>
      <div
        v-for="element in globalUserGroupList"
        :key="element.id"
        class="flex h-[38px] cursor-pointer items-center px-[8px]"
        :class="{
          'bg-[rgb(var(--primary-1))]': element.id === currentId,
        }"
        @click="handleListItemClick(element)"
      >
        <div class="flex flex-row flex-nowrap">
          <div class="one-line-text max-w-[156px] text-[var(--color-text-1)]">{{ element.name }}</div>
          <div v-if="element.type" class="text-[var(--color-text-4)]"
            >（{{ t(`system.userGroup.${element.type}`) }}）</div
          >
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
      @search="initData"
    >
      <div class="flex h-[38px] items-center justify-between px-[8px] leading-[24px]">
        <div class="text-[var(--color-text-input-border)]"> {{ t('system.userGroup.custom') }}</div>
        <div class="cursor-pointer text-[rgb(var(--primary-5))]"
          ><icon-plus-circle-fill style="font-size: 20px" @click="addUserGroup"
        /></div>
      </div>
    </AddOrUpdateUserGroupPopup>
    <div>
      <div
        v-for="element in customUserGroupList"
        :key="element.id"
        class="flex h-[38px] cursor-pointer items-center"
        :class="{ 'bg-[rgb(var(--primary-1))]': element.id === currentId }"
        @click="handleListItemClick(element)"
      >
        <AddOrUpdateUserGroupPopup
          :id="element.id"
          :visible="popVisible[element.id]"
          :default-name="popDefaultName"
          :list="customUserGroupList"
          @cancel="() => handlePopConfirmCancel(element.id)"
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
        </AddOrUpdateUserGroupPopup>
      </div>
    </div>
  </div>
  <AddUserModal :visible="addUserVisible" @cancel="addUserVisible = false" />
</template>

<script lang="ts" setup>
  import { ref, onMounted, computed } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { PopVisibleItem, RenameType, UserGroupItem } from '@/models/setting/usergroup';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddUserModal from './addUserModal.vue';
  import AddOrUpdateUserGroupPopup from './addOrUpdateUserGroupPopup.vue';
  import useModal from '@/hooks/useModal';
  import { Message } from '@arco-design/web-vue';
  import useUserGroupStore from '@/store/modules/setting/organization/usergroup';
  import { useAppStore } from '@/store';
  import { getOrgUserGroupList, deleteOrgUserGroup } from '@/api/modules/setting/usergroup';
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
