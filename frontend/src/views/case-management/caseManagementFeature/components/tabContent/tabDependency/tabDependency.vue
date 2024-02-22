<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <a-button v-if="showType === 'preposition'" class="mr-3" type="primary" @click="addCase">
          {{ t('caseManagement.featureCase.addPresetCase') }}
        </a-button>
        <a-button v-else type="primary" @click="addCase">
          {{ t('caseManagement.featureCase.addPostCase') }}
        </a-button>
      </div>
      <div>
        <a-radio-group v-model="showType" type="button" class="file-show-type ml-[4px]">
          <a-radio value="preposition" class="show-type-icon p-[2px]">{{
            t('caseManagement.featureCase.preCase')
          }}</a-radio>
          <a-radio value="postPosition" class="show-type-icon p-[2px]">{{
            t('caseManagement.featureCase.postCase')
          }}</a-radio>
        </a-radio-group>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
          allow-clear
          class="mx-[8px] w-[240px]"
        ></a-input-search>
      </div>
    </div>
    <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
      <template #operation="{ record }">
        <MsRemoveButton
          position="br"
          ok-text="common.confirm"
          remove-text="caseManagement.featureCase.cancelDependency"
          :title="t('caseManagement.featureCase.cancelDependencyTip', { name: characterLimit(record.name) })"
          :sub-title-tip="t('caseManagement.featureCase.cancelDependencyContent')"
          :loading="cancelLoading"
          @ok="cancelDependency(record)"
        />
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex w-full items-center justify-center">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="addCase">
            {{
              showType === 'preposition'
                ? t('caseManagement.featureCase.addPresetCase')
                : t('caseManagement.featureCase.addPostCase')
            }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <PreAndPostCaseDrawer
      ref="drawerRef"
      v-model:visible="showDrawer"
      :show-type="showType"
      :case-id="props.caseId"
      @success="successHandler"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';
  import PreAndPostCaseDrawer from './preAndPostCaseDrawer.vue';

  import { cancelPreOrPostCase, getDependOnCase } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { characterLimit } from '@/utils';

  const featureCaseStore = useFeatureCaseStore();

  const appStore = useAppStore();

  export type types = 'preposition' | 'postPosition';
  const currentProjectId = computed(() => appStore.currentProjectId);
  const showType = ref<types>('preposition');
  const { t } = useI18n();
  const keyword = ref<string>('');
  const props = defineProps<{
    caseId: string;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'id',
      width: 200,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnVersion',
      slotName: 'versionName',
      dataIndex: 'versionName',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'userName',
      dataIndex: 'userName',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
      showInTable: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getDependOnCase, {
    columns,
    scroll: { x: '100%' },
    heightUsed: 340,
    selectable: false,
    noDisable: true,
    showSetting: false,
    enableDrag: false,
  });

  function getParams() {
    setLoadListParams({
      projectId: currentProjectId.value,
      keyword: keyword.value,
      type: showType.value === 'preposition' ? 'PRE' : 'POST',
      id: props.caseId,
    });
  }

  async function initData() {
    getParams();
    await loadList();
    const { msPagination } = propsRes.value;
    featureCaseStore.setListCount(featureCaseStore.activeTab, msPagination?.total || 0);
  }

  const cancelLoading = ref<boolean>(false);
  // 取消依赖
  async function cancelDependency(record: any) {
    cancelLoading.value = true;
    try {
      await cancelPreOrPostCase(record.id);
      Message.success(t('caseManagement.featureCase.cancelFollowSuccess'));
      initData();
    } catch (error) {
      console.log(error);
    } finally {
      cancelLoading.value = false;
    }
  }

  const showDrawer = ref<boolean>(false);
  const drawerRef = ref();

  // 添加前后置用例
  function addCase() {
    showDrawer.value = true;
    drawerRef.value.initModules();
  }

  function successHandler() {
    initData();
  }

  watch(
    () => showType.value,
    (val) => {
      if (val) {
        initData();
      }
    }
  );

  onMounted(() => {
    if (props.caseId) {
      initData();
    }
  });
</script>

<style scoped></style>
