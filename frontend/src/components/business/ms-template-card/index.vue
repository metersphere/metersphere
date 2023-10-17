<template>
  <div class="outerWrapper p-[3px]">
    <div class="innerWrapper">
      <div class="content">
        <div class="logo-img h-[48px] w-[48px]">
          <svg-icon width="36px" height="36px" :name="svgList[props.cardItem.value]"></svg-icon>
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
              <span>{{ t('system.orgTemplate.TemplateManagement') }}</span> <a-divider direction="vertical" />
            </span>
            <span v-if="props.cardItem.value === 'BUG'" class="operation hover:text-[rgb(var(--primary-5))]">
              <span>{{ t('system.orgTemplate.workflowSetup') }}</span> <a-divider direction="vertical" />
            </span>
            <span class="rounded p-[2px] hover:bg-[rgb(var(--primary-9))]">
              <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect"
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

  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { useI18n } from '@/hooks/useI18n';

  import { SettingRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();

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

  const svgList = ref<Record<string, any>>({
    FUNCTIONAL: 'caseTemplate',
    API: 'api_ui_Template',
    UI: 'api_ui_Template',
    TEST_PLAN: 'testPlanTemplate',
    BUG: 'defectTemplate',
  });

  const handleMoreActionSelect = (item: ActionsItem) => {};

  // 字段设置
  const fieldSetting = () => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
      query: {
        type: props.cardItem.value,
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
