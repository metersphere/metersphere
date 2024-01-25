<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="innerVisible"
    :width="960"
    :footer="false"
    :title="t('project.fileManagement.detail')"
    :detail-id="props.fileId"
    :detail-index="props.activeFileIndex"
    :get-detail-func="getFileDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
    @loaded="loadedFile"
  >
    <template #titleRight="{ loading, detail }">
      <a-switch
        v-if="fileType === 'jar'"
        v-model:model-value="detail.enable"
        :before-change="handleEnableIntercept"
        :disabled="loading"
        size="small"
        class="mr-[4px]"
        type="line"
      />
      <a-tooltip :content="t('project.fileManagement.uploadTipSingle')">
        <MsIcon type="icon-icon-maybe_outlined" class="mr-[8px] cursor-pointer hover:text-[rgb(var(--primary-5))]" />
      </a-tooltip>
      <MsButton
        v-permission="['PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD']"
        type="icon"
        status="secondary"
        class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
        :disabled="loading"
        :loading="downLoading"
        @click="handleDownload(detail)"
      >
        <MsIcon type="icon-icon_bottom-align_outlined" class="mr-[4px]" />
        {{ t('project.fileManagement.download') }}
      </MsButton>
      <MsButton
        v-if="detail?.storage === 'GIT'"
        v-permission="['PROJECT_FILE_MANAGEMENT:READ+UPDATE']"
        type="icon"
        status="secondary"
        class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
        :disabled="loading"
        @click="upgradeRepositoryFile"
      >
        <MsIcon type="icon-icon_reset_outlined" class="mr-[4px]" />
        {{ t('project.fileManagement.updateFile') }}
      </MsButton>
    </template>
    <template #default="{ loading, detail }">
      <div class="flex h-full w-full">
        <div class="file-detail">
          <a-skeleton v-if="loading" :loading="loading" :animation="true">
            <a-skeleton-shape size="large" class="mb-[16px] h-[102px] w-[102px]" />
            <a-space direction="vertical" class="w-[28%]" size="large">
              <a-skeleton-line :rows="11" :line-height="24" />
            </a-space>
            <a-space direction="vertical" class="ml-[4%] w-[68%]" size="large">
              <a-skeleton-line :rows="11" :line-height="24" />
            </a-space>
          </a-skeleton>
          <template v-else>
            <div class="mb-[16px] h-[102px] w-[102px]">
              <a-spin class="h-full w-full" :loading="fileLoading">
                <MsThumbnailCard
                  mode="hover"
                  :type="detail.fileType || ''"
                  :url="`${CompressImgUrl}/${userStore.id}/${detail.id}`"
                  :footer-text="t('project.fileManagement.replaceFile')"
                  @click="handleFileIconClick"
                />
              </a-spin>
            </div>
            <MsDescription
              :descriptions="fileDescriptions"
              :label-width="currentLocale === 'zh-CN' ? '80px' : '100px'"
              :add-tag-func="addFileTag"
              @tag-close="handleFileTagClose"
            >
              <template #value="{ item }">
                <div class="flex flex-wrap items-center">
                  <a-tooltip
                    :content="`${item.value}`"
                    :mouse-enter-delay="300"
                    :disabled="item.value === undefined || item.value === null || item.value?.toString() === ''"
                    mini
                  >
                    <div :class="['one-line-text', 'flex-1']">
                      {{
                        item.value === undefined || item.value === null || item.value?.toString() === ''
                          ? '-'
                          : item.value
                      }}
                    </div>
                  </a-tooltip>
                  <template v-if="item.key === 'name'">
                    <popConfirm
                      mode="fileRename"
                      :field-config="{
                        field: detail.name,
                        placeholder: t('project.fileManagement.fileNamePlaceholder'),
                      }"
                      :node-id="detail.id"
                      :all-names="[]"
                      @rename-finish="detailDrawerRef?.initDetail"
                    >
                      <MsButton class="!mr-0 ml-[8px]">{{ t('common.rename') }}</MsButton>
                    </popConfirm>
                    <template v-if="fileType === 'image'">
                      <a-divider
                        direction="vertical"
                        class="mx-[8px] min-h-[12px] rounded-[var(--border-radius-small)]"
                      />
                      <MsButton class="ml-0" @click="previewVisible = true">
                        {{ t('common.preview') }}
                      </MsButton>
                    </template>
                  </template>
                  <template v-if="item.key === 'desc'">
                    <popConfirm
                      mode="fileUpdateDesc"
                      :title="t('project.fileManagement.desc')"
                      :field-config="{
                        field: detail.desc,
                        placeholder: t('project.fileManagement.descPlaceholder'),
                        maxLength: 250,
                        isTextArea: true,
                      }"
                      :node-id="detail.id"
                      :all-names="[]"
                      @update-desc-finish="detailDrawerRef?.initDetail"
                    >
                      <MsButton v-permission="['PROJECT_FILE_MANAGEMENT:READ+UPDATE']" class="ml-[8px]"
                        ><MsIcon type="icon-icon_edit_outlined"></MsIcon
                      ></MsButton>
                    </popConfirm>
                  </template>
                </div>
              </template>
            </MsDescription>
          </template>
        </div>
        <div class="file-relation">
          <a-tabs v-model:active-key="activeTab" :disabled="loading" class="no-content" @change="() => loadTable()">
            <a-tab-pane key="case" :title="t('project.fileManagement.cases')" />
            <a-tab-pane key="version" :title="t('project.fileManagement.versionHistory')" />
          </a-tabs>
          <div class="h-[16px] bg-[var(--color-text-n9)]"></div>
          <div v-if="activeTab === 'case'" class="flex items-center justify-between px-[16px] pt-[16px]">
            <div class="text-[var(--color-text-1)]">{{ t('project.fileManagement.caseList') }}</div>
            <a-input-search
              v-model:model-value="keyword"
              :placeholder="t('project.fileManagement.search')"
              allow-clear
              class="w-[240px]"
            >
            </a-input-search>
          </div>
          <div class="p-[16px]">
            <a-spin class="w-full" :loading="caseLoading">
              <ms-base-table
                v-if="activeTab === 'case'"
                v-bind="caseTableProps"
                :data="caseList"
                no-disable
                :action-config="caseBatchActions"
                v-on="caseTableEvent"
              >
                <template #action="{ record }">
                  <MsButton
                    v-permission="['PROJECT_FILE_MANAGEMENT:READ+UPDATE']"
                    type="text"
                    class="mr-[8px]"
                    @click="updateCase(record)"
                  >
                    {{ t('project.fileManagement.updateCaseFile') }}
                  </MsButton>
                </template>
              </ms-base-table>
            </a-spin>
            <ms-base-table
              v-if="activeTab === 'version'"
              v-bind="versionTableProps"
              no-disable
              v-on="versionTableEvent"
            >
            </ms-base-table>
          </div>
        </div>
      </div>
      <a-image-preview v-model:visible="previewVisible" :src="`${OriginImgUrl}/${userStore.id}/${detail.id}`" />
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useFileSystemAccess } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDescription, { type Description } from '@/components/pure/ms-description/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsPaginationI, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { getFileEnum } from '@/components/pure/ms-upload/iconMap';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import MsThumbnailCard from '@/components/business/ms-thumbnail-card/index.vue';
  import popConfirm from './popConfirm.vue';

  import {
    downloadFile,
    getAssociationList,
    getFileDetail,
    getFileHistoryList,
    reuploadFile,
    toggleJarFileStatus,
    updateFile,
    updateRepositoryFile,
    upgradeAssociation,
  } from '@/api/modules/project-management/fileManagement';
  import { CompressImgUrl, OriginImgUrl } from '@/api/requrls/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';
  import { useAppStore } from '@/store';
  import useUserStore from '@/store/modules/user';
  import { downloadByteFile, formatFileSize } from '@/utils';

  import { AssociationItem, FileDetail } from '@/models/projectManagement/file';

  const props = defineProps<{
    visible: boolean;
    fileId: string;
    activeFileIndex: number;
    tableData: any[];
    pagination: MsPaginationI;
    pageChange: (page: number) => Promise<void>;
  }>();

  const emit = defineEmits(['update:visible']);

  const { file: newFile, open } = useFileSystemAccess();
  const { t } = useI18n();
  const { currentLocale } = useLocale();
  const userStore = useUserStore();
  const appStore = useAppStore();

  const innerVisible = ref(false);
  const fileDescriptions = ref<Description[]>([]);
  const detailDrawerRef = ref<InstanceType<typeof MsDetailDrawer>>();

  watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
  );

  watch(
    () => innerVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  async function handleEnableIntercept(newValue: string | number | boolean) {
    try {
      await toggleJarFileStatus(props.fileId, newValue as boolean);
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  const downLoading = ref(false);
  /**
   * 下载单个文件
   * @param record 表格数据项
   */
  async function handleDownload(detail: FileDetail) {
    try {
      downLoading.value = true;
      const res = await downloadFile(detail.id);
      downloadByteFile(res, `${detail.name}.${detail.fileType}`);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      downLoading.value = false;
    }
  }

  const fileType = ref('unknown');
  const renameTitle = ref(''); // 重命名的文件名称

  const fileLoading = ref(false);
  watch(
    () => newFile.value,
    async (data) => {
      if (data) {
        try {
          fileLoading.value = true;
          await reuploadFile({
            request: {
              fileId: props.fileId,
              enable: false,
            },
            file: data,
          });
          Message.success(t('project.fileManagement.replaceFileSuccess'));
          detailDrawerRef.value?.initDetail();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          fileLoading.value = false;
        }
      }
    }
  );

  async function handleFileIconClick() {
    try {
      await open();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const previewVisible = ref(false);

  async function addFileTag(val: string, item: Description) {
    await updateFile({
      id: props.fileId,
      tags: Array.isArray(item.value) ? [...item.value, val] : [item.value, val],
    });
  }

  async function handleFileTagClose(tag: string | number, item: Description) {
    await updateFile({
      id: props.fileId,
      tags: Array.isArray(item.value) ? item.value.filter((e) => e !== tag) : [],
    });
  }

  /**
   * 更新仓库文件
   */
  async function upgradeRepositoryFile() {
    try {
      fileLoading.value = true;
      await updateRepositoryFile(props.fileId);
      Message.success(t('common.updateSuccess'));
      detailDrawerRef.value?.initDetail();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      fileLoading.value = false;
    }
  }

  const activeTab = ref('case');

  const caseColumns: MsTableColumn = [
    {
      title: 'project.fileManagement.id',
      dataIndex: 'id',
      showTooltip: true,
    },
    {
      title: 'project.fileManagement.name',
      dataIndex: 'sourceName',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'project.fileManagement.type',
      dataIndex: 'sourceType',
    },
    {
      title: 'project.fileManagement.fileVersion',
      dataIndex: 'fileVersion',
    },
    {
      title: 'common.operation',
      slotName: 'action',
      fixed: 'right',
      width: 130,
    },
  ];
  const {
    propsRes: caseTableProps,
    propsEvent: caseTableEvent,
    loadList: loadCaseList,
    setLoadListParams,
  } = useTable(getAssociationList, {
    scroll: { x: 800 },
    columns: caseColumns,
    selectable: true,
    showSelectAll: true,
    showPagination: false,
    size: 'default',
  });

  const caseBatchActions = {
    baseAction: [
      {
        label: 'project.fileManagement.updateCaseFile',
        eventTag: 'updateCaseFile',
      },
    ],
  };

  const keyword = ref('');
  const caseList = computed(() => caseTableProps.value.data.filter((item) => item.sourceName.includes(keyword.value)));
  const caseLoading = ref(false);

  async function updateCase(record: AssociationItem) {
    try {
      caseLoading.value = true;
      await upgradeAssociation(appStore.currentProjectId, record.id);
      Message.success(t('project.fileManagement.updateCaseFileSuccess'));
      loadCaseList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      caseLoading.value = false;
    }
  }

  const versionColumns: MsTableColumn = [
    {
      title: 'project.fileManagement.fileVersion',
      dataIndex: 'fileVersion',
      showTooltip: true,
      width: 180,
    },
    {
      title: 'project.fileManagement.record',
      dataIndex: 'updateHistory',
      showTooltip: true,
    },
    {
      title: 'project.fileManagement.operator',
      dataIndex: 'operator',
      showTooltip: true,
    },
    {
      title: 'project.fileManagement.operatorTime',
      dataIndex: 'operateTime',
      width: 180,
    },
  ];
  const {
    propsRes: versionTableProps,
    propsEvent: versionTableEvent,
    loadList: loadVersionList,
    setLoadListParams: setVersionLoadListParams,
  } = useTable(
    getFileHistoryList,
    {
      scroll: { x: '100%' },
      columns: versionColumns,
      selectable: false,
      showPagination: false,
    },
    (item) => {
      return {
        ...item,
        operateTime: dayjs(item.operateTime).format('YYYY-MM-DD HH:mm:ss'),
      };
    }
  );

  function loadTable() {
    if (activeTab.value === 'case') {
      setLoadListParams({
        id: props.fileId,
      });
      loadCaseList();
    } else {
      setVersionLoadListParams({
        id: props.fileId,
      });
      loadVersionList();
    }
  }

  function loadedFile(detail: FileDetail) {
    if (detail.fileType) {
      fileType.value = getFileEnum(`/${detail.fileType.toLowerCase()}`);
    }
    renameTitle.value = detail.name;
    fileDescriptions.value = [
      {
        label: t('project.fileManagement.name'),
        value: detail.name,
        key: 'name',
      },
      {
        label: t('project.fileManagement.desc'),
        value: detail.description,
        key: 'desc',
      },
      {
        label: t('project.fileManagement.type'),
        value: detail.fileType,
      },
      {
        label: t('project.fileManagement.size'),
        value: formatFileSize(detail.size),
      },
      {
        label: t('project.fileManagement.creator'),
        value: detail.createUser,
      },
      {
        label: t('project.fileManagement.fileModule'),
        value: detail.moduleName,
        key: 'fileModule',
      },
      {
        label: t('project.fileManagement.tag'),
        value: detail.tags,
        isTag: true,
        showTagAdd: true,
        closable: true,
        key: 'tag',
      },
      {
        label: t('project.fileManagement.createTime'),
        value: dayjs(detail.createTime).format('YYYY-MM-DD HH:mm:ss'),
      },
    ];
    if (detail?.storage !== 'MINIO') {
      fileDescriptions.value.splice(
        3,
        0,
        ...[
          {
            label: t('project.fileManagement.gitBranch'),
            value: detail.branch || '',
          },
          {
            label: t('project.fileManagement.gitPath'),
            value: detail.filePath || '',
          },
          {
            label: t('project.fileManagement.gitVersion'),
            value: detail.fileVersion || '',
          },
        ]
      );
    }
    loadTable();
  }

  watchEffect(() => {
    if (!innerVisible.value) {
      activeTab.value = 'case';
    }
  });
</script>

<style lang="less" scoped>
  .file-detail {
    @apply relative h-full;

    padding: 16px;
    width: 300px;
    border-right: 1px solid var(--color-text-n8);
  }
  .file-relation {
    width: calc(100% - 300px);
  }
  :deep(.no-content) {
    .arco-tabs-tab {
      padding: 8px !important;
    }
    .arco-tabs-content {
      display: none;
    }
  }
</style>
