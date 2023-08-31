<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button type="primary" @click="handleAddOrganization">{{
          currentTable === 'organization'
            ? t('system.organization.createOrganization')
            : t('system.organization.createProject')
        }}</a-button>
      </div>
      <div class="flex items-center">
        <a-input-search
          :placeholder="t('system.user.searchUser')"
          class="w-[240px]"
          allow-clear
          @press-enter="handleEnter"
          @search="handleSearch"
        ></a-input-search>
        <a-radio-group v-model="currentTable" class="ml-[14px]" type="button">
          <a-radio value="organization">{{
            t('system.organization.organizationCount', { count: organizationCount })
          }}</a-radio>
          <a-radio value="project">{{ t('system.organization.projectCount', { count: projectCount }) }}</a-radio>
        </a-radio-group>
      </div>
    </div>
    <div>
      <SystemOrganization v-if="currentTable === 'organization'" ref="orgTableRef" :keyword="currentKeyword" />
      <SystemProject v-if="currentTable === 'project'" ref="projectTabeRef" :keyword="currentKeyword" />
    </div>
  </MsCard>
  <AddOrganizationModal :visible="organizationVisible" @submit="tableSearch" @cancel="handleAddOrganizationCancel" />
  <AddProjectModal :visible="projectVisible" @submit="tableSearch" @cancel="handleAddProjectCancel" />
</template>

<script lang="ts" setup>
  import { ref, watch, nextTick, onBeforeMount } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import AddOrganizationModal from './components/addOrganizationModal.vue';
  import SystemOrganization from './components/systemOrganization.vue';
  import SystemProject from './components/systemProject.vue';
  import AddProjectModal from './components/addProjectModal.vue';
  import { getOrgAndProjectCount } from '@/api/modules/setting/organizationAndProject';

  const { t } = useI18n();
  const currentTable = ref('organization');
  const organizationVisible = ref(false);
  const organizationCount = ref(0);
  const projectCount = ref(0);
  const currentKeyword = ref('');
  const orgTableRef = ref();
  const projectTabeRef = ref();
  const projectVisible = ref(false);

  // 初始化项目数量和组织数量
  const initOrgAndProjectCount = async () => {
    try {
      const res = await getOrgAndProjectCount();
      organizationCount.value = res.organizationTotal;
      projectCount.value = res.projectTotal;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  const tableSearch = () => {
    if (currentTable.value === 'organization') {
      if (orgTableRef.value) {
        orgTableRef.value.fetchData();
      } else {
        nextTick(() => {
          orgTableRef.value?.fetchData();
        });
      }
    } else if (projectTabeRef.value) {
      projectTabeRef.value.fetchData();
    } else {
      nextTick(() => {
        projectTabeRef.value?.fetchData();
      });
    }
    initOrgAndProjectCount();
  };

  const handleSearch = (value: string) => {
    currentKeyword.value = value;
    tableSearch();
  };
  const handleEnter = (eve: Event) => {
    currentKeyword.value = (eve.target as HTMLInputElement).value;
    tableSearch();
  };

  const handleAddOrganization = () => {
    if (currentTable.value === 'organization') {
      organizationVisible.value = true;
    } else {
      projectVisible.value = true;
    }
  };

  const handleAddProjectCancel = () => {
    projectVisible.value = false;
  };
  const handleAddOrganizationCancel = () => {
    organizationVisible.value = false;
  };

  watch(
    () => currentTable.value,
    () => {
      tableSearch();
    }
  );
  onBeforeMount(() => {
    tableSearch();
  });
</script>
