<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="960"
    :footer="false"
    class="ms-drawer"
    no-content-padding
    unmount-on-close
  >
    <template #title>
      <div class="flex w-full items-center">
        {{ t('project.fileManagement.detail') }}
        <a-tooltip
          :content="isFirst ? t('project.fileManagement.noPrev') : t('project.fileManagement.prev')"
          :mouse-enter-delay="300"
          mini
        >
          <a-button
            type="outline"
            size="mini"
            class="arco-btn-outline--secondary ml-[16px] mr-[4px]"
            :disabled="isFirst || fileLoading"
            @click="emit('prevFile')"
          >
            <template #icon>
              <icon-left />
            </template>
          </a-button>
        </a-tooltip>
        <a-tooltip
          :content="isLast ? t('project.fileManagement.noNext') : t('project.fileManagement.next')"
          :mouse-enter-delay="300"
          mini
        >
          <a-button
            type="outline"
            size="mini"
            class="arco-btn-outline--secondary"
            :disabled="isLast || fileLoading"
            @click="emit('nextFile')"
          >
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </a-tooltip>
        <div class="ml-auto flex items-center">
          <a-switch
            v-if="fileType === 'jar'"
            :default-checked="fileDetail?.enable"
            :before-change="handleEnableIntercept"
            :disabled="fileLoading"
            size="small"
            class="mr-[8px]"
          />
          <MsButton
            type="icon"
            status="secondary"
            class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
            :disabled="fileLoading"
            @click="handleDownload"
          >
            <MsIcon type="icon-icon_bottom-align_outlined" class="mr-[4px]" />
            {{ t('project.fileManagement.download') }}
          </MsButton>
          <MsButton
            v-if="fileDetail?.storage !== 'minio'"
            type="icon"
            status="secondary"
            class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
            :disabled="fileLoading"
          >
            <MsIcon type="icon-icon_reset_outlined" class="mr-[4px]" />
            {{ t('project.fileManagement.updateFile') }}
          </MsButton>
        </div>
      </div>
    </template>
    <div class="flex h-full">
      <a-spin :loading="fileLoading">
        <div class="file-detail">
          <div class="file-detail-icon" @click="handleFileIconClick">
            <img
              v-if="fileType === 'image'"
              src="https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/6480dbc69be1b5de95010289787d64f1.png~tplv-uwbnlip3yd-webp.webp"
              class="h-full w-full"
            />
            <MsIcon v-else :type="FileIconMap[fileType][UploadStatus.done]" class="h-full w-full" />
            <div class="file-detail-icon-footer">
              {{ t('project.fileManagement.replaceFile') }}
            </div>
          </div>
          <MsDescription :descriptions="fileDescriptions" label-width="80px" :add-tag-func="addFileTag">
            <template #value="{ item }">
              <div class="flex flex-wrap items-center">
                <a-tooltip
                  :content="(item.value as string)"
                  :mouse-enter-delay="300"
                  :disabled="item.value === undefined || item.value === null || item.value?.toString() === ''"
                  mini
                >
                  <div :class="['one-line-text', item.key === 'name' ? 'max-w-[100px]' : '']">
                    {{
                      item.value === undefined || item.value === null || item.value?.toString() === ''
                        ? '-'
                        : item.value
                    }}
                  </div>
                </a-tooltip>
                <template v-if="item.key === 'name'">
                  <popConfirm mode="rename" :title="t('common.rename')" :all-names="[]">
                    <MsButton class="!mr-[4px] ml-[8px]">{{ t('common.rename') }}</MsButton>
                  </popConfirm>
                  <MsButton v-if="fileType === 'image'" class="ml-0" @click="previewVisible = true">
                    {{ t('common.preview') }}
                  </MsButton>
                </template>
              </div>
            </template>
          </MsDescription>
        </div>
      </a-spin>
      <div class="file-relation">
        <a-tabs v-model:active-key="activeTab" :disabled="fileLoading" class="no-content">
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
          <ms-base-table v-if="activeTab === 'version'" v-bind="versionTableProps" no-disable v-on="versionTableEvent">
          </ms-base-table>
        </div>
      </div>
    </div>
    <a-image-preview v-model:visible="previewVisible" :src="fileDetail?.url" />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref, watch, computed, watchEffect } from 'vue';
  import dayjs from 'dayjs';
  import { useFileSystemAccess } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { getFileEnum, FileIconMap } from '@/components/pure/ms-upload/iconMap';
  import MsDescription, { type Description } from '@/components/pure/ms-description/index.vue';
  import popConfirm from './popConfirm.vue';
  import { UploadStatus } from '@/enums/uploadEnum';
  import useTable from '@/components/pure/ms-table/useTable';
  import { getFileDetail, getFileCases, getFileVersions } from '@/api/modules/project-management/fileManagement';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { downloadUrlFile } from '@/utils';

  import type { MsTableColumn } from '@/components/pure/ms-table/type';

  const props = defineProps<{
    visible: boolean;
    fileId: string | number;
    isFirst?: boolean;
    isLast?: boolean;
  }>();

  const emit = defineEmits(['update:visible', 'prevFile', 'nextFile']);

  const { file: newFile, open } = useFileSystemAccess();
  const { t } = useI18n();

  const innerVisible = ref(false);
  const fileDetail = ref();
  const fileDescriptions = ref<Description[]>([]);
  const fileLoading = ref(false);

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

  async function initFileDetail() {
    try {
      fileLoading.value = true;
      await new Promise((resolve) => {
        setTimeout(() => {
          resolve(true);
        }, 3000);
      });
      fileDetail.value = await getFileDetail(props.fileId);
      fileDescriptions.value = [
        {
          label: t('project.fileManagement.name'),
          value: fileDetail.value.name,
          key: 'name',
        },
        {
          label: t('project.fileManagement.type'),
          value: fileDetail.value.type,
        },
        {
          label: t('project.fileManagement.size'),
          value: fileDetail.value.size,
        },
        {
          label: t('project.fileManagement.creator'),
          value: fileDetail.value.creator,
        },
        {
          label: t('project.fileManagement.fileModule'),
          value: fileDetail.value.fileModule,
          key: 'fileModule',
        },
        {
          label: t('project.fileManagement.tag'),
          value: fileDetail.value.tag,
          isTag: true,
          showTagAdd: true,
          key: 'tag',
        },
        {
          label: t('project.fileManagement.createTime'),
          value: dayjs(fileDetail.value.createTime).format('YYYY-MM-DD HH:mm:ss'),
        },
      ];
      if (fileDetail.value?.storage !== 'minio') {
        fileDescriptions.value.splice(
          3,
          0,
          ...[
            {
              label: t('project.fileManagement.gitBranch'),
              value: fileDetail.value.gitBranch,
            },
            {
              label: t('project.fileManagement.gitPath'),
              value: fileDetail.value.gitPath,
            },
            {
              label: t('project.fileManagement.gitVersion'),
              value: fileDetail.value.gitVersion,
            },
          ]
        );
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      fileLoading.value = false;
    }
  }

  watch(
    () => props.fileId,
    () => {
      initFileDetail();
    },
    {
      immediate: true,
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

  function handleDownload() {
    downloadUrlFile(fileDetail.value.url, fileDetail.value.name);
  }

  const fileType = computed(() => {
    if (fileDetail.value?.type) {
      return getFileEnum(`/${fileDetail.value.type.toLowerCase()}`);
    }
    return 'unknown';
  });

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
    },
    {
      title: 'project.fileManagement.creator',
      dataIndex: 'creator',
    },
    {
      title: 'project.fileManagement.createTime',
      dataIndex: 'createTime',
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
    if (activeTab.value === 'case') {
      loadCaseList();
    } else {
      loadVersionList();
    }
  });
</script>

<style lang="less" scoped>
  .file-detail {
    @apply relative h-full;

    padding: 16px;
    width: 300px;
    border-right: 1px solid var(--color-text-n8);
    .file-detail-icon {
      @apply relative inline-block cursor-pointer overflow-hidden;

      margin-bottom: 16px;
      width: 102px;
      height: 102px;
      border-radius: var(--border-radius-small);
      background-color: var(--color-text-n9);
      &:hover {
        .file-detail-icon-footer {
          @apply visible;
        }
      }
      .file-detail-icon-footer {
        @apply invisible absolute w-full text-center;

        bottom: 0;
        padding: 2px 0;
        font-size: 12px;
        font-weight: 500;
        color: #ffffff;
        background-color: #00000050;
      }
    }
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
