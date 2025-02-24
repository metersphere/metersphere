<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button
          v-if="showType === 'preposition'"
          v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
          class="mr-3"
          type="primary"
          @click="addCase"
        >
          {{ t('caseManagement.featureCase.addPresetCase') }}
        </a-button>
        <a-button v-else v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" type="primary" @click="addCase">
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
          :placeholder="t('caseManagement.featureCase.searchByName')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="searchDependCase"
          @press-enter="searchDependCase"
          @clear="searchDependCase"
        ></a-input-search>
      </div>
    </div>
    <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
      <template #num="{ record }">
        {{ record.num }}
      </template>
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
        <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
          <span v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE'])">
            {{ t('caseManagement.caseReview.tableNoData') }}
          </span>
          <span v-else>{{ t('caseManagement.featureCase.tableNoData') }}</span>
          <MsButton v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE'])" class="ml-[8px]" @click="addCase">
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
      @create="handleCreate"
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
  import { hasAnyPermission } from '@/utils/permission';

  const featureCaseStore = useFeatureCaseStore();
  // const activeTab = computed(() => featureCaseStore.activeTab);

  const appStore = useAppStore();

  export type types = 'preposition' | 'postPosition';
  const currentProjectId = computed(() => appStore.currentProjectId);
  const showType = ref<types>('preposition');
  const { t } = useI18n();
  const keyword = ref<string>('');
  const props = defineProps<{
    caseId: string;
  }>();

  const emit = defineEmits<{
    (e: 'create'): void;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      slotName: 'num',
      width: 200,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
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
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'userName',
      dataIndex: 'userName',
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
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getDependOnCase, {
    columns,
    scroll: { x: '100%' },
    heightUsed: 360,
    selectable: false,
    noDisable: true,
    showSetting: false,
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
    featureCaseStore.getCaseCounts(props.caseId);
  }

  const cancelLoading = ref<boolean>(false);
  // 取消依赖
  async function cancelDependency(record: any) {
    cancelLoading.value = true;
    try {
      const params = {
        id: record.id,
        caseId: record.caseId,
        type: showType.value === 'preposition' ? 'PRE' : 'POST',
      };
      await cancelPreOrPostCase(params);
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

  async function searchDependCase() {
    getParams();
    await loadList();
  }

  function handleCreate() {
    emit('create');
    showDrawer.value = false;
  }

  watch(
    () => showType.value,
    (val) => {
      if (val) {
        initData();
      }
    }
  );

  // watch(
  //   () => activeTab.value,
  //   (val) => {
  //     if (val === 'dependency') {
  //       initData();
  //     }
  //   }
  // );

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        initData();
      }
    }
  );

  onMounted(() => {
    initData();
  });
</script>

<style scoped></style>
