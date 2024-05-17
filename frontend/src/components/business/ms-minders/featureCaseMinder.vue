<template>
  <MsMinderEditor
    v-model:activeExtraKey="activeExtraKey"
    :tags="tags"
    :import-json="importJson"
    :replaceable-tags="replaceableTags"
    :insert-node="insertNode"
    :priority-disable-check="priorityDisableCheck"
    :after-tag-edit="afterTagEdit"
    :extract-content-tab-list="extractContentTabList"
    single-tag
    tag-enable
    sequence-enable
    @node-click="handleNodeClick"
    @save="handleMinderSave"
  >
    <template #extractTabContent>
      <div>
        <div v-if="activeExtraKey === 'baseInfo'" class="pl-[16px]">
          <a-skeleton v-if="baseInfoLoading" :loading="baseInfoLoading" :animation="true">
            <a-space direction="vertical" class="w-full" size="large">
              <a-skeleton-line :rows="rowLength" :line-height="30" :line-spacing="30" />
            </a-space>
          </a-skeleton>
          <a-form v-else ref="baseInfoFormRef" :model="baseInfoForm" layout="vertical">
            <a-form-item
              field="name"
              :label="t('ms.minders.caseName')"
              :rules="[{ required: true, message: t('ms.minders.caseNameNotNull') }]"
            >
              <a-input v-model:model-value="baseInfoForm.name" :placeholder="t('common.pleaseInput')"></a-input>
            </a-form-item>
            <a-form-item
              field="moduleId"
              asterisk-position="end"
              :label="t('caseManagement.featureCase.ModuleOwned')"
              :rules="[{ required: true, message: t('system.orgTemplate.moduleRuleTip') }]"
            >
              <a-tree-select
                v-model="baseInfoForm.moduleId"
                :allow-search="true"
                :data="caseTree"
                :field-names="{
                  title: 'name',
                  key: 'id',
                  children: 'children',
                }"
                :draggable="false"
                :tree-props="{
                  virtualListProps: {
                    height: 200,
                  },
                }"
              >
                <template #tree-slot-title="node">
                  <a-tooltip :content="`${node.name}`" position="tl">
                    <div class="one-line-text w-[300px] text-[var(--color-text-1)]">{{ node.name }}</div>
                  </a-tooltip>
                </template>
              </a-tree-select>
            </a-form-item>
            <MsFormCreate
              v-if="formRules.length"
              ref="formCreateRef"
              v-model:api="fApi"
              v-model:form-item="formItem"
              :form-rule="formRules"
            />
            <a-form-item field="tags" :label="t('common.tag')">
              <MsTagsInput v-model:model-value="baseInfoForm.tags" :max-tag-count="6" />
            </a-form-item>
          </a-form>
          <div class="flex items-center gap-[12px]">
            <a-button type="primary" @click="handleSave">{{ t('common.save') }}</a-button>
            <a-button type="secondary">{{ t('common.cancel') }}</a-button>
          </div>
        </div>
        <div v-else-if="activeExtraKey === 'attachment'" class="pl-[16px]">
          <MsAddAttachment
            v-model:file-list="fileList"
            multiple
            only-button
            @change="handleFileChange"
            @link-file="() => (showLinkFileDrawer = true)"
          />
          <MsFileList
            v-if="fileList.length > 0"
            ref="fileListRef"
            v-model:file-list="fileList"
            mode="static"
            :init-file-save-tips="t('ms.upload.waiting_save')"
            :show-upload-type-desc="true"
          >
            <template #actions="{ item }">
              <!-- 本地文件 -->
              <div v-if="item.local || item.status === 'init'" class="flex flex-nowrap">
                <MsButton
                  v-if="item.status !== 'init' && item.file.type.includes('image/')"
                  type="button"
                  status="primary"
                  class="!mr-[4px]"
                  @click="handlePreview(item)"
                >
                  {{ t('ms.upload.preview') }}
                </MsButton>
                <SaveAsFilePopover
                  v-model:visible="transferVisible"
                  :saving-file="activeTransferFileParams"
                  :file-save-as-source-id="activeCase.id"
                  :file-save-as-api="transferFileRequest"
                  :file-module-options-api="getTransferFileTree"
                  source-id-key="caseId"
                />
                <MsButton
                  v-if="item.status !== 'init'"
                  type="button"
                  status="primary"
                  class="!mr-[4px]"
                  @click="transferFile(item)"
                >
                  {{ t('caseManagement.featureCase.storage') }}
                </MsButton>
                <MsButton
                  v-if="item.status !== 'init'"
                  type="button"
                  status="primary"
                  class="!mr-[4px]"
                  @click="downloadFile(item)"
                >
                  {{ t('caseManagement.featureCase.download') }}
                </MsButton>
              </div>
              <!-- 关联文件 -->
              <div v-else class="flex flex-nowrap">
                <MsButton
                  v-if="item.file.type.includes('/image')"
                  type="button"
                  status="primary"
                  class="!mr-[4px]"
                  @click="handlePreview(item)"
                >
                  {{ t('ms.upload.preview') }}
                </MsButton>
                <MsButton
                  v-if="activeCase.id"
                  type="button"
                  status="primary"
                  class="!mr-[4px]"
                  @click="downloadFile(item)"
                >
                  {{ t('caseManagement.featureCase.download') }}
                </MsButton>
                <MsButton
                  v-if="activeCase.id && item.isUpdateFlag"
                  type="button"
                  status="primary"
                  @click="handleUpdateFile(item)"
                >
                  {{ t('common.update') }}
                </MsButton>
              </div>
            </template>
            <template #title="{ item }">
              <span v-if="item.isUpdateFlag" class="ml-4 flex items-center font-normal text-[rgb(var(--warning-6))]">
                <icon-exclamation-circle-fill />
                <span>{{ t('caseManagement.featureCase.fileIsUpdated') }}</span>
              </span>
            </template>
          </MsFileList>
        </div>
        <div v-else-if="activeExtraKey === 'comments'" class="pl-[16px]">
          <div class="flex items-center justify-between">
            <div class="text-[var(--color-text-4)]">
              {{
                t('ms.minders.commentTotal', {
                  num: activeComment === 'caseComment' ? commentList.length : reviewCommentList.length,
                })
              }}
            </div>
            <a-select
              v-model:model-value="activeComment"
              :options="commentTypeOptions"
              class="w-[120px]"
              @change="getAllCommentList"
            ></a-select>
          </div>
          <ReviewCommentList
            v-if="activeComment === 'reviewComment' || activeComment === 'executiveComment'"
            :review-comment-list="reviewCommentList"
            :active-comment="activeComment"
          />
          <template v-else>
            <MsComment
              :upload-image="handleUploadImage"
              :comment-list="commentList"
              :preview-url="PreviewEditorImageUrl"
              @delete="handleDelete"
              @update-or-add="handleUpdateOrAdd"
            />
            <MsEmpty v-if="commentList.length === 0" />
          </template>
          <inputComment
            ref="commentInputRef"
            v-model:content="content"
            v-model:notice-user-ids="noticeUserIds"
            v-permission="['FUNCTIONAL_CASE:READ+COMMENT']"
            :preview-url="PreviewEditorImageUrl"
            :is-active="isActive"
            mode="textarea"
            is-show-avatar
            is-use-bottom
            :upload-image="handleUploadImage"
            @publish="publishHandler"
            @cancel="cancelPublish"
          />
        </div>
        <div v-else class="pl-[16px]">
          <a-button
            v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE'])"
            class="mr-3"
            type="primary"
            @click="linkBug"
          >
            {{ t('caseManagement.featureCase.linkDefect') }}
          </a-button>
          <a-button v-permission="['PROJECT_BUG:READ+ADD']" type="outline" @click="createBug"
            >{{ t('caseManagement.featureCase.createDefect') }}
          </a-button>
          <div class="bug-list">
            <div v-for="item of bugList" :key="item.id" class="bug-item">
              <div class="mb-[4px] flex items-center justify-between">
                <MsButton type="text" @click="goBug(item.id)">{{ item.num }}</MsButton>
                <MsButton type="text" @click="disassociateBug(item.id)">
                  {{ t('ms.add.attachment.cancelAssociate') }}
                </MsButton>
              </div>
              <a-tooltip :content="item.name">
                <div class="one-line-text">{{ item.name }}</div>
              </a-tooltip>
            </div>
            <MsEmpty v-if="bugList.length === 0" />
          </div>
        </div>
      </div>
    </template>
  </MsMinderEditor>
  <LinkFileDrawer
    v-model:visible="showLinkFileDrawer"
    :get-tree-request="getModules"
    :get-count-request="getModulesCount"
    :get-list-request="getAssociatedFileListUrl"
    :get-list-fun-params="getListFunParams"
    @save="saveSelectAssociatedFile"
  />
  <a-image-preview v-model:visible="previewVisible" :src="imageUrl" />
  <AddDefectDrawer
    v-if="activeCase.id"
    v-model:visible="showCreateBugDrawer"
    :case-id="activeCase.id"
    :extra-params="{ caseId: activeCase.id }"
    @success="initBugList"
  />
  <LinkDefectDrawer
    v-if="activeCase.id"
    v-model:visible="showLinkBugDrawer"
    :case-id="activeCase.id"
    :drawer-loading="drawerLoading"
    @save="saveHandler"
  />
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type { MinderJson, MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import SaveAsFilePopover from '@/components/business/ms-add-attachment/saveAsFilePopover.vue';
  import MsComment from '@/components/business/ms-comment/comment';
  import inputComment from '@/components/business/ms-comment/input.vue';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';
  import LinkFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';
  import ReviewCommentList from '@/views/case-management/caseManagementFeature/components/tabContent/tabComment/reviewCommentList.vue';

  import {
    addOrUpdateCommentList,
    associatedDebug,
    cancelAssociatedDebug,
    createCommentList,
    deleteCommentList,
    downloadFileRequest,
    editorUploadFile,
    getAssociatedFileListUrl,
    getCaseDefaultFields,
    getCaseMinder,
    getCaseModuleTree,
    getCommentList,
    getReviewCommentList,
    getTestPlanExecuteCommentList,
    getTransferFileTree,
    previewFile,
    saveCaseMinder,
    transferFileRequest,
    updateFile,
  } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { downloadByteFile, getGenerateId } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { AssociatedList, OptionsFieldId } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode, TableQueryParams } from '@/models/common';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';

  import { convertToFile } from '@/views/case-management/caseManagementFeature/components/utils';

  const AddDefectDrawer = defineAsyncComponent(
    () => import('@/views/case-management/caseManagementFeature/components/tabContent/tabBug/addDefectDrawer.vue')
  );
  const LinkDefectDrawer = defineAsyncComponent(
    () => import('@/views/case-management/caseManagementFeature/components/tabContent/tabBug/linkDefectDrawer.vue')
  );

  const props = defineProps<{
    moduleId: string;
    moduleName: string;
  }>();

  const router = useRouter();
  const { openModal } = useModal();
  const appStore = useAppStore();
  const userStore = useUserStore();
  const { t } = useI18n();

  const importJson = ref<MinderJson>({
    root: {},
    template: 'default',
    treePath: [],
  });

  async function initMinder() {
    try {
      const res = await getCaseMinder({
        projectId: appStore.currentProjectId,
        moduleId: props.moduleId === 'all' ? '' : props.moduleId,
      });
      importJson.value.root.children = res;
      importJson.value.root.data = {
        id: props.moduleId === 'all' ? '' : props.moduleId,
        text: props.moduleName,
        resource: [t('common.module')],
      };
      window.minder.importJson(importJson.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watchEffect(() => {
    if (props.moduleId) {
      initMinder();
    }
  });

  const caseTag = t('common.case');
  const moduleTag = t('common.module');
  const topTags = [moduleTag, caseTag];
  const descTags = [t('ms.minders.stepDesc'), t('ms.minders.textDesc')];
  const tags = [...topTags, t('ms.minders.precondition'), ...descTags, t('ms.minders.stepExpect'), t('common.remark')];
  const visible = ref<boolean>(false);
  const activeCase = ref<any>({});
  const extractContentTabList = computed(() => {
    const fullTabList = [
      {
        label: t('common.baseInfo'),
        value: 'baseInfo',
      },
      {
        label: t('caseManagement.featureCase.attachment'),
        value: 'attachment',
      },
      {
        value: 'comments',
        label: t('caseManagement.featureCase.comments'),
      },
      {
        value: 'bug',
        label: t('caseManagement.featureCase.bug'),
      },
    ];
    if (activeCase.value.id) {
      return fullTabList;
    }
    return fullTabList.filter((item) => item.value === 'baseInfo');
  });
  const activeExtraKey = ref<'baseInfo' | 'attachment' | 'comments' | 'bug'>('baseInfo');

  function handleNodeClick(data: any) {
    if (data.resource && data.resource.includes(caseTag)) {
      visible.value = true;
      activeCase.value = data;
    }
  }

  async function handleMinderSave(data: any) {
    try {
      await saveCaseMinder({
        projectId: appStore.currentProjectId,
        versionId: '',
        updateCaseList: data,
        updateModuleList: [],
        deleteResourceList: [],
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 已选中节点的可替换标签判断
   * @param node 选中节点
   */
  function replaceableTags(node: MinderJsonNode) {
    if (node.data?.resource?.some((e) => topTags.includes(e))) {
      // 选中节点属于顶级节点，可替换为除自身外的顶级标签
      return !node.children || node.children.length === 0
        ? topTags.filter((tag) => !node.data?.resource?.includes(tag))
        : [];
    }
    if (node.data?.resource?.some((e) => descTags.includes(e))) {
      // 选中节点属于描述节点，可替换为除自身外的描述标签
      if (
        node.data?.resource?.includes(t('ms.minders.stepDesc')) &&
        (node.parent?.children?.filter((e) => e.data?.resource?.includes(t('ms.minders.stepDesc'))) || []).length > 1
      ) {
        // 如果当前节点是步骤描述，则需要判断是否有其他步骤描述节点，如果有，则不可替换为文本描述
        return [];
      }
      return descTags.filter((tag) => !node.data?.resource?.includes(tag));
    }
    if (
      (!node.data?.resource || node.data?.resource?.length === 0) &&
      (!node.parent?.data?.resource ||
        node.parent?.data?.resource.length === 0 ||
        node.parent?.data?.resource?.some((e) => topTags.includes(e)))
    ) {
      // 选中节点无标签，且父节点为顶级节点，可替换为顶级标签
      // 如果选中节点子级含有用例节点或模块节点，则不可将选中节点标记为用例
      return node.children &&
        (node.children.some((e) => e.data?.resource?.includes(caseTag)) ||
          node.children.some((e) => e.data?.resource?.includes(moduleTag)))
        ? topTags.filter((e) => e !== caseTag)
        : topTags;
    }
    return [];
  }

  function execInert(command: string, node?: MinderJsonNodeData) {
    if (window.minder.queryCommandState(command) !== -1) {
      window.minder.execCommand(command, node);
    }
  }

  /**
   * 插入前置条件
   * @param node 目标节点
   * @param type 插入类型
   */
  function inertPrecondition(node: MinderJsonNode, type: string) {
    const child: MinderJsonNode = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: t('ms.minders.precondition'),
        resource: [t('ms.minders.precondition')],
        expandState: 'expand',
      },
      children: [],
    };
    const sibling = {
      parent: child,
      data: {
        id: getGenerateId(),
        text: '',
        resource: [],
      },
    };
    execInert(type, child.data);
    nextTick(() => {
      execInert('AppendChildNode', sibling.data);
    });
  }

  /**
   * 插入备注
   * @param node 目标节点
   * @param type 插入类型
   */
  function insetRemark(node: MinderJsonNode, type: string) {
    const child = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: t('common.remark'),
        resource: [t('common.remark')],
      },
      children: [],
    };
    execInert(type, child.data);
  }

  // function insertTextDesc(node: MinderJsonNode, type: string) {
  //   const child = {
  //     parent: node,
  //     data: {
  //       id: getGenerateId(),
  //       text: t('ms.minders.textDesc'),
  //       resource: [t('ms.minders.textDesc')],
  //     },
  //     children: [],
  //   };
  //   const sibling = {
  //     parent: child,
  //     data: {
  //       id: getGenerateId(),
  //       text: t('ms.minders.stepExpect'),
  //       resource: [t('ms.minders.stepExpect')],
  //     },
  //   };
  //   execInert(type, {
  //     ...child,
  //     children: [sibling],
  //   });
  // }

  /**
   * 插入步骤描述
   * @param node 目标节点
   * @param type 插入类型
   */
  function insetStepDesc(node: MinderJsonNode, type: string) {
    const child = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: t('ms.minders.stepDesc'),
        resource: [t('ms.minders.stepDesc')],
      },
      children: [],
    };
    const sibling = {
      parent: child,
      data: {
        id: getGenerateId(),
        text: t('ms.minders.stepExpect'),
        resource: [t('ms.minders.stepExpect')],
      },
    };
    execInert(type, child.data);
    nextTick(() => {
      execInert('AppendChildNode', sibling.data);
    });
  }

  /**
   * 插入预期结果
   * @param node 目标节点
   * @param type 插入类型
   */
  function insertExpect(node: MinderJsonNode, type: string) {
    const child = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: t('ms.minders.stepExpect'),
        resource: [t('ms.minders.stepExpect')],
      },
      children: [],
    };
    execInert(type, child.data);
  }

  /**
   * 插入节点
   * @param node 目标节点
   * @param type 插入类型
   */
  function insertNode(node: MinderJsonNode, type: string) {
    switch (type) {
      case 'AppendChildNode':
        if (node.data?.resource?.includes(moduleTag)) {
          execInert('AppendChildNode');
        } else if (node.data?.resource?.includes(caseTag)) {
          // 给用例插入子节点
          if (!node.children || node.children.length === 0) {
            // 当前用例还没有子节点，默认添加一个前置条件
            inertPrecondition(node, type);
          } else if (node.children.length > 0) {
            // 当前用例有子节点
            let hasPreCondition = false;
            let hasTextDesc = false;
            let hasRemark = false;
            for (let i = 0; i < node.children.length; i++) {
              const child = node.children[i];
              if (child.data?.resource?.includes(t('ms.minders.precondition'))) {
                hasPreCondition = true;
              } else if (child.data?.resource?.includes(t('ms.minders.textDesc'))) {
                hasTextDesc = true;
              } else if (child.data?.resource?.includes(t('common.remark'))) {
                hasRemark = true;
              }
            }
            if (!hasPreCondition) {
              // 没有前置条件，则默认添加一个前置条件
              inertPrecondition(node, type);
            } else if (!hasRemark) {
              // 没有备注，则默认添加一个备注
              insetRemark(node, type);
            } else if (!hasTextDesc) {
              // 没有文本描述，则默认添加一个步骤描述
              insetStepDesc(node, type);
            }
          }
        } else if (
          (node.data?.resource?.includes(t('ms.minders.stepDesc')) ||
            node.data?.resource?.includes(t('ms.minders.textDesc'))) &&
          (!node.children || node.children.length === 0)
        ) {
          // 当前节点是步骤描述或文本描述，且没有子节点，则默认添加一个预期结果
          insertExpect(node, 'AppendChildNode');
        } else if (node.data?.resource?.includes(t('ms.minders.precondition'))) {
          // 当前节点是前置条件，则默认添加一个文本节点
          execInert('AppendChildNode');
        }
        break;
      case 'AppendParentNode':
        execInert('AppendParentNode');
        break;
      case 'AppendSiblingNode':
        if (node.parent?.data?.resource?.includes(caseTag) && node.parent?.children) {
          // 当前节点的父节点是用例
          let hasPreCondition = false;
          let hasTextDesc = false;
          let hasRemark = false;
          for (let i = 0; i < node.parent.children.length; i++) {
            const sibling = node.parent.children[i];
            if (sibling.data?.resource?.includes(t('ms.minders.precondition'))) {
              hasPreCondition = true;
            } else if (sibling.data?.resource?.includes(t('common.remark'))) {
              hasRemark = true;
            } else if (sibling.data?.resource?.includes(t('ms.minders.textDesc'))) {
              hasTextDesc = true;
            }
          }
          if (!hasPreCondition) {
            // 没有前置条件，则默认添加一个前置条件
            inertPrecondition(node, type);
          } else if (!hasRemark) {
            // 没有备注，则默认添加一个备注
            insetRemark(node, type);
          } else if (!hasTextDesc) {
            // 没有文本描述，则默认添加一个步骤描述
            insetStepDesc(node, type);
          }
        } else if (node.parent?.data?.resource?.includes(moduleTag) || !node.parent?.data?.resource) {
          // 当前节点的父节点是模块或没有标签，则默认添加一个文本节点
          execInert('AppendSiblingNode');
        }
        break;
      default:
        break;
    }
  }

  function priorityDisableCheck(node: MinderJsonNode) {
    if (node.data?.resource?.includes(caseTag)) {
      return false;
    }
    return true;
  }

  /**
   * 标签编辑后，如果将标签修改为模块，则删除已添加的优先级
   * @param node 选中节点
   * @param tag 更改后的标签
   */
  function afterTagEdit(node: MinderJsonNode, tag: string) {
    if (tag === moduleTag && node.data) {
      window.minder.execCommand('priority');
    }
  }

  const baseInfoFormRef = ref<FormInstance>();
  const baseInfoForm = ref({
    name: '',
    tags: [],
    templateId: '',
    moduleId: 'root',
  });
  const baseInfoLoading = ref(false);

  const rowLength = ref<number>(0);
  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref<any>(null);
  // 初始化模板默认字段
  async function initDefaultFields() {
    formRules.value = [];
    try {
      baseInfoLoading.value = true;
      const res = await getCaseDefaultFields(appStore.currentProjectId);
      const { customFields, id } = res;
      baseInfoForm.value.templateId = id;
      const result = customFields.map((item: any) => {
        const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
        let initValue = item.defaultValue;
        const optionsValue: OptionsFieldId[] = item.options;
        if (memberType.includes(item.type)) {
          if (item.defaultValue === 'CREATE_USER' || item.defaultValue.includes('CREATE_USER')) {
            initValue = item.type === 'MEMBER' ? userStore.id : [userStore.id];
          }
        }

        return {
          type: item.type,
          name: item.fieldId,
          label: item.fieldName,
          value: initValue,
          required: item.required,
          options: optionsValue || [],
          props: {
            modelValue: initValue,
            options: optionsValue || [],
          },
        };
      });
      formRules.value = result;
      baseInfoLoading.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const caseTree = ref<ModuleTreeNode[]>([]);

  async function initSelectTree() {
    try {
      caseTree.value = await getCaseModuleTree({ projectId: appStore.currentProjectId });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initDefaultFields();
    initSelectTree();
  });

  function handleSave() {
    if (activeExtraKey.value === 'baseInfo') {
      baseInfoFormRef.value?.validate((errors) => {
        if (!errors) {
          Message.success(t('common.saveSuccess'));
        }
      });
    }
  }

  const fileList = ref<MsFileItem[]>([]);

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }
  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        getListFunParams.value.combine.hiddenIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
      }
    },
    { deep: true }
  );

  const showLinkFileDrawer = ref<boolean>(false);

  function handleFileChange(_fileList: MsFileItem[]) {
    fileList.value = _fileList.map((e) => {
      return {
        ...e,
        enable: true, // 是否启用
      };
    });
  }

  const imageUrl = ref('');
  const previewVisible = ref<boolean>(false);

  // 预览图片
  async function handlePreview(item: MsFileItem) {
    try {
      previewVisible.value = true;
      if (item.status !== 'init') {
        const res = await previewFile({
          projectId: appStore.currentProjectId,
          caseId: activeCase.value.id,
          fileId: item.uid,
          local: item.local,
        });
        const blob = new Blob([res], { type: 'image/jpeg' });
        imageUrl.value = URL.createObjectURL(blob);
      } else {
        imageUrl.value = item.url || '';
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const transferVisible = ref<boolean>(false);

  const activeTransferFileParams = ref<MsFileItem>();

  // 转存
  function transferFile(item: MsFileItem) {
    activeTransferFileParams.value = { ...item };
    transferVisible.value = true;
  }

  // 下载
  async function downloadFile(item: MsFileItem) {
    try {
      const res = await downloadFileRequest({
        projectId: appStore.currentProjectId,
        caseId: activeCase.value.id,
        fileId: item.uid,
        local: item.local,
      });
      downloadByteFile(res, `${item.name}`);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 更新文件
  async function handleUpdateFile(item: MsFileItem) {
    try {
      await updateFile(appStore.currentProjectId, item.associationId);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  const activeComment = ref('caseComment');
  const commentTypeOptions = [
    {
      label: t('caseManagement.featureCase.caseComment'),
      value: 'caseComment',
    },
    {
      label: t('caseManagement.featureCase.reviewComment'),
      value: 'reviewComment',
    },
    {
      label: t('caseManagement.featureCase.executiveReview'),
      value: 'executiveComment',
    },
  ];
  const commentList = ref<CommentItem[]>([]);
  const reviewCommentList = ref<CommentItem[]>([]);

  // 初始化评论列表
  async function initCommentList() {
    try {
      const result = await getCommentList(activeCase.value.id);
      commentList.value = result;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 初始化评审评论
  async function initReviewCommentList() {
    try {
      const result = await getReviewCommentList(activeCase.value.id);
      reviewCommentList.value = result;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 初始化执行评论
  async function initTestPlanExecuteCommentList() {
    try {
      const result = await getTestPlanExecuteCommentList(activeCase.value.id);
      reviewCommentList.value = result;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function getAllCommentList() {
    switch (activeComment.value) {
      case 'caseComment':
        await initCommentList();
        break;
      case 'reviewComment':
        await initReviewCommentList();
        break;
      case 'executiveComment':
        await initTestPlanExecuteCommentList();
        break;
      default:
        break;
    }
  }

  // 添加或者更新评论
  async function handleUpdateOrAdd(item: CommentParams, cb: (result: boolean) => void) {
    try {
      await addOrUpdateCommentList(item);
      getAllCommentList();
      cb(true);
    } catch (error) {
      cb(false);
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  // 删除评论
  async function handleDelete(caseCommentId: string) {
    openModal({
      type: 'error',
      title: t('ms.comment.deleteConfirm'),
      content: t('ms.comment.deleteContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteCommentList(caseCommentId);
          Message.success(t('common.deleteSuccess'));
          getAllCommentList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        }
      },
      hideCancel: false,
    });
  }

  const commentInputRef = ref<InstanceType<typeof inputComment>>();
  const content = ref('');
  const isActive = ref<boolean>(false);

  const noticeUserIds = ref<string[]>([]);
  async function publishHandler(currentContent: string) {
    try {
      const params: CommentParams = {
        caseId: activeCase.value.id,
        notifier: noticeUserIds.value.join(';'),
        replyUser: '',
        parentId: '',
        content: currentContent,
        event: noticeUserIds.value.join(';') ? 'AT' : 'COMMENT', // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
      };
      await createCommentList(params);
      if (activeExtraKey.value === 'comments') {
        getAllCommentList();
      }

      Message.success(t('common.publishSuccessfully'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function cancelPublish() {
    isActive.value = !isActive.value;
  }

  const bugList = ref<any[]>([]);

  async function initBugList() {
    bugList.value = [
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100001,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100002,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100003,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100004,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100005,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100006,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100007,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100008,
      },
      {
        name: 'sdasd',
        id: '3d23d23d',
        num: 100009,
      },
    ];
  }

  const cancelLoading = ref<boolean>(false);
  // 取消关联
  async function disassociateBug(id: string) {
    cancelLoading.value = true;
    try {
      await cancelAssociatedDebug(id);
      bugList.value = bugList.value.filter((item) => item.id !== id);
      Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      cancelLoading.value = false;
    }
  }

  function goBug(id: string) {
    router.push({
      name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
      query: {
        id,
      },
    });
  }

  const showCreateBugDrawer = ref<boolean>(false);
  function createBug() {
    showCreateBugDrawer.value = true;
  }

  const showLinkBugDrawer = ref<boolean>(false);

  function linkBug() {
    showLinkBugDrawer.value = true;
  }

  const drawerLoading = ref<boolean>(false);
  async function saveHandler(params: TableQueryParams) {
    try {
      drawerLoading.value = true;
      await associatedDebug(params);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      initBugList();
      showLinkBugDrawer.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  :deep(.commentWrapper) {
    right: 0;
  }
  .bug-list {
    .ms-scroll-bar();

    overflow-y: auto;
    margin-bottom: 16px;
    height: calc(100% - 46px);
    border-radius: var(--border-radius-small);
    .bug-item {
      @apply cursor-pointer;
      &:not(:last-child) {
        margin-bottom: 8px;
      }

      padding: 8px;
      border: 1px solid var(--color-text-n8);
      border-radius: var(--border-radius-small);
      background-color: white;
      &:hover {
        @apply relative;

        background: var(--color-text-n9);
        box-shadow: inset 0 0 0.5px 0.5px rgb(var(--primary-5));
      }
    }
  }
</style>
