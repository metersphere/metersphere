<template>
  <div v-if="isDelete" class="mb-6">
    <a-alert type="error">{{ t('project.basicInfo.alertDescription') }}</a-alert>
  </div>
  <div class="wrapper mb-6 flex justify-between">
    <span class="font-medium text-[var(--color-text-000)]">{{ t('project.basicInfo.basicInfo') }}</span>
    <MsTableMoreAction :list="tableActions" @select="handleSelect($event)">
      <a-button type="outline">{{ t('project.basicInfo.action') }}</a-button>
    </MsTableMoreAction>
  </div>
  <div class="project-info mb-6 h-[112px] bg-white p-1">
    <div class="inner-wrapper rounded-md p-4">
      <div class="detail-info flex flex-col justify-between rounded-md p-4">
        <div class="flex items-center">
          <span class="mr-1 font-medium text-[var(--color-text-000)]">具体的项目名称</span>
          <span v-if="!isDelete" class="button enable-button mr-1">{{ t('project.basicInfo.enable') }}</span>
          <span v-else class="button delete-button">{{ t('project.basicInfo.deleted') }}</span>
        </div>
        <div class="one-line-text text-xs text-[--color-text-4]"
          >描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述</div
        >
      </div>
    </div>
  </div>
  <div class="ml-1 flex flex-col">
    <div class="label-item">
      <span class="label">{{ t('project.basicInfo.createBy') }}</span>
      <span>罗老师</span>
    </div>
    <div class="label-item">
      <span class="label">{{ t('project.basicInfo.organization') }}</span>
      <MsTag>疯狂的刚子疯狂的刚子疯狂的刚子疯狂的刚子疯狂的刚子疯狂的刚子</MsTag>
    </div>
    <div class="label-item">
      <span class="label">{{ t('project.basicInfo.createTime') }}</span>
      <span>2023-04-23 15:33:23</span>
    </div>
  </div>
  <UpdateProjectModal v-model:visible="isVisible" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import UpdateProjectModal from './components/updateProjectModal.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  const { t } = useI18n();
  const { openModal } = useModal();

  const tableActions: ActionsItem[] = [
    {
      label: 'project.basicInfo.edit',
      eventTag: 'edit',
    },
    {
      label: 'project.basicInfo.enable',
      eventTag: 'enable',
    },
    {
      label: 'project.basicInfo.finish',
      eventTag: 'finish',
    },
    {
      isDivider: true,
    },
    {
      label: 'project.basicInfo.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const isDelete = ref<boolean>(true);

  const isVisible = ref<boolean>(false);

  const editHandler = () => {
    isVisible.value = true;
  };

  const finishHandler = () => {
    openModal({
      type: 'warning',
      title: t('project.basicInfo.finishedProject'),
      content: t('project.basicInfo.finishedProjectTip'),
      okText: t('project.basicInfo.confirmFinish'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {},
      hideCancel: false,
    });
  };

  const deleteHandler = () => {
    openModal({
      type: 'error',
      title: t('project.member.deleteTip', { name: '此项目' }),
      content: t('project.member.deleteContentTip'),
      okText: t('project.basicInfo.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {},
      hideCancel: false,
    });
  };

  function handleSelect(item: ActionsItem) {
    switch (item.eventTag) {
      case 'edit':
        editHandler();
        break;
      case 'finish':
        finishHandler();
        break;
      case 'delete':
        deleteHandler();
        break;
      default:
        break;
    }
  }
</script>

<style scoped lang="less">
  .project-info {
    border-radius: 4px;
    box-shadow: 0 0 10px rgb(120 56 135/5%);
    .inner-wrapper {
      height: 100%;
      background: rgb(var(--primary-1));
      .detail-info {
        height: 100%;
        background: url('@/assets/images/basic_bg.png');
        background-size: cover;
        .button {
          border-radius: 2px;
          @apply inline-block px-2 py-1 text-xs;
        }
        .enable-button {
          color: rgb(var(--success-5));
          background: rgb(var(--success-1));
        }
        .delete-button {
          color: rgb(var(--danger-5));
          background: rgb(var(--danger-1));
        }
      }
    }
  }
  .label-item {
    margin-bottom: 16px;
    height: 22px;
    line-height: 22px;
    span {
      float: left;
    }
    .label {
      margin-right: 16px;
      width: 120px;
      color: var(--color-text-3);
    }
  }
</style>
