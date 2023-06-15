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
        <div class="primary-color" style="font-size: 20px"><icon-plus-circle-fill /></div>
      </div>
      <div>
        <div
          v-for="element in customList"
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
            position="bl"
            @cancel="() => handlePopConfirmCancel(element.id)"
          >
            <div class="draglist-item flex grow flex-row justify-between">
              <div class="usergroup-title leading-[24px]"> {{ element.name }}</div>
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
</template>

<script lang="ts" setup>
  import { ref, onBeforeMount } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { PopVisibleItem, RenameType, UserGroupListItem } from './type';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddUserModal from './addUserModal.vue';
  import AddUserGroupModal from './addUserGroupModal.vue';
  import popconfirm from './popconfirm.vue';
  import useUserGroupStore from '@/store/modules/system/usergroup';

  const { t } = useI18n();

  const searchKey = ref('');
  const store = useUserGroupStore();
  // 请求loading
  const currentId = ref(0);
  const addUserVisible = ref(false);
  const addUserGroupVisible = ref(false);
  // 修改用户组名字，权限范围
  const popVisible = ref<PopVisibleItem>({});
  // 用户组和权限范围的状态
  const popType = ref<RenameType>('rename');
  const popDefaultName = ref('');
  // 用户列表
  const customList = ref<UserGroupListItem[]>([
    { name: '系统管理员', title: '', id: 1, authScope: 'system' },
    { name: '系统成员', title: '', id: 2, authScope: 'system' },
    { name: '组织管理员', title: '', id: 3, authScope: 'system' },
    { name: '组织成员', title: '', id: 4, authScope: 'system' },
    { name: '项目管理员', title: '', id: 5, authScope: 'system' },
    { name: '项目成员', title: '', id: 6, authScope: 'system' },
    { name: '自定义用户组1', title: '项目', id: 7, authScope: 'project' },
    { name: '自定义用户组2', title: '组织', id: 8, authScope: 'oraganation' },
  ]);

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

  const currentSystemId = ref(0);

  const addUser = (id: number) => {
    currentSystemId.value = id;
    addUserVisible.value = true;
  };

  const addSystemUserGroup = () => {
    // eslint-disable-next-line no-console
    addUserGroupVisible.value = true;
  };

  function searchData(keyword: string) {
    // eslint-disable-next-line no-console
    console.log(keyword);
  }

  const handleMoreAction = (item: ActionsItem, id: number) => {
    if (item.eventTag !== 'delete') {
      popType.value = item.eventTag as RenameType;
      const tmpObj = customList.value.filter((ele) => ele.id === id)[0];
      popVisible.value = { ...popVisible.value, [id]: true };
      if (item.eventTag === 'rename') {
        popDefaultName.value = tmpObj.name;
      } else {
        popDefaultName.value = tmpObj.authScope;
      }
    }
  };
  const handlePopConfirmCancel = (id: number) => {
    popVisible.value = { ...popVisible.value, [id]: false };
  };
  onBeforeMount(() => {
    const tmpObj: PopVisibleItem = {};
    customList.value.forEach((element) => {
      tmpObj[element.id] = false;
    });
    popVisible.value = tmpObj;
  });
  const handleListItemClick = (element: UserGroupListItem) => {
    const { id, name, title } = element;
    currentId.value = id;
    store.setInfo({ currentName: name, currentTitle: title });
  };
</script>

<style scoped lang="less">
  .primary-color {
    color: rgb(var(--primary-5));
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
