<template>
  <div :class="`card-wrapper ${props.item.fullScreen ? '' : 'card-min-height'}`">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div>
          <MsSelect
            v-model:model-value="projectId"
            :options="appStore.projectList"
            value-key="id"
            label-key="name"
            :search-keys="['name']"
            class="!w-[200px]"
            :prefix="t('workbench.homePage.project')"
            @change="changeProject"
          >
          </MsSelect>
        </div>
      </div>
      <div class="mt-[16px]">
        <div class="case-count-wrapper">
          <div class="case-count-item">
            <PassRatePie
              :project-id="projectId"
              :options="relatedOptions"
              :has-permission="hasPermission"
              tooltip-text="workbench.homePage.associateCaseCoverRateTooltip"
              :value-list="coverRateValueList"
            />
          </div>
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

  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import PassRatePie from './passRatePie.vue';

  import { workAssociateCaseDetail } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { PassRateDataType, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { handleUpdateTabPie } from '../utils';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
    refreshKey: number;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
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

  const relatedOptions = ref<Record<string, any>>({});
  const hasPermission = ref<boolean>(false);

  const coverRateValueList = ref<{ value: number | string; label: string; name: string }[]>([
    {
      label: t('workbench.homePage.covered'),
      value: '-',
      name: '',
    },
    {
      label: t('workbench.homePage.notCover'),
      value: '-',
      name: '',
    },
  ]);

  const showSkeleton = ref(false);

  async function getRelatedCaseCount() {
    try {
      showSkeleton.value = true;
      const { startTime, endTime, dayNumber } = timeForm.value;
      const detail: PassRateDataType = await workAssociateCaseDetail({
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: [],
      });

      hasPermission.value = detail.errorCode !== 109001;

      const { statusStatisticsMap } = detail;

      const { options, valueList } = handleUpdateTabPie(
        statusStatisticsMap?.cover || [],
        hasPermission.value,
        `${props.item.key}-cover`
      );
      relatedOptions.value = options;
      coverRateValueList.value = valueList;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  function changeProject() {
    nextTick(() => {
      emit('change');
    });
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

  watch([() => props.refreshKey, () => projectId.value], async ([refreshKey]) => {
    if (refreshKey) {
      await nextTick();
      getRelatedCaseCount();
    }
  });
</script>

<style scoped lang="less"></style>
