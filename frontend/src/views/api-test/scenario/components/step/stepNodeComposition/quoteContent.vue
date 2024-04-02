<template>
  <div
    v-if="
      [ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.API_SCENARIO].includes(props.data.stepType)
    "
    class="flex items-center gap-[4px]"
  >
    <a-popover
      position="bl"
      content-class="quote-content-detail-popover"
      arrow-class="hidden"
      @popup-visible-change="handleVisibleChange"
    >
      <MsIcon type="icon-icon-draft" class="text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]" />
      <template #content>
        <div class="flex flex-col gap-[16px]">
          <div>
            <div class="mb-[2px] text-[var(--color-text-4)]">{{ t('apiScenario.belongProject') }}</div>
            <div class="text-[14px] text-[var(--color-text-1)]">
              {{ originProjectName }}
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
    </a-popover>
    <MsTag
      v-if="props.data.originProjectId !== appStore.currentProjectId"
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

  import { getStepProjectInfo } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioStepType } from '@/enums/apiEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  import getStepType from '@/views/api-test/scenario/components/common/stepType/utils';

  const props = defineProps<{
    data: ScenarioStepItem;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const originProjectName = ref('');

  async function handleVisibleChange(val: boolean) {
    if (val && props.data.originProjectId) {
      const res = await getStepProjectInfo(props.data.originProjectId);
      originProjectName.value = res.name;
    }
  }

  function goDetail() {
    const _stepType = getStepType(props.data);
    switch (true) {
      case _stepType.isCopyApi:
      case _stepType.isQuoteApi:
        openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
          pId: props.data.originProjectId,
          dId: props.data.resourceId,
        });
        break;
      case _stepType.isCopyScenario:
      case _stepType.isQuoteScenario:
        openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
          pId: props.data.originProjectId,
          sId: props.data.resourceId,
        });
        break;
      case _stepType.isQuoteCase:
      case _stepType.isCopyCase:
        openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
          pId: props.data.originProjectId,
          cId: props.data.resourceId,
        });
        break;
      default:
        break;
    }
  }
</script>

<style lang="less">
  .quote-content-detail-popover {
    width: 300px;
  }
</style>
