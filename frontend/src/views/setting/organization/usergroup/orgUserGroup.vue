<template>
  <div class="card">
    <div class="flex h-full flex-row">
      <Transition>
        <div v-if="leftCollapse" class="user-group-left ms-scroll-bar">
          <UserGroupLeft ref="ugLeftRef" @handle-select="handleSelect" />
        </div>
      </Transition>
      <Transition>
        <div class="usergroup-collapse" :style="{ left: leftCollapse ? '300px' : '0' }" @click="handleCollapse">
          <icon-double-left v-if="leftCollapse" class="text-[12px] text-[var(--color-text-brand)]" />
          <icon-double-right v-else class="text-[12px] text-[var(--color-text-brand)]" />
        </div>
      </Transition>
      <div class="p-[24px]" :style="{ width: leftCollapse ? 'calc(100% - 300px)' : '100%' }">
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
  </div>
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
  /**
   * @description 系统设置-组织-用户组
   */
  import { computed, nextTick, onMounted, provide, ref, watchEffect } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import AuthTable from '@/components/business/ms-user-group-comp/authTable.vue';
  import UserGroupLeft from '@/components/business/ms-user-group-comp/msUserGroupLeft.vue';
  import UserTable from '@/components/business/ms-user-group-comp/userTable.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { CurrentUserGroupItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';

  // 注入系统层级
  provide('systemType', AuthScopeEnum.ORGANIZATION);
  const currentTable = ref('user');
  const leftCollapse = ref(true);

  const { t } = useI18n();
  const currentKeyword = ref('');
  const ugLeftRef = ref();
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
    if (leftCollapse.value) {
      nextTick(() => {
        ugLeftRef.value?.initData(currentUserGroupItem.value.id, false);
      });
    }
  };
  const menuWidth = computed(() => {
    const width = appStore.menuCollapse ? 86 : appStore.menuWidth;
    if (leftCollapse.value) {
      return width + 300;
    }
    return width;
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
  onMounted(() => {
    ugLeftRef.value?.initData();
  });
</script>

<style lang="scss" scoped>
  .card {
    @apply overflow-hidden bg-white;

    position: relative;
    height: calc(100vh - 88px);
    border-radius: var(--border-radius-large);
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
  }
  .user-group-left {
    position: relative;
    overflow-x: hidden;
    overflow-y: auto;
    padding-right: 6px;
    width: 300px;
    min-width: 300px;
    height: 100%;
    border-right: 1px solid var(--color-border-1);
  }
  .usergroup-collapse {
    position: absolute;
    top: 50%;
    z-index: 101;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 16px;
    height: 36px;
    background-color: var(--color-text-n8);
    flex-shrink: 0;
    cursor: pointer;
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
