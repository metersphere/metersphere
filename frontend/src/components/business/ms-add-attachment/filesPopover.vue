<template>
  <a-popover
    v-model:popup-visible="visible"
    trigger="click"
    position="bl"
    :disabled="inputFiles.length === 0"
    content-class="ms-add-attachment-files-popover"
    arrow-class="hidden"
    :popup-offset="0"
  >
    <slot></slot>
    <template #content>
      <div class="flex w-[200px] flex-col gap-[8px]">
        <template v-if="alreadyDeleteFiles.length > 0">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-[4px]">
              <icon-exclamation-circle-fill class="!text-[rgb(var(--warning-6))]" :size="18" />
              <div class="text-[var(--color-text-4)]">{{ t('ms.add.attachment.alreadyDelete') }}</div>
            </div>
            <MsButton type="text" :disabled="props.disabled" size="mini" @click="clearDeletedFiles">
              {{ t('ms.add.attachment.quickClear') }}
            </MsButton>
          </div>
          <div class="file-list">
            <div v-for="file of alreadyDeleteFiles" :key="file.value" class="file-list-item">
              <MsTag size="small" max-width="100%">
                {{ file.label }}
              </MsTag>
              <a-tooltip :content="t('ms.add.attachment.remove')">
                <MsButton type="text" status="secondary" :disabled="props.disabled" @click="handleClose(file)">
                  <MsIcon
                    type="icon-icon_delete-trash_outlined1"
                    :class="props.disabled ? '' : 'hover:text-[rgb(var(--primary-5))]'"
                    size="16"
                  />
                </MsButton>
              </a-tooltip>
            </div>
          </div>
        </template>
        <template v-if="otherFiles.length > 0">
          <div v-if="alreadyDeleteFiles.length > 0" class="mt-[4px] text-[var(--color-text-4)]">
            {{ t('ms.add.attachment.other') }}
          </div>
          <div class="file-list">
            <div v-for="file of otherFiles" :key="file.value" class="file-list-item">
              <MsTag size="small" max-width="100%">
                {{ file.label }}
              </MsTag>
              <div v-if="file.local === true" class="flex items-center">
                <template v-if="hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+ADD'])">
                  <a-tooltip :content="t('ms.add.attachment.saveAs')">
                    <MsButton
                      type="text"
                      status="secondary"
                      class="!mr-0"
                      :disabled="props.disabled"
                      @click="handleOpenSaveAs(file)"
                    >
                      <MsIcon
                        type="icon-icon_unloading"
                        :class="props.disabled ? '' : 'hover:text-[rgb(var(--primary-5))]'"
                        size="16"
                      />
                    </MsButton>
                  </a-tooltip>
                  <a-divider direction="vertical" :margin="4"></a-divider>
                </template>
                <a-tooltip :content="t('ms.add.attachment.remove')">
                  <MsButton type="text" status="secondary" :disabled="props.disabled" @click="handleClose(file)">
                    <MsIcon
                      type="icon-icon_delete-trash_outlined1"
                      :class="props.disabled ? '' : 'hover:text-[rgb(var(--primary-5))]'"
                      size="16"
                    />
                  </MsButton>
                </a-tooltip>
              </div>
              <a-tooltip v-else :content="t('ms.add.attachment.cancelAssociate')">
                <MsButton type="text" status="secondary" :disabled="props.disabled" @click="handleClose(file)">
                  <MsIcon
                    type="icon-icon_unlink"
                    :class="props.disabled ? '' : 'hover:text-[rgb(var(--primary-5))]'"
                    size="16"
                  />
                </MsButton>
              </a-tooltip>
            </div>
          </div>
        </template>
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { TagData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';

  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  const props = withDefaults(
    defineProps<{
      disabled?: boolean;
      inputClass?: string;
      inputSize?: 'small' | 'medium' | 'large' | 'mini';
      tagSize?: Size;
      fields?: {
        id: string; // 自定义文件的 id 字段名，用于详情展示，接口返回的字段名
        name: string;
      };
    }>(),
    {
      fields: () => ({
        id: 'uid',
        name: 'name',
      }),
    }
  );
  const emit = defineEmits<{
    (e: 'deleteFile', fileId?: string | number): void;
    (e: 'openSaveAs', file: TagData): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
  // 只做回显用
  const inputFiles = defineModel<TagData[]>('inputFiles', {
    required: true,
  });
  const fileList = defineModel<MsFileItem[]>('fileList', {
    // TODO:这里的文件含有组件内部定义的属性，应该继承MsFileItem类型并扩展声明组件定义的类型属性
    required: true,
  });

  const alreadyDeleteFiles = computed(() => {
    return inputFiles.value.filter((item) => item.delete);
  });
  const otherFiles = computed(() => {
    return inputFiles.value.filter((item) => !item.delete);
  });

  function clearDeletedFiles() {
    inputFiles.value = inputFiles.value.filter((item) => !item.delete);
    fileList.value = fileList.value.filter((item) => !item.delete);
  }

  function handleClose(data: TagData) {
    inputFiles.value = inputFiles.value.filter((item) => item.value !== data.value);
    fileList.value = fileList.value.filter((item) => (item.uid || item[props.fields.id]) !== data.value);
    if (inputFiles.value.length === 0) {
      visible.value = false;
    }
    emit('deleteFile', data.value);
  }

  function handleOpenSaveAs(file: TagData) {
    emit('openSaveAs', file);
  }
</script>

<style lang="less">
  .ms-add-attachment-files-popover {
    padding: 16px;
    .arco-popover-content {
      margin-top: 0;
    }
  }
</style>

<style lang="less" scoped>
  .file-list {
    @apply flex flex-col overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();

    gap: 8px;
    max-height: 200px;
    .file-list-item {
      @apply flex items-center justify-between;

      gap: 8px;
    }
  }
</style>
