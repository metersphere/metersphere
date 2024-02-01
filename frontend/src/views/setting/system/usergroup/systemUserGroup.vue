<template>
  <div class="card">
    <MsSplitBox v-model:width="leftWidth" @expand-change="handleCollapse">
      <template #first>
        <UserGroupLeft ref="ugLeftRef" @handle-select="handleSelect" @add-user-success="handleAddMember" />
      </template>
      <template #second>
        <div class="p-[24px]">
          <div class="flex flex-row items-center justify-between">
            <a-tooltip :content="currentUserGroupItem.name">
              <div class="one-line-text max-w-[300px]">{{ currentUserGroupItem.name }}</div>
            </a-tooltip>
            <div class="flex items-center">
              <a-input-search
                v-if="currentTable === 'user'"
                :placeholder="t('system.user.searchUser')"
                class="w-[240px]"
                allow-clear
                @press-enter="handleEnter"
                @search="handleSearch"
                @clear="handleSearch('')"
              ></a-input-search>
              <a-radio-group
                v-if="couldShowUser && couldShowAuth"
                v-model="currentTable"
                class="ml-[14px]"
                type="button"
              >
                <a-radio v-if="couldShowAuth" value="auth">{{ t('system.userGroup.auth') }}</a-radio>
                <a-radio v-if="couldShowUser" value="user">{{ t('system.userGroup.user') }}</a-radio>
              </a-radio-group>
            </div>
          </div>
          <div class="mt-[16px]">
            <UserTable
              v-if="currentTable === 'user' && couldShowUser"
              ref="userRef"
              :keyword="currentKeyword"
              :current="currentUserGroupItem"
              :delete-permission="['SYSTEM_USER_ROLE:READ+DELETE']"
              :read-permission="['SYSTEM_USER_ROLE:READ']"
              :update-permission="['SYSTEM_USER_ROLE:READ+UPDATE']"
            />
            <AuthTable
              v-if="currentTable === 'auth' && couldShowAuth"
              :current="currentUserGroupItem"
              :width="bottomWidth"
              :save-permission="['SYSTEM_USER_ROLE:READ+UPDATE']"
              :disabled="!hasAnyPermission(['SYSTEM_USER_ROLE:READ+UPDATE'])"
            />
          </div>
        </div>
      </template>
    </MsSplitBox>
  </div>
</template>

<script lang="ts" setup>
  /**
   * @description 系统设置-系统-用户组
   */
  import { computed, nextTick, onMounted, provide, ref, watchEffect } from 'vue';

  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import AuthTable from '@/components/business/ms-user-group-comp/authTable.vue';
  import UserGroupLeft from '@/components/business/ms-user-group-comp/msUserGroupLeft.vue';
  import UserTable from '@/components/business/ms-user-group-comp/userTable.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { addPixelValues } from '@/utils/css';
  import { hasAnyPermission } from '@/utils/permission';

  import { CurrentUserGroupItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';

  const currentTable = ref('auth');
  provide('systemType', AuthScopeEnum.SYSTEM);

  const { t } = useI18n();
  const currentKeyword = ref('');
  const ugLeftRef = ref();

  const currentUserGroupItem = ref<CurrentUserGroupItem>({
    id: '',
    name: '',
    type: AuthScopeEnum.SYSTEM,
    internal: true,
  });

  const userRef = ref();
  const appStore = useAppStore();
  const leftCollapse = ref(true);
  const leftWidth = ref('300px');
  const bottomWidth = computed(() => {
    const width = appStore.menuCollapse ? '86px' : `${appStore.menuWidth}px`;
    if (leftCollapse.value) {
      return `calc(100% - ${addPixelValues(width, leftWidth.value, '20px')})`;
    }
    return `calc(100% - ${addPixelValues(width, '16px')})`;
  });

  const tableSearch = () => {
    if (currentTable.value === 'user' && userRef.value) {
      userRef.value.fetchData();
    } else if (!userRef.value) {
      nextTick(() => {
        userRef.value.fetchData();
      });
    }
  };

  const handleSearch = (value: string) => {
    currentKeyword.value = value;
    tableSearch();
  };
  const handleEnter = (eve: Event) => {
    currentKeyword.value = (eve.target as HTMLInputElement).value;
    tableSearch();
  };

  const handleSelect = (item: CurrentUserGroupItem) => {
    currentUserGroupItem.value = item;
  };

  const handleAddMember = (id: string) => {
    if (id === currentUserGroupItem.value.id) {
      tableSearch();
    }
  };

  const couldShowUser = computed(() => currentUserGroupItem.value.type === AuthScopeEnum.SYSTEM);
  const couldShowAuth = computed(() => currentUserGroupItem.value.id !== 'admin');
  const handleCollapse = (collapse: boolean) => {
    leftCollapse.value = collapse;
    if (collapse) {
      leftWidth.value = '300px';
      nextTick(() => {
        ugLeftRef.value?.initData(currentUserGroupItem.value.id);
      });
    }
  };
  watchEffect(() => {
    if (!couldShowAuth.value) {
      currentTable.value = 'user';
    } else if (!couldShowUser.value) {
      currentTable.value = 'auth';
    } else {
      currentTable.value = 'auth';
    }
  });
  onMounted(() => {
    ugLeftRef.value?.initData();
  });
</script>

<style lang="less" scoped>
  .card {
    @apply overflow-hidden bg-white;

    position: relative;
    height: calc(100vh - 88px);
    border-radius: var(--border-radius-large);
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
  }
</style>
