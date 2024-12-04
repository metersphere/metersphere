<template>
  <div class="card-wrapper card-min-height">
    <CardSkeleton v-if="showSkeleton" :show-line-number="2" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div>
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
        </div>
      </div>
      <div class="mt-[16px]">
        <MsBaseTable
          v-bind="propsRes"
          :action-config="{
            baseAction: [],
            moreAction: [],
          }"
          class="mt-[16px]"
          v-on="propsEvent"
        >
          <template #num="{ record }">
            <MsButton type="text" @click="openDetail(record)">{{ record.num || '-' }}</MsButton>
          </template>
          <template v-if="isNoPermission" #empty>
            <div class="w-full">
              <slot name="empty">
                <div class="flex h-[40px] flex-col items-center justify-center">
                  <span class="text-[14px] text-[var(--color-text-4)]">{{ t('common.noResource') }}</span>
                </div>
              </slot>
            </div>
          </template>
        </MsBaseTable>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口变更列表
   */
  import { ref } from 'vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';

  import { workApiChangeList } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import type { ApiDefinitionDetail } from '@/models/apiTest/management';
  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const { openNewPage } = useOpenNewPage();

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

  const projectId = ref<string>(innerProjectIds.value[0]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );
  const columns: MsTableColumn = [
    {
      title: 'ID',
      slotName: 'num',
      dataIndex: 'num',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'project.commonScript.apiName',
      slotName: 'name',
      dataIndex: 'name',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'workbench.homePage.associationCASE',
      slotName: 'caseTotal',
      dataIndex: 'caseTotal',
      width: 200,
    },
    {
      title: 'workbench.homePage.associatedScene',
      slotName: 'scenarioTotal',
      dataIndex: 'scenarioTotal',
      showDrag: true,
      width: 100,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
      width: 189,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    workApiChangeList,
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 272,
      showSelectAll: false,
      validatePermission: true,
    },
    (item) => ({
      ...item,
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  // 跳转详情
  function openDetail(record: ApiDefinitionDetail) {
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
      dId: record.id,
      pId: projectId.value,
    });
  }

  const isNoPermission = ref<boolean>(false);
  const showSkeleton = ref(false);
  async function initData() {
    showSkeleton.value = true;
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      setLoadListParams({
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: [],
      });
      await loadList();
      isNoPermission.value = false;
    } catch (error) {
      isNoPermission.value = error === 'no_project_permission';
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
    initData();
  });

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
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initData();
      }
    },
    {
      deep: true,
    }
  );

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    initData();
  });
</script>

<style scoped></style>
