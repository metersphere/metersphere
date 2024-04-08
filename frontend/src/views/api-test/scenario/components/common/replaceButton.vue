<template>
  <MsButton type="icon" status="secondary" @click="importApiDrawerVisible = true">
    <MsIcon type="icon-icon_swich" class="mr-[8px]" />
    {{ t('common.replace') }}
  </MsButton>
  <importApiDrawer
    v-if="importApiDrawerVisible"
    v-model:visible="importApiDrawerVisible"
    :scenario-id="props.scenarioId"
    :case-id="props.resourceId"
    :api-id="props.resourceId"
    single-select
    @copy="handleImportApiApply('copy', $event)"
    @quote="handleImportApiApply('quote', $event)"
  />
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { ImportData } from './importApiDrawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

  import useCreateActions from '../step/createAction/useCreateActions';

  const importApiDrawer = defineAsyncComponent(() => import('./importApiDrawer/index.vue'));

  const props = defineProps<{
    steps: ScenarioStepItem[];
    step: ScenarioStepItem;
    resourceId: string | number;
    scenarioId?: string | number;
  }>();
  const emit = defineEmits<{
    (e: 'replace', replaceItem: ScenarioStepItem): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { buildInsertStepInfos } = useCreateActions();

  const importApiDrawerVisible = ref(false);

  /**
   * 处理导入系统请求
   * @param type 导入类型
   * @param data 导入数据
   */
  function handleImportApiApply(type: 'copy' | 'quote', data: ImportData) {
    const refType = type === 'copy' ? ScenarioStepRefType.COPY : ScenarioStepRefType.REF;
    let replaceItem: ScenarioStepItem[];
    if (data.api.length > 0) {
      replaceItem = buildInsertStepInfos(
        data.api,
        ScenarioStepType.API,
        refType,
        props.step.sort,
        props.step.projectId || appStore.currentProjectId
      );
    } else if (data.case.length > 0) {
      replaceItem = buildInsertStepInfos(
        data.case,
        ScenarioStepType.API_CASE,
        refType,
        props.step.sort,
        props.step.projectId || appStore.currentProjectId
      );
    } else {
      replaceItem = buildInsertStepInfos(
        data.scenario,
        ScenarioStepType.API_SCENARIO,
        refType,
        props.step.sort,
        props.step.projectId || appStore.currentProjectId
      );
    }
    emit('replace', replaceItem[0]);
  }
</script>

<style lang="less" scoped></style>
