<template>
  <MsDetailDrawer
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
        :default-checked="detail?.enable"
        :before-change="handleEnableIntercept"
        :disabled="loading"
        size="small"
        class="mr-[8px]"
      />
      <MsButton
        type="icon"
        status="secondary"
        class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
        :disabled="loading"
        @click="handleDownload"
      >
        <MsIcon type="icon-icon_bottom-align_outlined" class="mr-[4px]" />
        {{ t('project.fileManagement.download') }}
      </MsButton>
      <MsButton
        v-if="detail?.storage !== 'minio'"
        type="icon"
        status="secondary"
        class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
        :disabled="loading"
      >
        <MsIcon type="icon-icon_reset_outlined" class="mr-[4px]" />
        {{ t('project.fileManagement.updateFile') }}
      </MsButton>
    </template>
    <template #default="{ loading, detail }">
      <div class="flex h-full">
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
            <div class="mb-[16px] w-[102px]">
              <MsPreviewCard
                mode="hover"
                :type="detail?.type"
                :url="detail?.url"
                :footer-text="t('project.fileManagement.replaceFile')"
                @click="handleFileIconClick"
              />
            </div>
            <MsDescription
              :descriptions="fileDescriptions"
              :label-width="currentLocale === 'zh-CN' ? '80px' : '100px'"
              :add-tag-func="addFileTag"
            >
              <template #value="{ item }">
                <div class="flex flex-wrap items-center">
                  <a-tooltip
                    :content="(item.value as string)"
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
                      mode="rename"
                      :field-config="{ placeholder: t('project.fileManagement.fileNamePlaceholder') }"
                      :all-names="[]"
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
                      mode="rename"
                      :title="t('project.fileManagement.desc')"
                      :field-config="{
                        field: detail.desc,
                        placeholder: t('project.fileManagement.descPlaceholder'),
                        maxLength: 250,
                        isTextArea: true,
                      }"
                      :all-names="[]"
                    >
                      <MsButton class="ml-[8px]"><MsIcon type="icon-icon_edit_outlined"></MsIcon></MsButton>
                    </popConfirm>
                  </template>
                </div>
              </template>
            </MsDescription>
          </template>
        </div>
        <div class="file-relation">
          <a-tabs v-model:active-key="activeTab" :disabled="loading" class="no-content">
            <a-tab-pane key="case" :title="t('project.fileManagement.cases')" />
            <a-tab-pane key="version" :title="t('project.fileManagement.versionHistory')" />
          </a-tabs>
          <div class="h-[16px] bg-[var(--color-text-n9)]"></div>
          <div v-if="activeTab === 'case'" class="flex items-center justify-between p-[16px]">
            <div class="text-[var(--color-text-1)]">{{ t('project.fileManagement.caseList') }}</div>
            <a-input-search
              v-model:model-value="keyword"
              :placeholder="t('project.fileManagement.search')"
              allow-clear
              class="w-[240px]"
              @press-enter="searchCase"
              @search="searchCase"
            >
            </a-input-search>
          </div>
          <div class="p-[16px]">
            <ms-base-table
              v-if="activeTab === 'case'"
              v-bind="caseTableProps"
              no-disable
              :action-config="caseBatchActions"
              v-on="caseTableEvent"
            >
              <template #action="{ record }">
                <MsButton type="text" class="mr-[8px]" @click="updateCase(record)">
                  {{ t('project.fileManagement.updateCaseFile') }}
                </MsButton>
              </template>
            </ms-base-table>
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
      <a-image-preview v-model:visible="previewVisible" :src="detail?.url" />
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { ref, watch, watchEffect } from 'vue';
  import dayjs from 'dayjs';
  import { useFileSystemAccess } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { getFileEnum } from '@/components/pure/ms-upload/iconMap';
  import MsDescription, { type Description } from '@/components/pure/ms-description/index.vue';
  import popConfirm from './popConfirm.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import { getFileDetail, getFileCases, getFileVersions } from '@/api/modules/project-management/fileManagement';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import MsPreviewCard from '@/components/business/ms-thumbnail-card/index.vue';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { downloadUrlFile } from '@/utils';
  import useLocale from '@/locale/useLocale';

  import type { MsPaginationI, MsTableColumn } from '@/components/pure/ms-table/type';

  const props = defineProps<{
    visible: boolean;
    fileId: string | number;
    activeFileIndex: number;
    tableData: any[];
    pagination?: MsPaginationI;
    pageChange: (page: number) => Promise<void>;
  }>();

  const emit = defineEmits(['update:visible']);

  const { file: newFile, open } = useFileSystemAccess();
  const { t } = useI18n();
  const { currentLocale } = useLocale();

  const innerVisible = ref(false);
  const fileDescriptions = ref<Description[]>([]);

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
    await new Promise((resolve) => {
      setTimeout(() => {
        resolve(true);
      }, 1000);
    });
    return true;
  }

  function handleDownload(detail: any) {
    downloadUrlFile(detail.url, detail.name);
  }

  const fileType = ref('unknown');

  function loadedFile(detail: any) {
    if (detail.type) {
      fileType.value = getFileEnum(`/${detail.type.toLowerCase()}`);
    }
    fileDescriptions.value = [
      {
        label: t('project.fileManagement.name'),
        value: detail.name,
        key: 'name',
      },
      {
        label: t('project.fileManagement.desc'),
        value: detail.desc,
        key: 'desc',
      },
      {
        label: t('project.fileManagement.type'),
        value: detail.type,
      },
      {
        label: t('project.fileManagement.size'),
        value: detail.size,
      },
      {
        label: t('project.fileManagement.creator'),
        value: detail.creator,
      },
      {
        label: t('project.fileManagement.fileModule'),
        value: detail.fileModule,
        key: 'fileModule',
      },
      {
        label: t('project.fileManagement.tag'),
        value: detail.tag,
        isTag: true,
        showTagAdd: true,
        key: 'tag',
      },
      {
        label: t('project.fileManagement.createTime'),
        value: dayjs(detail.createTime).format('YYYY-MM-DD HH:mm:ss'),
      },
    ];
    if (detail?.storage !== 'minio') {
      fileDescriptions.value.splice(
        3,
        0,
        ...[
          {
            label: t('project.fileManagement.gitBranch'),
            value: detail.gitBranch,
          },
          {
            label: t('project.fileManagement.gitPath'),
            value: detail.gitPath,
          },
          {
            label: t('project.fileManagement.gitVersion'),
            value: detail.gitVersion,
          },
        ]
      );
    }
  }

  watch(
    () => newFile.value,
    (data) => {
      if (data) {
        Message.success(t('project.fileManagement.replaceFileSuccess'));
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

  async function addFileTag(val: string) {
    await new Promise((resolve) => {
      setTimeout(() => {
        resolve(true);
      }, 2000);
    });
  }

  const activeTab = ref('case');

  const caseColumns: MsTableColumn = [
    {
      title: 'project.fileManagement.id',
      dataIndex: 'id',
      width: 100,
    },
    {
      title: 'project.fileManagement.name',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'project.fileManagement.type',
      dataIndex: 'type',
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
    setKeyword,
  } = useTable(getFileCases, {
    tableKey: TableKeyEnum.FILE_MANAGEMENT_CASE,
    scroll: { x: 800 },
    columns: caseColumns,
    selectable: true,
    showSelectAll: true,
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

  function searchCase() {
    setKeyword(keyword.value);
    loadCaseList();
  }

  function updateCase(record: any) {
    console.log(record);
  }

  const versionColumns: MsTableColumn = [
    {
      title: 'project.fileManagement.fileVersion',
      dataIndex: 'fileVersion',
    },
    {
      title: 'project.fileManagement.record',
      dataIndex: 'record',
      showTooltip: true,
    },
    {
      title: 'project.fileManagement.creator',
      dataIndex: 'creator',
      showTooltip: true,
    },
    {
      title: 'project.fileManagement.createTime',
      dataIndex: 'createTime',
      width: 180,
    },
  ];
  const {
    propsRes: versionTableProps,
    propsEvent: versionTableEvent,
    loadList: loadVersionList,
  } = useTable(getFileVersions, {
    tableKey: TableKeyEnum.FILE_MANAGEMENT_VERSION,
    scroll: { x: '100%' },
    columns: versionColumns,
    selectable: false,
  });

  watchEffect(() => {
    if (innerVisible.value) {
      if (activeTab.value === 'case') {
        loadCaseList();
      } else {
        loadVersionList();
      }
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
    width: 660px;
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
