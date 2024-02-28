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
        @search="searchCase"
        @press-enter="searchCase"
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
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex w-full items-center justify-center">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <a-dropdown @select="handleSelect">
            <MsButton class="ml-[8px]">
              {{ t('caseManagement.featureCase.linkCase') }}
            </MsButton>
            <template #content>
              <a-doption v-for="item of caseTypeOptions" :key="item.value" :value="item.value">{{
                t(item.label)
              }}</a-doption>
            </template>
          </a-dropdown>
        </div>
      </template>
    </ms-base-table>
    <MsCaseAssociate
      v-model:visible="innerVisible"
      v-model:project-id="innerProject"
      v-model:currentSelectCase="currentSelectCase"
      :ok-button-disabled="associateForm.reviewers.length === 0"
      :get-modules-func="getModuleTree"
      :modules-params="modulesTreeParams"
      :get-table-func="getUnAssociatedList"
      :table-params="getTableParams"
      :modules-count="modulesCount"
      :module-options="caseTypeOptions"
      :confirm-loading="confirmLoading"
      :case-id="props.bugId"
      :associated-ids="associatedIds"
      :type="RequestModuleEnum.CASE_MANAGEMENT"
      @close="emit('close')"
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
  import { RequestModuleEnum } from '@/components/business/ms-case-associate/utils';

  import {
    batchAssociation,
    cancelAssociation,
    getAssociatedList,
    getModuleTree,
    getUnAssociatedList,
  } from '@/api/modules/bug-management';
  import { postTabletList } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import type { TableQueryParams } from '@/models/common';

  import Message from '@arco-design/web-vue/es/message';

  const appStore = useAppStore();
  const featureCaseStore = useFeatureCaseStore();

  const { t } = useI18n();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    bugId: string; // 缺陷id
  }>();

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

  const { propsRes, propsEvent, loadList, setLoadListParams, setKeyword } = useTable(getAssociatedList, {
    columns,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });

  const innerVisible = ref(false);
  const innerProject = ref(currentProjectId.value);

  const associateForm = ref({
    reviewers: [],
  });

  const associatedIds = ref<string[]>([]);

  const currentSelectCase = ref<string>('');

  const modulesTreeParams = ref<TableQueryParams>({});

  const getTableParams = ref<TableQueryParams>({});

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    currentSelectCase.value = value as string;
    innerVisible.value = true;
  }

  async function getFetch() {
    setLoadListParams({
      keyword: keyword.value,
      bugId: props.bugId,
    });
    await loadList();
    const { msPagination } = propsRes.value;
    featureCaseStore.setListCount(featureCaseStore.activeTab, msPagination?.total || 0);
  }

  async function cancelLink(record: any) {
    try {
      const { id } = record;
      await cancelAssociation(id);
    } catch (error) {
      console.log(error);
    }
  }

  const caseTypeOptions = ref<{ label: string; value: string }[]>([]);

  const modulesCount = ref<Record<string, any>>({});

  const confirmLoading = ref<boolean>(false);

  async function saveHandler(params: TableQueryParams) {
    try {
      confirmLoading.value = true;
      await batchAssociation(params);
      Message.success(t('caseManagement.featureCase.AssociatedSuccess'));
      innerVisible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  const moduleMaps: Record<string, { label: string; value: string }[]> = {
    caseManagement: [
      {
        value: 'FUNCTIONAL',
        label: t('menu.caseManagement.featureCase'),
      },
    ],
  };

  async function getEnabledModules() {
    const result = await postTabletList({ projectId: currentProjectId.value });
    debugger;
    const caseArr = result.filter((item) => Object.keys(moduleMaps).includes(item.module));
    caseArr.forEach((item: any) => {
      const currentModule = moduleMaps[item.module];
      caseTypeOptions.value.push(...currentModule);
    });
    currentSelectCase.value = caseTypeOptions.value[0].value;
  }

  async function searchCase() {
    setKeyword(keyword.value);
    await loadList();
  }

  onMounted(async () => {
    getEnabledModules();
    getFetch();
  });
</script>

<style scoped></style>
