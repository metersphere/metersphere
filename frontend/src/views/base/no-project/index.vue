<template>
  <single-logo-layout is-project>
    <div class="mt-[24px] flex items-center justify-center">
      <a-select class="w-[280px]" allow-search @change="selectProject">
        <template #arrow-icon>
          <icon-caret-down />
        </template>
        <a-tooltip v-for="project of projectList" :key="project.id" :mouse-enter-delay="500" :content="project.name">
          <a-option
            :value="project.id"
            :class="project.id === appStore.currentProjectId ? 'arco-select-option-selected' : ''"
          >
            {{ project.name }}
          </a-option>
        </a-tooltip>
        <template #empty>
          <div class="text-[var(--color-text-4)]">
            {{ t('common.noSelectProject') }}
          </div>
        </template>
      </a-select>
    </div>
  </single-logo-layout>
</template>

<script lang="ts" setup>
  import { onBeforeMount, Ref, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import SingleLogoLayout from '@/layout/single-logo-layout.vue';

  import { getProjectList, switchProject } from '@/api/modules/project-management/project';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore, useUserStore } from '@/store';
  import { getFirstRouteNameByPermission } from '@/utils/permission';

  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import type { ProjectListItem } from '@/models/setting/project';

  const appStore = useAppStore();

  const projectList: Ref<ProjectListItem[]> = ref([]);

  const userStore = useUserStore();

  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();

  async function initProjects() {
    try {
      const res = await getProjectList(appStore.getCurrentOrgId);
      projectList.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function selectProject(value: SelectValue) {
    appStore.setCurrentProjectId(value as string);
    try {
      await switchProject({
        projectId: value as string,
        userId: userStore.id || '',
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      router.replace({
        name: getFirstRouteNameByPermission(router.getRoutes()),
        query: {
          ...route.query,
          organizationId: appStore.currentOrgId,
          projectId: appStore.currentProjectId,
        },
      });
    }
  }

  onBeforeMount(() => {
    initProjects();
  });
</script>

<style lang="less" scoped></style>
