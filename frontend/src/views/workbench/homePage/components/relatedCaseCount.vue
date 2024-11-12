<template>
  <div :class="`card-wrapper ${props.item.fullScreen ? '' : 'card-min-height'}`">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.useCasesCount') }} </div>
      <div>
        <MsSelect
          v-model:model-value="projectId"
          :options="appStore.projectList"
          value-key="id"
          label-key="name"
          :search-keys="['name']"
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <div class="case-count-wrapper">
        <div class="case-count-item">
          <PassRatePie
            :options="options"
            tooltip-text="workbench.homePage.associateCaseCoverRateTooltip"
            :size="60"
            :value-list="coverRateValueList"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 关联用例数量
   */
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';

  import { workAssociateCaseDetail } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { commonRatePieOptions } from '../utils';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const options = ref<Record<string, any>>(cloneDeep(commonRatePieOptions));

  const coverRateValueList = ref<{ value: number; label: string; name: string }[]>([]);

  async function getRelatedCaseCount() {
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      const detail = await workAssociateCaseDetail({
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: [],
      });
      const { cover } = detail.statusStatisticsMap;
      coverRateValueList.value = cover.slice(1).map((item) => {
        return {
          value: item.count,
          label: item.name,
          name: item.name,
        };
      });
      options.value.series.data = coverRateValueList.value;
      options.value.title.text = cover[0].name ?? '';
      options.value.title.subtext = `${cover[0].count ?? 0}%`;
      options.value.series.color = ['#00C261', '#D4D4D8'];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onMounted(() => {
    getRelatedCaseCount();
  });

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
      }
    }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
        getRelatedCaseCount();
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        getRelatedCaseCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less"></style>
