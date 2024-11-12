<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t(props.item.label) }} </div>
      <div class="flex items-center gap-[8px]">
        <MsSelect
          v-model:model-value="projectId"
          :options="appStore.projectList"
          allow-clear
          allow-search
          value-key="id"
          label-key="name"
          :search-keys="['name']"
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
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

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { commonColorConfig, getCommonBarOptions } from '../utils';
  import type { SelectOptionData } from '@arco-design/web-vue';

  const { t } = useI18n();
  const appStore = useAppStore();
  const props = defineProps<{
    item: SelectedCardItem;
  }>();

  const memberIds = ref('');
  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = computed<string>({
    get: () => {
      const [newProject] = innerProjectIds.value;
      return newProject;
    },
    set: (val) => val,
  });

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
  const members = computed(() => ['张三', '李四', '王五', '小王']);
  const hasRoom = computed(() => members.value.length >= 7);
  const seriesData = ref<Record<string, any>[]>([
    {
      name: '新创建',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [400, 200, 400, 200, 400, 200],
    },
    {
      name: '激活',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
    {
      name: '处理中',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
    {
      name: '已关闭',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
    {
      name: '新创建1',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [400, 200, 400, 200, 400, 200],
    },
    {
      name: '激活1',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
    {
      name: '处理中1',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
    {
      name: '已关闭1',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
    {
      name: '已关闭2',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        // borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
    {
      name: '已关闭3',
      type: 'bar',
      barWidth: 12,
      stack: 'bug',
      itemStyle: {
        borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
  ]);
  const defectStatusColor = ['#811FA3', '#FFA200', '#3370FF', '#F24F4F'];

  function getDefectMemberDetail() {
    options.value = getCommonBarOptions(hasRoom.value, [...defectStatusColor, ...commonColorConfig]);
    options.value.xAxis.data = members.value;
    options.value.series = seriesData.value;
  }

  onMounted(() => {
    getDefectMemberDetail();
  });

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
        getDefectMemberDetail();
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
</script>

<style scoped></style>
