<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.waitForReview') }} </div>
      <div>
        <MsSelect
          v-model:model-value="projectIds"
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

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { SelectOptionData } from '@arco-design/web-vue';

  const appStore = useAppStore();

  const { t } = useI18n();
  const projectIds = ref('');
  const projectOptions = ref<SelectOptionData[]>([]);
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
      slotName: 'reviewName',
      dataIndex: 'reviewName',
      width: 200,
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
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(undefined, {
    columns,
    scroll: { x: '100%' },
    selectable: false,
    heightUsed: 272,
    showSelectAll: false,
  });

  onMounted(() => {
    setLoadListParams({});
    loadList();
  });
</script>

<style scoped></style>
