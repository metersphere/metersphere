<template>
  <div class="flex items-center gap-[4px]">
    <!-- <a-popover position="bl" content-class="detail-popover" arrow-class="hidden">
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
              {{ `【${props.data.resourceNum}】${props.data.resourceName}` }}
            </div>
          </div>
        </div>
      </template>
    </a-popover> -->
    <MsTag
      v-if="props.data.projectId !== appStore.currentProjectId"
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
  // import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';
  // import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  // import { ApiTestRouteEnum } from '@/enums/routeEnum';

  // import getStepType from '@/views/api-test/scenario/components/common/stepType/utils';

  const props = defineProps<{
    data: ScenarioStepItem;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  // const { openNewPage } = useOpenNewPage();

  // function goDetail() {
  //   const _stepType = getStepType(props.data);
  //   switch (true) {
  //     case _stepType.isCopyApi:
  //     case _stepType.isQuoteApi:
  //       openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, { dId: props.data.id, pId: props.data.projectId });
  //       break;
  //     case _stepType.isCopyScenario:
  //     case _stepType.isQuoteScenario:
  //       openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, { sId: props.data.id, pId: props.data.projectId });
  //       break;
  //     case _stepType.isQuoteCase:
  //     case _stepType.isCopyCase:
  //       openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, { cId: props.data.id, pId: props.data.projectId });
  //       break;
  //     default:
  //       break;
  //   }
  // }
</script>

<style lang="less" scoped>
  .detail-popover {
    width: 350px;
  }
</style>
