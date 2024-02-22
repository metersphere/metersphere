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
        {{ t('system.orgTemplate.addAttachmentTip') }}
      </div>
    </div>
  </a-form-item>
  <template v-else>
    <div v-if="props.multiple" class="flex w-full items-center gap-[4px]">
      <dropdownMenu v-model:file-list="innerFileList" @link-file="associatedFile" @change="handleChange" />
      <MsTagsInput
        v-model:model-value="inputFiles"
        :class="props.inputClass"
        placeholder=" "
        :max-tag-count="1"
        :size="props.inputSize"
        readonly
      >
        <template #tag="{ data }">
          <MsTag
            :size="props.tagSize"
            class="m-0 border-none p-0"
            :self-style="{ backgroundColor: 'transparent !important' }"
          >
            {{ data.label }}
          </MsTag>
        </template>
      </MsTagsInput>
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
      fileList: MsFileItem[];
      multiple?: boolean;
      inputClass?: string;
      inputSize?: 'small' | 'medium' | 'large' | 'mini';
      tagSize?: Size;
      fields?: {
        id: string;
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
          // 这里取自定义的字段名，因为存在查看的场景时不会与刚选择的文件信息一样
          value: item?.[props.fields.id] || '',
          label: item?.[props.fields.name] || '',
        }));
      } else {
        inputFileName.value = defaultFiles[0]?.[props.fields.name] || '';
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
        value: item?.uid || '',
        label: item?.name || '',
      }));
    } else {
      inputFileName.value = fileItem.name || '';
    }
    emit('change', _fileList, fileItem);
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

  function handleClose(data: TagData) {
    inputFiles.value = inputFiles.value.filter((item) => item.value !== data.value);
    innerFileList.value = innerFileList.value.filter((item) => item[props.fields.id] !== data.value);
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
  :deep(.arco-input-tag-inner) {
    @apply flex items-center;
    .arco-input-tag-tag {
      @apply !my-0 !bg-transparent;

      max-width: calc(100% - 50px);
    }
  }
</style>
