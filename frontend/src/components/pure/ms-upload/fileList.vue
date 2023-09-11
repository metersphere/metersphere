<template>
  <div class="sticky top-[0] z-[9999] mb-[8px] flex justify-between bg-white">
    <a-radio-group v-model:model-value="fileListTab" type="button" size="small">
      <a-radio value="all">{{ `${t('ms.upload.all')} (${innerFileList.length})` }}</a-radio>
      <a-radio value="waiting">{{ `${t('ms.upload.uploading')} (${totalWaitingFileList.length})` }}</a-radio>
      <a-radio value="success">{{ `${t('ms.upload.success')} (${totalSuccessFileList.length})` }}</a-radio>
      <a-radio value="error">{{ `${t('ms.upload.fail')} (${totalFailFileList.length})` }}</a-radio>
    </a-radio-group>
    <slot name="tabExtra"></slot>
  </div>
  <MsList :data="filterFileList" :bordered="false" :split="false" item-border no-hover>
    <template #item="{ item }">
      <a-list-item
        class="mb-[8px] rounded-[var(--border-radius-small)] border border-solid border-[var(--color-text-n8)] !p-[8px_12px]"
      >
        <a-list-item-meta>
          <template #avatar>
            <a-avatar shape="square" class="rounded-[var(--border-radius-mini)] bg-[var(--color-text-n9)]">
              <a-image
                v-if="item.file.type.includes('image/')"
                :src="item.url"
                :alt="item.file.name"
                width="40"
                height="40"
              />
              <MsIcon
                v-else
                :type="getFileIcon(item)"
                size="24"
                :class="getFileEnum(item.file?.type) === 'unknown' ? 'text-[var(--color-text-4)]' : ''"
              ></MsIcon>
            </a-avatar>
          </template>
          <template #title>
            <div class="font-normal">{{ item.file.name }}</div>
          </template>
          <template #description>
            <div v-if="item.status === UploadStatus.init" class="text-[12px] text-[var(--color-text-4)]">
              {{ t('ms.upload.waiting') }}
            </div>
            <div v-else-if="item.status === UploadStatus.done" class="text-[12px] text-[var(--color-text-4)]">
              {{
                `${formatFileSize(item.file.size)}  ${t('ms.upload.uploadAt')} ${dayjs(item.uploadedTime).format(
                  'YYYY-MM-DD HH:mm:ss'
                )}`
              }}
            </div>
            <a-progress
              v-else-if="item.status === UploadStatus.uploading"
              :percent="asyncTaskStore.uploadFileTask.singleProgress / 100"
              :show-text="false"
              size="large"
              class="w-[200px]"
            />
            <div v-else-if="item.status === UploadStatus.error" class="text-[rgb(var(--danger-6))]">
              {{ t('ms.upload.uploadFail') }}
            </div>
          </template>
        </a-list-item-meta>
        <template #actions>
          <div class="flex items-center">
            <MsButton
              v-if="item.file.type.includes('image/')"
              type="button"
              status="primary"
              class="!mr-0"
              @click="handlePreview(item)"
            >
              {{ t('ms.upload.preview') }}
            </MsButton>
            <MsButton
              v-if="item.status === UploadStatus.error"
              type="button"
              status="secondary"
              class="!mr-0"
              @click="reupload(item)"
            >
              {{ t('ms.upload.reUpload') }}
            </MsButton>
            <MsButton type="button" status="danger" class="!mr-[4px]" @click="deleteFile(item)">
              {{ t('ms.upload.delete') }}
            </MsButton>
            <slot name="actions" :item="item"></slot>
          </div>
        </template>
      </a-list-item>
    </template>
  </MsList>
  <a-image-preview-group
    v-model:visible="previewVisible"
    v-model:current="previewCurrent"
    infinite
    :src-list="previewList"
  />
</template>

<script setup lang="ts">
  import { computed, onBeforeUnmount, ref, watch } from 'vue';
  import dayjs from 'dayjs';
  import { useI18n } from '@/hooks/useI18n';
  import { formatFileSize } from '@/utils';
  import useAsyncTaskStore from '@/store/modules/app/asyncTask';
  import MsList from '@/components/pure/ms-list/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { UploadStatus } from '@/enums/uploadEnum';
  import { getFileEnum, getFileIcon } from './iconMap';

  import type { MsFileItem } from './types';

  const props = defineProps<{
    fileList: MsFileItem[];
    route?: string; // 用于后台上传文件时，查看详情跳转的路由
    routeQuery?: Record<string, string>; // 用于后台上传文件时，查看详情跳转的路由参数
    handleDelete?: (item: MsFileItem) => void;
    handleReupload?: (item: MsFileItem) => void;
  }>();
  const emit = defineEmits<{
    (e: 'update:fileList', fileList: MsFileItem[]): void;
    (e: 'delete', item: MsFileItem): void;
    (e: 'finish'): void;
    (e: 'start'): void;
  }>();

  const asyncTaskStore = useAsyncTaskStore();
  const { t } = useI18n();

  const fileListTab = ref('all');
  const innerFileList = ref<MsFileItem[]>(props.fileList);

  watch(
    () => props.fileList,
    (val) => {
      innerFileList.value = val.sort((a, b) => {
        if (a.status === UploadStatus.init && b.status !== UploadStatus.init) {
          return -1; // "init" 排在前面
        }
        if (a.status !== UploadStatus.init && b.status === UploadStatus.init) {
          return 1; // "init" 排在前面
        }
        return 0; // 保持原始顺序
      });
    }
  );

  watch(
    () => innerFileList.value,
    (val) => {
      emit('update:fileList', val);
    }
  );

  const totalWaitingFileList = computed(() => {
    return innerFileList.value.filter(
      (e) => e.status && (e.status === UploadStatus.init || e.status === UploadStatus.uploading)
    );
  });
  const totalSuccessFileList = computed(() => {
    return innerFileList.value.filter((e) => e.status && e.status === UploadStatus.done);
  });
  const totalFailFileList = computed(() => {
    return innerFileList.value.filter((e) => e.status && e.status === UploadStatus.error);
  });

  const filterFileList = computed(() => {
    switch (fileListTab.value) {
      case 'waiting':
        return totalWaitingFileList.value;
      case 'success':
        return totalSuccessFileList.value;
      case 'error':
        return totalFailFileList.value;
      default:
        return innerFileList.value;
    }
  });

  /**
   * 开始上传
   */
  function startUpload() {
    emit('start');
    asyncTaskStore.startUpload(innerFileList.value, props.route, props.routeQuery);
  }

  /**
   * 后台上传
   */
  function backstageUpload() {
    asyncTaskStore.uploadFileTask.isBackstageUpload = true;
    if (asyncTaskStore.uploadFileTask.uploadQueue.length === 0) {
      // 开启后台上传时，如果队列为空，则说明是直接触发后台上传，并不是先startUpload然后再进行后台上传，需要触发一下上传任务开启
      startUpload();
    }
  }

  watch(
    () => asyncTaskStore.uploadFileTask.finishedTime,
    (val) => {
      if (val) {
        emit('finish');
      }
    }
  );

  const previewVisible = ref(false);
  const previewCurrent = ref(0);

  const previewList = computed(() => {
    return innerFileList.value.filter((item) => item.file?.type.includes('image/')).map((item) => item.url);
  });

  function handlePreview(item: MsFileItem) {
    previewVisible.value = true;
    previewCurrent.value = previewList.value.indexOf(item.url);
  }

  function deleteFile(item: MsFileItem) {
    if (typeof props.handleDelete === 'function') {
      props.handleDelete(item);
    } else {
      const index = innerFileList.value.findIndex((e) => e.uid === item.uid);
      if (index !== -1) {
        innerFileList.value.splice(index, 1);
      }
      emit('delete', item);
    }
  }

  function reupload(item: MsFileItem) {
    if (typeof props.handleReupload === 'function') {
      props.handleReupload(item);
    } else {
      item.status = UploadStatus.init;
      if (asyncTaskStore.uploadFileTask.uploadQueue.length > 0) {
        // 此时队列中还有任务，则 push 入队列末尾
        asyncTaskStore.uploadFileTask.uploadQueue.push(item);
      } else {
        // 此时队列任务已清空
        startUpload();
      }
    }
  }

  // 在组件销毁时清除定时器
  onBeforeUnmount(() => {
    if (asyncTaskStore.uploadFileTask.timer !== null) {
      clearInterval(asyncTaskStore.uploadFileTask.timer);
      asyncTaskStore.uploadFileTask.timer = null;
    }
  });

  defineExpose({
    startUpload,
    backstageUpload,
  });
</script>

<style lang="less" scoped></style>
