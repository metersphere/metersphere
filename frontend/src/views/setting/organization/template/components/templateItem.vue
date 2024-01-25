<template>
  <div class="outerWrapper p-[3px]">
    <div class="innerWrapper">
      <div class="content">
        <div class="logo-img h-[48px] w-[48px]">
          <svg-icon width="36px" height="36px" :name="props.cardItem.value"></svg-icon>
        </div>
        <div class="template-operation">
          <div class="flex items-center">
            <span class="font-medium">{{ props.cardItem.name }}</span>
            <span v-if="!isEnableProject" class="enable">{{ t('system.orgTemplate.enabledTemplates') }}</span>
          </div>
          <div class="flex min-w-[300px] flex-nowrap items-center">
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="fieldSetting">{{ t('system.orgTemplate.fieldSetting') }}</span>
              <a-divider direction="vertical" />
            </span>
            <span v-permission="['ORGANIZATION_TEMPLATE:READ']" class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="templateManagement">{{ t('system.orgTemplate.TemplateManagement') }}</span>
              <a-divider v-if="isEnableProject || props.cardItem.key === 'BUG'" direction="vertical" />
            </span>
            <span v-if="props.cardItem.key === 'BUG'" class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="workflowSetup">{{ t('system.orgTemplate.workflowSetup') }}</span>
              <a-divider v-if="isEnableProject && props.cardItem.key === 'BUG'" direction="vertical" />
            </span>
            <span
              v-if="isEnableProject"
              v-permission="['ORGANIZATION_TEMPLATE:READ+ENABLE']"
              class="rounded p-[2px] hover:bg-[rgb(var(--primary-9))]"
            >
              <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect"
            /></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 模版-模版管理小卡片
   */
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { enableOrOffTemplate } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';

  const { t } = useI18n();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();

  const currentOrgId = computed(() => appStore.currentOrgId);

  const props = defineProps<{
    cardItem: Record<string, any>;
    mode: 'organization' | 'project';
  }>();

  const emit = defineEmits<{
    (e: 'fieldSetting', key: string): void;
    (e: 'templateManagement', key: string): void;
    (e: 'workflowSetup', key: string): void;
    (e: 'updateState'): void;
  }>();

  // 先判断项目是否是开启
  const isEnableProject = computed(() => {
    return props.mode === 'organization'
      ? !templateStore.projectStatus[props.cardItem.key]
      : templateStore.ordStatus[props.cardItem.key];
  });

  const moreActions = ref<ActionsItem[]>([
    {
      label: t('system.orgTemplate.enable'),
      eventTag: 'enable',
      danger: true,
    },
  ]);

  // 启用模板
  const enableHandler = async () => {
    try {
      if (props.mode) {
        await enableOrOffTemplate(currentOrgId.value, props.cardItem.key);
        Message.success(t('system.orgTemplate.enabledSuccessfully'));
        await templateStore.getStatus();
        emit('updateState');
      }
    } catch (error) {
      console.log(error);
    }
  };

  const handleMoreActionSelect = () => {
    enableHandler();
  };

  // 字段设置
  const fieldSetting = () => {
    emit('fieldSetting', props.cardItem.key);
  };

  const templateManagement = () => {
    emit('templateManagement', props.cardItem.key);
  };

  const workflowSetup = () => {
    emit('workflowSetup', props.cardItem.key);
  };

  const templateCardInfo = ref<Record<string, any>>({});

  watch(
    () => props.cardItem,
    (val) => {
      if (val) {
        templateCardInfo.value = { ...props.cardItem };
      }
    },
    { deep: true }
  );
</script>

<style scoped lang="less">
  :deep(.arco-divider-vertical) {
    margin: 0 8px;
  }
  .outerWrapper {
    box-shadow: 0 6px 15px rgba(120 56 135/ 5%);
    @apply rounded bg-white;
    .innerWrapper {
      background: var(--color-bg-3);
      @apply rounded p-6;
      .content {
        @apply flex;
        .logo-img {
          @apply mr-3 flex items-center bg-white;
        }
        .template-operation {
          .operation {
            cursor: pointer;
          }
          .enable {
            color: var(--color-text-4);
            background: var(--color-text-n8);
            @apply ml-4 rounded p-1 text-xs;
          }
          @apply flex flex-col justify-between;
        }
      }
    }
  }
</style>
