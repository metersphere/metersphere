<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t(props.item.label) }} </div>
      <div class="flex items-center gap-[8px]">
        <MsSelect
          v-model:model-value="projectId"
          :options="appStore.projectList"
          allow-search
          value-key="id"
          label-key="name"
          :search-keys="['name']"
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
          @change="changeProject"
        >
        </MsSelect>
        <MsSelect
          v-model:model-value="memberIds"
          :options="memberOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('workbench.homePage.staff')"
          :multiple="true"
          :has-all-select="true"
          :default-all-select="true"
          @change="changeMember"
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <MsChart height="260px" :options="options" />
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 缺陷处理人数量
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { workBugHandlerDetail, workHandleUserOptions } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';

  import type { OverViewOfProject, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { getColorScheme, getCommonBarOptions, handleNoDataDisplay } from '../utils';
  import type { SelectOptionData } from '@arco-design/web-vue';

  const { t } = useI18n();
  const appStore = useAppStore();
  const props = defineProps<{
    item: SelectedCardItem;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const innerHandleUsers = defineModel<string[]>('handleUsers', {
    required: true,
  });

  const memberIds = ref<string[]>(innerHandleUsers.value);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const memberOptions = ref<SelectOptionData[]>([]);

  const options = ref<Record<string, any>>({});

  const defectStatusColor = ['#811FA3', '#FFA200', '#3370FF', '#F24F4F'];
  const hasPermission = ref<boolean>(false);

  function handleData(detail: OverViewOfProject) {
    options.value = getCommonBarOptions(detail.xaxis.length >= 7, [
      ...defectStatusColor,
      ...getColorScheme(detail.xaxis.length),
    ]);
    const { invisible, text } = handleNoDataDisplay(detail.xaxis, hasPermission.value);
    options.value.graphic.invisible = invisible;
    options.value.graphic.style.text = text;
    options.value.xAxis.data = detail.xaxis.map((e) => characterLimit(e, 10));

    let maxAxis = 5;
    options.value.series = detail.projectCountList.map((item) => {
      const countData: Record<string, any>[] = item.count.map((e) => {
        return {
          name: item.name,
          value: e,
          originValue: e,
        };
      });

      const itemMax = Math.max(...item.count);

      maxAxis = Math.max(itemMax, maxAxis);
      return {
        name: item.name,
        type: 'bar',
        stack: 'bugMember',
        barWidth: 12,
        data: countData,
        itemStyle: {
          borderRadius: [2, 2, 0, 0],
        },
        barMinHeight: ((optionData: Record<string, any>[]) => {
          optionData.forEach((itemValue: any, index: number) => {
            if (itemValue.value === 0) optionData[index].value = null;
          });
          let hasZero = false;
          for (let i = 0; i < optionData.length; i++) {
            if (optionData[i].value === 0) {
              hasZero = true;
              break;
            }
          }
          return hasZero ? 0 : 5;
        })(countData),
      };
    });
    options.value.yAxis[0].max = maxAxis < 100 ? 50 : maxAxis + 50;
  }

  async function getDefectMemberDetail() {
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      const detail = await workBugHandlerDetail({
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: innerHandleUsers.value,
      });
      hasPermission.value = detail.errorCode !== 109001;

      handleData(detail);
    } catch (error) {
      console.log(error);
    }
  }

  async function getMemberOptions() {
    const [newProjectId] = innerProjectIds.value;
    const res = await workHandleUserOptions(newProjectId);
    memberOptions.value = res.map((e: any) => ({
      label: e.text,
      value: e.value,
    }));
  }

  function changeProject() {
    memberIds.value = [];
    nextTick(() => {
      getMemberOptions();
      getDefectMemberDetail();
      emit('change');
    });
  }

  function changeMember() {
    nextTick(() => {
      getDefectMemberDetail();
      emit('change');
    });
  }

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
    () => memberIds.value,
    (val) => {
      if (val) {
        innerHandleUsers.value = val;
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        getDefectMemberDetail();
      }
    },
    {
      deep: true,
    }
  );

  onMounted(() => {
    getMemberOptions();
    getDefectMemberDetail();
  });
</script>

<style scoped lang="less">
  :deep(.arco-select-view-multiple.arco-select-view-size-medium .arco-select-view-tag) {
    margin-top: 1px;
    margin-bottom: 1px;
    max-width: 80px;
    height: auto;
    min-height: 24px;
    line-height: 22px;
    vertical-align: middle;
  }
</style>
