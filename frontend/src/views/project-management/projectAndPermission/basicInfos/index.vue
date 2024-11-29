<template>
  <div v-if="projectDetail?.deleted" class="mb-6">
    <a-alert type="error">{{ t('project.basicInfo.alertDescription') }}</a-alert>
  </div>
  <div class="wrapper mb-[16px] flex justify-between border-b border-[var(--color-text-n8)] pb-[16px]">
    <span class="font-medium text-[var(--color-text-1)]">{{ t('project.basicInfo.basicInfo') }}</span>
    <a-button
      v-show="!projectDetail?.deleted"
      v-permission="['PROJECT_BASE_INFO:READ+UPDATE']"
      type="outline"
      @click="editHandler"
      >{{ t('project.basicInfo.edit') }}</a-button
    >
  </div>
  <div class="detail-info mb-[16px] flex items-center gap-[12px]">
    <div class="flex items-center rounded-[var(--border-radius-small)] bg-[rgb(var(--primary-1))] p-[9px]">
      <MsIcon type="icon-icon_project" class="text-[rgb(var(--primary-4))]" :size="22" />
    </div>
    <div class="flex flex-col">
      <div class="flex items-center">
        <a-tooltip :content="projectDetail?.name">
          <div class="one-line-text mr-1 max-w-[300px] font-medium text-[var(--color-text-1)]">
            {{ projectDetail?.name }}
          </div>
        </a-tooltip>
        <div class="button mr-1" :class="[projectDetail?.deleted ? 'delete-button' : 'enable-button']">
          {{ projectDetail?.deleted ? t('project.basicInfo.deleted') : t('project.basicInfo.enable') }}
        </div>
      </div>
      <a-tooltip v-if="projectDetail?.description" :content="projectDetail?.description">
        <div class="one-line-text text-[12px] text-[--color-text-4]">{{ projectDetail?.description }}</div>
      </a-tooltip>
    </div>
  </div>
  <div class="grid grid-cols-2 gap-[16px] rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[16px]">
    <div class="label-item">
      <span class="label">{{ t('project.basicInfo.createBy') }}：</span>
      <a-tooltip v-if="translateTextToPX(projectDetail?.createUser) > 300" :content="projectDetail?.createUser">
        <span class="one-line-text" style="max-width: 300px">{{ projectDetail?.createUser }}</span>
      </a-tooltip>
      <span v-else>{{ projectDetail?.createUser }}</span>
    </div>
    <div class="label-item">
      <span class="label">{{ t('project.basicInfo.organization') }}：</span>
      <MsTag>{{ projectDetail?.organizationName }}</MsTag>
    </div>
    <div class="label-item">
      <span class="label">{{ t('project.basicInfo.resourcePool') }}：</span>
      <MsTag v-for="pool of projectDetail?.resourcePoolList" :key="pool.id">{{ pool.name }}</MsTag>
    </div>
    <div class="label-item">
      <span class="label">{{ t('project.basicInfo.createTime') }}：</span>
      <span>{{ dayjs(projectDetail?.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
    </div>
  </div>
  <UpdateProjectModal ref="projectDetailRef" v-model:visible="isVisible" @success="getProjectDetail()" />
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-基本信息
   */
  import { inject, onBeforeMount, ref } from 'vue';
  import dayjs from 'dayjs';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import UpdateProjectModal from './components/updateProjectModal.vue';

  import { getProjectInfo } from '@/api/modules/project-management/basicInfo';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { translateTextToPX } from '@/utils/css';

  import type { ProjectBasicInfoModel } from '@/models/projectManagement/basicInfo';

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const updateLoading = inject('reload', (flag: boolean) => {});

  const projectDetail = ref<ProjectBasicInfoModel>();

  const getProjectDetail = async () => {
    updateLoading(true);
    try {
      projectDetail.value = await getProjectInfo(currentProjectId.value);
    } catch (error) {
      console.log(error);
    } finally {
      updateLoading(false);
    }
  };

  const isVisible = ref<boolean>(false);
  const projectDetailRef = ref();

  const editHandler = () => {
    isVisible.value = true;
    projectDetailRef.value.editProject(projectDetail.value);
  };

  watch(currentProjectId, () => {
    getProjectDetail();
  });

  onBeforeMount(async () => {
    getProjectDetail();
  });
</script>

<style scoped lang="less">
  .detail-info {
    .button {
      padding: 0 4px;
      font-size: 12px;
      border-radius: 2px;
      line-height: 20px;
      @apply inline-block;
    }
    .enable-button {
      color: rgb(var(--success-5));
      background: rgb(var(--success-1));
    }
    .disabled-button {
      color: var(--color-text-4);
      background: var(--color-text-n8);
    }
    .delete-button {
      color: rgb(var(--danger-5));
      background: rgb(var(--danger-1));
    }
  }
  .label-item {
    height: 22px;
    line-height: 22px;
    .label {
      width: 120px;
      color: var(--color-text-3);
    }
  }
</style>
