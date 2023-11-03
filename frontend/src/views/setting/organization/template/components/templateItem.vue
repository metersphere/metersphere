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
            <span class="enable">{{ t('system.orgTemplate.enabledTemplates') }}</span>
          </div>
          <div class="flex min-w-[300px] flex-nowrap items-center">
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="fieldSetting">{{ t('system.orgTemplate.fieldSetting') }}</span>
              <a-divider direction="vertical" />
            </span>
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="templateManagement">{{ t('system.orgTemplate.TemplateManagement') }}</span>
              <a-divider v-if="!props.cardItem.enable || props.cardItem.key === 'BUG'" direction="vertical" />
            </span>
            <span v-if="props.cardItem.key === 'BUG'" class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="workflowSetup">{{ t('system.orgTemplate.workflowSetup') }}</span>
              <a-divider v-if="!props.cardItem.enable && props.cardItem.key === 'BUG'" direction="vertical" />
            </span>
            <span v-if="!props.cardItem.enable" class="rounded p-[2px] hover:bg-[rgb(var(--primary-9))]">
              <MsTableMoreAction :list="moreActions" @select="(item) => handleMoreActionSelect"
            /></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { isEnableTemplate } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';

  import { SettingRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();

  const currentOrgId = computed(() => appStore.currentOrgId);

  const props = defineProps<{
    cardItem: Record<string, any>;
  }>();

  const router = useRouter();

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
      await isEnableTemplate(currentOrgId.value);
      Message.success(t('system.orgTemplate.enabledSuccessfully'));
      templateStore.getStatus();
    } catch (error) {
      console.log(error);
    }
  };

  const handleMoreActionSelect = (item: ActionsItem) => {
    if (item.eventTag === 'enable') {
      enableHandler();
    }
  };

  // 字段设置
  const fieldSetting = () => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
      query: {
        type: props.cardItem.key,
      },
    });
  };

  const templateManagement = () => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
      query: {
        type: props.cardItem.key,
      },
    });
  };

  const workflowSetup = () => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW,
      query: {
        type: props.cardItem.key,
      },
    });
  };
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
