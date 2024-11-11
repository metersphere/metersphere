<template>
  <div class="flex flex-col gap-[16px]">
    <template v-if="appStore.projectList.length > 0">
      <div class="flex items-center justify-end gap-[12px]">
        <MsProjectSelect
          v-model:project="currentProject"
          class="w-[240px]"
          use-default-arrow-icon
          @change="handleRefresh"
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
        <a-button type="outline" class="arco-btn-outline--secondary p-[10px]" @click="handleRefresh">
          <MsIcon type="icon-icon_reset_outlined" size="14" />
        </a-button>
      </div>

      <testPlanTable
        v-if="features.includes(FeatureEnum.TEST_PLAN)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_create"
      />
      <testCaseTable
        v-if="features.includes(FeatureEnum.TEST_CASE)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_create"
      />
      <caseReviewTable
        v-if="features.includes(FeatureEnum.CASE_REVIEW)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_create"
      />
      <apiCaseTable
        v-if="features.includes(FeatureEnum.API_CASE)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_create"
      />
      <scenarioCaseTable
        v-if="features.includes(FeatureEnum.API_SCENARIO)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_create"
      />
      <bugTable
        v-if="features.includes(FeatureEnum.BUG)"
        :project="currentProject"
        :refresh-id="refreshId"
        type="my_create"
      />
    </template>
    <NoData v-else all-screen />
  </div>
</template>

<script setup lang="ts">
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

  import { FeatureEnum } from '@/enums/workbenchEnum';

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentProject = ref(appStore.currentProjectId);
  const features = ref<FeatureEnum[]>(Object.values(FeatureEnum));
  const featureOptions = Object.keys(FeatureEnum).map((key) => ({
    label: t(`ms.workbench.myFollowed.feature.${key}`),
    value: key as FeatureEnum,
  }));
  const refreshId = ref('');

  function handleRefresh() {
    refreshId.value = getGenerateId();
  }
</script>

<style lang="less" scoped></style>
