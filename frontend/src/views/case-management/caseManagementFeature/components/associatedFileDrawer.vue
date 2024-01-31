<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
    :title="t('caseManagement.featureCase.associatedFile')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-loading="drawerLoading"
    :width="960"
    unmount-on-close
    :show-continue="false"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="mb-4 grid grid-cols-4 gap-2">
      <div class="col-end-4">
        <a-select v-model="fileType" @change="changeSelect">
          <a-option key="" value="">{{ t('common.all') }}</a-option>
          <a-option v-for="item of fileTypeList" :key="item" :value="item">{{ item }}</a-option>
          <template #prefix
            ><span>{{ t('caseManagement.featureCase.fileType') }}</span></template
          >
        </a-select></div
      >
      <div>
        <a-input-search
          v-model="searchParams.keyword"
          :max-length="255"
          :placeholder="t('project.member.searchMember')"
          allow-clear
          @search="searchHandler"
          @press-enter="searchHandler"
        ></a-input-search
      ></div>
    </div>
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record }">
        <div class="flex items-center">
          <span> <MsIcon class="mr-1" :type="getFileType(record.fileType)" /></span>
          <span>{{ record.name }}</span>
        </div>
      </template>
    </MsBaseTable>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { FileIconMap, getFileEnum } from '@/components/pure/ms-upload/iconMap';

  import { getAssociatedFileListUrl } from '@/api/modules/case-management/featureCase';
  import { getFileTypes } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { AssociatedList } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { UploadStatus } from '@/enums/uploadEnum';

  const { t } = useI18n();
  const showDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);
  const appStore = useAppStore();
  const getCurrentProjectId = computed(() => appStore.getCurrentProjectId);
  const fileTypeList = ref<string[]>([]);
  const fileType = ref('');

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'save', selectList: AssociatedList[]): void;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.fileName',
      dataIndex: 'name',
      slotName: 'name',
      showInTable: true,
      showTooltip: true,
      showDrag: false,
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.description',
      dataIndex: 'description',
      showInTable: true,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tags',
      dataIndex: 'tags',
      isTag: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      dataIndex: 'createUser',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnUpdateUser',
      dataIndex: 'updateUser',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnUpdateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      showInTable: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getAssociatedFileListUrl,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_ASSOCIATED_TABLE,
      selectable: true,
      noDisable: true,
      columns,
      scroll: {
        x: 1400,
      },
      heightUsed: 300,
    },
    (record) => ({
      ...record,
      tags: record.tags || [],
    })
  );

  const searchParams = ref<TableQueryParams>({
    keyword: '',
    filter: {},
    moduleIds: [],
    fileType: fileType.value,
    projectId: getCurrentProjectId.value,
  });

  const initData = async () => {
    setLoadListParams({ ...searchParams.value });
    await loadList();
  };

  const searchHandler = () => {
    initData();
    resetSelector();
  };

  function changeSelect(value: string | number | boolean | Record<string, any>) {
    searchParams.value.fileType = value;
    initData();
  }
  const tableSelected = ref<AssociatedList[]>([]);

  function handleDrawerConfirm() {
    const selectedIds = [...propsRes.value.selectedKeys];
    tableSelected.value = propsRes.value.data.filter((item: any) => selectedIds.indexOf(item.id) > -1);
    emit('save', tableSelected.value);
    showDrawer.value = false;
    propsRes.value.selectedKeys.clear();
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
    resetSelector();
  }

  function getFileType(type: string) {
    const fileTypes = type ? getFileEnum(`/${type.toLowerCase()}`) : 'unknown';
    return FileIconMap[fileTypes][UploadStatus.done];
  }

  watch(
    () => props.visible,
    (val) => {
      showDrawer.value = val;
      if (val) {
        initData();
      }
    }
  );

  watch(
    () => showDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  onBeforeMount(async () => {
    fileTypeList.value = await getFileTypes(appStore.currentProjectId);
  });
</script>

<style scoped></style>
