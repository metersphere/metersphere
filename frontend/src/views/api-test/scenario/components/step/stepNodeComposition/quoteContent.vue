<template>
  <div class="flex items-center gap-[4px]">
    <a-popover position="bl" content-class="detail-popover" arrow-class="hidden">
      <MsIcon type="icon-icon-draft" class="text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]" />
      <template #content>
        <div class="flex flex-col gap-[16px]">
          <div>
            <div class="mb-[2px] text-[var(--color-text-4)]">{{ t('apiScenario.belongProject') }}</div>
            <div class="text-[14px] text-[var(--color-text-1)]">
              {{ props.data.belongProjectName }}
            </div>
          </div>
          <div>
            <div class="mb-[2px] text-[var(--color-text-4)]">{{ t('apiScenario.detailName') }}</div>
            <div class="cursor-pointer text-[14px] text-[rgb(var(--primary-5))]" @click="goDetail">
              {{ `【${props.data.num}】${props.data.name}` }}
            </div>
          </div>
        </div>
      </template>
    </a-popover>
    <MsTag
      v-if="props.data.belongProjectId !== props.data.currentProjectId"
      theme="outline"
      size="small"
      :self-style="{
        color: 'var(--color-text-4)',
        border: '1px solid var(--color-text-input-border)',
        backgroundColor: 'transparent',
      }"
    >
      {{ t('apiScenario.crossProject') }}
    </MsTag>
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { ScenarioStepType } from '@/enums/apiEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    data: {
      id: string | number;
      belongProjectId: string;
      belongProjectName: string;
      num: number;
      name: string;
      type: ScenarioStepType;
      currentProjectId: string;
    };
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  function goDetail() {
    switch (props.data.type) {
      case ScenarioStepType.COPY_API:
      case ScenarioStepType.QUOTE_API:
        openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, { dId: props.data.id });
        break;
      case ScenarioStepType.QUOTE_SCENARIO:
      case ScenarioStepType.COPY_SCENARIO:
        openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, { sId: props.data.id });
        break;
      case ScenarioStepType.COPY_CASE:
      case ScenarioStepType.QUOTE_CASE:
        openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, { cId: props.data.id });
        break;
      default:
        break;
    }
  }
</script>

<style lang="less" scoped>
  .detail-popover {
    width: 350px;
  }
</style>
