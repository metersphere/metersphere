<template>
  <a-upload v-bind="{ ...props }" :accept="UploadAcceptEnum[props.accept]">
    <template #upload-button>
      <div class="ms-upload-area">
        <div class="ms-upload-icon-box">
          <div class="ms-upload-icon" :class="`ms-upload-icon--${props.iconType}`"></div>
        </div>
        <div v-if="props.mainText" class="ms-upload-main-text">{{ t(props.mainText) }}</div>
        <div v-if="props.subText" class="ms-upload-sub-text">{{ t(props.subText) }}</div>
      </div>
    </template>
  </a-upload>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { UploadAcceptEnum } from '@/enums/uploadEnum';

  import type { UploadType } from './types';

  const { t } = useI18n();

  // 上传 组件 props
  type UploadProps = Partial<{
    mainText: string;
    subText: string;
    class: string;
    multiple: boolean;
    limit: number;
    imagePreview: boolean;
    showFileList: boolean;
    iconType: string;
    [key: string]: any;
  }> & {
    accept: UploadType;
  };

  const props = defineProps<UploadProps>();
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

        background-image: url('@/assets/svg/icons/uploadfile.svg');
        &--excel {
          background-image: url('@/assets/svg/icons/excel.svg');
        }
        &--word {
          background-image: url('@/assets/svg/icons/word.svg');
        }
        &--pdf {
          background-image: url('@/assets/svg/icons/pdf.svg');
        }
        &--txt {
          background-image: url('@/assets/svg/icons/txt.svg');
        }
        &--vedio {
          background-image: url('@/assets/svg/icons/vedio.svg');
        }
        &--sql {
          background-image: url('@/assets/svg/icons/sql.svg');
        }
        &--csv {
          background-image: url('@/assets/svg/icons/csv.svg');
        }
        &--zip {
          background-image: url('@/assets/svg/icons/zip.svg');
        }
        &--xmind {
          background-image: url('@/assets/svg/icons/xmind.svg');
        }
        &--image {
          background-image: url('@/assets/svg/icons/image.svg');
        }
        &--jar {
          background-image: url('@/assets/svg/icons/jar.svg');
        }
      }
    }
    .ms-upload-main-text {
      color: var(--color-text-1);
    }
    .ms-upload-sub-text {
      margin-bottom: 6px;
      font-size: 12px;
      color: var(--color-text-4);
    }
  }
</style>
