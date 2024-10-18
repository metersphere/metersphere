<template>
  <MsCard simple no-content-padding>
    <MsSplitBox v-model:width="leftWidth" @expand-change="handleCollapse">
      <template #first>
        <div class="mr-1">
          <UserGroupLeft
            ref="ugLeftRef"
            :add-permission="['ORGANIZATION_USER_ROLE:READ+ADD']"
            :update-permission="['ORGANIZATION_USER_ROLE:READ+UPDATE']"
            :is-global-disable="true"
            @handle-select="handleSelect"
            @add-user-success="handleAddMember"
          />
        </div>
      </template>
      <template #second>
        <div>
          <div class="flex flex-row items-center justify-between p-[16px]">
            <a-radio-group v-if="couldShowUser && couldShowAuth" v-model="currentTable" size="medium" type="button">
              <a-radio v-if="couldShowAuth" value="auth" class="show-type-icon p-[2px]">
                {{ t('system.userGroup.auth') }}
              </a-radio>
              <a-radio v-if="couldShowUser" value="user" class="show-type-icon p-[2px]">
                {{ t('system.userGroup.user') }}
              </a-radio>
            </a-radio-group>
            <div class="flex items-center">
              <a-input-search
                v-if="currentTable === 'user'"
                :placeholder="t('system.user.searchUser')"
                class="w-[240px]"
                allow-clear
                @press-enter="handleEnter"
                @search="handleSearch"
                @clear="() => handleSearch('')"
              ></a-input-search>
            </div>
          </div>
          <div>
            <UserTable
              v-if="currentTable === 'user' && couldShowUser"
              ref="userRef"
              :keyword="currentKeyword"
              :current="currentUserGroupItem"
              :delete-permission="['ORGANIZATION_USER_ROLE:READ+DELETE']"
              :read-permission="['ORGANIZATION_USER_ROLE:READ']"
              :update-permission="['ORGANIZATION_USER_ROLE:READ+UPDATE']"
            />
            <AuthTable
              v-if="currentTable === 'auth' && couldShowAuth"
              :current="currentUserGroupItem"
              :width="bottomWidth"
              :save-permission="['ORGANIZATION_USER_ROLE:READ+UPDATE']"
              :disabled="
                !hasAnyPermission(['ORGANIZATION_USER_ROLE:READ+UPDATE']) || currentUserGroupItem.scopeId === 'global'
              "
            />
          </div>
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script lang="ts" setup>
  /**
   * @description 系统设置-组织-用户组
   */
  import { computed, nextTick, onMounted, provide, ref, watchEffect } from 'vue';
  import { useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
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

  const router = useRouter();
  // 注入系统层级
  provide('systemType', AuthScopeEnum.ORGANIZATION);
  const currentTable = ref('user');

  const { t } = useI18n();
  const currentKeyword = ref('');
  const ugLeftRef = ref();
  const currentUserGroupItem = ref<CurrentUserGroupItem>({
    id: '',
    name: '',
    type: AuthScopeEnum.ORGANIZATION,
    internal: true,
  });
  const userRef = ref();
  const appStore = useAppStore();
  const leftCollapse = ref(true);
  const leftWidth = ref('300px');
  const bottomWidth = computed(() => {
    const width = appStore.menuCollapse ? `${appStore.collapsedWidth}px` : `${appStore.menuWidth}px`;
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
  const handleCollapse = (collapse: boolean) => {
    leftCollapse.value = collapse;
    if (collapse) {
      leftWidth.value = '300px';
      nextTick(() => {
        ugLeftRef.value?.initData(currentUserGroupItem.value.id);
      });
    }
  };
  const handleSelect = (item: CurrentUserGroupItem) => {
    currentUserGroupItem.value = item;
  };

  const handleAddMember = (id: string) => {
    if (id === currentUserGroupItem.value.id) {
      tableSearch();
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
    ugLeftRef.value?.initData(router.currentRoute.value.query.id, true);
  });
</script>

<style lang="less" scoped></style>
