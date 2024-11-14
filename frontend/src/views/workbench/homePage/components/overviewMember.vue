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
          allow-search
          allow-clear
          value-key="value"
          label-key="label"
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
    <!-- 概览图 -->
    <div class="mt-[16px]">
      <MsChart height="300px" :options="options" />
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 人员概览
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { getProjectOptions } from '@/api/modules/project-management/projectMember';
  import { workMemberViewDetail } from '@/api/modules/workbench';
  import { contentTabList } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';

  import type { OverViewOfProject, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { commonColorConfig, getCommonBarOptions, handleNoDataDisplay } from '../utils';

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

  const innerHandleUsers = defineModel<string[]>('handleUsers', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const memberIds = ref<string[]>(innerHandleUsers.value);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const memberOptions = ref<{ label: string; value: string }[]>([]);
  const options = ref<Record<string, any>>({});
  function handleData(detail: OverViewOfProject) {
    options.value = getCommonBarOptions(detail.xaxis.length >= 7, commonColorConfig);
    const { invisible, text } = handleNoDataDisplay(detail.xaxis, detail.projectCountList);
    options.value.graphic.invisible = invisible;
    options.value.graphic.style.text = text;
    options.value.xAxis.data = detail.xaxis.map((e) => characterLimit(e, 10));
    options.value.series = detail.projectCountList.map((item, index) => {
      return {
        name: t(contentTabList[index].label),
        type: 'bar',
        stack: 'member',
        barWidth: 12,
        data: item.count,
        itemStyle: {
          borderRadius: [2, 2, 0, 0],
        },
      };
    });
  }

  async function initOverViewMemberDetail() {
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      const params = {
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: innerHandleUsers.value,
      };
      const detail = await workMemberViewDetail(params);
      handleData(detail);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function getMemberOptions() {
    const [newProjectId] = innerProjectIds.value;
    const res = await getProjectOptions(newProjectId);
    memberOptions.value = res.map((e: any) => ({
      label: e.name,
      value: e.id,
    }));
  }

  function changeProject() {
    memberIds.value = [];
    getMemberOptions();
    nextTick(() => {
      initOverViewMemberDetail();
      emit('change');
    });
  }

  function changeMember() {
    nextTick(() => {
      initOverViewMemberDetail();
      emit('change');
    });
  }

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
        memberIds.value = [];
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
        initOverViewMemberDetail();
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initOverViewMemberDetail();
      }
    },
    {
      deep: true,
    }
  );

  onMounted(() => {
    getMemberOptions();
    initOverViewMemberDetail();
  });
</script>

<style scoped lang="less"></style>
