<template>
  <MsCard simple>
    <MsTrialAlert :tip-content="t('system.authorized.orgAndProTipContent')" />
    <div class="mb-4 flex items-center justify-between">
      <div class="flex items-center">
        <a-radio-group v-model="currentTable" size="medium" class="mr-[14px]" type="button">
          <a-radio value="organization">
            {{ t('system.organization.organizationCount', { count: organizationCount }) }}
          </a-radio>
          <a-radio value="project">{{ t('system.organization.projectCount', { count: projectCount }) }}</a-radio>
        </a-radio-group>
        <a-button
          v-if="currentTable !== 'organization' || licenseStore.hasLicense()"
          v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD']"
          type="primary"
          @click="handleAddOrganization"
          >{{
            currentTable === 'organization'
              ? t('system.organization.createOrganization')
              : t('system.organization.createProject')
          }}</a-button
        >
      </div>
      <a-input-search
        v-model="keyword"
        :placeholder="t('system.organization.searchIndexPlaceholder')"
        class="w-[240px]"
        allow-clear
        @press-enter="handleEnter"
        @search="handleSearch"
        @clear="handleSearch('')"
      ></a-input-search>
    </div>
    <div>
      <SystemOrganization v-if="currentTable === 'organization'" ref="orgTableRef" :keyword="currentKeyword" />
      <SystemProject v-if="currentTable === 'project'" ref="projectTableRef" :keyword="currentKeyword" />
    </div>
  </MsCard>
  <AddOrganizationModal :visible="organizationVisible" @submit="tableSearch" @cancel="handleAddOrganizationCancel" />
  <AddProjectModal :visible="projectVisible" @submit="tableSearch" @cancel="handleAddProjectCancel" />
</template>

<script lang="ts" setup>
  /**
   * @description 系统设置-系统-组织与项目
   */
  import { nextTick, onBeforeMount, ref, watch } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsTrialAlert from '@/components/business/ms-trial-alert/index.vue';
  import AddOrganizationModal from './components/addOrganizationModal.vue';
  import AddProjectModal from './components/addProjectModal.vue';
  import SystemOrganization from './components/systemOrganization.vue';
  import SystemProject from './components/systemProject.vue';

  import { getOrgAndProjectCount } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';

  const { t } = useI18n();
  const currentTable = ref('organization');
  const organizationVisible = ref(false);
  const organizationCount = ref(0);
  const projectCount = ref(0);
  const currentKeyword = ref('');
  const keyword = ref('');
  const orgTableRef = ref();
  const projectTableRef = ref();
  const projectVisible = ref(false);
  const licenseStore = useLicenseStore();

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
    } else if (projectTableRef.value) {
      projectTableRef.value.fetchData();
    } else {
      nextTick(() => {
        projectTableRef.value?.fetchData();
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

  const handleAddProjectCancel = (shouldSearch: boolean) => {
    projectVisible.value = false;
    if (shouldSearch) {
      tableSearch();
    }
  };
  const handleAddOrganizationCancel = (shouldSearch: boolean) => {
    organizationVisible.value = false;
    if (shouldSearch) {
      tableSearch();
    }
  };

  watch(
    () => currentTable.value,
    () => {
      currentKeyword.value = '';
      keyword.value = '';
    }
  );

  onBeforeMount(() => {
    initOrgAndProjectCount();
  });
</script>
