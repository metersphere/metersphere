<template>
  <div class="pl-4">
    <div class="header">
      <div
        ><span class="one-line-text max-w-[300px]">{{ moduleInfo.name }}</span
        ><span class="ml-[4px] text-[var(--color-text-4)]">({{ moduleInfo.count }})</span></div
      >
      <div class="header-right">
        <a-select v-model="tableFileType" class="w-[240px]" :loading="fileTypeLoading" @change="searchList">
          <a-option key="" value="">{{ t('common.all') }}</a-option>
          <a-option v-for="item of tableFileTypeOptions" :key="item" :value="item">
            {{ item }}
          </a-option>
        </a-select>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
          allow-clear
          class="w-[240px]"
          @search="searchList"
          @press-enter="searchList"
      /></div>
    </div>
    <ms-base-table v-bind="propsRes" ref="tableRef" no-disable v-on="propsEvent">
      <template #name="{ record }">
        <MsTag
          v-if="record.fileType.toLowerCase() === 'jar'"
          theme="light"
          type="success"
          :self-style="
            record.enable
              ? {}
              : {
                  color: 'var(--color-text-4)',
                  backgroundColor: 'var(--color-text-n9)',
                }
          "
        >
          {{ t(record.enable ? 'common.enable' : 'common.disable') }}
        </MsTag>
        <a-tooltip :content="record.name">
          <div class="one-line-text max-w-[168px]">{{ record.name }}</div>
        </a-tooltip>
      </template>
      <template #size="{ record }">
        <span>{{ formatFileSize(record.size) }}</span>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { debounce } from 'lodash-es';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { getFileTypes, getRepositoryFileTypes } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { findNodeByKey, formatFileSize } from '@/utils';

  import type { AssociatedList } from '@/models/caseManagement/featureCase';
  import type { CommonList, ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { FileListQueryParams } from '@/models/projectManagement/file';
  import { Repository } from '@/models/projectManagement/file';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const props = defineProps<{
    activeFolder: string;
    activeFolderType: 'folder' | 'module' | 'storage';
    offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
    modulesCount: Record<string, any>; // 模块数量
    folderTree: ModuleTreeNode[];
    selectFile: AssociatedList[]; // 表格选中项
    getListRequest: (params: TableQueryParams) => Promise<CommonList<AssociatedList>>;
    showType: 'Module' | 'Storage'; // 展示类型
    storageList: Repository[]; // 存储库列表
    getListFunParams: TableQueryParams; // 表格额外去重参数
  }>();
  const emit = defineEmits<{
    (e: 'init', params: FileListQueryParams): void;
    (e: 'update:selectFile', val: AssociatedList[]): void;
  }>();

  const tableFileTypeOptions = ref<string[]>([]);
  const tableFileType = ref(''); // 文件格式筛选
  const keyword = ref('');
  const fileTypeLoading = ref(false);
  const fileType = ref('module'); // 当前查看的文件类型，模块/存储库
  const appStore = useAppStore();
  const userStore = useUserStore();
  const combine = ref<Record<string, any>>({});
  const isMyOrAllFolder = computed(() => ['my', 'all'].includes(props.activeFolder)); // 是否是我的文件/全部文件

  const columns: MsTableColumn = [
    {
      title: 'project.fileManagement.name',
      slotName: 'name',
      dataIndex: 'name',
      width: 270,
    },
    {
      title: 'project.fileManagement.type',
      dataIndex: 'fileType',
      width: 90,
    },
    {
      title: 'project.fileManagement.tag',
      dataIndex: 'tags',
      isTag: true,
    },
    {
      title: 'project.fileManagement.creator',
      dataIndex: 'creator',
      showTooltip: true,
      width: 120,
    },
    {
      title: 'project.fileManagement.updater',
      dataIndex: 'updateUser',
      showTooltip: true,
      width: 120,
    },
    {
      title: 'project.fileManagement.updateTime',
      dataIndex: 'updateTime',
      width: 180,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    props.getListRequest,
    {
      columns,
      tableKey: TableKeyEnum.FILE_MANAGEMENT_FILE,
      showSetting: false,
      selectable: true,
      showSelectAll: true,
      heightUsed: 300,
    },
    (item) => {
      return {
        ...item,
        tags: item.tags?.map((e: string) => ({ id: e, name: e })) || [],
      };
    }
  );

  function emitTableParams() {
    emit('init', {
      keyword: keyword.value,
      fileType: tableFileType.value,
      moduleIds: [],
      projectId: appStore.currentProjectId,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
      combine: combine.value,
    });
  }

  function setTableParams() {
    if (props.activeFolder === 'my') {
      combine.value.createUser = userStore.id;
    } else {
      combine.value.createUser = '';
    }
    if (fileType.value === 'storage') {
      combine.value.storage = 'git';
    } else {
      combine.value.storage = 'minio';
    }
    let moduleIds: string[] = [props.activeFolder, ...props.offspringIds];

    if (isMyOrAllFolder.value) {
      moduleIds = [];
    }
    setLoadListParams({
      keyword: keyword.value,
      fileType: tableFileType.value,
      moduleIds,
      projectId: appStore.currentProjectId,
      combine: { ...combine.value, ...props.getListFunParams.combine },
    });
  }

  const searchList = debounce(() => {
    setTableParams();
    loadList();
    emitTableParams();
  }, 300);

  /**
   * 初始化文件类型筛选选项
   */
  async function initFileTypes() {
    try {
      fileTypeLoading.value = true;
      let res: string[] = [];
      if (fileType.value === 'storage') {
        res = await getRepositoryFileTypes(appStore.currentProjectId);
      } else {
        res = await getFileTypes(appStore.currentProjectId);
      }
      tableFileType.value = '';
      tableFileTypeOptions.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      fileTypeLoading.value = false;
    }
  }

  watch(
    () => props.activeFolderType,
    () => {
      initFileTypes();
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.activeFolderType,
    (val) => {
      if (val === 'folder') {
        fileType.value = 'module';
      } else {
        fileType.value = val;
      }
      setTableParams();
    }
  );

  watch(
    () => props.activeFolder,
    () => {
      keyword.value = '';
      searchList();
      resetSelector();
    },
    { immediate: true }
  );

  const moduleInfo = computed(() => {
    if (props.showType === 'Module') {
      return {
        name: findNodeByKey<Record<string, any>>(props.folderTree, props.activeFolder, 'id')?.name,
        count: props.modulesCount[props.activeFolder],
      };
    }
    const storageItem = props.storageList.find((item) => item.id === props.activeFolder);
    return {
      name: storageItem?.name,
      count: storageItem?.count,
    };
  });

  const tableSelected = ref<AssociatedList[]>([]);

  const selectedIds = computed(() => {
    return [...propsRes.value.selectedKeys];
  });

  watch(
    () => selectedIds.value,
    () => {
      tableSelected.value = propsRes.value.data.filter((item: any) => selectedIds.value.indexOf(item.id) > -1);
      emit('update:selectFile', tableSelected.value);
    }
  );

  defineExpose({
    resetSelector,
  });

  onMounted(() => {
    resetSelector();
  });

  onUnmounted(() => {
    resetSelector();
  });
</script>

<style scoped lang="less">
  .header {
    @apply flex items-center justify-between;

    margin-bottom: 16px;
    .header-right {
      @apply ml-auto flex items-center justify-end;

      width: 70%;
      gap: 8px;
      .show-type-icon {
        :deep(.arco-radio-button-content) {
          @apply flex;

          padding: 4px;
          line-height: 20px;
        }
      }
    }
  }
</style>
