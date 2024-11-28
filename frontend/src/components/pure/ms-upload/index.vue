<template>
  <a-upload
    v-if="showDropArea"
    v-bind="{ ...props }"
    v-model:file-list="innerFileList"
    :accept="
      [UploadAcceptEnum.none, UploadAcceptEnum.unknown].includes(UploadAcceptEnum[props.accept])
        ? '*'
        : UploadAcceptEnum[props.accept]
    "
    :multiple="props.multiple"
    :disabled="props.disabled"
    :class="getAllScreenClass"
    @change="handleChange"
    @before-upload="beforeUpload"
    @exceed-limit="() => Message.warning(t('ms.upload.overLimit', { limit: props.limit }))"
  >
    <template #upload-button>
      <slot>
        <div
          class="ms-upload-area"
          :class="[
            props.isAllScreen ? 'ms-upload-area-dotted-border h-[100vh]' : 'ms-upload-area-thin-border h-[154px]',
          ]"
        >
          <div class="ms-upload-icon-box">
            <MsIcon
              v-if="props.accept !== UploadAcceptEnum.none"
              :type="fileIconType"
              class="ms-upload-icon text-[var(--color-text-4)]"
            />
            <div v-else class="ms-upload-icon ms-upload-icon--default"></div>
          </div>
          <!-- 支持多文件上传时，不需要展示选择文件后的信息，已选的文件使用文件列表搭配展示 -->
          <template v-if="innerFileList.length === 0 || props.multiple">
            <div class="ms-upload-main-text">
              {{ t(props.mainText || 'ms.upload.importModalDragText') }}
            </div>
            <div v-if="showSubText" class="ms-upload-sub-text">
              <slot name="subText">
                {{
                  t(props.subText || 'ms.upload.importModalFileTip', {
                    type: UploadAcceptEnum[props.accept],
                    size: props.maxSize || appStore.getFileMaxSize,
                  })
                }}
              </slot>
            </div>
          </template>
          <template v-else>
            <div class="ms-upload-main-text w-full">
              <a-tooltip :content="innerFileList[0]?.name">
                <span class="one-line-text w-[80%] text-center"> {{ innerFileList[0]?.name }}</span>
              </a-tooltip>
            </div>
            <div class="ms-upload-sub-text">{{ formatFileSize(innerFileList[0]?.file?.size || 0) }}</div>
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
  import { FileIconMap, getFileEnum, getFileIcon } from '@/components/pure/ms-upload/iconMap';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { formatFileSize } from '@/utils';

  import { UploadAcceptEnum, UploadStatus } from '@/enums/uploadEnum';

  import type { MsFileItem, UploadType } from './types';

  const { t } = useI18n();

  // 上传 组件 props
  type UploadProps = Partial<{
    fileList: MsFileItem[];
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
    draggable: boolean; // 是否支持拖拽上传
    isAllScreen?: boolean; // 是否是全屏显示拖拽上传
    fileTypeTip?: string; // 上传文件类型错误提示
    limit: number; // 限制上传文件数量
    allowRepeat?: boolean; // 自定义上传文件框，是否允许重复文件名替换
  }> & {
    accept: UploadType;
  };

  const props = withDefaults(defineProps<UploadProps>(), {
    showSubText: true,
    isLimit: true,
    isAllScreen: false,
    cutHeight: 110,
    allowRepeat: false,
    sizeUnit: 'MB',
  });

  const emit = defineEmits(['update:fileList', 'change']);

  const appStore = useAppStore();

  const innerFileList = defineModel<MsFileItem[]>('fileList', {
    default: () => [],
  });

  const fileIconType = computed(() => {
    // 单选并且选了文件，按文件类型展示图标(单选文件选择后直接展示绿色图标)
    if (innerFileList.value.length > 0 && !props.multiple) {
      return getFileIcon(innerFileList.value[0], UploadStatus.done);
    }
    // 多选直接按照类型展示
    return FileIconMap[props.accept][UploadStatus.init];
  });

  async function beforeUpload(file: File) {
    if (innerFileList.value.length > 0) {
      // 附件上传校验名称重复
      if (!props.allowRepeat) {
        const isRepeat = innerFileList.value.filter((item) => item.name === file.name && item.local).length >= 1;
        if (isRepeat) {
          Message.warning(t('ms.add.attachment.repeatFileTip'));
          return Promise.resolve(false);
        }
      }
    }
    const maxSize = props.maxSize || appStore.getFileMaxSize;
    const _maxSize = props.sizeUnit === 'MB' ? maxSize * 1024 * 1024 : maxSize * 1024;
    if (props.isLimit && file.size > _maxSize) {
      Message.warning(t('ms.upload.overSize', { size: maxSize, unit: props.sizeUnit }));
      return Promise.resolve(false);
    }
    if (!props.multiple) {
      // 单文件上传时，清空之前的文件（得放到校验文件大小之后，避免文件大小限制后文件丢失）
      innerFileList.value = [];
    }
    const fileFormatMatch = file.name.match(/\.([a-zA-Z0-9]+)$/);
    const fileFormatType = fileFormatMatch ? fileFormatMatch[1] : 'none';
    // 校验类型
    if (props.accept !== getFileEnum(fileFormatType) && props.accept !== 'none') {
      Message.error(props.fileTypeTip ? props.fileTypeTip : t('ms.upload.fileTypeValidate', { type: props.accept }));
      return Promise.resolve(false);
    }
    return Promise.resolve(true);
  }

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    fileItem.local = true;
    emit('change', _fileList, fileItem);
  }

  const showDropArea = ref(!props.isAllScreen);

  watch(
    () => props.isAllScreen,
    (val) => {
      showDropArea.value = !val;
    },
    { immediate: true }
  );

  const getAllScreenClass = computed(() => {
    return props.isAllScreen
      ? ['!fixed', 'right-0', 'left-0', 'bottom-0', 'top-0', 'm-auto', 'z-[999]', 'opacity-90']
      : [];
  });

  // 禁用默认拖拽事件
  function disableDefaultEvents() {
    const doc = document.querySelector('body');
    if (doc) {
      doc.addEventListener('dragleave', (e) => e.preventDefault()); // 拖离
      doc.addEventListener('drop', (e) => e.preventDefault()); // 拖后放
      doc.addEventListener('dragenter', (e) => e.preventDefault()); // 拖进
      doc.addEventListener('dragover', (e) => e.preventDefault()); // 结束拖拽
    }
  }

  const menuWidth = ref<number>();
  const resizeObserver = ref();
  const targetElement = ref();

  function init() {
    const ele = document.querySelector('body');
    targetElement.value = document.querySelector('.menu-wrapper');
    resizeObserver.value = new ResizeObserver((entries) => {
      entries.forEach((item) => {
        menuWidth.value = item.contentRect.width;
      });
    });

    resizeObserver.value.observe(targetElement.value);
    menuWidth.value = targetElement.value.getBoundingClientRect().width;
    if (ele) {
      ele.addEventListener('dragenter', (event) => {
        const { dataTransfer } = event;
        if (dataTransfer && dataTransfer.types.includes('Files')) {
          // 处理拖拽的文件
          showDropArea.value = true;
        }
      });
      // 拖后放
      ele.addEventListener('dragleave', (e: any) => {
        if (
          e.target.nodeName === 'HTML' ||
          e.target === e.explicitOriginalTarget ||
          (!e.fromElement &&
            (e.clientX <= 0 || e.clientY <= 0 || e.clientX >= window.innerWidth || e.clientY >= window.innerHeight))
        ) {
          showDropArea.value = false;
        }
      });
      // 拖离
      ele.addEventListener('drop', (e) => {
        showDropArea.value = false;
        e.preventDefault();
      });
    }
  }

  onMounted(() => {
    if (props.isAllScreen) {
      disableDefaultEvents();
      init();
    }
  });

  onBeforeUnmount(() => {
    if (props.isAllScreen) resizeObserver.value?.disconnect();
  });
</script>

<style lang="less" scoped>
  .ms-upload-area {
    border-color: rgb(var(--primary-5)) !important;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);

    @apply flex flex-col items-center justify-center;
    .ms-upload-icon-box {
      @apply rounded-full;

      margin-bottom: 16px;
      padding: 8px;
      width: 48px;
      height: 48px;
      background-color: var(--color-text-fff);
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
      line-height: 16px;
    }
  }
  .ms-upload-area-thin-border {
    border: 1px dashed var(--color-text-input-border);
  }
  .ms-upload-area-dotted-border {
    border: 4px dashed var(--color-text-input-border);
  }
</style>
