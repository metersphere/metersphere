<template>
  <a-upload
    v-if="showDropArea"
    v-bind="{ ...props }"
    v-model:file-list="fileList"
    :accept="
      [UploadAcceptEnum.none, UploadAcceptEnum.unknown].includes(UploadAcceptEnum[props.accept])
        ? '*'
        : UploadAcceptEnum[props.accept]
    "
    :multiple="props.multiple"
    :disabled="props.disabled"
    :class="getAllScreenClass"
    :style="{
      width: props.isAllScreen ? `calc(100% - ${menuWidth}px - 16px)` : '',
    }"
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
    draggable: boolean; // 是否支持拖拽上传
    isAllScreen?: boolean; // 是否是全屏显示拖拽上传
    cutHeight: number; // 被剪切高度
  }> & {
    accept: UploadType;
    fileList: MsFileItem[];
  };

  const props = withDefaults(defineProps<UploadProps>(), {
    showSubText: true,
    isLimit: true,
    isAllScreen: false,
    cutHeight: 110,
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

  const total = ref(''); // 总高度
  const other = ref(''); // 被减去高度
  const showDropArea = ref(!props.isAllScreen);

  watch(
    () => props.isAllScreen,
    (val) => {
      if (val) {
        total.value = '100vh';
        other.value = `${props.cutHeight}px`;
        showDropArea.value = false;
      } else {
        total.value = '154px';
        other.value = '0px';
        showDropArea.value = true;
      }
    },
    { immediate: true }
  );

  const getAllScreenClass = computed(() => {
    return props.isAllScreen ? ['!fixed', 'right-[16px]', '-bottom-[10px]', 'z-[999]', 'opacity-90'] : [];
  });

  // 禁用默认拖拽事件
  function disableDefaultEvents() {
    const doc = document.documentElement;
    doc.addEventListener('dragleave', (e) => e.preventDefault()); // 拖离
    doc.addEventListener('drop', (e) => e.preventDefault()); // 拖后放
    doc.addEventListener('dragenter', (e) => e.preventDefault()); // 拖进
    doc.addEventListener('dragover', (e) => e.preventDefault()); // 结束拖拽
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
      ele.addEventListener('dragenter', () => {
        showDropArea.value = true;
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
    if (props.isAllScreen) resizeObserver.value.disconnect();
  });
</script>

<style lang="less" scoped>
  .ms-upload-area {
    height: calc(v-bind(total) - v-bind(other));
    border: 1px dashed var(--color-text-input-border);
    border-color: rgb(var(--primary-5)) !important;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);

    @apply flex flex-col items-center justify-center;
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
      line-height: 16px;
    }
  }
</style>
