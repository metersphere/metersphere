<template>
  <div class="mb-4 grid grid-cols-4 gap-2">
    <div class="col-span-2">
      <a-button v-permission="['PROJECT_USER:READ+ADD']" class="mr-3" type="primary" @click="addMember">
        {{ t('project.member.addMember') }}
      </a-button>
    </div>
    <div>
      <a-select v-model="roleIds" @change="changeSelect">
        <a-option v-for="item of userGroupAll" :key="item.id" :value="item.id">{{ t(item.name) }}</a-option>
        <template #prefix
          ><span>{{ t('project.member.tableColumnUserGroup') }}</span></template
        >
      </a-select></div
    >
    <div>
      <a-input-search
        v-model="keyword"
        :max-length="255"
        :placeholder="t('project.member.searchMember')"
        allow-clear
        @search="searchHandler"
        @press-enter="searchHandler"
        @clear="searchHandler"
      ></a-input-search
    ></div>
  </div>
  <memberTable
    v-if="isMounted"
    ref="memberTableRef"
    :keyword="keyword"
    :role-ids="roleIds"
    :user-group-options="userGroupOptions"
  />
</template>

<script setup lang="ts">
  import { getProjectUserGroup } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ProjectUserOption } from '@/models/projectManagement/projectAndPermission';

  const memberTable = defineAsyncComponent(() => import('./components/memberTable.vue'));

  /**
   * @description 项目管理-项目与权限-成员
   */
  const appStore = useAppStore();
  const { t } = useI18n();

  const userGroupAll = ref<ProjectUserOption[]>([]);
  const userGroupOptions = ref<ProjectUserOption[]>([]);

  const initOptions = async () => {
    userGroupOptions.value = await getProjectUserGroup(appStore.currentProjectId);
    userGroupAll.value = [
      {
        id: '',
        name: '全部',
      },
      ...userGroupOptions.value,
    ];
  };
  initOptions();
  const memberTableRef = ref<InstanceType<typeof memberTable> | null>(null);

  const roleIds = ref<string>('');
  const keyword = ref<string>('');
  const initData = async () => {
    memberTableRef.value?.initData();
  };

  const searchHandler = () => {
    initData();
  };

  const changeSelect = () => {
    initData();
  };

  function addMember() {
    memberTableRef.value?.addMember();
  }

  const isMounted = ref(false);

  onMounted(() => {
    isMounted.value = true; // 解决memberTable因为异步初始化列导致的路由组件渲染两次的问题
  });
</script>

<style scoped></style>
