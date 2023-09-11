<template>
  <MsCard simple>
    <div class="flex flex-row">
      <div class="user-group-left" :style="{ padding: leftCollapse ? '24px 24px 24px 0' : 0 }">
        <UserGroupLeft v-if="leftCollapse" @on-select="handleSelect" />
        <div class="usergroup-collapse" @click="handleCollapse">
          <MsIcon v-if="leftCollapse" type="icon-icon_up-left_outlined" class="icon" />
          <MsIcon v-else type="icon-icon_down-right_outlined" class="icon" />
        </div>
      </div>
      <div class="relative w-[100%] overflow-x-scroll p-[24px]">
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
            ></a-input-search>
            <a-radio-group v-if="couldShowUser && couldShowAuth" v-model="currentTable" class="ml-[14px]" type="button">
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
          />
          <AuthTable v-if="currentTable === 'auth' && couldShowAuth" ref="authRef" :current="currentUserGroupItem" />
        </div>
      </div>
    </div>
  </MsCard>
  <div
    v-if="currentTable === 'auth'"
    class="fixed bottom-[16px] right-[16px] z-[999] flex justify-between bg-white p-[24px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
    :style="{ width: `calc(100% - ${menuWidth + 16}px)` }"
  >
    <ms-button class="btn" :disabled="!canSave" @click="handleReset">{{ t('system.userGroup.reset') }}</ms-button>
    <a-button class="btn" :disabled="!canSave" type="primary" @click="handleSave">{{
      t('system.userGroup.save')
    }}</a-button>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, watchEffect, nextTick, provide } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import UserGroupLeft from '@/components/business/ms-user-group-comp/msUserGroupLeft.vue';
  import UserTable from '@/components/business/ms-user-group-comp//userTable.vue';
  import AuthTable from '@/components/business/ms-user-group-comp/authTable.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { useAppStore } from '@/store';
  import { AuthScopeEnum } from '@/enums/commonEnum';
  import { CurrentUserGroupItem } from '@/models/setting/usergroup';

  // 注入系统层级
  provide('systemType', AuthScopeEnum.ORGANIZATION);
  const currentTable = ref('user');
  const leftCollapse = ref(true);

  const { t } = useI18n();
  const currentKeyword = ref('');
  const currentUserGroupItem = ref<CurrentUserGroupItem>({
    id: '',
    name: '',
    type: AuthScopeEnum.ORGANIZATION,
    internal: true,
  });
  const authRef = ref<{
    handleReset: () => void;
    handleSave: () => void;
    canSave: boolean;
  }>();
  const userRef = ref();
  const appStore = useAppStore();

  const tableSearch = () => {
    if (currentTable.value === 'user' && userRef.value) {
      userRef.value.fetchData();
    } else if (!userRef.value) {
      nextTick(() => {
        userRef.value?.fetchData();
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

  const couldShowUser = computed(() => currentUserGroupItem.value.type === AuthScopeEnum.ORGANIZATION);
  const couldShowAuth = computed(() => currentUserGroupItem.value.id !== 'admin');

  const handleSelect = (item: CurrentUserGroupItem) => {
    currentUserGroupItem.value = item;
  };

  const handleCollapse = () => {
    leftCollapse.value = !leftCollapse.value;
  };
  const menuWidth = computed(() => {
    const width = appStore.menuCollapse ? 86 : appStore.menuWidth;
    if (leftCollapse.value) {
      return width + 300;
    }
    return width + 24;
  });

  const handleReset = () => {
    authRef.value?.handleReset();
  };
  const handleSave = () => {
    authRef.value?.handleSave();
  };
  const canSave = computed(() => {
    if (currentTable.value === 'auth') {
      return authRef.value?.canSave;
    }
    return true;
  });
  watchEffect(() => {
    if (!couldShowAuth.value) {
      currentTable.value = 'user';
    } else if (!couldShowUser.value) {
      currentTable.value = 'auth';
    } else {
      currentTable.value = 'auth';
    }
  });
</script>

<style lang="scss" scoped>
  .title {
    color: var(--color-text-1);
  }
  .user-group {
    height: calc(100vh - 72px);
  }
  .user-group-left {
    position: relative;
    width: 300px;
    height: calc(100vh - 125px);
    border-right: 1px solid var(--color-border);
    .usergroup-collapse {
      position: absolute;
      top: 50%;
      right: -16px;
      z-index: 100;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 16px;
      height: 36px;
      background-color: var(--color-text-n8);
      opacity: 0;
      cursor: pointer;
      &:hover {
        opacity: 1;
      }
      .icon {
        font-size: 12px;
        color: var(--color-text-brand);
      }
    }
  }
</style>
