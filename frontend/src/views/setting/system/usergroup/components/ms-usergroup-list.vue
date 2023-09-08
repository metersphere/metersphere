<template>
  <a-input-search
    class="w-[252px]"
    :placeholder="t('system.userGroup.searchHolder')"
    allow-clear
    @press-enter="enterData"
    @search="searchData"
  />
  <div class="mt-2">
    <CreateUserGroupPopup
      :list="systemUserGroupList"
      :visible="systemUserGroupVisible"
      auth-scope="SYSTEM"
      @cancel="systemUserGroupVisible = false"
      @submit="handleCreateUserGroup"
    >
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1 text-[var(--color-text-4)]">
          <MsIcon
            v-if="systemToggle"
            class="cursor-pointer"
            type="icon-icon_expand-down_filled"
            size="12"
            @click="systemToggle = false"
          />
          <MsIcon
            v-else
            class="cursor-pointer"
            type="icon-icon_expand-right_filled"
            size="12"
            @click="systemToggle = true"
          />
          <div class="text-[14px]">
            {{ t('system.userGroup.systemUserGroup') }}
          </div>
        </div>
        <MsMoreAction :list="createSystemUGActionItem" @select="systemUserGroupVisible = true">
          <icon-plus-circle-fill class="text-[rgb(var(--primary-7))]" size="20" />
        </MsMoreAction>
      </div>
    </CreateUserGroupPopup>
    <Transition>
      <div v-if="systemToggle">
        <div
          v-for="element in systemUserGroupList"
          :key="element.id"
          class="flex h-[38px] cursor-pointer items-center py-[7px] pl-[20px] pr-[4px]"
          :class="{ 'bg-[rgb(var(--primary-1))]': element.id === currentId }"
          @click="handleListItemClick(element)"
        >
          <CreateUserGroupPopup
            :list="systemUserGroupList"
            v-bind="popVisible[element.id]"
            @cancel="handleRenameCancel(element)"
            @submit="handleRenameCancel(element, element.id)"
          >
            <div class="flex grow flex-row items-center justify-between">
              <a-tooltip :content="element.name">
                <div
                  class="one-line-text max-w-[156px] text-[var(--color-text-1)]"
                  :class="{ 'text-[rgb(var(--primary-7))]': element.id === currentId }"
                  >{{ element.name }}</div
                >
              </a-tooltip>
              <div v-if="element.id === currentId && !element.internal" class="flex flex-row items-center gap-[8px]">
                <MsMoreAction :list="addMemberActionItem" @select="handleAddMember">
                  <div class="icon-button">
                    <MsIcon type="icon-icon_add_outlined" size="16" />
                  </div>
                </MsMoreAction>
                <MsMoreAction :list="moreAction" @select="(value) => handleMoreAction(value, element.id, 'SYSTEM')">
                  <div class="icon-button">
                    <MsIcon type="icon-icon_more_outlined" size="16" />
                  </div>
                </MsMoreAction>
              </div>
            </div>
          </CreateUserGroupPopup>
        </div>
        <a-divider class="my-[0px] mt-[6px]" />
      </div>
    </Transition>
  </div>
  <div class="mt-2">
    <CreateUserGroupPopup
      :list="orgUserGroupList"
      :visible="orgUserGroupVisible"
      auth-scope="ORGANIZATION"
      @cancel="orgUserGroupVisible = false"
      @submit="handleCreateUserGroup"
    >
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1 text-[var(--color-text-4)]">
          <MsIcon
            v-if="orgToggle"
            class="cursor-pointer"
            type="icon-icon_expand-down_filled"
            size="12"
            @click="orgToggle = false"
          />
          <MsIcon
            v-else
            class="cursor-pointer"
            type="icon-icon_expand-right_filled"
            size="12"
            @click="orgToggle = true"
          />
          <div class="text-[14px]">
            {{ t('system.userGroup.orgUserGroup') }}
          </div>
        </div>
        <MsMoreAction :list="createOrgUGActionItem" @select="orgUserGroupVisible = true">
          <icon-plus-circle-fill class="text-[rgb(var(--primary-7))]" size="20" />
        </MsMoreAction>
      </div>
    </CreateUserGroupPopup>
    <Transition>
      <div v-if="orgToggle">
        <div
          v-for="element in orgUserGroupList"
          :key="element.id"
          class="flex h-[38px] cursor-pointer items-center py-[7px] pl-[20px] pr-[4px]"
          :class="{ 'bg-[rgb(var(--primary-1))]': element.id === currentId }"
          @click="handleListItemClick(element)"
        >
          <CreateUserGroupPopup
            :list="orgUserGroupList"
            v-bind="popVisible[element.id]"
            @cancel="handleRenameCancel(element)"
            @submit="handleRenameCancel(element, element.id)"
          >
            <div class="flex grow flex-row items-center justify-between">
              <a-tooltip :content="element.name">
                <div
                  class="one-line-text max-w-[156px] text-[var(--color-text-1)]"
                  :class="{ 'text-[rgb(var(--primary-7))]': element.id === currentId }"
                  >{{ element.name }}</div
                >
              </a-tooltip>
              <div v-if="element.id === currentId && !element.internal" class="flex flex-row items-center gap-[8px]">
                <MsMoreAction :list="addMemberActionItem" @select="handleAddMember">
                  <div class="icon-button">
                    <MsIcon type="icon-icon_add_outlined" size="16" />
                  </div>
                </MsMoreAction>
                <MsMoreAction
                  :list="moreAction"
                  @select="(value) => handleMoreAction(value, element.id, 'ORGANIZATION')"
                >
                  <div class="icon-button">
                    <MsIcon type="icon-icon_more_outlined" size="16" />
                  </div>
                </MsMoreAction>
              </div>
            </div>
          </CreateUserGroupPopup>
        </div>
        <a-divider class="my-[0px] mt-[6px]" />
      </div>
    </Transition>
  </div>
  <div class="mt-2">
    <CreateUserGroupPopup
      :list="projectUserGroupList"
      :visible="projectUserGroupVisible"
      auth-scope="PROJECT"
      @cancel="projectUserGroupVisible = false"
      @submit="handleCreateUserGroup"
    >
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1 text-[var(--color-text-4)]">
          <MsIcon
            v-if="projectToggle"
            class="cursor-pointer"
            type="icon-icon_expand-down_filled"
            size="12"
            @click="projectToggle = false"
          />
          <MsIcon
            v-else
            class="cursor-pointer"
            type="icon-icon_expand-right_filled"
            size="12"
            @click="projectToggle = true"
          />
          <div class="text-[14px]">
            {{ t('system.userGroup.projectUserGroup') }}
          </div>
        </div>
        <MsMoreAction :list="createProjectUGActionItem" @select="projectUserGroupVisible = true">
          <icon-plus-circle-fill class="text-[rgb(var(--primary-7))]" size="20" />
        </MsMoreAction>
      </div>
    </CreateUserGroupPopup>
    <Transition>
      <div v-if="projectToggle">
        <div
          v-for="element in projectUserGroupList"
          :key="element.id"
          class="flex h-[38px] cursor-pointer items-center py-[7px] pl-[20px] pr-[4px]"
          :class="{ 'bg-[rgb(var(--primary-1))]': element.id === currentId }"
          @click="handleListItemClick(element)"
        >
          <CreateUserGroupPopup
            :list="projectUserGroupList"
            v-bind="popVisible[element.id]"
            @cancel="handleRenameCancel(element)"
            @submit="handleRenameCancel(element, element.id)"
          >
            <div class="flex grow flex-row items-center justify-between">
              <a-tooltip :content="element.name">
                <div
                  class="one-line-text max-w-[156px] text-[var(--color-text-1)]"
                  :class="{ 'text-[rgb(var(--primary-7))]': element.id === currentId }"
                  >{{ element.name }}</div
                >
              </a-tooltip>
              <div v-if="element.id === currentId && !element.internal" class="flex flex-row items-center gap-[8px]">
                <MsMoreAction :list="addMemberActionItem" @select="handleAddMember">
                  <div class="icon-button">
                    <MsIcon type="icon-icon_add_outlined" size="16" />
                  </div>
                </MsMoreAction>
                <MsMoreAction :list="moreAction" @select="(value) => handleMoreAction(value, element.id, 'PROJECT')">
                  <div class="icon-button">
                    <MsIcon type="icon-icon_more_outlined" size="16" />
                  </div>
                </MsMoreAction>
              </div>
            </div>
          </CreateUserGroupPopup>
        </div>
      </div>
    </Transition>
  </div>

  <AddUserModal :visible="userModalVisible" @cancel="userModalVisible = false" />
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { useI18n } from '@/hooks/useI18n';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { UserGroupItem, PopVisible, PopVisibleItem, AuthScopeType } from '@/models/setting/usergroup';
  import { getUserGroupList, deleteUserGroup } from '@/api/modules/setting/usergroup';
  import { computed, onMounted, ref } from 'vue';
  import CreateUserGroupPopup from './createOrUpdateUserGroup.vue';
  import AddUserModal from './addUserModal.vue';
  import useUserGroupStore from '@/store/modules/setting/system/usergroup';
  import { Message } from '@arco-design/web-vue';
  import useModal from '@/hooks/useModal';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();

  const store = useUserGroupStore();
  const { openModal } = useModal();

  // 用户组列表
  const userGroupList = ref<UserGroupItem[]>([]);

  const currentId = ref('');

  const userModalVisible = ref(false);

  // 气泡弹窗
  const popVisible = ref<PopVisible>({});

  // 系统用户创建用户组visible
  const systemUserGroupVisible = ref(false);
  // 组织用户创建用户组visible
  const orgUserGroupVisible = ref(false);
  // 项目用户创建用户组visible
  const projectUserGroupVisible = ref(false);

  // 系统用户组Toggle
  const systemToggle = ref(true);
  // 组织用户组Toggle
  const orgToggle = ref(true);
  // 项目用户组Toggle
  const projectToggle = ref(true);

  // 系统用户组列表
  const systemUserGroupList = computed(() => {
    return userGroupList.value.filter((ele) => ele.type === 'SYSTEM');
  });
  // 组织用户组列表
  const orgUserGroupList = computed(() => {
    return userGroupList.value.filter((ele) => ele.type === 'ORGANIZATION');
  });
  // 项目用户组列表
  const projectUserGroupList = computed(() => {
    return userGroupList.value.filter((ele) => ele.type === 'PROJECT');
  });

  const createSystemUGActionItem: ActionsItem[] = [
    { label: 'system.userGroup.addSysUserGroup', eventTag: 'createUserGroup' },
  ];
  const createOrgUGActionItem: ActionsItem[] = [
    { label: 'system.userGroup.addOrgUserGroup', eventTag: 'createUserGroup' },
  ];
  const createProjectUGActionItem: ActionsItem[] = [
    { label: 'system.userGroup.addProjectUserGroup', eventTag: 'createUserGroup' },
  ];

  const addMemberActionItem: ActionsItem[] = [{ label: 'system.userGroup.addMember', eventTag: 'addMember' }];
  const moreAction: ActionsItem[] = [
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
  const initData = async (id?: string) => {
    try {
      const res = await getUserGroupList();
      if (res.length > 0) {
        userGroupList.value = res;
        let tmpItem = res[0];
        if (id) {
          tmpItem = res.find((i) => i.id === id) || res[0];
        }
        handleListItemClick(tmpItem);
        // 弹窗赋值
        const tmpObj: PopVisible = {};
        res.forEach((element) => {
          tmpObj[element.id] = { visible: false, authScope: element.type, defaultName: '', id: element.id };
        });
        popVisible.value = tmpObj;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  // 点击更多操作
  const handleMoreAction = (item: ActionsItem, id: string, authScope: AuthScopeType) => {
    if (item.eventTag === 'rename') {
      const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
      const visibleItem: PopVisibleItem = { visible: true, authScope, defaultName: tmpObj.name, id };
      popVisible.value[id] = visibleItem;
    }
    if (item.eventTag === 'delete') {
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

  // 点击添加成员
  const handleAddMember = () => {
    userModalVisible.value = true;
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
  const handleCreateUserGroup = (id: string) => {
    initData(id);
  };
  const handleRenameCancel = (element: UserGroupItem, id?: string) => {
    if (id) {
      initData(id);
    }
    popVisible.value[element.id].visible = false;
  };
  onMounted(() => {
    initData();
  });
</script>

<style lang="less" scoped>
  .icon-increase {
    background-color: rgb(var(--primary-7));
  }
  .icon-button {
    display: flex;
    box-sizing: border-box;
    justify-content: center;
    align-items: center;
    width: 24px;
    height: 24px;
    color: rgb(var(--primary-7));
  }
  .icon-button:hover {
    background-color: rgb(var(--primary-9));
  }
  .v-enter-active,
  .v-leave-active {
    transition: opacity 0.5s ease;
  }
  .v-enter-from,
  .v-leave-to {
    opacity: 0;
  }
</style>
