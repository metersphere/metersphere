<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <a-button v-if="showType === 'preposition'" class="mr-3" type="primary" @click="addCase('preposition')">
          {{ t('caseManagement.featureCase.addPresetCase') }}
        </a-button>
        <a-button v-else type="primary" @click="addCase('postPosition')">
          {{ t('caseManagement.featureCase.addPostCase') }}
        </a-button>
      </div>
      <div>
        <a-radio-group v-model:model-value="showType" type="button" class="file-show-type ml-[4px]">
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
    <ms-base-table v-if="showType === 'preposition'" ref="tableRef" v-bind="prePropsRes" v-on="preTableEvent">
      <template #operation="{ record }">
        <MsButton @click="cancelDependency(record)">{{ t('caseManagement.featureCase.cancelDependency') }}</MsButton>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex items-center justify-center">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="addCase('preposition')">
            {{ t('caseManagement.featureCase.addPresetCase') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <ms-base-table v-else v-bind="postPropsRes" v-on="postTableEvent">
      <template #operation="{ record }">
        <MsButton @click="cancelDependency(record)">{{ t('caseManagement.featureCase.cancelDependency') }}</MsButton>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex items-center justify-center">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="addCase('postPosition')">
            {{ t('caseManagement.featureCase.addPostCase') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <PreAndPostCaseDrawer ref="drawerRef" v-model:visible="showDrawer" :show-type="showType" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import PreAndPostCaseDrawer from './preAndPostCaseDrawer.vue';

  import { getRecycleListRequest } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  export type types = 'preposition' | 'postPosition';

  const showType = ref<types>('preposition');
  const { t } = useI18n();
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
      title: 'caseManagement.featureCase.tableColumnVersion',
      slotName: 'defectState',
      dataIndex: 'defectState',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUser',
      dataIndex: 'createUser',
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

  const {
    propsRes: prePropsRes,
    propsEvent: preTableEvent,
    loadList: loadPreList,
    setLoadListParams: setPreListParams,
  } = useTable(getRecycleListRequest, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEPENDENCY_PRE_CASE,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });

  const {
    propsRes: postPropsRes,
    propsEvent: postTableEvent,
    loadList: loadPostList,
    setLoadListParams: setPostListParams,
  } = useTable(getRecycleListRequest, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEPENDENCY_POST_CASE,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });

  // 取消依赖
  function cancelDependency(record: any) {}

  function getFetch() {
    if (showType.value === 'preposition') {
      setPreListParams({ keyword: keyword.value });
      loadPreList();
    } else {
      setPostListParams({ keyword: keyword.value });
      loadPostList();
    }
  }

  const showDrawer = ref<boolean>(false);
  const drawerRef = ref();
  // 添加前后置用例
  function addCase(type: string) {
    showDrawer.value = true;
    drawerRef.value.initModules();
  }

  watch(
    () => showType.value,
    (val) => {
      if (val) {
        getFetch();
      }
    }
  );
</script>

<style scoped></style>
