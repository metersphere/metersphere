<template>
  <div class="flex flex-col px-[24px] pb-[24px]">
    <div class="sticky top-0 z-[999] bg-white pt-[24px]">
      <a-input-search
        :placeholder="t('system.userGroup.searchHolder')"
        allow-clear
        @press-enter="enterData"
        @search="searchData"
        @clear="searchData('')"
      />
    </div>
    <div v-if="showSystem" v-permission="['SYSTEM_USER_ROLE:READ']" class="mt-2">
      <CreateUserGroupPopup
        :list="systemUserGroupList"
        :visible="systemUserGroupVisible"
        :auth-scope="AuthScopeEnum.SYSTEM"
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

          <icon-plus-circle-fill
            v-permission="props.addPermission"
            class="cursor-pointer text-[rgb(var(--primary-7))]"
            size="20"
            @click="handleCreateUG(AuthScopeEnum.SYSTEM)"
          />
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
              <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                <a-tooltip :content="element.name">
                  <div
                    class="one-line-text text-[var(--color-text-1)]"
                    :class="{ 'text-[rgb(var(--primary-7))]': element.id === currentId }"
                    >{{ element.name }}</div
                  >
                </a-tooltip>
                <div v-if="element.id === currentId && !element.internal" class="flex flex-row items-center gap-[8px]">
                  <MsMoreAction
                    v-if="element.type === systemType"
                    v-permission="props.updatePermission"
                    :list="addMemberActionItem"
                    @select="handleAddMember"
                  >
                    <div class="icon-button">
                      <MsIcon type="icon-icon_add_outlined" size="16" />
                    </div>
                  </MsMoreAction>
                  <MsMoreAction
                    v-if="isSystemShowAll"
                    :list="systemMoreAction"
                    @select="(value) => handleMoreAction(value, element.id, AuthScopeEnum.SYSTEM)"
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
    <div v-if="showOrg" v-permission="['ORGANIZATION_USER_ROLE:READ']" class="mt-2">
      <CreateUserGroupPopup
        :list="orgUserGroupList"
        :visible="orgUserGroupVisible"
        :auth-scope="AuthScopeEnum.ORGANIZATION"
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

          <icon-plus-circle-fill
            v-permission="props.addPermission"
            class="cursor-pointer text-[rgb(var(--primary-7))]"
            size="20"
            @click="orgUserGroupVisible = true"
          />
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
              <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                <a-tooltip :content="element.name">
                  <div
                    class="one-line-text text-[var(--color-text-1)]"
                    :class="{ 'text-[rgb(var(--primary-7))]': element.id === currentId }"
                    >{{ element.name }}</div
                  >
                </a-tooltip>
                <div v-if="element.id === currentId && !element.internal" class="flex flex-row items-center gap-[8px]">
                  <MsMoreAction
                    v-if="element.type === systemType"
                    v-permission="props.updatePermission"
                    :list="addMemberActionItem"
                    @select="handleAddMember"
                  >
                    <div class="icon-button">
                      <MsIcon type="icon-icon_add_outlined" size="16" />
                    </div>
                  </MsMoreAction>
                  <MsMoreAction
                    v-if="isOrdShowAll"
                    v-permission="props.updatePermission"
                    :list="orgMoreAction"
                    @select="(value) => handleMoreAction(value, element.id, AuthScopeEnum.ORGANIZATION)"
                  >
                    <div class="icon-button">
                      <MsIcon type="icon-icon_more_outlined" size="16" />
                    </div>
                  </MsMoreAction>
                </div>
              </div>
            </CreateUserGroupPopup>
          </div>
          <a-divider v-if="showSystem" class="my-[0px] mt-[6px]" />
        </div>
      </Transition>
    </div>
    <div v-if="showProject" v-permission="['PROJECT_GROUP:READ']" class="mt-2">
      <CreateUserGroupPopup
        :list="projectUserGroupList"
        :visible="projectUserGroupVisible"
        :auth-scope="AuthScopeEnum.PROJECT"
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

          <icon-plus-circle-fill
            v-permission="props.addPermission"
            class="cursor-pointer text-[rgb(var(--primary-7))]"
            size="20"
            @click="projectUserGroupVisible = true"
          />
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
              <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                <a-tooltip :content="element.name">
                  <div
                    class="one-line-text text-[var(--color-text-1)]"
                    :class="{ 'text-[rgb(var(--primary-7))]': element.id === currentId }"
                    >{{ element.name }}</div
                  >
                </a-tooltip>
                <div v-if="element.id === currentId && !element.internal" class="flex flex-row items-center gap-[8px]">
                  <MsMoreAction
                    v-if="element.type === systemType"
                    v-permission="props.updatePermission"
                    :list="addMemberActionItem"
                    @select="handleAddMember"
                  >
                    <div v-permission="props.updatePermission" class="icon-button">
                      <MsIcon type="icon-icon_add_outlined" size="16" />
                    </div>
                  </MsMoreAction>
                  <MsMoreAction
                    v-if="isProjectShowAll"
                    :list="projectMoreAction"
                    @select="(value) => handleMoreAction(value, element.id, AuthScopeEnum.PROJECT)"
                  >
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
  </div>
  <AddUserModal :visible="userModalVisible" :current-id="currentItem.id" @cancel="handleAddUserCancel" />
</template>

<script setup lang="ts">
  import { computed, inject, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddUserModal from './addUserModal.vue';
  import CreateUserGroupPopup from './createOrUpdateUserGroup.vue';

  import {
    deleteOrgUserGroup,
    deleteUserGroup,
    getOrgUserGroupList,
    getProjectUserGroupList,
    getUserGroupList,
  } from '@/api/modules/setting/usergroup';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { CurrentUserGroupItem, PopVisible, PopVisibleItem, UserGroupItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'handleSelect', element: UserGroupItem): void;
    (e: 'addUserSuccess', id: string): void;
  }>();

  const props = defineProps<{
    addPermission: string[];
    updatePermission: string[];
  }>();

  const appStore = useAppStore();
  const { openModal } = useModal();

  const systemType = inject<AuthScopeEnum>('systemType');

  const showSystem = computed(() => systemType === AuthScopeEnum.SYSTEM);
  const showOrg = computed(() => systemType === AuthScopeEnum.SYSTEM || systemType === AuthScopeEnum.ORGANIZATION);
  const showProject = computed(() => systemType === AuthScopeEnum.SYSTEM || systemType === AuthScopeEnum.PROJECT);

  // 用户组列表
  const userGroupList = ref<UserGroupItem[]>([]);

  const currentItem = ref<CurrentUserGroupItem>({ id: '', name: '', internal: false, type: AuthScopeEnum.SYSTEM });
  const currentId = ref('');
  const currentName = computed(() => currentItem.value.name);

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
    return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.SYSTEM);
  });
  // 组织用户组列表
  const orgUserGroupList = computed(() => {
    return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.ORGANIZATION);
  });
  // 项目用户组列表
  const projectUserGroupList = computed(() => {
    return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.PROJECT);
  });

  const addMemberActionItem: ActionsItem[] = [
    { label: 'system.userGroup.addMember', eventTag: 'addMember', permission: props.updatePermission },
  ];

  const systemMoreAction: ActionsItem[] = [
    {
      label: 'system.userGroup.rename',
      danger: false,
      eventTag: 'rename',
      permission: props.updatePermission,
    },
    {
      isDivider: true,
    },
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
      permission: ['SYSTEM_USER_ROLE:READ+DELETE'],
    },
  ];
  const orgMoreAction: ActionsItem[] = [
    {
      label: 'system.userGroup.rename',
      danger: false,
      eventTag: 'rename',
      permission: props.updatePermission,
    },
    {
      isDivider: true,
    },
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
      permission: ['ORGANIZATION_USER_ROLE:READ+DELETE'],
    },
  ];
  const projectMoreAction: ActionsItem[] = [
    {
      label: 'system.userGroup.rename',
      danger: false,
      eventTag: 'rename',
      permission: props.updatePermission,
    },
    {
      isDivider: true,
    },
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
      permission: ['PROJECT_GROUP:READ+DELETE'],
    },
  ];

  // 点击用户组列表
  const handleListItemClick = (element: UserGroupItem) => {
    const { id, name, type, internal } = element;
    currentItem.value = { id, name, type, internal };
    currentId.value = id;
    emit('handleSelect', element);
  };

  // 用户组数据初始化
  const initData = async (id?: string, isSelect = true) => {
    try {
      let res: UserGroupItem[] = [];
      if (systemType === AuthScopeEnum.SYSTEM && hasAnyPermission(['SYSTEM_USER_ROLE:READ'])) {
        res = await getUserGroupList();
      } else if (systemType === AuthScopeEnum.ORGANIZATION && hasAnyPermission(['ORGANIZATION_USER_ROLE:READ'])) {
        res = await getOrgUserGroupList(appStore.currentOrgId);
      } else if (systemType === AuthScopeEnum.PROJECT && hasAnyPermission(['PROJECT_GROUP:READ'])) {
        res = await getProjectUserGroupList(appStore.currentProjectId);
      }
      if (res.length > 0) {
        userGroupList.value = res;
        if (isSelect) {
          // leftCollapse 切换时不重复数据请求
          if (id) {
            handleListItemClick(res.find((i) => i.id === id) || res[0]);
          } else {
            handleListItemClick(res[0]);
          }
        }
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
  const handleMoreAction = (item: ActionsItem, id: string, authScope: AuthScopeEnum) => {
    if (item.eventTag === 'rename') {
      const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
      const visibleItem: PopVisibleItem = { visible: true, authScope, defaultName: tmpObj.name, id };
      popVisible.value[id] = visibleItem;
    }
    if (item.eventTag === 'delete') {
      let content = '';
      switch (authScope) {
        case AuthScopeEnum.SYSTEM:
          content = t('system.userGroup.beforeDeleteUserGroup');
          break;
        case AuthScopeEnum.ORGANIZATION:
          content = t('org.userGroup.beforeDeleteUserGroup');
          break;
        default:
          content = t('project.userGroup.beforeDeleteUserGroup');
          break;
      }
      openModal({
        type: 'error',
        title: t('system.userGroup.isDeleteUserGroup', { name: characterLimit(currentName.value) }),
        content,
        okText: t('system.userGroup.confirmDelete'),
        cancelText: t('system.userGroup.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            if (systemType === AuthScopeEnum.SYSTEM) {
              await deleteUserGroup(id);
            }
            if (systemType === AuthScopeEnum.ORGANIZATION) {
              await deleteOrgUserGroup(id);
            }
            // TODO 项目用户组删除用户组
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
      initData('', false);
      return;
    }
    const keyword = (eve.target as HTMLInputElement).value;
    const tmpArr = userGroupList.value.filter((ele) => ele.name.includes(keyword));
    userGroupList.value = tmpArr;
  }
  function searchData(value: string) {
    if (!value) {
      initData('', false);
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
      initData(id, true);
    }
    popVisible.value[element.id].visible = false;
  };
  defineExpose({
    initData,
  });
  const handleCreateUG = (scoped: AuthScopeEnum) => {
    if (scoped === AuthScopeEnum.SYSTEM) {
      systemUserGroupVisible.value = true;
    } else if (scoped === AuthScopeEnum.ORGANIZATION) {
      orgUserGroupVisible.value = true;
    } else if (scoped === AuthScopeEnum.PROJECT) {
      projectUserGroupVisible.value = true;
    }
  };
  const handleAddUserCancel = (shouldSearch: boolean) => {
    userModalVisible.value = false;
    if (shouldSearch) {
      emit('addUserSuccess', currentId.value);
    }
  };

  const isSystemShowAll = computed(() => {
    return hasAnyPermission([...props.updatePermission, 'SYSTEM_USER_ROLE:READ+DELETE']);
  });
  const isOrdShowAll = computed(() => {
    return hasAnyPermission([...props.updatePermission, 'ORGANIZATION_USER_ROLE:READ+DELETE']);
  });
  const isProjectShowAll = computed(() => {
    return hasAnyPermission([...props.updatePermission, 'PROJECT_GROUP:READ+DELETE']);
  });
</script>

<style lang="less" scoped>
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
