<template>
  <MsCard simple>
    <div class="flex flex-row">
      <div class="user-group-left" :style="{ padding: collapse ? '24px 24px 24px 0' : 0 }">
        <user-group-left v-if="collapse" />
        <div class="usergroup-collapse" @click="handleCollapse">
          <MsIcon v-if="collapse" type="icon-icon_up-left_outlined" class="icon" />
          <MsIcon v-else type="icon-icon_down-right_outlined" class="icon" />
        </div>
      </div>
      <div class="relative w-[100%] overflow-x-scroll p-[24px]">
        <div class="flex flex-row items-center justify-between">
          <div class="title">{{ store.userGroupInfo.currentName }}</div>
          <div class="flex items-center">
            <a-input-search
              v-if="currentTable === 'user'"
              :placeholder="t('system.user.searchUser')"
              class="w-[240px]"
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
          <user-table v-if="currentTable === 'user' && couldShowUser" ref="userRef" :keyword="currentKeyword" />
          <auth-table v-if="currentTable === 'auth' && couldShowAuth" ref="authRef" />
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
  import { ref, computed, watchEffect, nextTick } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import useUserGroupStore from '@/store/modules/setting/organization/usergroup';
  import UserGroupLeft from './components/index.vue';
  import UserTable from './components/userTable.vue';
  import AuthTable from './components/authTable.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { useAppStore } from '@/store';

  const currentTable = ref('auth');

  const { t } = useI18n();
  const currentKeyword = ref('');
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

  const store = useUserGroupStore();
  const couldShowUser = computed(() => store.userGroupInfo.currentType === 'ORGANIZATION');
  const couldShowAuth = computed(() => store.userGroupInfo.currentId !== 'admin');
  const handleCollapse = () => {
    store.setCollapse(!store.collapse);
  };
  const collapse = computed(() => store.collapse);
  const menuWidth = computed(() => {
    const width = appStore.menuCollapse ? 86 : appStore.menuWidth;
    if (store.collapse) {
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
@/store/modules/setting/system/usergroup
