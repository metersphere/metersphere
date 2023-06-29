<template>
  <div class="user-group flex flex-row bg-white">
    <div class="user-group-left">
      <UserGroupLeft />
    </div>
    <div class="grow-1 w-[100%] overflow-x-scroll p-[24px]">
      <div class="grow-1 flex flex-row items-center justify-between">
        <div class="title">{{ store.userGroupInfo.currentName }}</div>
        <div class="flex items-center">
          <a-input class="w-[240px]" :placeholder="t('system.userGroup.searchPlacehoder')">
            <template #prefix>
              <icon-search />
            </template>
          </a-input>
          <a-radio-group v-model="currentTable" class="ml-[14px]" type="button">
            <a-radio value="auth">{{ t('system.userGroup.auth') }}</a-radio>
            <a-radio value="user">{{ t('system.userGroup.user') }}</a-radio>
          </a-radio-group>
        </div>
      </div>
      <div class="grow-1 mt-[16px]">
        <user-table v-if="currentTable === 'user'" />
        <auth-table v-if="currentTable === 'auth'" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import useUserGroupStore from '@/store/modules/system/usergroup';
  import UserGroupLeft from './components/index.vue';
  import UserTable from './components/userTable.vue';
  import AuthTable from './components/authTable.vue';

  const currentTable = ref('auth');

  const { t } = useI18n();

  const store = useUserGroupStore();
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
    padding: 24px;
    border-right: 1px solid var(--color-border);
  }
</style>
