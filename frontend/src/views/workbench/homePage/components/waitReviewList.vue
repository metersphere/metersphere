<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t(props.item.label) }} </div>
      <div>
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
      </div>
    </div>
    <div>
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
          <a-tooltip :content="`${record.num}`">
            <a-button type="text" class="px-0 !text-[14px] !leading-[22px]" @click="openDetail(record.id)">
              <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
            </a-button>
          </a-tooltip>
        </template>
        <template #passRateColumn>
          <div class="flex items-center text-[var(--color-text-3)]">
            {{ t('caseManagement.caseReview.passRate') }}
            <a-tooltip :content="t('caseManagement.caseReview.passRateTip')" position="right">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </template>
        <template #passRate="{ record }">
          <div class="mr-[8px] w-[100px]">
            <passRateLine :review-detail="record" height="5px" />
          </div>
          <div class="text-[var(--color-text-1)]">
            {{ `${record.passRate}%` }}
          </div>
        </template>
        <template #reviewPassRule="{ record }">
          <a-tag
            :color="record.reviewPassRule === 'SINGLE' ? 'rgb(var(--success-2))' : 'rgb(var(--link-2))'"
            :class="record.reviewPassRule === 'SINGLE' ? '!text-[rgb(var(--success-6))]' : '!text-[rgb(var(--link-6))]'"
          >
            {{
              record.reviewPassRule === 'SINGLE'
                ? t('caseManagement.caseReview.single')
                : t('caseManagement.caseReview.multi')
            }}
          </a-tag>
        </template>
        <template #createUserName="{ record }">
          <a-tooltip :content="`${record.createUserName}`" position="tl">
            <div class="one-line-text">{{ record.createUserName }}</div>
          </a-tooltip>
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
</template>

<script setup lang="ts">
  /** *
   * @desc 待我评审列表
   */
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsSelect from '@/components/business/ms-select';
  import passRateLine from '@/views/case-management/caseReview/components/passRateLine.vue';

  import { workReviewList } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  const { openNewPage } = useOpenNewPage();

  const appStore = useAppStore();

  const { t } = useI18n();

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
      title: 'caseManagement.caseReview.reviewName',
      slotName: 'name',
      dataIndex: 'name',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'caseManagement.caseReview.passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateColumn',
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.type',
      slotName: 'reviewPassRule',
      dataIndex: 'reviewPassRule',
      showDrag: true,
      width: 100,
    },
    {
      title: 'common.creator',
      slotName: 'createUserName',
      dataIndex: 'createUser',
      width: 200,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(workReviewList, {
    columns,
    scroll: { x: '100%' },
    selectable: false,
    heightUsed: 272,
    showSelectAll: false,
  });

  const isNoPermission = ref<boolean>(false);

  async function initData() {
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
    } catch (error) {
      isNoPermission.value = error === 'no_project_permission';
      // eslint-disable-next-line no-console
    }
  }

  function openDetail(id: number) {
    openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL, { id });
  }

  function changeProject() {
    nextTick(() => {
      initData();
      emit('change');
    });
  }

  onMounted(() => {
    initData();
  });

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
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
        initData();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped></style>
