<template>
  <a-upload
    v-bind="{ ...props }"
    v-model:file-list="fileList"
    :accept="UploadAcceptEnum[props.accept]"
    :multiple="props.multiple"
    @before-upload="beforeUpload"
  >
    <template #upload-button>
      <div class="ms-upload-area">
        <div class="ms-upload-icon-box">
          <MsIcon v-if="fileList.length > 0" :type="IconMap[props.accept]" class="ms-upload-icon" />
          <div v-else class="ms-upload-icon ms-upload-icon--default"></div>
        </div>
        <template v-if="fileList.length === 0">
          <div class="ms-upload-main-text">
            {{ t(props.mainText || 'ms.upload.importModalDragtext') }}
          </div>
          <div class="ms-upload-sub-text">
            {{
              t(props.subText || 'ms.upload.importModalFileTip', {
                type: UploadAcceptEnum[props.accept],
                size: props.maxSize || defaultMaxSize,
              })
            }}
          </div>
        </template>
        <template v-else>
          <div class="ms-upload-main-text">
            {{ fileList[0]?.name }}
          </div>
          <div class="ms-upload-sub-text">{{ formatFileSize(fileList[0]?.file?.size || 0) }}</div>
        </template>
      </div>
    </template>
  </a-upload>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { UploadAcceptEnum } from '@/enums/uploadEnum';
  import { formatFileSize } from '@/utils';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { FileItem, Message } from '@arco-design/web-vue';
  import type { UploadType } from './types';

  const { t } = useI18n();

  // 上传 组件 props
  type UploadProps = Partial<{
    mainText: string; // 主要文案
    subText: string; // 次要文案
    class: string;
    multiple: boolean;
    imagePreview: boolean;
    showFileList: boolean;
    iconType: string;
    maxSize: number; // 文件大小限制，单位 MB
    [key: string]: any;
  }> & {
    accept: UploadType;
    fileList: FileItem[];
  };

  const props = defineProps<UploadProps>();
  const emit = defineEmits(['update:fileList']);

  const defaultMaxSize = 50;

  const fileList = ref<FileItem[]>(props.fileList);

  watch(
    () => fileList.value,
    (val) => {
      emit('update:fileList', val);
    }
  );

  const IconMap = {
    excel: 'icon-icon_file-excel_colorful1',
    word: 'icon-icon_file-word_colorful1',
    pdf: 'icon-icon_file-pdf_colorful1',
    txt: 'icon-icon_file-text_colorful1',
    vedio: 'icon-icon_file-vedio_colorful1',
    sql: 'icon-icon_file-sql_colorful1',
    csv: 'icon-icon_file-CSV_colorful1',
    zip: 'icon-a-icon_file-compressed_colorful1',
    xmind: 'icon-icon_file-xmind_colorful1',
    image: 'icon-icon_file-image_colorful1',
    jar: 'icon-icon_file-jar_colorful',
  };

  async function beforeUpload(file: File) {
    if (!props.multiple && fileList.value.length > 0) {
      // 单文件上传时，清空之前的文件
      fileList.value = [];
    }
    const maxSize = props.maxSize || defaultMaxSize;
    if (file.size > maxSize * 1024 * 1024) {
      Message.warning(t('ms.upload.overSize'));
      return Promise.resolve(false);
    }
    return Promise.resolve(true);
  }
</script>

<style lang="less" scoped>
  .ms-upload-area {
    @apply flex w-full flex-col items-center justify-center;

    height: 154px;
    border: 1px dashed var(--color-text-input-border);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
    .ms-upload-icon-box {
      @apply rounded-full bg-white;

      margin-bottom: 16px;
      padding: 8px;
      width: 48px;
      height: 48px;
      .ms-upload-icon {
        @apply h-full w-full bg-cover bg-no-repeat;
        &--default {
          background-image: url('@/assets/svg/icons/uploadfile.svg');
        }
      }
    }
    .ms-upload-main-text {
      @apply flex items-center justify-center gap-1;

      color: var(--color-text-1);
    }
    .ms-upload-sub-text {
      margin-bottom: 6px;
      font-size: 12px;
      color: var(--color-text-4);
    }
  }
</style>
