<template>
  <a-form-item
    v-if="props.mode === 'button'"
    field="attachment"
    :class="props.onlyButton ? 'hidden-item' : ''"
    :label="t('caseManagement.featureCase.addAttachment')"
  >
    <!-- TODO:跟下面统一样式 -->
    <div class="flex flex-col">
      <div class="mb-1" :class="props.onlyButton ? 'mb-[12px]' : ''">
        <a-dropdown
          v-model:popup-visible="buttonDropDownVisible"
          :disabled="props.disabled"
          position="tr"
          trigger="click"
        >
          <a-button :disabled="props.disabled" type="outline" class="arco-btn-outline--secondary">
            <template #icon> <icon-plus class="text-[14px]" /> </template>
            {{ t('system.orgTemplate.addAttachment') }}
          </a-button>
          <template #content>
            <MsUpload
              v-model:file-list="fileList"
              accept="none"
              :auto-upload="false"
              :show-file-list="false"
              :limit="50"
              size-unit="MB"
              :multiple="props.multiple"
              class="w-full"
              @change="handleChange"
            >
              <a-button type="text" class="arco-dropdown-option !text-[var(--color-text-1)]">
                <icon-upload />{{ t('caseManagement.featureCase.uploadFile') }}
              </a-button>
            </MsUpload>
            <a-button
              v-permission="['PROJECT_FILE_MANAGEMENT:READ']"
              type="text"
              class="arco-dropdown-option !text-[var(--color-text-1)]"
              @click="associatedFile"
            >
              <MsIcon type="icon-icon_link-copy_outlined" size="16" />
              {{ t('caseManagement.featureCase.associatedFile') }}
            </a-button>
          </template>
        </a-dropdown>
      </div>
      <div v-if="!props.onlyButton" class="!hover:bg-[rgb(var(--primary-1))] !text-[var(--color-text-4)]">
        {{ t('system.orgTemplate.addAttachmentTip') }}
      </div>
    </div>
  </a-form-item>
  <template v-else>
    <div v-if="props.multiple" class="flex w-full items-center">
      <dropdownMenu
        :file-list="fileList"
        :disabled="props.disabled"
        :accept="props.accept"
        @link-file="associatedFile"
        @change="handleChange"
      />
      <saveAsFilePopover
        v-if="props.fileSaveAsSourceId"
        v-model:visible="saveFilePopoverVisible"
        :saving-file="savingFile"
        :file-id-key="props.fields.id"
        :file-save-as-api="props.fileSaveAsApi"
        :file-save-as-source-id="props.fileSaveAsSourceId"
        :file-module-options-api="props.fileModuleOptionsApi"
        @finish="handleSaveFileFinish"
      />
      <a-popover
        v-model:popup-visible="inputFilesPopoverVisible"
        trigger="click"
        position="bl"
        :disabled="inputFiles.length === 0"
        content-class="ms-add-attachment-files-popover"
        arrow-class="hidden"
        :popup-offset="0"
      >
        <div class="h-full flex-1">
          <MsTagsInput
            v-model:model-value="inputFiles"
            :input-class="props.inputClass"
            placeholder=" "
            :disabled="props.disabled"
            :max-tag-count="1"
            :size="props.inputSize"
            readonly
            no-tooltip
          >
            <template v-if="alreadyDeleteFiles.length > 0" #prefix>
              <icon-exclamation-circle-fill class="!text-[rgb(var(--warning-6))]" :size="18" />
            </template>
            <template #tag="{ data }">
              <MsTag
                :size="props.tagSize"
                class="m-0 border-none p-0"
                :self-style="{ backgroundColor: 'transparent !important' }"
                :closable="data.value !== '__arco__more' && !props.disabled"
                @close="() => handleClose(data)"
              >
                {{ data.value === '__arco__more' ? data.label.replace('...', '') : data.label }}
              </MsTag>
            </template>
          </MsTagsInput>
        </div>
        <template #content>
          <div class="flex w-[200px] flex-col gap-[8px]">
            <template v-if="alreadyDeleteFiles.length > 0">
              <div class="flex items-center gap-[4px]">
                <icon-exclamation-circle-fill class="!text-[rgb(var(--warning-6))]" :size="18" />
                <div class="text-[var(--color-text-4)]">{{ t('ms.add.attachment.alreadyDelete') }}</div>
                <MsButton type="text" :disabled="props.disabled" @click="clearDeletedFiles">
                  {{ t('ms.add.attachment.quickClear') }}
                </MsButton>
              </div>
              <div class="file-list">
                <div v-for="file of alreadyDeleteFiles" :key="file.value" class="file-list-item">
                  <a-tooltip :content="file.label" :mouse-enter-delay="300">
                    <MsTag size="small" max-width="100%">
                      {{ file.label }}
                    </MsTag>
                  </a-tooltip>
                  <a-tooltip :content="t('ms.add.attachment.remove')">
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
            <template v-if="otherFiles.length > 0">
              <div v-if="alreadyDeleteFiles.length > 0" class="mt-[4px] text-[var(--color-text-4)]">
                {{ t('ms.add.attachment.other') }}
              </div>
              <div class="file-list">
                <div v-for="file of otherFiles" :key="file.value" class="file-list-item">
                  <a-tooltip :content="file.label" :mouse-enter-delay="300">
                    <MsTag size="small" max-width="100%">
                      {{ file.label }}
                    </MsTag>
                  </a-tooltip>
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
                          type="icon-icon_delete-trash_outlined"
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
    </div>
    <div v-else class="flex w-full items-center gap-[4px]">
      <dropdownMenu
        :accept="props.accept"
        :disabled="props.disabled"
        @link-file="associatedFile"
        @change="handleChange"
      />
      <a-input
        v-model:model-value="inputFileName"
        :disabled="props.disabled"
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
    :filetype="props.accept"
    @save="saveSelectAssociatedFile"
  />
</template>

<script setup lang="ts">
  import { TagData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem, UploadType } from '@/components/pure/ms-upload/types';
  import LinkFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';
  import dropdownMenu from './dropdownMenu.vue';
  import saveAsFilePopover from './saveAsFilePopover.vue';

  import { getAssociatedFileListUrl } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import { AssociatedList } from '@/models/caseManagement/featureCase';
  import { TableQueryParams, TransferFileParams } from '@/models/common';

  import { convertToFile } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = withDefaults(
    defineProps<{
      disabled?: boolean;
      mode?: 'button' | 'input';
      onlyButton?: boolean;
      accept?: UploadType;
      multiple?: boolean;
      inputClass?: string;
      inputSize?: 'small' | 'medium' | 'large' | 'mini';
      tagSize?: Size;
      fields?: {
        id: string; // 自定义文件的 id 字段名，用于详情展示，接口返回的字段名
        name: string;
      };
      fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
      fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
      fileModuleOptionsApi?: (...args: any[]) => Promise<any>; // 文件转存目录下拉框接口
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
    (e: 'upload', file: File): void;
    (e: 'change', _fileList: MsFileItem[], fileItem?: MsFileItem): void;
    (e: 'linkFile'): void;
    (e: 'deleteFile', fileId?: string | number): void;
  }>();

  const { t } = useI18n();

  const fileList = defineModel<MsFileItem[]>('fileList', {
    // TODO:这里的文件含有组件内部定义的属性，应该继承MsFileItem类型并扩展声明组件定义的类型属性
    required: true,
  });
  const inputFileName = ref('');
  const inputFiles = ref<TagData[]>([]);
  const showDrawer = ref(false);
  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });
  const buttonDropDownVisible = ref(false);

  watch(
    () => fileList.value,
    () => {
      // 回显文件
      const defaultFiles = fileList.value.filter((item) => item) || [];
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
    },
    {
      immediate: true,
    }
  );

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    if (props.multiple) {
      fileList.value.push(fileItem);
      inputFiles.value.push({
        ...fileItem,
        value: fileItem[props.fields.id] || fileItem.uid || '',
        label: fileItem[props.fields.name] || fileItem.name || '',
      });
    } else {
      fileList.value = [fileItem];
      inputFileName.value = fileItem.name || '';
    }
    fileItem.local = true;
    emit('change', fileList.value, fileItem);
    nextTick(() => {
      // 在 emit 文件上去之后再关闭菜单
      buttonDropDownVisible.value = false;
    });
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
    () => fileList.value,
    (arr) => {
      getListFunParams.value.combine.hiddenIds = arr
        .filter((item) => !item.local)
        .map((item) => item[props.fields.id] || item.uid);
    },
    { deep: true, immediate: true }
  );

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    // TODO : 这里需要优化选择跨页的数据
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    if (props.mode === 'button') {
      fileList.value.push(...fileResultList);
    } else if (props.multiple) {
      fileList.value.push(...fileResultList);
      inputFiles.value.push(
        ...fileResultList.map((item) => ({
          ...item,
          value: item?.uid || '',
          label: item?.name || '',
        }))
      );
    } else {
      // 单选文件
      const file = fileResultList[0];
      fileList.value = [{ ...file, fileId: file.uid || '', fileName: file.name || '' }];
      inputFileName.value = file.name || '';
    }
    nextTick(() => {
      // fileList赋值以后需要 nextTick 才能获取到更新后的值
      emit('change', fileList.value);
    });
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
    fileList.value = fileList.value.filter((item) => (item.uid || item[props.fields.id]) !== data.value);
    if (inputFiles.value.length === 0) {
      inputFilesPopoverVisible.value = false;
    }
    emit('deleteFile', data.value);
  }

  function handleFileClear() {
    inputFileName.value = '';
    inputFiles.value = [];
    fileList.value = [];
    emit('change', []);
  }

  const saveFilePopoverVisible = ref(false);
  const savingFile = ref<MsFileItem>();

  /**
   * 打开文件转存弹窗
   * @param item 点击转存的文件标签项
   */
  function handleOpenSaveAs(item: TagData) {
    inputFilesPopoverVisible.value = false;
    // 这里先判定 uid 是否存在，存在则是刚上传的文件；否则是已保存过后的详情文件
    savingFile.value = fileList.value.find((file) => file.uid === item.value || file[props.fields.id] === item.value);
    saveFilePopoverVisible.value = true;
  }

  function handleSaveFileFinish(fileId: string, fileName: string) {
    if (savingFile.value) {
      inputFiles.value = inputFiles.value.map((e) => {
        if (e.value === savingFile.value?.fileId || e.value === savingFile.value?.uid) {
          // 被存储过的文件没有 uid，只有fileId；刚上传还未保存的文件只有 uid，没有fileId
          e.value = fileId;
          e.local = false;
          e.label = fileName;
        }
        return e;
      });
      savingFile.value.fileId = fileId;
      savingFile.value.local = false;
      savingFile.value.name = fileName;
    }
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
