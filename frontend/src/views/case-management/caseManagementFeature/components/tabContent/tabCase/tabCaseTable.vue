<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <a-dropdown @select="handleSelect">
        <a-button v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" type="primary">
          {{ t('caseManagement.featureCase.linkCase') }}
        </a-button>
        <template #content>
          <a-doption v-for="item of caseTypeOptions" :key="item.value" :value="item.value">{{
            t(item.label)
          }}</a-doption>
        </template>
      </a-dropdown>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByName')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchCase"
        @press-enter="searchCase"
        @clear="searchCase"
      ></a-input-search>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #operation="{ record }">
        <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" @click="cancelLink(record)">{{
          t('caseManagement.featureCase.cancelLink')
        }}</MsButton>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
          <span v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE'])">{{
            t('caseManagement.caseReview.tableNoData')
          }}</span>
          <span v-else>{{ t('caseManagement.featureCase.tableNoData') }}</span>

          <a-dropdown @select="handleSelect">
            <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" class="ml-[8px]">
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
      <template #sourceType="{ record }">
        {{ caseTypeOptions.find((e) => e.value === record.sourceType)?.label }}
      </template>
    </ms-base-table>
    <!-- TODO: 涉及接口调整放到下一个版本再替换暂时还是使用原来的 -->
    <MsCaseAssociate
      v-model:visible="innerVisible"
      :current-select-case="currentSelectCase"
      :ok-button-disabled="associateForm.reviewers.length === 0"
      :get-modules-func="getPublicLinkModuleTree"
      :modules-params="modulesTreeParams"
      :get-table-func="getPublicLinkCaseList"
      :table-params="getTableParams"
      :modules-count="modulesCount"
      :module-options="caseTypeOptions"
      :confirm-loading="confirmLoading"
      :case-id="props.caseId"
      :associated-ids="associatedIds"
      :type="RequestModuleEnum.API_CASE"
      :is-hidden-case-level="true"
      @close="emit('close')"
      @save="saveHandler"
    >
    </MsCaseAssociate>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsCaseAssociate from '@/components/business/ms-case-associate/index.vue';
  import { RequestModuleEnum } from '@/components/business/ms-case-associate/utils';

  import {
    associationPublicCase,
    cancelAssociatedCase,
    getAssociatedCasePage,
    getPublicLinkCaseList,
    getPublicLinkModuleTree,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { hasAnyPermission } from '@/utils/permission';

  import type { TableQueryParams } from '@/models/common';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import Message from '@arco-design/web-vue/es/message';

  const appStore = useAppStore();
  const featureCaseStore = useFeatureCaseStore();

  const { t } = useI18n();

  const currentProjectId = computed(() => appStore.currentProjectId);
  const props = defineProps<{
    caseId: string; // 用例id
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
      dataIndex: 'sourceNum',
      slotName: 'sourceNum',
      width: 200,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'sourceName',
      dataIndex: 'sourceName',
      showInTable: true,
      showTooltip: true,
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.projectName',
      slotName: 'projectName',
      dataIndex: 'projectName',
      showInTable: true,
      showTooltip: true,
      width: 300,
    },
    // {
    //   title: 'caseManagement.featureCase.tableColumnVersion',
    //   slotName: 'versionName',
    //   dataIndex: 'versionName',
    //   showInTable: true,
    //   showTooltip: true,
    //   width: 300,
    // },
    {
      title: 'caseManagement.featureCase.changeType',
      slotName: 'sourceType',
      dataIndex: 'sourceType',
      showInTable: true,
      showTooltip: true,
      width: 300,
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

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getAssociatedCasePage, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEPENDENCY_PRE_CASE,
    scroll: { x: '100%' },
    showSelectorAll: false,
    heightUsed: 340,
  });

  const innerVisible = ref(false);

  const associateForm = ref({
    reviewers: [],
  });

  const associatedIds = ref<string[]>([]);

  const currentSelectCase = ref<CaseLinkEnum>(CaseLinkEnum.API);

  const modulesTreeParams = ref<TableQueryParams>({});

  const getTableParams = ref<TableQueryParams>({});

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    currentSelectCase.value = value as CaseLinkEnum;
    innerVisible.value = true;
  }

  const modulesCount = ref<Record<string, any>>({});

  const confirmLoading = ref<boolean>(false);

  async function getFetch() {
    setLoadListParams({
      keyword: keyword.value,
      sourceId: props.caseId,
      // projectId: currentProjectId.value, // 加了项目筛选再打开，目前先关闭
      // sourceType: currentSelectCase.value, // 加了类型筛选再打开， 目前先关闭
      condition: {
        keyword: keyword.value,
      },
    });
    await loadList();
    featureCaseStore.getCaseCounts(props.caseId);
  }

  async function saveHandler(params: TableQueryParams) {
    try {
      confirmLoading.value = true;
      await associationPublicCase(params);
      Message.success(t('caseManagement.featureCase.AssociatedSuccess'));
      innerVisible.value = false;
      getFetch();
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  // @desc 这个模块不使用动态获取模块需求调整目前这些菜单也写死
  const caseTypeOptions = ref<{ label: string; value: keyof typeof CaseLinkEnum }[]>([
    {
      value: 'API',
      label: t('caseManagement.featureCase.apiCase'),
    },
    {
      value: 'SCENARIO',
      label: t('caseManagement.featureCase.sceneCase'),
    },
    // TODO 这个版本不显示
    // {
    //   value: 'UI',
    //   label: t('caseManagement.featureCase.uiCase'),
    // },
    // {
    //   value: 'PERFORMANCE',
    //   label: t('caseManagement.featureCase.propertyCase'),
    // },
  ]);

  async function cancelLink(record: any) {
    try {
      await cancelAssociatedCase({
        selectIds: [record.id],
        caseId: props.caseId,
        sourceType: record.sourceType,
      });
      getFetch();
      Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  const searchCase = debounce(() => {
    getFetch();
  }, 100);

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        getFetch();
      }
    }
  );

  onMounted(() => {
    getFetch();
  });
</script>

<style scoped></style>
