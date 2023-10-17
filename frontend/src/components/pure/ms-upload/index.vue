<template>
  <a-upload
    v-bind="{ ...props }"
    v-model:file-list="fileList"
    :accept="UploadAcceptEnum[props.accept]"
    :multiple="props.multiple"
    :disabled="props.disabled"
    @change="handleChange"
    @before-upload="beforeUpload"
  >
    <template #upload-button>
      <slot>
        <div class="ms-upload-area">
          <div class="ms-upload-icon-box">
            <MsIcon
              v-if="props.accept !== UploadAcceptEnum.none"
              :type="FileIconMap[props.accept][UploadStatus.done]"
              class="ms-upload-icon"
            />
            <div v-else class="ms-upload-icon ms-upload-icon--default"></div>
          </div>
          <!-- 支持多文件上传时，不需要展示选择文件后的信息，已选的文件使用文件列表搭配展示 -->
          <template v-if="fileList.length === 0 || props.multiple">
            <div class="ms-upload-main-text">
              {{ t(props.mainText || 'ms.upload.importModalDragText') }}
            </div>
            <div v-if="showSubText" class="ms-upload-sub-text">
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
      </slot>
    </template>
  </a-upload>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { formatFileSize } from '@/utils';

  import { UploadAcceptEnum, UploadStatus } from '@/enums/uploadEnum';

  import { FileIconMap } from './iconMap';
  import type { MsFileItem, UploadType } from './types';

  const { t } = useI18n();

  // 上传 组件 props
  type UploadProps = Partial<{
    mainText: string; // 主要文案
    subText: string; // 次要文案
    showSubText: boolean; // 是否显示次要文案
    class: string;
    multiple: boolean;
    imagePreview: boolean;
    showFileList: boolean;
    disabled: boolean;
    iconType: string;
    maxSize: number; // 文件大小限制，单位 MB
    sizeUnit: 'MB' | 'KB'; // 文件大小单位
    isLimit: boolean; // 是否限制文件大小
  }> & {
    accept: UploadType;
    fileList: MsFileItem[];
  };

  const props = withDefaults(defineProps<UploadProps>(), {
    showSubText: true,
    isLimit: true,
  });
  const emit = defineEmits(['update:fileList', 'change']);

  const defaultMaxSize = 50;

  const fileList = ref<MsFileItem[]>(props.fileList);

  watch(
    () => props.fileList,
    (val) => {
      fileList.value = val;
    }
  );

  watch(
    () => fileList.value,
    (val) => {
      emit('update:fileList', val);
    }
  );

  async function beforeUpload(file: File) {
    if (!props.multiple && fileList.value.length > 0) {
      // 单文件上传时，清空之前的文件
      fileList.value = [];
    }
    const maxSize = props.maxSize || defaultMaxSize;
    const _maxSize = props.sizeUnit === 'MB' ? maxSize * 1024 * 1024 : maxSize * 1024;
    if (props.isLimit && file.size > _maxSize) {
      Message.warning(t('ms.upload.overSize'));
      return Promise.resolve(false);
    }
    return Promise.resolve(true);
  }

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    emit('change', _fileList, fileItem);
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
