<template>
  <div class="h-full pl-[16px]">
    <a-form ref="caseFormRef" class="rounded-[4px]" :model="detailForm" layout="vertical">
      <a-form-item
        v-if="props.activeNodeDetailResource.showPrerequisite"
        class="relative !mb-[16px]"
        field="precondition"
        :label="t('system.orgTemplate.precondition')"
        asterisk-position="end"
      >
        <MsRichText
          v-model:raw="detailForm.prerequisite"
          v-model:filed-ids="prerequisiteFileIds"
          :upload-image="handleUploadImage"
          :preview-url="`${PreviewEditorImageUrl}/${currentProjectId}`"
          class="mt-2"
        />
      </a-form-item>
      <!-- 文本描述 -->
      <template v-if="props.activeNodeDetailResource.showTextDesc">
        <a-form-item
          class="relative !mb-[16px]"
          field="precondition"
          :label="t('system.orgTemplate.textDescription')"
          asterisk-position="end"
        >
          <MsRichText
            v-model:raw="detailForm.textDescription"
            v-model:filed-ids="textDescriptionFileIds"
            :upload-image="handleUploadImage"
            :preview-url="`${PreviewEditorImageUrl}/${currentProjectId}`"
          />
        </a-form-item>

        <a-form-item field="remark" class="!mb-[16px]" :label="t('caseManagement.featureCase.expectedResult')">
          <MsRichText
            v-model:raw="detailForm.expectedResult"
            v-model:filed-ids="expectedResultFileIds"
            :upload-image="handleUploadImage"
            :preview-url="`${PreviewEditorImageUrl}/${currentProjectId}`"
          />
        </a-form-item>
      </template>

      <a-form-item
        v-if="props.activeNodeDetailResource.showRemark"
        field="description"
        class="!mb-[16px]"
        :label="t('caseManagement.featureCase.remark')"
      >
        <MsRichText
          v-model:filed-ids="descriptionFileIds"
          v-model:raw="detailForm.description"
          :upload-image="handleUploadImage"
          :preview-url="`${PreviewEditorImageUrl}/${currentProjectId}`"
        />
      </a-form-item>
    </a-form>
    <div v-if="hasEditPermission" class="flex items-center gap-[12px] bg-[var(--color-text-fff)] py-[16px]">
      <a-tooltip :content="t('ms.minders.moduleNewTip')" :disabled="!props.activeCase.moduleIsNew">
        <a-button
          v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
          type="primary"
          :loading="saveLoading"
          :disabled="props.activeCase.moduleIsNew"
          @click="handleSave"
        >
          {{ t('common.save') }}
        </a-button>
      </a-tooltip>
      <a-button type="secondary" :disabled="saveLoading" @click="handleCancel">{{ t('common.cancel') }}</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';

  import { editorUploadFile, getCaseDefaultFields, updateCaseRequest } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const { t } = useI18n();

  const props = defineProps<{
    activeNodeDetailResource: {
      showPrerequisite: boolean;
      showTextDesc: boolean;
      showRemark: boolean;
    };
    activeCase: Record<string, any>;
  }>();

  const emit = defineEmits<{
    (e: 'initTemplate', id: string): void;
    (e: 'cancel'): void;
    (e: 'saved'): void;
  }>();

  const hasEditPermission = hasAnyPermission(['FUNCTIONAL_CASE:READ+MINDER']);

  const detailForm = ref<Record<string, any>>({
    projectId: currentProjectId.value,
    templateId: '',
    name: '',
    prerequisite: '',
    caseEditType: 'TEXT',
    steps: '',
    textDescription: '',
    expectedResult: '',
    description: '',
    publicCase: false,
    moduleId: '',
    versionId: '',
    tags: [],
    customFields: [],
    relateFileMetaIds: [],
  });

  // 前置操作附件id
  const prerequisiteFileIds = ref<string[]>([]);
  // 文本描述附件id
  const textDescriptionFileIds = ref<string[]>([]);
  // 预期结果附件id
  const expectedResultFileIds = ref<string[]>([]);
  // 描述附件id
  const descriptionFileIds = ref<string[]>([]);
  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  const attachmentsList = ref([]);
  const fileList = ref<MsFileItem[]>([]);
  // 后台传过来的local文件的item列表
  const oldLocalFileList = computed(() => {
    return attachmentsList.value.filter((item: any) => item.local);
  });

  // 后台已保存本地文件
  const currentOldLocalFileList = computed(() => {
    return fileList.value.filter((item) => item.local && item.status !== 'init').map((item: any) => item.uid);
  });

  // 删除本地上传的文件id
  const deleteFileMetaIds = computed(() => {
    return oldLocalFileList.value
      .filter((item: any) => !currentOldLocalFileList.value.includes(item.id))
      .map((item: any) => item.id);
  });

  // 已经关联过的id列表
  const associateFileIds = computed(() => {
    return attachmentsList.value.filter((item: any) => !item.local).map((item: any) => item.id);
  });

  // 当前新增传过来的关联list
  const currentAlreadyAssociateFileList = computed(() => {
    return fileList.value
      .filter((item) => !item.local && !associateFileIds.value.includes(item.uid))
      .map((item: any) => item.uid);
  });

  // 取消关联文件id
  const unLinkFilesIds = computed(() => {
    const deleteAssociateFileIds = fileList.value
      .filter(
        (item: any) =>
          !currentAlreadyAssociateFileList.value.includes(item.uid) && associateFileIds.value.includes(item.uid)
      )
      .map((item) => item.uid);
    return associateFileIds.value.filter(
      (id: string) => !currentAlreadyAssociateFileList.value.includes(id) && !deleteAssociateFileIds.includes(id)
    );
  });

  // 所有附近文件id
  const allAttachmentsFileIds = computed(() => {
    return [
      ...prerequisiteFileIds.value,
      ...textDescriptionFileIds.value,
      ...expectedResultFileIds.value,
      ...descriptionFileIds.value,
    ];
  });

  // 新增关联文件ID列表
  const newAssociateFileListIds = computed(() => {
    return fileList.value
      .filter((item: any) => !item.local && !associateFileIds.value.includes(item.uid))
      .map((item: any) => item.uid);
  });

  function getParams() {
    const customFieldsArr = detailForm.value.customFields?.map((item: any) => {
      return {
        fieldId: item.fieldId,
        value: item.defaultValue,
      };
    });

    return {
      request: {
        ...detailForm.value,
        caseEditType: 'TEXT',
        deleteFileMetaIds: deleteFileMetaIds.value,
        unLinkFilesIds: unLinkFilesIds.value,
        newAssociateFileListIds: newAssociateFileListIds.value,
        customFields: customFieldsArr,
        caseDetailFileIds: allAttachmentsFileIds.value,
      },
      fileList: fileList.value.filter((item: any) => item.status === 'init'), // 总文件列表
    };
  }

  function handleCancel() {
    emit('cancel');
  }

  const caseFormRef = ref<FormInstance>();
  const saveLoading = ref(false);
  function handleSave() {
    caseFormRef.value?.validate().then(async (res: any) => {
      if (!res) {
        try {
          saveLoading.value = true;
          await updateCaseRequest(getParams());
          const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
          let text = selectedNode?.data?.text ?? '';
          if (props.activeNodeDetailResource.showPrerequisite) {
            text = detailForm.value.prerequisite;
          } else if (props.activeNodeDetailResource.showTextDesc) {
            text = detailForm.value.textDescription;
            if (selectedNode && selectedNode?.children && selectedNode?.children?.[0]?.data) {
              selectedNode.children[0].data.text = detailForm.value.expectedResult;
              selectedNode.children[0].render();
            }
          } else {
            text = detailForm.value.description;
          }

          if (selectedNode?.data) {
            selectedNode.data = {
              ...selectedNode.data,
              text,
              html: text,
              isNew: false,
            };
            selectedNode.data.changed = false;
          }
          // TODO html 没有生效没有找到解决方案
          window.minder.execCommand('html', text);
          window.minder.execCommand('text', text);
          window.minder.refresh();
          Message.success(t('caseManagement.featureCase.editSuccess'));
          emit('saved');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          saveLoading.value = false;
        }
      }
    });
  }

  async function initDefaultFields() {
    try {
      const res = await getCaseDefaultFields(appStore.currentProjectId);
      const { id } = res;
      detailForm.value.templateId = id;
      emit('initTemplate', id);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => props.activeCase,
    (val) => {
      if (val) {
        detailForm.value = { ...detailForm.value, ...val };
      }
    },
    {
      deep: true,
      immediate: true,
    }
  );

  onBeforeMount(() => {
    initDefaultFields();
  });
</script>

<style scoped lang="less">
  :deep(.arco-form-item-label) {
    font-weight: bold !important;
  }
</style>
