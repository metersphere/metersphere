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
    <PreAndPostCaseDrawer ref="drawerRef" v-model:visible="showDrawer" :show-type="showType" :case-id="props.caseId" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import PreAndPostCaseDrawer from './preAndPostCaseDrawer.vue';

  import { getDependOnCase } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

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
      slotName: 'defectState',
      dataIndex: 'defectState',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUser',
      dataIndex: 'createUser',
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
    enableDrag: true,
  });

  // 取消依赖
  function cancelDependency(record: any) {}

  function getParams() {
    setLoadListParams({
      projectId: currentProjectId.value,
      keyword: keyword.value,
      type: showType.value === 'preposition' ? 'PRE' : 'POST',
      id: props.caseId,
    });
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
    () => {
      getParams();
      loadList();
    }
  );

  onBeforeMount(() => {
    getParams();
    loadList();
  });
</script>

<style scoped></style>
