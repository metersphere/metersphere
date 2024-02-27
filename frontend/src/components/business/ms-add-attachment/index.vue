<template>
  <a-form-item v-if="props.mode === 'button'" field="attachment" :label="t('caseManagement.featureCase.addAttachment')">
    <div class="flex flex-col">
      <div class="mb-1">
        <a-dropdown position="tr" trigger="hover">
          <a-button type="outline">
            <template #icon> <icon-plus class="text-[14px]" /> </template>
            {{ t('system.orgTemplate.addAttachment') }}
          </a-button>
          <template #content>
            <a-upload
              ref="uploadRef"
              v-model:file-list="innerFileList"
              :limit="50"
              :auto-upload="false"
              :show-file-list="false"
              :before-upload="beforeUpload"
              @change="handleChange"
            >
              <template #upload-button>
                <a-button type="text" class="arco-dropdown-option !text-[var(--color-text-1)]">
                  <icon-upload />{{ t('caseManagement.featureCase.uploadFile') }}
                </a-button>
              </template>
            </a-upload>
            <a-button type="text" class="arco-dropdown-option !text-[var(--color-text-1)]" @click="associatedFile">
              <MsIcon type="icon-icon_link-copy_outlined" size="16" />
              {{ t('caseManagement.featureCase.associatedFile') }}
            </a-button>
          </template>
        </a-dropdown>
      </div>
      <div class="!hover:bg-[rgb(var(--primary-1))] !text-[var(--color-text-4)]">
        {{ t('bugManagement.edit.addAttachmentTip') }}
      </div>
    </div>
  </a-form-item>
  <template v-else>
    <div v-if="props.multiple" class="flex w-full items-center gap-[4px]">
      <dropdownMenu v-model:file-list="innerFileList" @link-file="associatedFile" @change="handleChange" />
      <a-popover
        v-model:popup-visible="inputFilesPopoverVisible"
        trigger="click"
        position="bottom"
        :disabled="inputFiles.length === 0"
      >
        <MsTagsInput
          v-model:model-value="inputFiles"
          :input-class="props.inputClass"
          placeholder=" "
          :max-tag-count="1"
          :size="props.inputSize"
          readonly
          class="!w-[calc(100%-28px)]"
        >
          <template v-if="alreadyDeleteFiles.length > 0" #prefix>
            <icon-exclamation-circle-fill class="!text-[rgb(var(--warning-6))]" :size="18" />
          </template>
          <template #tag="{ data }">
            <MsTag
              :size="props.tagSize"
              class="m-0 border-none p-0"
              :self-style="{ backgroundColor: 'transparent !important' }"
              :closable="data.value !== '__arco__more'"
              @close="handleClose(data)"
            >
              {{ data.value === '__arco__more' ? data.label.replace('...', '') : data.label }}
            </MsTag>
          </template>
        </MsTagsInput>
        <template #content>
          <div class="flex w-[200px] flex-col gap-[8px]">
            <template v-if="alreadyDeleteFiles.length > 0">
              <div class="flex items-center gap-[4px]">
                <icon-exclamation-circle-fill class="!text-[rgb(var(--warning-6))]" :size="18" />
                <div class="text-[var(--color-text-4)]">{{ t('ms.add.attachment.alreadyDelete') }}</div>
                <MsButton type="text" @click="clearDeletedFiles">{{ t('ms.add.attachment.quickClear') }}</MsButton>
              </div>
              <div class="file-list">
                <div v-for="file of alreadyDeleteFiles" :key="file.value">
                  <MsTag size="small" max-width="100%" closable @close="handleClose(file)">
                    {{ file.label }}
                  </MsTag>
                </div>
              </div>
            </template>
            <template v-if="otherFiles.length > 0">
              <div v-if="alreadyDeleteFiles.length > 0" class="mt-[4px] text-[var(--color-text-4)]">
                {{ t('ms.add.attachment.other') }}
              </div>
              <div class="file-list">
                <div v-for="file of otherFiles" :key="file.value">
                  <MsTag size="small" max-width="100%" closable @close="handleClose(file)">
                    {{ file.label }}
                  </MsTag>
                </div>
              </div>
            </template>
          </div>
        </template>
      </a-popover>
    </div>
    <div v-else class="flex w-full items-center gap-[4px]">
      <dropdownMenu v-model:file-list="innerFileList" @link-file="associatedFile" @change="handleChange" />
      <a-input
        v-model:model-value="inputFileName"
        :class="props.inputClass"
        :size="props.inputSize"
        allow-clear
        readonly
        @clear="handleFileClear"
      >
      </a-input>
    </div>
  </template>
  <LinkFileDrawer
    v-model:visible="showDrawer"
    :get-tree-request="getModules"
    :get-count-request="getModulesCount"
    :get-list-request="getAssociatedFileListUrl"
    :get-list-fun-params="getListFunParams"
    :selector-type="props.multiple ? 'checkbox' : 'radio'"
    @save="saveSelectAssociatedFile"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { TagData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import LinkFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';
  import dropdownMenu from './dropdownMenu.vue';

  import { getAssociatedFileListUrl } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';

  import { AssociatedList } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';

  import { convertToFile } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = withDefaults(
    defineProps<{
      mode: 'button' | 'input';
      fileList: MsFileItem[]; // TODO:这里的文件含有组件内部定义的属性，应该继承MsFileItem类型并扩展声明组件定义的类型属性
      multiple?: boolean;
      inputClass?: string;
      inputSize?: 'small' | 'medium' | 'large' | 'mini';
      tagSize?: Size;
      fields?: {
        id: string; // 自定义文件的 id 字段名，用于详情展示，接口返回的字段名
        name: string;
      };
    }>(),
    {
      mode: 'button',
      multiple: true,
      fields: () => ({
        id: 'uid',
        name: 'name',
      }),
    }
  );
  const emit = defineEmits<{
    (e: 'update:fileList', fileList: MsFileItem[]): void;
    (e: 'upload', file: File): void;
    (e: 'change', _fileList: MsFileItem[], fileItem?: MsFileItem): void;
    (e: 'linkFile'): void;
    (e: 'deleteFile', fileId?: string | number): void;
  }>();

  const { t } = useI18n();

  const innerFileList = useVModel(props, 'fileList', emit);
  const inputFileName = ref('');
  const inputFiles = ref<TagData[]>([]);
  const showDrawer = ref(false);
  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });

  onBeforeMount(() => {
    // 回显文件
    const defaultFiles = props.fileList.filter((item) => item) || [];
    if (defaultFiles.length > 0) {
      if (props.multiple) {
        inputFiles.value = defaultFiles.map((item) => ({
          ...item,
          // 这里取自定义的字段名，因为存在查看的场景时不会与刚选择的文件信息一样
          value: item?.[props.fields.id] || item.uid || '', // 取uid是因为有可能是本地上传然后组件卸载然后重新挂载，这时候取自定义 id 会是空的
          label: item?.[props.fields.name] || item?.name || '',
        }));
      } else {
        inputFileName.value = defaultFiles[0]?.[props.fields.name] || defaultFiles[0]?.name || '';
      }
      getListFunParams.value.combine.hiddenIds = defaultFiles
        .filter((item) => !item?.local)
        .map((item) => item?.[props.fields.id] || '')
        .filter((item) => item);
    }
  });

  function beforeUpload(file: File) {
    emit('upload', file);
  }

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    innerFileList.value = _fileList.map((item) => ({ ...item, local: true }));
    if (props.multiple) {
      inputFiles.value = _fileList.map((item) => ({
        ...item,
        value: item?.uid || '',
        label: item?.name || '',
      }));
    } else {
      inputFileName.value = fileItem.name || '';
    }
    emit('change', _fileList, { ...fileItem, local: true });
  }

  function associatedFile() {
    // TODO: 按钮模式逻辑待合并
    if (props.mode === 'button') {
      emit('linkFile');
    } else {
      showDrawer.value = true;
    }
  }

  // 监视文件列表处理关联和本地文件
  watch(
    () => innerFileList.value,
    () => {
      getListFunParams.value.combine.hiddenIds = innerFileList.value
        .filter((item) => !item.local)
        .map((item) => item[props.fields.id] || item.uid);
    },
    { deep: true, immediate: true }
  );

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    if (props.mode === 'button') {
      innerFileList.value.push(...fileResultList);
    } else if (props.multiple) {
      innerFileList.value.push(...fileResultList);
      inputFiles.value.push(
        ...fileResultList.map((item) => ({
          ...item,
          value: item?.uid || '',
          label: item?.name || '',
        }))
      );
    } else {
      // 单选文件
      innerFileList.value = fileResultList;
      inputFileName.value = fileResultList[0].name || '';
    }
    emit('change', innerFileList.value);
  }

  const inputFilesPopoverVisible = ref(false);
  const alreadyDeleteFiles = computed(() => {
    return inputFiles.value.filter((item) => item.delete);
  });
  const otherFiles = computed(() => {
    return inputFiles.value.filter((item) => !item.delete);
  });

  function clearDeletedFiles() {
    inputFiles.value = inputFiles.value.filter((item) => !item.delete);
  }

  function handleClose(data: TagData) {
    inputFiles.value = inputFiles.value.filter((item) => item.value !== data.value);
    innerFileList.value = innerFileList.value.filter(
      (item) => (item[props.fields.id] || item.uid) !== (data[props.fields.id] || data.value)
    );
    if (inputFiles.value.length === 0) {
      inputFilesPopoverVisible.value = false;
    }
    emit('deleteFile', data.value);
  }

  function handleFileClear() {
    inputFileName.value = '';
    inputFiles.value = [];
    innerFileList.value = [];
    emit('change', []);
  }
</script>

<style lang="less" scoped>
  .file-list {
    @apply flex flex-col overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();

    gap: 8px;
    max-height: 100px;
  }
  :deep(.arco-input-tag-has-prefix) {
    padding-left: 4px;
  }
  :deep(.arco-input-tag-prefix) {
    padding-right: 4px;
  }
  :deep(.arco-input-tag-inner) {
    @apply flex w-full items-center;
    .arco-input-tag-tag {
      @apply !my-0 !bg-transparent;

      max-width: calc(100% - 56px);
    }
    .arco-input-tag-input {
      @apply hidden;
    }
  }
</style>
