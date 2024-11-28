<template>
  <div>
    <div
      v-if="props.mode === 'remote' && props.showTab"
      class="sticky top-[0] z-[9999] mb-[8px] flex justify-between bg-[var(--color-text-fff)]"
    >
      <a-radio-group v-model:model-value="fileListTab" type="button" size="small">
        <a-radio value="all">{{ `${t('ms.upload.all')} (${innerFileList.length})` }}</a-radio>
        <a-radio value="waiting">{{ `${t('ms.upload.uploading')} (${totalWaitingFileList.length})` }}</a-radio>
        <a-radio value="success">{{ `${t('ms.upload.success')} (${totalSuccessFileList.length})` }}</a-radio>
        <a-radio value="error">{{ `${t('ms.upload.fail')} (${totalFailFileList.length})` }}</a-radio>
      </a-radio-group>
      <slot name="tabExtra"></slot>
    </div>
    <MsList
      v-if="props.showMode === 'fileList'"
      :data="filterFileList"
      :bordered="false"
      :split="false"
      item-border
      no-hover
    >
      <template #item="{ item }">
        <a-list-item
          class="mb-[8px] w-full rounded-[var(--border-radius-small)] border border-solid border-[var(--color-text-n8)] !p-[8px_12px]"
          :style="{
            borderColor: item.status === UploadStatus.error ? 'rgb(var(--danger-5))' : 'var(--color-text-n8)',
          }"
        >
          <a-list-item-meta>
            <template #avatar>
              <a-avatar shape="square" class="rounded-[var(--border-radius-mini)] bg-[var(--color-text-n9)]">
                <a-image v-if="item.file.type.includes('image/')" :src="item.url" width="40" height="40" hide-footer />
                <MsIcon v-else :type="getFileIcon(item)" size="24" :class="'text-[var(--color-text-4)]'" />
              </a-avatar>
            </template>
            <template #title>
              <div class="m-b[2px] flex items-center">
                <a-tooltip :content="item.file.name">
                  <div class="show-file-name">
                    <div
                      :class="`file-name-first one-line-text pl-[4px] font-normal max-w-[${
                        props.fileNameMaxWidth || '300px'
                      }]`"
                    >
                      {{ item.file.name.slice(0, item.file.name.indexOf('.')) }}
                    </div>
                    <span class="font-normal text-[var(--color-text-1)]">
                      {{ item.file.name.slice(item.file.name.lastIndexOf('.')) }}
                    </span>
                  </div>
                </a-tooltip>
                <slot name="title" :item="item"></slot>
                <div v-if="props.buttonInTitle" class="ml-auto flex items-center font-normal">
                  <slot name="titleAction" :item="item">
                    <MsButton
                      v-if="item.file.type.includes('image/')"
                      type="button"
                      status="primary"
                      class="!mr-0"
                      @click="handlePreview(item)"
                    >
                      {{ t('ms.upload.preview') }}
                    </MsButton>
                    <a-divider v-if="item.file.type.includes('image/')" direction="vertical" />
                    <MsButton
                      v-if="item.status === UploadStatus.error"
                      type="button"
                      status="secondary"
                      class="!mr-0"
                      @click="reupload(item)"
                    >
                      {{ t('ms.upload.reUpload') }}
                    </MsButton>
                    <a-divider v-if="item.status === UploadStatus.error" direction="vertical" />
                  </slot>
                  <MsButton
                    v-if="props.showDelete && item.status !== 'uploading'"
                    type="button"
                    :status="item.deleteContent ? 'primary' : 'danger'"
                    class="!mr-[4px]"
                    @click="deleteFile(item)"
                  >
                    {{ t(item.deleteContent) || t('ms.upload.delete') }}
                  </MsButton>
                </div>
              </div>
            </template>
            <template #description>
              <div
                v-if="item.status === UploadStatus.init"
                class="text-[12px] leading-[16px] text-[var(--color-text-4)]"
              >
                {{ initFileSaveTips ? initFileSaveTips : t('ms.upload.waiting') }}
              </div>
              <div
                v-else-if="item.status === UploadStatus.done"
                class="flex items-center gap-[8px] pl-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]"
              >
                <div class="one-line-text max-w-[421px]" style="display: flex">
                  <a-tooltip
                    :content="`${formatFileSize(item.file.size)}  ${item.createUserName || ''}  ${getUploadDesc(
                      item
                    )} ${dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss')}`"
                  >
                    <div class="one-line-text">
                      {{
                        `${formatFileSize(item.file.size)}  ${item.createUserName || ''}  ${getUploadDesc(
                          item
                        )} ${dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss')}`
                      }}
                    </div>
                  </a-tooltip>
                  <div v-if="showUploadSuccess(item)" class="ml-4 flex items-center">
                    <MsIcon type="icon-icon_succeed_colorful" class="mr-2" />
                    {{ t('ms.upload.uploadSuccess') }}
                  </div>
                </div>
              </div>
              <a-progress
                v-else-if="item.status === UploadStatus.uploading"
                :percent="asyncTaskStore.uploadFileTask.singleProgress / 100"
                :show-text="false"
                size="large"
                class="w-[200px]"
              />
              <div
                v-else-if="item.status === UploadStatus.error"
                class="text-[12px] leading-[20px] text-[rgb(var(--danger-6))]"
              >
                {{ item.errMsg || t('ms.upload.uploadFail') }}
              </div>
            </template>
          </a-list-item-meta>
          <template v-if="!props.buttonInTitle" #actions>
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
              <a-divider v-if="item.file.type.includes('image/')" direction="vertical" />
              <MsButton
                v-if="item.status === UploadStatus.error"
                type="button"
                status="secondary"
                class="!mr-0"
                @click="reupload(item)"
              >
                {{ t('ms.upload.reUpload') }}
              </MsButton>
              <a-divider v-if="item.status === UploadStatus.error" direction="vertical" />
              <MsButton
                v-if="props.showDelete && item.status !== 'uploading'"
                type="button"
                :status="item.deleteContent ? 'primary' : 'danger'"
                class="!mr-[4px]"
                @click="deleteFile(item)"
              >
                {{ t(item.deleteContent) || t('ms.upload.delete') }}
              </MsButton>
              <slot name="actions" :item="item"></slot>
            </div>
          </template>
        </a-list-item>
      </template>
    </MsList>
    <div v-else class="flex w-full items-center gap-[8px]">
      <div v-for="item of filterFileList" :key="item.uid" class="image-item">
        <a-image
          :src="item.url"
          width="40"
          height="40"
          :preview="false"
          class="cursor-pointer"
          @click="handlePreview(item)"
        />
        <icon-close-circle-fill class="image-item-close-icon" @click="deleteFile(item)" />
      </div>
    </div>
    <a-image-preview-group
      v-model:visible="previewVisible"
      v-model:current="previewCurrent"
      infinite
      :src-list="previewList"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeUnmount, ref, watch } from 'vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAsyncTaskStore from '@/store/modules/app/asyncTask';
  import { formatFileSize } from '@/utils';

  import { UploadStatus } from '@/enums/uploadEnum';

  import { getFileIcon } from './iconMap';
  import type { MsFileItem } from './types';

  const props = withDefaults(
    defineProps<{
      mode?: 'static' | 'remote'; // 静态|远程
      fileList: MsFileItem[];
      showMode?: 'fileList' | 'imageList'; // 展示模式, 文件列表|图片列表
      uploadFunc?: (params: any) => Promise<any>; // 上传文件时，自定义上传方法
      requestParams?: Record<string, any>; // 上传文件时，额外的请求参数
      route?: string; // 用于后台上传文件时，查看详情跳转的路由
      routeQuery?: Record<string, string>; // 用于后台上传文件时，查看详情跳转的路由参数
      showTab?: boolean; // 是否显示tab
      handleDelete?: (item: MsFileItem) => void;
      handleReupload?: (item: MsFileItem) => void;
      showDelete?: boolean; // 是否展示删除按钮
      handleView?: (item: MsFileItem) => void; // 是否自定义预览
      showUploadTypeDesc?: boolean; // 自定义上传类型关联于&上传于
      initFileSaveTips?: string; // 上传初始文件时的提示
      buttonInTitle?: boolean; // 按钮是否在标题中
      fileNameMaxWidth?: string; // 文件名称最大宽度
    }>(),
    {
      mode: 'remote',
      showTab: true,
      showDelete: true,
      showMode: 'fileList',
      boolean: false,
      showUploadTypeDesc: false,
      buttonInTitle: false,
    }
  );
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

  function getUploadDesc(item: MsFileItem) {
    if (item.local !== undefined) {
      return item.local ? t('ms.upload.uploadAt') : t('ms.upload.associatedAt');
    }
    return t('ms.upload.uploadAt');
  }
  function showUploadSuccess(item: MsFileItem) {
    if (item.local !== undefined) {
      return item.local;
    }
    return true;
  }

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
    if (props.mode === 'remote' && props.uploadFunc) {
      asyncTaskStore.setUploadFunc(props.uploadFunc, props.requestParams);
      asyncTaskStore.startUpload(innerFileList.value, props.route, props.routeQuery);
    }
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
    return innerFileList.value.filter((item: any) => item.file?.type.includes('image/')).map((item: any) => item.url);
  });

  function handlePreview(item: MsFileItem) {
    if (typeof props.handleView === 'function') {
      props.handleView(item);
    } else {
      previewVisible.value = true;
      previewCurrent.value = previewList.value.indexOf(item.url);
    }
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
      item.status = UploadStatus.init; // 重置状态
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
      clearInterval(asyncTaskStore.uploadFileTask.timer as unknown as number);
      asyncTaskStore.uploadFileTask.timer = null;
    }
  });

  defineExpose({
    startUpload,
    backstageUpload,
  });
</script>

<style lang="less" scoped>
  :deep(.arco-list) {
    overflow: auto;
    .arco-list-content {
      min-width: 425px;
    }
  }
  .image-item {
    @apply relative;
    &:hover {
      .image-item-close-icon {
        @apply visible;
      }
    }
    .image-item-close-icon {
      @apply invisible absolute cursor-pointer rounded-full;

      top: -7px;
      right: -5px;
      z-index: 10;
      color: var(--color-text-4);
      background-color: var(--color-text-n8);
      cursor: pointer;
    }
  }
  .show-file-name {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    padding-bottom: 4px;
  }
</style>
