<template>
  <div class="mb-4 flex items-center justify-between">
    <a-button type="primary" :disabled="!hasAnyPermission(['PROJECT_BUG:READ+UPDATE'])" @click="handleSelect">{{
      t('caseManagement.featureCase.linkCase')
    }}</a-button>
    <a-input-search
      v-model:model-value="keyword"
      :placeholder="t('caseManagement.featureCase.searchByIdAndName')"
      allow-clear
      class="mx-[8px] w-[240px]"
      @search="searchCase"
      @press-enter="searchCase"
      @clear="searchCase"
    ></a-input-search>
  </div>
  <ms-base-table v-bind="propsRes" v-on="propsEvent">
    <template #relateCaseNum="{ record }">
      <a-tooltip :content="`${record.relateCaseNum}`">
        <!-- TOTO 暂时没有用例id的字段 需要后台加caseId -->
        <a-button type="text" class="px-0" @click="openDetail(record.relateCaseId, record.projectId)">
          <div class="one-line-text max-w-[168px]">{{ record.relateCaseNum }}</div>
        </a-button>
      </a-tooltip>
    </template>
    <template #defectName="{ record }">
      <span class="one-line-text max-w[300px]"> {{ record.relateCaseName }}</span
      ><span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
    </template>
    <template #operation="{ record }">
      <MsButton :disabled="!hasAnyPermission(['PROJECT_BUG:READ+UPDATE'])" @click="cancelLink(record)">{{
        t('caseManagement.featureCase.cancelLink')
      }}</MsButton>
    </template>
    <template v-if="(keyword || '').trim() === ''" #empty>
      <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
        {{ t('caseManagement.caseReview.tableNoData') }}
        <MsButton class="ml-[8px]" :disabled="!hasAnyPermission(['PROJECT_BUG:READ+UPDATE'])" @click="handleSelect">
          {{ t('caseManagement.featureCase.linkCase') }}
        </MsButton>
      </div>
    </template>
    <template #relatePlanTitle>
      <div class="flex items-center">
        <div class="font-medium text-[var(--color-text-3)]">
          {{ t('bugManagement.detail.isPlanRelateCase') }}
        </div>
        <a-popover position="rt">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-3)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
          <template #title>
            <div class="w-[300px]"> {{ t('bugManagement.detail.isPlanRelateCaseTip1') }} </div>
            <br />
            <div class="w-[300px]"> {{ t('bugManagement.detail.isPlanRelateCaseTip2') }} </div>
            <br />
            <div class="w-[300px]"> {{ t('bugManagement.detail.isPlanRelateCaseTip3') }} </div>
          </template>
        </a-popover>
      </div>
    </template>
    <template #isRelatePlanCase="{ record }">
      <span class="text-[var(--color-text-1)]">{{ record.isRelatePlanCase ? t('common.yes') : t('common.no') }}</span>
    </template>
  </ms-base-table>
  <MsCaseAssociate
    v-model:visible="innerVisible"
    :current-select-case="currentSelectCase"
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
    :type="RequestModuleEnum.BUG_MANAGEMENT"
    :is-hidden-case-level="true"
    @close="emit('close')"
    @save="saveHandler"
  >
  </MsCaseAssociate>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsCaseAssociate from '@/components/business/ms-case-associate/index.vue';
  import { RequestModuleEnum } from '@/components/business/ms-case-associate/utils';

  import {
    batchAssociation,
    cancelAssociation,
    checkCasePermission,
    getAssociatedList,
    getModuleTree,
    getUnAssociatedList,
  } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { NO_RESOURCE_ROUTE_NAME } from '@/router/constants';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { hasAnyPermission } from '@/utils/permission';

  import type { TableQueryParams } from '@/models/common';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { RouteEnum } from '@/enums/routeEnum';

  import Message from '@arco-design/web-vue/es/message';

  const appStore = useAppStore();
  const featureCaseStore = useFeatureCaseStore();
  const router = useRouter();
  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    bugId?: string; // 缺陷id
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:project', val: string): void;
    (e: 'success', val: string[]): void;
    (e: 'close'): void;
    (e: 'updateCaseSuccess'): void;
  }>();
  const keyword = ref<string>('');

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'relateCaseNum',
      slotName: 'relateCaseNum',
      width: 200,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      dataIndex: 'relateCaseName',
      showInTable: true,
      showTooltip: true,
      width: 300,
      showDrag: false,
    },
    {
      title: 'bugManagement.project',
      slotName: 'projectName',
      dataIndex: 'projectName',
      showInTable: true,
      showTooltip: true,
      width: 300,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.changeType',
      dataIndex: 'relateCaseTypeName',
      showInTable: true,
      showTooltip: true,
      width: 300,
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
    heightUsed: 310,
  });

  const innerVisible = ref(false);

  const associateForm = ref({
    reviewers: [],
  });

  const associatedIds = ref<string[]>([]);

  const currentSelectCase = ref<CaseLinkEnum>(CaseLinkEnum.FUNCTIONAL);

  const modulesTreeParams = ref<TableQueryParams>({});

  const getTableParams = ref<TableQueryParams>({});

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    innerVisible.value = true;
  }

  async function getFetch() {
    setKeyword(keyword.value);
    setLoadListParams({
      bugId: props.bugId,
    });
    await loadList();
    const { msPagination } = propsRes.value;
    featureCaseStore.setListCount(featureCaseStore.activeTab, msPagination?.total || 0);
  }

  async function cancelLink(record: any) {
    try {
      const { relateId } = record;
      await cancelAssociation(relateId);
      await getFetch();
      emit('updateCaseSuccess');
      Message.success(t('common.unLinkSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  const caseTypeOptions = ref<{ label: string; value: string }[]>([
    {
      label: 'menu.caseManagement.featureCase',
      value: 'FUNCTIONAL',
    },
  ]);

  const modulesCount = ref<Record<string, any>>({});

  const confirmLoading = ref<boolean>(false);

  async function saveHandler(params: TableQueryParams) {
    try {
      confirmLoading.value = true;
      await batchAssociation(params);
      await getFetch();
      Message.success(t('common.linkSuccess'));
      emit('updateCaseSuccess');
      innerVisible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  async function searchCase() {
    setKeyword(keyword.value);
    setLoadListParams({
      bugId: props.bugId,
    });
    await loadList();
  }

  async function openDetail(id: string, projectId: string) {
    try {
      const res = await checkCasePermission(currentProjectId.value, 'FUNCTIONAL');
      if (res) {
        openNewPage(RouteEnum.CASE_MANAGEMENT_CASE, {
          pId: projectId,
          id,
        });
      } else {
        window.open(`${window.location.origin}#${router.resolve({ name: NO_RESOURCE_ROUTE_NAME }).fullPath}`);
      }
    } catch (error) {
      console.log(error);
    }
  }

  onMounted(async () => {
    getFetch();
  });

  watch(
    () => props.bugId,
    () => {
      getFetch();
    }
  );
</script>

<style scoped></style>
