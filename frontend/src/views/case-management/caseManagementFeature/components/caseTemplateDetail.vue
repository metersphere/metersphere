<template>
  <div class="wrapper-preview">
    <div class="preview-left pr-4">
      <a-form ref="caseFormRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.orgTemplate.caseName')"
          :rules="[{ required: true, message: t('system.orgTemplate.caseNamePlaceholder') }]"
          required
          asterisk-position="end"
        >
          <a-input
            v-model="form.name"
            :max-length="255"
            :placeholder="t('system.orgTemplate.caseNamePlaceholder')"
            show-word-limit
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item field="precondition" :label="t('system.orgTemplate.precondition')" asterisk-position="end">
          <MsRichText v-model:model-value="form.prerequisite" />
        </a-form-item>
        <a-form-item
          field="step"
          :label="
            form.caseEditType === 'STEP'
              ? t('system.orgTemplate.stepDescription')
              : t('system.orgTemplate.textDescription')
          "
          class="relative"
        >
          <div class="absolute left-16 top-0">
            <a-divider direction="vertical" />
            <a-dropdown :popup-max-height="false" @select="handleSelectType">
              <span class="changeType">{{ t('system.orgTemplate.changeType') }} <icon-down /></span>
              <template #content>
                <a-doption value="STEP" :class="getSelectTypeClass('STEP')">
                  {{ t('system.orgTemplate.stepDescription') }}</a-doption
                >
                <a-doption value="TEXT" :class="getSelectTypeClass('TEXT')">{{
                  t('system.orgTemplate.textDescription')
                }}</a-doption>
              </template>
            </a-dropdown>
          </div>
          <!-- 步骤描述 -->
          <div v-if="form.caseEditType === 'STEP'" class="w-full">
            <MsBaseTable v-bind="propsRes" ref="stepTableRef" v-on="propsEvent">
              <template #index="{ rowIndex }">
                <div class="circle text-xs font-medium"> {{ rowIndex + 1 }}</div>
              </template>
              <template #caseStep="{ record }">
                <a-textarea
                  v-if="record.showStep"
                  v-model="record.step"
                  size="mini"
                  :auto-size="true"
                  class="w-max-[267px]"
                  :placeholder="t('system.orgTemplate.stepTip')"
                  @blur="blurHandler(record, 'step')"
                />
                <div
                  v-else-if="record.step && !record.showStep"
                  class="w-full cursor-pointer"
                  @click="edit(record, 'step')"
                  >{{ record.step }}</div
                >
                <div
                  v-else-if="!record.caseStep && !record.showStep"
                  class="placeholder w-full cursor-pointer text-[var(--color-text-brand)]"
                  @click="edit(record, 'step')"
                  >{{ t('system.orgTemplate.stepTip') }}</div
                >
              </template>
              <template #expectedResult="{ record }">
                <a-textarea
                  v-if="record.showExpected"
                  v-model="record.expected"
                  size="mini"
                  :auto-size="true"
                  class="w-max-[267px]"
                  :placeholder="t('system.orgTemplate.expectationTip')"
                  @blur="blurHandler(record, 'expected')"
                />
                <div
                  v-else-if="record.expected && !record.showExpected"
                  class="w-full cursor-pointer"
                  @click="edit(record, 'expected')"
                  >{{ record.expected }}</div
                >
                <div
                  v-else-if="!record.expected && !record.showExpected"
                  class="placeholder w-full cursor-pointer text-[var(--color-text-brand)]"
                  @click="edit(record, 'expected')"
                  >{{ t('system.orgTemplate.expectationTip') }}</div
                >
              </template>
              <template #operation="{ record }">
                <MsTableMoreAction
                  v-if="!record.internal"
                  :list="moreActions"
                  @select="(item:ActionsItem) => handleMoreActionSelect(item,record)"
                />
              </template>
            </MsBaseTable>
            <a-button class="mt-2 px-0" type="text" @click="addStep">
              <template #icon>
                <icon-plus class="text-[14px]" />
              </template>
              {{ t('system.orgTemplate.addStep') }}
            </a-button>
          </div>
          <!-- 文本描述 -->
          <MsRichText v-else v-model:modelValue="form.textDescription" />
        </a-form-item>
        <a-form-item
          v-if="form.caseEditType === 'TEXT'"
          field="remark"
          :label="t('caseManagement.featureCase.expectedResult')"
        >
          <MsRichText v-model:modelValue="form.expectedResult" />
        </a-form-item>
        <a-form-item field="remark" :label="t('caseManagement.featureCase.remark')">
          <MsRichText v-model:modelValue="form.description" />
        </a-form-item>
        <a-form-item field="attachment" :label="t('caseManagement.featureCase.addAttachment')">
          <div class="flex flex-col">
            <div class="mb-1">
              <a-dropdown position="tr" trigger="hover">
                <a-button type="outline">
                  <template #icon> <icon-plus class="text-[14px]" /> </template
                  >{{ t('system.orgTemplate.addAttachment') }}</a-button
                >
                <template #content>
                  <a-upload
                    ref="uploadRef"
                    v-model:file-list="fileList"
                    :auto-upload="false"
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    @change="handleChange"
                  >
                    <template #upload-button>
                      <a-button type="text" class="!text-[var(--color-text-1)]">
                        <icon-upload />{{ t('caseManagement.featureCase.uploadFile') }}</a-button
                      >
                    </template>
                  </a-upload>
                  <a-button type="text" class="!text-[var(--color-text-1)]" @click="associatedFile">
                    <MsIcon type="icon-icon_link-copy_outlined" size="16" />{{
                      t('caseManagement.featureCase.associatedFile')
                    }}</a-button
                  >
                </template>
              </a-dropdown>
            </div>
            <div class="!hover:bg-[rgb(var(--primary-1))] !text-[var(--color-text-4)]">{{
              t('system.orgTemplate.addAttachmentTip')
            }}</div>
          </div>
        </a-form-item>
      </a-form>
      <!-- 文件列表开始 -->
      <div class="w-[90%]">
        <MsFileList ref="fileListRef" v-model:file-list="fileList" mode="static">
          <template #actions="{ item }">
            <!-- 本地文件 -->
            <div v-if="item.local || item.status === 'init'" class="flex flex-nowrap">
              <MsButton type="button" status="danger" class="!mr-[4px]" @click="transferFile(item)">
                {{ t('caseManagement.featureCase.storage') }}
              </MsButton>
              <MsButton type="button" status="primary" class="!mr-[4px]" @click="downloadFile(item)">
                {{ t('caseManagement.featureCase.download') }}
              </MsButton>
            </div>
            <!-- 关联文件 -->
            <div v-else class="flex flex-nowrap">
              <MsButton type="button" status="primary" class="!mr-[4px]" @click="cancelAssociated(item)">
                {{ t('caseManagement.featureCase.cancelLink') }}
              </MsButton>
              <MsButton type="button" status="primary" class="!mr-[4px]" @click="downloadFile(item)">
                {{ t('caseManagement.featureCase.download') }}
              </MsButton>
            </div>
          </template>
        </MsFileList>
      </div>
      <!-- 文件列表结束 -->
    </div>
    <!-- 自定义字段开始 -->
    <div class="preview-right px-4">
      <div>
        <a-skeleton v-if="isLoading" :loading="isLoading" :animation="true">
          <a-space direction="vertical" class="w-full" size="large">
            <a-skeleton-line :rows="rowLength" :line-height="30" :line-spacing="30" />
          </a-space>
        </a-skeleton>
        <a-form v-else ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
          <a-form-item
            field="moduleId"
            asterisk-position="end"
            :label="t('system.orgTemplate.modules')"
            :rules="[{ required: true, message: t('system.orgTemplate.moduleRuleTip') }]"
          >
            <a-tree-select
              v-model="form.moduleId"
              :allow-search="true"
              :data="caseTree"
              :field-names="{
                title: 'name',
                key: 'id',
                children: 'children',
              }"
              :tree-props="{
                virtualListProps: {
                  height: 200,
                },
              }"
            ></a-tree-select>
          </a-form-item>
          <MsFormCreate
            v-if="formRules.length"
            ref="formCreateRef"
            v-model:api="fApi"
            :form-rule="formRules"
            :form-create-key="FormCreateKeyEnum.CASE_MANAGEMENT_FIELD"
          />
          <a-form-item field="tags" :label="t('system.orgTemplate.tags')">
            <a-input-tag v-model="form.tags" :placeholder="t('formCreate.PleaseEnter')" allow-clear />
          </a-form-item>
        </a-form>
      </div>
    </div>
    <!-- 自定义字段结束 -->
  </div>
  <div class=" ">
    <MsUpload
      v-model:file-list="fileList"
      accept="none"
      :auto-upload="false"
      :sub-text="acceptType === 'jar' ? '' : t('project.fileManagement.normalFileSubText', { size: 50 })"
      multiple
      draggable
      size-unit="MB"
      :max-size="50"
      :is-all-screen="true"
      class="mb-[16px]"
      @change="handleChange"
    />
  </div>
  <AssociatedFileDrawer v-model:visible="showDrawer" @save="saveSelectAssociatedFile" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { FormInstance } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/form-create.vue';
  import type { FormItem } from '@/components/pure/ms-form-create/types';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import AssociatedFileDrawer from './associatedFileDrawer.vue';

  import { getCaseDefaultFields, getCaseDetail } from '@/api/modules/case-management/featureCase';
  import { getProjectFieldList } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useFormCreateStore from '@/store/modules/form-create/form-create';
  import { getGenerateId } from '@/utils';

  import type { AssociatedList, CreateCase, StepList } from '@/models/caseManagement/featureCase';
  import type { CustomField, DefinedFieldItem } from '@/models/setting/template';
  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import {
    getCustomDetailFields,
    getTotalFieldOptionList,
  } from '@/views/setting/organization/template/components/fieldSetting';

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();
  const formCreateStore = useFormCreateStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    formModeValue: Record<string, any>; // 表单值
  }>();

  const emit = defineEmits(['update:formModeValue', 'changeFile']);
  const acceptType = ref('none'); // 模块-上传文件类型

  const templateFieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.numberIndex',
      dataIndex: 'index',
      slotName: 'index',
      width: 100,
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.useCaseStep',
      slotName: 'caseStep',
      dataIndex: 'caseStep',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.expectedResult',
      dataIndex: 'expectedResult',
      slotName: 'expectedResult',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.operation',
      slotName: 'operation',
      fixed: 'right',
      width: 200,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_DETAIL_TABLE,
    columns: templateFieldColumns,
    scroll: { x: '800px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: false,
    showPagination: false,
    enableDrag: true,
  });

  const moreActions: ActionsItem[] = [
    {
      label: 'caseManagement.featureCase.copyStep',
      eventTag: 'copyStep',
    },
    {
      label: 'caseManagement.featureCase.InsertStepsBefore',
      eventTag: 'InsertStepsBefore',
    },
    {
      label: 'caseManagement.featureCase.afterInsertingSteps',
      eventTag: 'afterInsertingSteps',
    },
    {
      isDivider: true,
    },
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  const formRef = ref<FormInstance>();
  const caseFormRef = ref<FormInstance>();

  // 默认模版自定义字段
  const selectData = ref<DefinedFieldItem[]>([]);

  // 步骤描述
  const stepData = ref<StepList[]>([
    {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    },
  ]);

  const featureCaseStore = useFeatureCaseStore();
  const modelId = computed(() => featureCaseStore.moduleId[0]);
  const caseTree = computed(() => featureCaseStore.caseTree);

  const initForm: CreateCase = {
    projectId: currentProjectId.value,
    templateId: '',
    name: '',
    prerequisite: '',
    caseEditType: 'STEP',
    steps: '',
    textDescription: '',
    expectedResult: '',
    description: '',
    publicCase: false,
    moduleId: modelId.value,
    versionId: '',
    tags: [],
    customFields: [],
    relateFileMetaIds: [],
  };

  const form = ref<CreateCase>({ ...initForm });

  watch(
    () => stepData.value,
    () => {
      const res = stepData.value.map((item, index) => {
        return {
          num: index,
          desc: item.step,
          result: item.expected,
        };
      });
      form.value.steps = JSON.stringify(res);
    },
    { deep: true }
  );
  // 总参数
  const params = ref<Record<string, any>>({
    request: {},
    fileList: [], // 总文件列表
  });

  // 获取类型样式
  function getSelectTypeClass(type: string) {
    return form.value.caseEditType === type ? ['bg-[rgb(var(--primary-1))]', '!text-[rgb(var(--primary-5))]'] : [];
  }

  // 更改类型
  const handleSelectType = (value: string | number | Record<string, any> | undefined) => {
    form.value.caseEditType = value as string;
  };

  // 添加步骤
  const addStep = () => {
    stepData.value.push({
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    });
  };

  // 复制步骤
  function copyStep(record: StepList) {
    stepData.value.push({
      ...record,
      id: getGenerateId(),
    });
  }

  // 删除步骤
  function deleteStep(record: StepList) {
    stepData.value = stepData.value.filter((item: any) => item.id !== record.id);
  }

  // 步骤之前插入步骤
  function insertStepsBefore(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    };
    stepData.value.splice(index, 0, insertItem);
  }

  // 步骤之后插入步骤
  function afterInsertingSteps(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    };
    stepData.value.splice(index + 1, 0, insertItem);
  }

  // 编辑步骤
  function edit(record: StepList, type: string) {
    if (type === 'step') {
      record.showStep = true;
    } else {
      record.showExpected = true;
    }
  }
  // 失去焦点回调
  function blurHandler(record: StepList, type: string) {
    if (type === 'step') {
      record.showStep = false;
    } else {
      record.showExpected = false;
    }
  }

  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem, record: StepList) => {
    switch (item.eventTag) {
      case 'copyStep':
        copyStep(record);
        break;
      case 'InsertStepsBefore':
        insertStepsBefore(record);
        break;
      case 'afterInsertingSteps':
        afterInsertingSteps(record);
        break;
      default:
        deleteStep(record);
        break;
    }
  };

  // 总自定义字段
  const totalTemplateField = ref<DefinedFieldItem[]>([]);

  const isLoading = ref<boolean>(true);

  // 获取所有字段
  async function getAllCaseFields() {
    try {
      totalTemplateField.value = await getProjectFieldList({ scopedId: currentProjectId.value, scene: 'FUNCTIONAL' });
      totalTemplateField.value = getTotalFieldOptionList(totalTemplateField.value as DefinedFieldItem[]);
    } catch (error) {
      console.log(error);
    }
  }

  // 初始化模版默认字段
  async function initDefaultFields() {
    try {
      isLoading.value = true;
      await getAllCaseFields();
      const res = await getCaseDefaultFields(currentProjectId.value);

      const { customFields, id } = res;
      form.value.templateId = id;

      selectData.value = getCustomDetailFields(totalTemplateField.value as DefinedFieldItem[], customFields);

      isLoading.value = false;
    } catch (error) {
      console.log(error);
    }
  }

  const fileList = ref<MsFileItem[]>([]);

  function beforeUpload() {
    return Promise.resolve(true);
  }

  // 将文件信息转换为文件格式
  function convertToFile(fileInfo: AssociatedList): MsFileItem {
    const fileName = fileInfo.fileType ? `${fileInfo.name}.${fileInfo.fileType || ''}` : `${fileInfo.name}`;
    const type = fileName.split('.')[1];
    const file = new File([new Blob()], `${fileName}`, {
      type: `application/${type}`,
    });
    Object.defineProperty(file, 'size', { value: fileInfo.size });
    return {
      enable: fileInfo.enable || false,
      file,
      name: fileName,
      percent: 0,
      status: 'done',
      uid: fileInfo.id,
      url: `http://172.16.200.18:8081/${fileInfo.filePath || ''}`,
      local: fileInfo.local,
    };
  }
  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }

  const title = ref('');
  const isEditOrCopy = computed(() => !!route.query.id);
  const attachmentsList = ref([]);

  // 后台传过来的local文件的item列表
  const oldLocalFileList = computed(() => {
    return attachmentsList.value.filter((item: any) => item.local);
  });

  // 后台已保存本地文件
  const currentOldLocalFileList = computed(() => {
    return fileList.value.filter((item) => item.local && item.status !== 'init').map((item: any) => item.uid);
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

  // 新增关联文件ID列表
  const newAssociateFileListIds = computed(() => {
    return fileList.value
      .filter((item: any) => !item.local && !associateFileIds.value.includes(item.uid))
      .map((item: any) => item.uid);
  });

  // 删除本地上传的文件id
  const deleteFileMetaIds = computed(() => {
    return oldLocalFileList.value
      .filter((item: any) => !currentOldLocalFileList.value.includes(item.id))
      .map((item: any) => item.id);
  });

  // 取消关联文件id
  const unLinkFilesIds = computed(() => {
    return associateFileIds.value.filter((id: string) => !currentAlreadyAssociateFileList.value.includes(id));
  });

  // 处理详情字段
  function getDetailData(detailResult: CreateCase) {
    const { customFields, attachments, steps, tags } = detailResult;
    form.value = {
      ...detailResult,
      name: route.params.mode === 'copy' ? `${detailResult.name}_copy` : detailResult.name,
      tags: JSON.parse(tags as string),
    };
    // 处理自定义字段
    selectData.value = getCustomDetailFields(
      totalTemplateField.value as DefinedFieldItem[],
      customFields as CustomField[]
    );
    // 处理步骤
    if (steps) {
      stepData.value = JSON.parse(steps).map((item: any) => {
        return {
          step: item.desc,
          expected: item.result,
        };
      });
    }
    attachmentsList.value = attachments;

    // 处理文件列表
    fileList.value = attachments
      .map((fileInfo: any) => {
        return {
          ...fileInfo,
          name: fileInfo.fileName,
        };
      })
      .map((fileInfo: any) => {
        return convertToFile(fileInfo);
      });

    // 处理删除本地文件id
  }

  // 处理详情
  async function getCaseInfo() {
    try {
      isLoading.value = true;
      await getAllCaseFields();
      const detailResult = await getCaseDetail(route.query.id as string);
      getDetailData(detailResult);
    } catch (error) {
      console.log(error);
    } finally {
      isLoading.value = false;
    }
  }

  watchEffect(() => {
    if (route.params.mode === 'edit' || route.params.mode === 'copy') {
      getCaseInfo();
    } else {
      initDefaultFields();
    }
  });
  // 处理关联文件和已关联文件本地文件和已上传文本文件
  function getFilesParams() {
    form.value.deleteFileMetaIds = deleteFileMetaIds.value;
    form.value.unLinkFilesIds = unLinkFilesIds.value;
    params.value.fileList = fileList.value.filter((item: any) => item.status === 'init');
    form.value.relateFileMetaIds = newAssociateFileListIds.value;
  }

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        form.value.relateFileMetaIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
        params.value.fileList = fileList.value.filter((item) => item.local && item.status === 'init');
        if (isEditOrCopy.value) {
          getFilesParams();
        }
      }
    },
    { deep: true }
  );

  // 监视表单变化
  watch(
    () => form.value,
    (val) => {
      if (val) {
        if (val) {
          params.value.request = { ...form.value };
          emit('update:formModeValue', params.value);
          featureCaseStore.setModuleId([form.value.moduleId], []);
        }
      }
    },
    { deep: true }
  );

  // 监视父组件传递过来的参数处理
  watch(
    () => props.formModeValue,
    () => {
      // 这里要处理预览的参数格式给到params
      params.value = {
        ...props.formModeValue,
      };
    },
    { deep: true }
  );

  const showDrawer = ref<boolean>(false);

  function associatedFile() {
    showDrawer.value = true;
  }

  watch(
    () => stepData.value,
    (val) => {
      setProps({ data: val });
    },
    { deep: true }
  );

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    fileList.value = _fileList.map((e) => {
      return {
        ...e,
        enable: true, // 是否启用
        local: true, // 是否本地文件
      };
    });
  }

  const rowLength = ref<number>(0);
  const formRuleField = ref<FormItem[][]>([]);
  const formRules = ref<FormItem[]>([]);
  const fApi = ref<any>({});
  const formRuleList = computed(() => formCreateStore.formCreateRuleMap.get(FormCreateKeyEnum.CASE_MANAGEMENT_FIELD));

  // 处理表单格式
  const getFormRules = () => {
    formRuleField.value = [];
    formRules.value = [];
    if (selectData.value && selectData.value.length) {
      selectData.value.forEach((item: any) => {
        const currentFormItem = item.formRules?.map((rule: any) => {
          let optionsItem = [];
          if (rule.options && rule.options.length) {
            optionsItem = rule.options.map((opt: any) => {
              return {
                text: opt.label,
                value: opt.value,
              };
            });
          }
          return {
            type: item.type,
            name: item.id,
            label: item.name,
            value: isEditOrCopy.value ? JSON.parse(rule.value) : rule.value,
            options: optionsItem,
            required: item.required,
            props: {
              modelValue: isEditOrCopy.value ? JSON.parse(rule.value) : rule.value,
              options: optionsItem,
            },
          };
        });
        formRuleField.value.push(currentFormItem as FormItem[]);
      });
      formRules.value = formRuleField.value.flatMap((item) => item);
    }
  };

  // 监视表单右侧自定义字段改变收集自定义字段参数
  watch(
    () => formRuleList.value,
    () => {
      const customFieldsMaps: Record<string, any> = {};
      formRuleList.value?.forEach((item) => {
        customFieldsMaps[item.field as string] = item.value;
      });
      form.value.customFields = customFieldsMaps as Record<string, any>;
    },
    { deep: true }
  );

  // 监视自定义字段改变处理formCreate
  watch(
    () => selectData.value,
    () => {
      getFormRules();
      rowLength.value = formRules.value.length + 2;
    },
    { deep: true }
  );

  onMounted(() => {
    setProps({ data: stepData.value });
  });

  onBeforeUnmount(() => {
    formRules.value = [];
    formRuleField.value = [];
  });

  // 转存
  function transferFile(item: any) {}
  // 下载
  function downloadFile(item: any) {}
  // 取消关联
  function cancelAssociated(item: any) {}

  defineExpose({
    caseFormRef,
    formRef,
    fApi,
  });
</script>

<style scoped lang="less">
  .wrapper-preview {
    display: flex;
    .preview-left {
      width: calc(100% - 396px);
      border-right: 1px solid var(--color-text-n8);
      .changeType {
        padding: 2px 4px;
        border-radius: 4px;
        color: var(--color-text-4);
        :deep(.arco-icon-down) {
          font-size: 14px;
        }
        &:hover {
          color: rgb(var(--primary-5));
          background: rgb(var(--primary-1));
          cursor: pointer;
        }
      }
    }
    .preview-right {
      width: 428px;
    }
  }
  .circle {
    width: 16px;
    height: 16px;
    line-height: 16px;
    border-radius: 50%;
    text-align: center;
    color: var(--color-text-4);
    background: var(--color-text-n8);
  }
</style>
