<template>
  <div class="flex flex-col gap-[16px]">
    <template v-if="appStore.projectList.length > 0">
      <div
        class="sticky top-0 z-[999] mb-[-16px] flex items-center justify-end gap-[12px] bg-[var(--color-bg-3)] pb-[16px]"
      >
        <MsProjectSelect
          ref="projectSelectRef"
          v-model:project="currentProject"
          class="w-[240px]"
          use-default-arrow-icon
          @change="handleProjectSelect"
        >
          <template #prefix>
            {{ t('menu.projectManagementShort') }}
          </template>
        </MsProjectSelect>
        <MsSelect
          v-model:model-value="features"
          :options="featureOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('project.messageManagement.function')"
          :multiple="true"
          :has-all-select="true"
          :default-all-select="true"
        />
        <a-button type="outline" class="arco-btn-outline--secondary p-[10px]" @click="() => handleRefresh()">
          <MsIcon type="icon-icon_reset_outlined" size="14" />
        </a-button>
      </div>
      <testPlanTable
        v-if="features.includes(FeatureEnum.TEST_PLAN)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_follow"
      />
      <testCaseTable
        v-if="features.includes(FeatureEnum.TEST_CASE)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_follow"
      />
      <caseReviewTable
        v-if="features.includes(FeatureEnum.CASE_REVIEW)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_follow"
      />
      <apiCaseTable
        v-if="features.includes(FeatureEnum.API_CASE)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_follow"
      />
      <scenarioCaseTable
        v-if="features.includes(FeatureEnum.API_SCENARIO)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_follow"
      />
      <bugTable
        v-if="features.includes(FeatureEnum.BUG)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_follow"
      />
    </template>
    <NoData v-else all-screen />
  </div>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsProjectSelect from '@/components/business/ms-project-select/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import apiCaseTable from '../components/apiCaseTable.vue';
  import bugTable from '../components/bugTable.vue';
  import caseReviewTable from '../components/caseReviewTable.vue';
  import NoData from '../components/notData.vue';
  import scenarioCaseTable from '../components/scenarioCaseTable.vue';
  import testCaseTable from '../components/testCaseTable.vue';
  import testPlanTable from '../components/testPlanTable.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { ProjectListItem } from '@/models/setting/project';
  import { FeatureEnum } from '@/enums/workbenchEnum';

  import { featuresMap } from '../components/config';

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentProject = ref(appStore.currentProjectId);
  const features = ref<FeatureEnum[]>([]);
  const fullFeaturesOptions = Object.keys(FeatureEnum).map((key) => ({
    label: t(`ms.workbench.myFollowed.feature.${key}`),
    value: key as FeatureEnum,
  }));
  const featureOptions = ref<SelectOptionData[]>([]);
  const refreshId = ref('');
  const projectSelectRef = ref<InstanceType<typeof MsProjectSelect>>();

  async function handleProjectSelect(val?: string, _project?: ProjectListItem) {
    if (_project) {
      const _currentProjectFeatures = JSON.parse(_project.moduleSetting);
      featureOptions.value = fullFeaturesOptions.filter((item) =>
        _currentProjectFeatures.includes(featuresMap[item.value])
      );
      features.value = featureOptions.value.map((item) => item.value as FeatureEnum);
    }
    refreshId.value = getGenerateId();
  }

  function handleRefresh() {
    projectSelectRef.value?.init();
  }
</script>

<style lang="less" scoped></style>
