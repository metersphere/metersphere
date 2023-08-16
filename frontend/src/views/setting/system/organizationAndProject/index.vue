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
      <SystemOrganization v-if="currentTable === 'organization'" :keyword="currentKeyword" />
      <SystemProject v-if="currentTable === 'project'" :keyword="currentKeyword" />
    </div>
  </MsCard>
  <AddOrganizationModal :visible="organizationVisible" @cancel="handleAddOrganizationCancel" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import AddOrganizationModal from './components/addOrganizationModal.vue';
  import SystemOrganization from './components/systemOrganization.vue';
  import SystemProject from './components/systemProject.vue';

  const { t } = useI18n();
  const currentTable = ref('project');
  const organizationVisible = ref(false);
  const organizationCount = ref(0);
  const projectCount = ref(0);
  const currentKeyword = ref('');

  const handleSearch = (value: string) => {
    currentKeyword.value = value;
  };
  const handleEnter = (eve: Event) => {
    currentKeyword.value = (eve.target as HTMLInputElement).value;
  };

  const handleAddOrganization = () => {
    organizationVisible.value = true;
  };
  const handleAddOrganizationCancel = () => {
    organizationVisible.value = false;
  };
</script>
