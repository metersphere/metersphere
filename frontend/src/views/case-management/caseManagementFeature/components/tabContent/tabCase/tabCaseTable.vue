<template>
  <div>
    <div class="flex items-center justify-between">
      <a-dropdown @select="handleSelect">
        <a-button type="primary"> {{ t('caseManagement.featureCase.linkCase') }} </a-button>
        <template #content>
          <a-doption v-for="item of caseTypeOptions" :key="item.value" :value="item.value">{{
            t(item.label)
          }}</a-doption>
        </template>
      </a-dropdown>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
      ></a-input-search>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #defectName="{ record }">
        <span class="one-line-text max-w[300px]"> {{ record.name }}</span
        ><span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
      </template>
      <template #operation="{ record }">
        <MsButton @click="cancelLink(record)">{{ t('caseManagement.featureCase.cancelLink') }}</MsButton>
      </template>
    </ms-base-table>
    <MsCaseAssociate
      v-model:visible="innerVisible"
      v-model:project="innerProject"
      v-model:currentSelectCase="currentSelectCase"
      :ok-button-disabled="associateForm.reviewers.length === 0"
      :get-modules-func="getCaseModuleTree"
      :get-table-func="getCaseList"
      :modules-count="modulesCount"
      :module-options="caseTypeOptions"
      :confirm-loading="confirmLoading"
      :associated-ids="associatedIds"
      @close="emit('close')"
      @init="getModuleCount"
      @save="saveHandler"
    >
    </MsCaseAssociate>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsCaseAssociate from '@/components/business/ms-case-associate/index.vue';

  import { getAssociatedIds } from '@/api/modules/case-management/caseReview';
  import {
    getCaseList,
    getCaseModulesCounts,
    getCaseModuleTree,
    getRecycleListRequest,
  } from '@/api/modules/case-management/featureCase';
  import { postTabletList } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { CaseModuleQueryParams } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const appStore = useAppStore();

  const { t } = useI18n();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:project', val: string): void;
    (e: 'success', val: string[]): void;
    (e: 'close'): void;
  }>();
  const keyword = ref<string>('');

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'id',
      width: 200,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.projectName',
      slotName: 'projectName',
      dataIndex: 'projectName',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnVersion',
      slotName: 'version',
      dataIndex: 'version',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.changeType',
      slotName: 'type',
      dataIndex: 'type',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getRecycleListRequest, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEPENDENCY_PRE_CASE,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });

  const innerVisible = ref(false);
  const innerProject = ref('');

  const associateForm = ref({
    reviewers: [],
  });

  const associatedIds = ref<string[]>([]);

  async function getLinkedIds() {
    // try {
    //   associatedIds.value = await getAssociatedIds('1111');
    // } catch (error) {
    //   console.log(error);
    // }
  }

  const currentSelectCase = ref<string | number | Record<string, any> | undefined>('');
  function handleSelect(value: string | number | Record<string, any> | undefined) {
    currentSelectCase.value = value;
    innerVisible.value = true;
    getLinkedIds();
  }

  function cancelLink(record: any) {}

  const caseTypeOptions = ref<{ label: string; value: string }[]>([]);

  const modulesCount = ref<Record<string, any>>({});

  async function getModuleCount(params: CaseModuleQueryParams) {
    try {
      modulesCount.value = await getCaseModulesCounts(params);
    } catch (error) {
      console.log(error);
    }
  }

  const confirmLoading = ref<boolean>(false);

  function saveHandler(params: TableQueryParams) {}

  const moduleMaps: Record<string, { label: string; value: string }[]> = {
    apiTest: [
      {
        value: 'API_CASE',
        label: t('caseManagement.featureCase.apiCase'),
      },
      {
        value: 'SCENE_CASE',
        label: t('caseManagement.featureCase.sceneCase'),
      },
    ],
    uiTest: [
      {
        value: 'UI_CASE',
        label: t('caseManagement.featureCase.uiCase'),
      },
    ],
    loadTest: [
      {
        value: 'LOAD_CASE',
        label: t('caseManagement.featureCase.propertyCase'),
      },
    ],
  };

  onBeforeMount(async () => {
    const result = await postTabletList({ projectId: currentProjectId.value });
    const caseArr = result.filter((item) => Object.keys(moduleMaps).includes(item.module));
    caseArr.forEach((item: any) => {
      const currentModule = moduleMaps[item.module];
      caseTypeOptions.value.push(...currentModule);
    });
  });
</script>

<style scoped></style>
