<template>
  <div class="flex flex-col px-[16px] pb-[16px]">
    <div class="sticky top-0 z-[999] bg-[var(--color-text-fff)] pb-[8px] pt-[16px]">
      <a-input-search
        :placeholder="t('system.userGroup.searchHolder')"
        allow-clear
        @press-enter="enterData"
        @search="searchData"
        @clear="searchData('')"
      />
    </div>
    <div v-if="showSystem" v-permission="['SYSTEM_USER_ROLE:READ']" class="mt-2">
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
        <CreateUserGroupPopup
          :list="systemUserGroupList"
          :visible="systemUserGroupVisible"
          :auth-scope="AuthScopeEnum.SYSTEM"
          @cancel="systemUserGroupVisible = false"
          @submit="handleCreateUserGroup"
        >
          <a-tooltip :content="`创建${t('system.userGroup.systemUserGroup')}`" position="right">
            <MsIcon
              v-permission="props.addPermission"
              type="icon-icon_create_planarity"
              size="20"
              class="cursor-pointer text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
              @click="handleCreateUG(AuthScopeEnum.SYSTEM)"
            />
          </a-tooltip>
        </CreateUserGroupPopup>
      </div>

      <Transition>
        <div v-if="systemToggle">
          <div
            v-for="element in systemUserGroupList"
            :key="element.id"
            class="list-item"
            :class="{ '!bg-[rgb(var(--primary-1))]': element.id === currentId }"
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
                    class="list-item-name one-line-text text-[var(--color-text-1)]"
                    :class="{ '!text-[rgb(var(--primary-5))]': element.id === currentId }"
                  >
                    {{ element.name }}
                  </div>
                </a-tooltip>
                <div
                  v-if="
                    element.type === systemType ||
                    (isSystemShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      systemMoreAction.length > 0)
                  "
                  class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                  :class="{ '!opacity-100': element.id === currentId }"
                >
                  <div v-if="element.type === systemType" class="icon-button">
                    <MsIcon
                      v-permission="props.updatePermission"
                      type="icon-icon_add_outlined"
                      size="16"
                      @click="handleAddMember"
                    />
                  </div>
                  <MsMoreAction
                    v-if="
                      isSystemShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      systemMoreAction.length > 0
                    "
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
        <CreateUserGroupPopup
          :list="orgUserGroupList"
          :visible="orgUserGroupVisible"
          :auth-scope="AuthScopeEnum.ORGANIZATION"
          @cancel="orgUserGroupVisible = false"
          @submit="handleCreateUserGroup"
        >
          <a-tooltip :content="`创建${t('system.userGroup.orgUserGroup')}`" position="right">
            <MsIcon
              v-permission="props.addPermission"
              type="icon-icon_create_planarity"
              size="20"
              class="cursor-pointer text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
              @click="orgUserGroupVisible = true"
            />
          </a-tooltip>
        </CreateUserGroupPopup>
      </div>
      <Transition>
        <div v-if="orgToggle">
          <div
            v-for="element in orgUserGroupList"
            :key="element.id"
            class="list-item"
            :class="{ '!bg-[rgb(var(--primary-1))]': element.id === currentId }"
            @click="handleListItemClick(element)"
          >
            <CreateUserGroupPopup
              :list="orgUserGroupList"
              v-bind="popVisible[element.id]"
              @cancel="handleRenameCancel(element)"
              @submit="handleRenameCancel(element, element.id)"
            >
              <div class="flex w-full grow flex-row items-center justify-between">
                <div class="flex w-[calc(100%-56px)] items-center gap-[4px]">
                  <a-tooltip
                    :content="
                      systemType === AuthScopeEnum.ORGANIZATION
                        ? element.name +
                          `(${
                            element.internal
                              ? t('common.internal')
                              : element.scopeId === 'global'
                              ? t('common.system.custom')
                              : t('common.custom')
                          })`
                        : element.name
                    "
                  >
                    <div
                      :class="`list-item-name one-line-text  text-[var(--color-text-1)] ${
                        systemType === AuthScopeEnum.ORGANIZATION ? 'max-w-[calc(100%-86px)]' : 'w-full'
                      } ${element.id === currentId ? 'text-[rgb(var(--primary-5))]' : ''}`"
                    >
                      {{ element.name }}
                    </div>
                    <!-- 系统内置 -->
                    <div
                      v-if="systemType === AuthScopeEnum.ORGANIZATION"
                      class="one-line-text ml-1 text-[var(--color-text-4)]"
                    >
                      {{
                        `(${
                          element.internal
                            ? t('common.internal')
                            : element.scopeId === 'global'
                            ? t('common.system.custom')
                            : t('common.custom')
                        })`
                      }}
                    </div>
                  </a-tooltip>
                </div>
                <!-- 操作 -->
                <div
                  v-if="
                    element.type === systemType ||
                    (isOrdShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      orgMoreAction.length > 0)
                  "
                  class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                  :class="{ '!opacity-100': element.id === currentId }"
                >
                  <div v-if="element.type === systemType" class="icon-button">
                    <MsIcon
                      v-permission="props.updatePermission"
                      type="icon-icon_add_outlined"
                      size="16"
                      @click="handleAddMember"
                    />
                  </div>
                  <MsMoreAction
                    v-if="
                      isOrdShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      orgMoreAction.length > 0
                    "
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
        <CreateUserGroupPopup
          :list="projectUserGroupList"
          :visible="projectUserGroupVisible"
          :auth-scope="AuthScopeEnum.PROJECT"
          @cancel="projectUserGroupVisible = false"
          @submit="handleCreateUserGroup"
        >
          <a-tooltip :content="`创建${t('system.userGroup.projectUserGroup')}`" position="right">
            <MsIcon
              v-permission="props.addPermission"
              type="icon-icon_create_planarity"
              size="20"
              class="cursor-pointer text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
              @click="projectUserGroupVisible = true"
            />
          </a-tooltip>
        </CreateUserGroupPopup>
      </div>
      <Transition>
        <div v-if="projectToggle">
          <div
            v-for="element in projectUserGroupList"
            :key="element.id"
            class="list-item"
            :class="{ '!bg-[rgb(var(--primary-1))]': element.id === currentId }"
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
                    class="list-item-name one-line-text text-[var(--color-text-1)]"
                    :class="{ '!text-[rgb(var(--primary-5))]': element.id === currentId }"
                    >{{ element.name }}</div
                  >
                </a-tooltip>
                <div
                  v-if="
                    element.type === systemType ||
                    (isProjectShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      projectMoreAction.length > 0)
                  "
                  class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                  :class="{ '!opacity-100': element.id === currentId }"
                >
                  <div v-if="element.type === systemType" class="icon-button">
                    <MsIcon
                      v-permission="props.updatePermission"
                      type="icon-icon_add_outlined"
                      size="16"
                      @click="handleAddMember"
                    />
                  </div>
                  <MsMoreAction
                    v-if="
                      isProjectShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      projectMoreAction.length > 0
                    "
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
    isGlobalDisable: boolean;
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
            const item = res.find((i) => i.id === id);
            if (item) {
              handleListItemClick(item);
            } else {
              Message.warning(t('common.resourceDeleted'));
              handleListItemClick(res[0]);
            }
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
    const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
    if (item.eventTag === 'rename') {
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
        title: t('system.userGroup.isDeleteUserGroup', { name: characterLimit(tmpObj.name) }),
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
  .list-item {
    padding: 7px 4px 7px 20px;
    height: 38px;
    border-radius: var(--border-radius-small);
    @apply flex cursor-pointer items-center hover:bg-[rgb(var(--primary-9))];
    &:hover .list-item-action {
      opacity: 1;
    }
  }
</style>
