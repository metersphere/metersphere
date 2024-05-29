<template>
  <NoPermissionLayoutVue is-project>
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
  </NoPermissionLayoutVue>
</template>

<script lang="ts" setup>
  import { onBeforeMount, Ref, ref } from 'vue';
  import { useRouter } from 'vue-router';

  import NoPermissionLayoutVue from '@/layout/no-permission-layout.vue';

  import { getProjectList, switchProject } from '@/api/modules/project-management/project';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore, useUserStore } from '@/store';
  import { getFirstRouteNameByPermission } from '@/utils/permission';

  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import type { ProjectListItem } from '@/models/setting/project';

  const appStore = useAppStore();

  const projectList: Ref<ProjectListItem[]> = ref([]);

  const userStore = useUserStore();

  const router = useRouter();
  const { t } = useI18n();

  async function initProjects() {
    try {
      if (appStore.getCurrentOrgId) {
        const res = await getProjectList(appStore.getCurrentOrgId); // TODO:
        projectList.value = res;
      } else {
        projectList.value = [];
      }
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
      await userStore.checkIsLogin(true);
      appStore.hideLoading();
      router.replace({
        name: getFirstRouteNameByPermission(router.getRoutes()),
        query: {
          orgId: appStore.currentOrgId,
          pId: value as string,
        },
      });
    }
  }

  onBeforeMount(() => {
    initProjects();
  });
</script>

<style lang="less" scoped></style>
