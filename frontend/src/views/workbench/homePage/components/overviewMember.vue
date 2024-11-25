<template>
  <div class="card-wrapper">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div class="flex items-center gap-[8px]">
          <MsSelect
            v-model:model-value="projectId"
            :options="appStore.projectList"
            allow-search
            value-key="id"
            label-key="name"
            :search-keys="['name']"
            class="!w-[200px]"
            :prefix="t('workbench.homePage.project')"
            @change="changeProject"
          >
          </MsSelect>

          <MsSelect
            :key="props.refreshKey"
            v-model:model-value="innerHandleUsers"
            :options="memberOptions"
            allow-search
            allow-clear
            value-key="value"
            label-key="label"
            :search-keys="['label']"
            class="!w-[220px]"
            :prefix="t('workbench.homePage.staff')"
            :multiple="true"
            :has-all-select="true"
            :default-all-select="innerHandleUsers.length === 0"
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
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 人员概览
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';

  import { workMemberViewDetail, workProjectMemberOptions } from '@/api/modules/workbench';
  import { contentTabList } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { characterLimit, sleep } from '@/utils';

  import type { OverViewOfProject, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { getCommonBarOptions, handleNoDataDisplay } from '../utils';

  const { t } = useI18n();
  const appStore = useAppStore();
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

  const innerHandleUsers = defineModel<string[]>('handleUsers', {
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
  const hasPermission = ref<boolean>(false);

  const memberOptions = ref<{ label: string; value: string }[]>([]);
  const options = ref<Record<string, any>>({});
  const color = ['#811FA3', '#FFCA59', '#00C261', '#FFA1FF', '#F9F871', '#3370FF', '#F24F4F'];

  function handleData(detail: OverViewOfProject) {
    options.value = getCommonBarOptions(detail.xaxis.length >= 7, color);
    const { invisible, text } = handleNoDataDisplay(detail.xaxis, hasPermission.value);
    options.value.graphic.invisible = invisible;
    options.value.graphic.style.text = text;
    options.value.xAxis.data = detail.xaxis.map((e) => characterLimit(e, 10));

    let maxAxis = 5;

    // 处理data数据
    options.value.series = detail.projectCountList.map((item, sid) => {
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
        name: t(contentTabList[sid].label),
        type: 'bar',
        barWidth: 12,
        legendHoverLink: true,
        large: true,
        itemStyle: {
          borderRadius: [2, 2, 0, 0],
        },
        data: countData,
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
    options.value.yAxis[0].max = maxAxis < 100 ? 100 : maxAxis + 50;
  }
  const showSkeleton = ref(false);

  async function initOverViewMemberDetail() {
    try {
      showSkeleton.value = true;

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
      hasPermission.value = detail.errorCode !== 109001;
      handleData(detail);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  async function getMemberOptions() {
    const [newProjectId] = innerProjectIds.value;
    const res = await workProjectMemberOptions(newProjectId);
    memberOptions.value = res.map((e: any) => ({
      label: e.name,
      value: e.id,
    }));
  }
  const isInit = ref(true);

  function changeProject() {
    innerHandleUsers.value = [];
    nextTick(() => {
      getMemberOptions();
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
    () => props.item.projectIds,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
      }
    },
    { immediate: true }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
      }
    },
    { immediate: true }
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
    isInit.value = false;
    initOverViewMemberDetail();
    if (props.item.projectIds.length) {
      getMemberOptions();
    }
  });

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    initOverViewMemberDetail();
    await sleep(50);
    if (props.item.projectIds.length) {
      getMemberOptions();
    }
  });
</script>

<style scoped lang="less"></style>
