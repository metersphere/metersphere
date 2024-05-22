<template>
  <MsMinderEditor
    v-model:activeExtraKey="activeExtraKey"
    v-model:extra-visible="extraVisible"
    v-model:loading="loading"
    :tags="[]"
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
      <div v-if="activeExtraKey === 'baseInfo'" class="h-full pl-[16px]">
        <div class="baseInfo-form">
          <a-skeleton v-if="baseInfoLoading" :loading="baseInfoLoading" :animation="true">
            <a-space direction="vertical" class="w-full" size="large">
              <a-skeleton-line :rows="10" :line-height="30" :line-spacing="30" />
            </a-space>
          </a-skeleton>
          <a-form v-else ref="baseInfoFormRef" :model="baseInfoForm" layout="vertical">
            <a-form-item
              field="name"
              :label="t('ms.minders.caseName')"
              :rules="[{ required: true, message: t('ms.minders.caseNameNotNull') }]"
              asterisk-position="end"
            >
              <a-input
                v-model:model-value="baseInfoForm.name"
                :placeholder="t('common.pleaseInput')"
                allow-clear
              ></a-input>
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
        </div>
        <div class="flex items-center gap-[12px] bg-white py-[16px]">
          <a-button
            v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
            type="primary"
            :loading="saveLoading"
            @click="handleSave"
          >
            {{ t('common.save') }}
          </a-button>
          <a-button type="secondary" :disabled="saveLoading">{{ t('common.cancel') }}</a-button>
        </div>
      </div>
      <div v-else-if="activeExtraKey === 'attachment'" class="pl-[16px]">
        <a-spin :loading="attachmentLoading" class="h-full w-full">
          <MsAddAttachment
            v-model:file-list="fileList"
            multiple
            only-button
            @change="(files, file) => handleFileChange(file ? [file] : [])"
            @link-file="() => (showLinkFileDrawer = true)"
          />
          <MsFileList
            v-if="fileList.length > 0"
            ref="fileListRef"
            v-model:file-list="fileList"
            mode="static"
            :init-file-save-tips="t('ms.upload.waiting_save')"
            :show-upload-type-desc="true"
            :handle-delete="deleteFileHandler"
            show-delete
            button-in-title
          >
            <template #title="{ item }">
              <span v-if="item.isUpdateFlag" class="ml-4 flex items-center font-normal text-[rgb(var(--warning-6))]">
                <icon-exclamation-circle-fill />
                <span>{{ t('caseManagement.featureCase.fileIsUpdated') }}</span>
              </span>
            </template>
            <template #titleAction="{ item }">
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
                  :file-save-as-source-id="activeCase.id || ''"
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
          </MsFileList>
        </a-spin>
      </div>
      <div v-else-if="activeExtraKey === 'comments'" class="pl-[16px]">
        <div class="mb-[16px] flex items-center justify-between">
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
        <a-button v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE'])" class="mr-3" type="primary" @click="linkBug">
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
    checkFileIsUpdateRequest,
    createCommentList,
    deleteCommentList,
    deleteFileOrCancelAssociation,
    downloadFileRequest,
    editorUploadFile,
    getAssociatedFileListUrl,
    getCaseDefaultFields,
    getCaseDetail,
    getCaseMinder,
    getCaseModuleTree,
    getCommentList,
    getReviewCommentList,
    getTestPlanExecuteCommentList,
    getTransferFileTree,
    previewFile,
    saveCaseMinder,
    transferFileRequest,
    updateCaseRequest,
    updateFile,
    uploadOrAssociationFile,
  } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { downloadByteFile, getGenerateId, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { AssociatedList, AttachFileInfo, OptionsFieldId } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';

  import { convertToFile, initFormCreate } from '@/views/case-management/caseManagementFeature/components/utils';
  import { Api } from '@form-create/arco-design';

  const AddDefectDrawer = defineAsyncComponent(
    () => import('@/views/case-management/caseManagementFeature/components/tabContent/tabBug/addDefectDrawer.vue')
  );
  const LinkDefectDrawer = defineAsyncComponent(
    () => import('@/views/case-management/caseManagementFeature/components/tabContent/tabBug/linkDefectDrawer.vue')
  );

  const props = defineProps<{
    moduleId: string;
    moduleName: string;
    modulesCount: Record<string, number>; // 模块数量
  }>();

  const router = useRouter();
  const { openModal } = useModal();
  const appStore = useAppStore();
  const userStore = useUserStore();
  const { t } = useI18n();

  const caseTag = t('common.case');
  const moduleTag = t('common.module');
  const topTags = [moduleTag, caseTag];
  const descTags = [t('ms.minders.stepDesc'), t('ms.minders.textDesc')];
  const importJson = ref<MinderJson>({
    root: {},
    template: 'default',
    treePath: [],
  });
  const caseTree = ref<MinderJsonNode[]>([]);
  const loading = ref(false);

  async function initCaseTree() {
    try {
      loading.value = true;
      const res = await getCaseModuleTree({
        projectId: appStore.currentProjectId,
        moduleId: props.moduleId === 'all' ? '' : props.moduleId,
      });
      caseTree.value = mapTree<MinderJsonNode>(res, (e) => ({
        ...e,
        data: {
          id: e.id,
          text: e.name,
          resource: e.data?.id === 'fakeNode' ? [] : [moduleTag],
          expandState: e.level === 1 ? 'expand' : 'collapse',
          count: props.modulesCount[e.id],
        },
        children:
          props.modulesCount[e.id] > 0 && !e.children?.length
            ? [
                {
                  data: {
                    id: 'fakeNode',
                    text: 'fakeNode',
                    resource: ['fakeNode'],
                  },
                },
              ]
            : e.children,
      }));
      importJson.value.root = {
        children: caseTree.value,
        data: {
          id: 'all',
          text: t('ms.minders.allModule'),
          resource: [moduleTag],
        },
      };
      window.minder.importJson(importJson.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function initMinder() {
    try {
      loading.value = true;
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
    } finally {
      loading.value = false;
    }
  }

  watchEffect(() => {
    if (props.moduleId === 'all') {
      initCaseTree();
    } else {
      initMinder();
    }
  });

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
    if (Object.keys(node.data || {}).length === 0 || node.data?.id === 'root') {
      // 没有数据的节点或默认模块节点不可替换
      return [];
    }
    if (node.data?.resource?.some((e) => topTags.includes(e))) {
      // 选中节点属于顶级节点，可替换为除自身外的顶级标签
      return !node.children || node.children.length === 0
        ? topTags.filter((tag) => !node.data?.resource?.includes(tag))
        : [];
    }
    if (node.data?.resource?.some((e) => descTags.includes(e))) {
      // 选中节点属于描述节点，可替换为除自身外的描述标签
      if (
        node.data.resource.includes(t('ms.minders.stepDesc')) &&
        (node.parent?.children?.filter((e) => e.data?.resource?.includes(t('ms.minders.stepDesc'))) || []).length > 1
      ) {
        // 如果当前节点是步骤描述，则需要判断是否有其他步骤描述节点，如果有，则不可替换为文本描述
        return [];
      }
      return descTags.filter((tag) => !node.data?.resource?.includes(tag));
    }
    if (
      (!node.data?.resource || node.data.resource.length === 0) &&
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

  /**
   * 执行插入节点
   * @param command 插入命令
   * @param node 目标节点
   */
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

  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref<Api>();
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
        if (item.internal && item.type === 'SELECT') {
          // TODO:过滤用例等级字段，等级字段后续可自定义，需要调整
          return false;
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
      formRules.value = result.filter((e: any) => e);
      baseInfoLoading.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const extraVisible = ref<boolean>(false);
  const activeCase = ref<Record<string, any>>({});
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

  const fileList = ref<MsFileItem[]>([]);
  const attachmentsList = ref<AttachFileInfo[]>([]);
  const checkUpdateFileIds = ref<string[]>([]);

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

  const showLinkFileDrawer = ref(false);
  const attachmentLoading = ref(false);

  /**
   * 初始化用例详情
   * @param data 节点数据/用例数据
   */
  async function initCaseDetail(data: MinderJsonNodeData | Record<string, any>) {
    try {
      baseInfoLoading.value = true;
      const res = await getCaseDetail(data.id);
      activeCase.value = res;
      baseInfoForm.value.name = res.name;
      const fileIds = (res.attachments || []).map((item: any) => item.id) || [];
      if (fileIds.length) {
        checkUpdateFileIds.value = await checkFileIsUpdateRequest(fileIds);
      }
      formRules.value = initFormCreate(res.customFields, ['FUNCTIONAL_CASE:READ+UPDATE']);
      if (res.attachments) {
        attachmentsList.value = res.attachments;
        // 处理文件列表
        fileList.value = res.attachments
          .map((fileInfo: any) => {
            return {
              ...fileInfo,
              name: fileInfo.fileName,
              isUpdateFlag: checkUpdateFileIds.value.includes(fileInfo.id),
            };
          })
          .map((fileInfo: any) => {
            return convertToFile(fileInfo);
          });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      baseInfoLoading.value = false;
    }
  }

  /**
   * 处理文件更改
   * @param _fileList 文件列表
   * @param isAssociated 是否是关联文件
   */
  async function handleFileChange(_fileList: MsFileItem[], isAssociated = false) {
    try {
      attachmentLoading.value = true;
      const params = {
        request: {
          caseId: activeCase.value.id,
          projectId: appStore.currentProjectId,
          fileIds: isAssociated ? _fileList.map((item) => item.uid) : [],
          enable: true,
        },
        file: isAssociated ? _fileList.map((item) => item.file) : _fileList[0].file,
      };
      await uploadOrAssociationFile(params);
      if (isAssociated) {
        fileList.value.unshift(..._fileList);
      } else {
        fileList.value = fileList.value.map((item) => {
          if (item.status === 'init') {
            return { ...item, status: 'done', local: true };
          }
          return item;
        });
      }
      Message.success(t('ms.upload.uploadSuccess'));
      await initCaseDetail(activeCase.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      fileList.value = fileList.value.map((item) => ({ ...item, status: 'error' }));
    } finally {
      attachmentLoading.value = false;
    }
  }

  /**
   * 处理关联文件
   * @param fileData 文件信息集合
   */
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    handleFileChange(fileResultList, true);
  }

  const imageUrl = ref('');
  const previewVisible = ref<boolean>(false);

  // 预览图片
  async function handlePreview(item: MsFileItem) {
    try {
      imageUrl.value = '';
      previewVisible.value = true;
      if (!item.local) {
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

  // 删除本地文件
  async function deleteFileHandler(item: MsFileItem) {
    if (!item.local) {
      try {
        const params = {
          id: item.uid,
          local: item.local,
          caseId: activeCase.value.id,
          projectId: appStore.currentProjectId,
        };
        await deleteFileOrCancelAssociation(params);
        Message.success(
          item.local ? t('caseManagement.featureCase.deleteSuccess') : t('caseManagement.featureCase.cancelLinkSuccess')
        );
        fileList.value = fileList.value.filter((e) => e.uid !== item.uid);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    } else {
      openModal({
        type: 'error',
        title: t('caseManagement.featureCase.deleteFile', { name: item?.name }),
        content: t('caseManagement.featureCase.deleteFileTip'),
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            const params = {
              id: item.uid,
              local: item.local,
              caseId: activeCase.value.id,
              projectId: appStore.currentProjectId,
            };
            await deleteFileOrCancelAssociation(params);
            Message.success(
              item.local
                ? t('caseManagement.featureCase.deleteSuccess')
                : t('caseManagement.featureCase.cancelLinkSuccess')
            );
            fileList.value = fileList.value.filter((e) => e.uid !== item.uid);
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    }
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

  /**
   * 处理脑图节点激活/点击
   * @param node 被激活/点击的节点
   */
  async function handleNodeClick(node: MinderJsonNode) {
    const { data } = node;
    if (data?.resource && data.resource.includes(caseTag)) {
      extraVisible.value = true;
      activeExtraKey.value = 'baseInfo';
      initCaseDetail(data);
    } else if (data?.resource?.includes(moduleTag) && data.count > 0 && data.isLoaded !== true) {
      try {
        loading.value = true;
        const res = await getCaseMinder({
          projectId: appStore.currentProjectId,
          moduleId: data.id,
        });
        const fakeNode = node.children?.find((e) => e.data?.id === undefined); // 移除占位的虚拟节点
        if (fakeNode) {
          window.minder.removeNode(fakeNode);
        }
        if ((!res || res.length === 0) && node.children?.length) {
          // 如果模块下没有用例且有别的模块节点，正常展开
          node.expand();
          node.renderTree();
          window.minder.layout();
          return;
        }
        // TODO:递归渲染存在的子节点
        res.forEach((e) => {
          // 用例节点
          const child = window.minder.createNode(e.data, node);
          child.render();
          e.children?.forEach((item) => {
            // 前置/步骤/备注节点
            const grandChild = window.minder.createNode(item.data, child);
            grandChild.render();
            item.children?.forEach((subItem) => {
              // 预期结果节点
              const greatGrandChild = window.minder.createNode(subItem.data, grandChild);
              greatGrandChild.render();
            });
          });
          child.expand();
          child.renderTree();
        });
        node.expand();
        node.renderTree();
        window.minder.layout();
        if (node.data) {
          node.data.isLoaded = true;
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        loading.value = false;
      }
    } else {
      extraVisible.value = false;
      activeCase.value = {};
    }
  }

  onBeforeMount(() => {
    initDefaultFields();
  });

  const saveLoading = ref(false);
  function handleSave() {
    if (activeExtraKey.value === 'baseInfo') {
      baseInfoFormRef.value?.validate((errors) => {
        if (!errors) {
          fApi.value?.validate(async (valid) => {
            if (valid === true) {
              try {
                saveLoading.value = true;
                const data = {
                  ...baseInfoForm.value,
                  id: activeCase.value.id,
                  projectId: appStore.currentProjectId,
                  caseEditType: activeCase.value.caseEditType,
                  customFields: formItem.value.map((item: any) => {
                    return {
                      fieldId: item.field,
                      value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
                    };
                  }),
                };
                await updateCaseRequest({
                  request: data,
                  fileList: [],
                });
                const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
                if (selectedNode.data) {
                  selectedNode.data.text = baseInfoForm.value.name;
                }
                Message.success(t('common.saveSuccess'));
              } catch (error) {
                // eslint-disable-next-line no-console
                console.log(error);
              } finally {
                saveLoading.value = false;
              }
            }
          });
        }
      });
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

  watch(
    () => activeExtraKey.value,
    (val) => {
      if (val === 'comments') {
        getAllCommentList();
      }
    }
  );
</script>

<style lang="less" scoped>
  :deep(.commentWrapper) {
    right: 0;
  }
  .baseInfo-form {
    .ms-scroll-bar();

    overflow-y: auto;
    height: calc(100% - 64px);
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
