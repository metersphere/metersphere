<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawerVisible"
    :width="850"
    :footer="false"
    :title="t('bugManagement.detail.title', { id: detailInfo?.num, name: detailInfo?.title })"
    :tooltip-text="(detailInfo && detailInfo.title) || null"
    :detail-id="props.detailId"
    :detail-index="props.detailIndex"
    :get-detail-func="getBugDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
    show-full-screen
    unmount-on-close
    :mask="false"
    @loading-detail="setDetailLoading"
    @loaded="loadedBug"
    @get-detail="getDetail"
  >
    <template #titleLeft>
      <div class="flex items-center">
        <MsTag
          size="medium"
          :closable="false"
          :type="props.currentPlatform === detailInfo.platform ? 'primary' : 'default'"
          theme="light"
        >
          {{ detailInfo['platform'] }}
        </MsTag>
      </div>
    </template>
    <template #titleRight="{ loading }">
      <div class="rightButtons flex items-center">
        <MsButton
          v-permission="['PROJECT_BUG:READ+UPDATE']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :loading="editLoading"
          :disabled="loading || props.currentPlatform !== detailInfo.platform"
          @click="updateHandler"
        >
          <MsIcon type="icon-icon_edit_outlined" class="mr-1 font-[16px]" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :loading="shareLoading"
          :disabled="loading"
          @click="shareHandler"
        >
          <MsIcon type="icon-icon_link-copy_outlined" class="mr-1 font-[16px]" />
          {{ t('caseManagement.featureCase.share') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :loading="followLoading"
          :disabled="loading"
          @click="followHandler"
        >
          <MsIcon
            :type="detailInfo.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
            class="mr-1 font-[16px]"
            :class="[detailInfo.followFlag ? 'text-[rgb(var(--warning-6))]' : '']"
          />
          {{ t('caseManagement.featureCase.follow') }}
        </MsButton>
        <MsButton
          v-permission="['PROJECT_BUG:READ+ADD', 'PROJECT_BUG:READ+DELETE']"
          type="icon"
          status="secondary"
          class="mr-2 !rounded-[var(--border-radius-small)]"
        >
          <a-dropdown position="br" :hide-on-select="false">
            <div>
              <icon-more class="mr-1" />
              <span> {{ t('caseManagement.featureCase.more') }}</span>
            </div>
            <template #content>
              <a-doption
                v-permission="['PROJECT_BUG:READ+ADD']"
                :disabled="props.currentPlatform !== detailInfo.platform"
                @click="handleCopy"
              >
                <MsIcon type="icon-icon_copy_filled" class="font-[16px]" />
                {{ t('common.copy') }}
              </a-doption>
              <a-doption
                v-permission="['PROJECT_BUG:READ+DELETE']"
                class="error-6 text-[rgb(var(--danger-6))]"
                @click="deleteHandler"
              >
                <MsIcon type="icon-icon_delete-trash_outlined1" class="font-[16px] text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </MsButton>
      </div>
    </template>
    <template #default>
      <div ref="wrapperRef" class="bg-[var(--color-text-fff)]">
        <div class="header relative h-[48px] pl-2">
          <MsTab
            v-model:active-key="activeTab"
            :content-tab-list="contentTabList"
            :get-text-func="getTabBadge"
            class="no-content relative border-b"
          />
        </div>
        <div
          :class="`${!commentInputIsActive ? 'h-[calc(100vh-174px)]' : 'h-[calc(100vh-378px)]'} content-wrapper w-full`"
        >
          <a-spin :loading="detailLoading" class="h-full w-full">
            <div class="tab-pane-container">
              <BugDetailTab
                v-if="activeTab === 'detail'"
                ref="bugDetailTabRef"
                :allow-edit="hasAnyPermission(['PROJECT_BUG:READ+UPDATE'])"
                :detail-info="detailInfo"
                :current-custom-fields="currentCustomFields"
                :is-platform-default-template="isPlatformDefaultTemplate"
                :platform-system-fields="platformSystemFields"
                :current-platform="props.currentPlatform"
                @update-success="updateSuccessHandler"
              />
              <BasicInfo
                v-if="activeTab === 'basicInfo'"
                v-model:tags="tags"
                :form-rule="formRules"
                :detail="detailInfo"
                :current-custom-fields="currentCustomFields"
                :current-platform="props.currentPlatform"
                :is-platform-default-template="isPlatformDefaultTemplate"
                :loading="rightLoading"
                :platform-system-fields="platformSystemFields"
                @update-success="loadList"
              />

              <BugCaseTab
                v-else-if="activeTab === 'case'"
                :bug-id="detailInfo.id"
                @update-case-success="updateSuccess"
              />

              <CommentTab v-else-if="activeTab === 'comment'" ref="commentRef" :bug-id="detailInfo.id" />

              <BugHistoryTab v-else-if="activeTab === 'history'" :bug-id="detailInfo.id" />
            </div>
          </a-spin>
        </div>
      </div>
      <CommentInput
        v-if="activeTab === 'comment' && hasAnyPermission(['PROJECT_BUG:READ+COMMENT'])"
        ref="commentInputRef"
        v-model:notice-user-ids="noticeUserIds"
        v-model:filed-ids="uploadFileIds"
        :content="commentContent"
        is-show-avatar
        :upload-image="handleUploadImage"
        is-use-bottom
        :preview-url="`${EditorPreviewFileUrl}/${appStore.currentProjectId}`"
        @publish="publishHandler"
      />
    </template>
  </MsDetailDrawer>
  <DeleteModal
    :id="props.detailId"
    v-model:visible="deleteVisible"
    :name="detailInfo.title"
    :remote-func="deleteSingleBug"
    @submit="handleSingleDelete"
  />
</template>

<script setup lang="ts">
  import { useRoute, useRouter } from 'vue-router';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { FormItem } from '@/components/pure/ms-form-create/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import CommentInput from '@/components/business/ms-comment/input.vue';
  import { CommentParams } from '@/components/business/ms-comment/types';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import BasicInfo from './basicInfo.vue';
  import BugCaseTab from './bugCaseTab.vue';
  import BugDetailTab from './bugDetailTab.vue';
  import BugHistoryTab from './bugHistoryTab.vue';
  import CommentTab from './commentTab.vue';

  import {
    createOrUpdateComment,
    deleteSingleBug,
    editorUploadFile,
    followBug,
    getBugDetail,
    getTemplateById,
  } from '@/api/modules/bug-management';
  import { EditorPreviewFileUrl } from '@/api/requrls/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import useUserStore from '@/store/modules/user';
  import { hasAnyPermission } from '@/utils/permission';

  import type { CustomFieldItem } from '@/models/bug-management';
  import { BugEditCustomField, BugEditFormObject } from '@/models/bug-management';
  import { BugManagementRouteEnum, RouteEnum } from '@/enums/routeEnum';

  const DeleteModal = defineAsyncComponent(() => import('@/views/bug-management/components/deleteModal.vue'));

  const router = useRouter();
  const route = useRoute();
  const detailDrawerRef = ref<InstanceType<typeof MsDetailDrawer>>();
  const wrapperRef = ref();

  const { t } = useI18n();
  const { openDeleteModal } = useModal();
  const { copy, isSupported } = useClipboard({ legacy: true });

  const emit = defineEmits<{
    (e: 'submit'): void;
  }>();

  const props = defineProps<{
    visible: boolean;
    detailId: string; // 详情 id
    detailIndex?: number; // 详情 下标
    detailDefaultTab: string; // 详情默认 tab
    tableData?: any[]; // 表格数据
    pagination?: MsPaginationI; // 分页器对象
    pageChange?: (page: number) => Promise<void>; // 分页变更函数
    currentPlatform: string;
  }>();
  const caseCount = ref(0);
  const appStore = useAppStore();
  const commentContent = ref('');
  const commentRef = ref();
  const noticeUserIds = ref<string[]>([]); // 通知人ids
  const formRules = ref<FormItem[]>([]); // 表单规则

  const currentProjectId = computed(() => appStore.currentProjectId);
  const showDrawerVisible = defineModel<boolean>('visible', { default: false });
  const bugDetailTabRef = ref();
  const isPlatformDefaultTemplate = ref(false);
  const rightLoading = ref(false);
  const detailLoading = ref(false);
  const activeTab = ref<string>('detail');
  const currentDetailId = ref(props.detailId);

  const commentInputRef = ref<InstanceType<typeof CommentInput>>();
  const commentInputIsActive = computed(() => commentInputRef.value?.isActive);

  const detailInfo = ref<Record<string, any>>({ match: [] }); // 存储当前详情信息，通过loadBug 获取
  const tags = ref([]);
  const platformSystemFields = ref<BugEditCustomField[]>([]); // 平台系统字段
  const userStore = useUserStore();
  // 处理表单格式
  const getFormRules = (arr: BugEditCustomField[], valueObj: BugEditFormObject) => {
    formRules.value = [];
    const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
    if (Array.isArray(arr) && arr.length) {
      formRules.value = arr.map((item: any) => {
        let initValue = valueObj[item.fieldId];
        const initOptions = item.options ? item.options : JSON.parse(item.platformOptionJson);
        if (memberType.includes(item.type)) {
          // 详情为空, 默认值为当前
          if (
            initValue == null &&
            initValue === '' &&
            (item.defaultValue === 'CREATE_USER' || item.defaultValue.includes('CREATE_USER'))
          ) {
            initValue = item.type === 'MEMBER' ? userStore.id : [userStore.id];
          }
        }
        return {
          type: item.type,
          name: item.fieldId,
          label: item.fieldName,
          value: initValue,
          options: initOptions,
          required: item.required as boolean,
          platformPlaceHolder: item.platformPlaceHolder,
          props: {
            modelValue: initValue,
            options: initOptions,
            disabled: !hasAnyPermission(['PROJECT_BUG:READ+UPDATE']),
          },
        };
      });
    }
  };

  const currentCustomFields = ref<CustomFieldItem[]>([]);

  const getOptionFromTemplate = (field: CustomFieldItem | undefined) => {
    if (field) {
      return field.options ? field.options : JSON.parse(field.platformOptionJson);
    }
    return [];
  };
  // TODO:: Record<string, any>
  async function loadedBug(detail: BugEditFormObject) {
    currentDetailId.value = detail.id;
    // 是否平台默认模板
    isPlatformDefaultTemplate.value = detail.platformDefault;
    // 关闭loading
    detailLoading.value = false;
    const customFieldsRes = await getTemplateById({
      projectId: appStore.currentProjectId,
      id: detail.templateId,
      fromStatusId: detail.status,
      platformBugKey: detail.platformBugId,
      showLocal: detail.platform === 'Local',
    });
    // 详情信息, TAG赋值
    detailInfo.value = { ...detail };
    tags.value = detail.tags || [];
    caseCount.value = detailInfo.value.linkCaseCount;
    const tmpObj: Record<string, any> = { status: detailInfo.value.status };
    platformSystemFields.value = customFieldsRes.customFields.filter(
      (field: Record<string, any>) => field.platformSystemField
    );
    currentCustomFields.value = customFieldsRes.customFields || [];
    if (detailInfo.value.customFields && Array.isArray(detailInfo.value.customFields)) {
      const MULTIPLE_TYPE = ['MULTIPLE_SELECT', 'MULTIPLE_INPUT', 'CHECKBOX', 'MULTIPLE_MEMBER'];
      const SINGLE_TYPE = ['RADIO', 'SELECT', 'MEMBER'];
      detail.customFields.forEach((item: Record<string, any>) => {
        if (MULTIPLE_TYPE.includes(item.type)) {
          const multipleOptions = getOptionFromTemplate(
            currentCustomFields.value.find((filed: any) => item.id === filed.fieldId)
          );
          // 如果该值在选项中已经被删除掉
          const optionsIds = (multipleOptions || []).map((e: any) => e.value);
          if (item.value) {
            if (item.type !== 'MULTIPLE_INPUT') {
              tmpObj[item.id] = optionsIds.filter((e: any) => JSON.parse(item.value).includes(e));
            } else {
              tmpObj[item.id] = JSON.parse(item.value);
            }
          }
        } else if (item.type === 'INT' || item.type === 'FLOAT') {
          tmpObj[item.id] = Number(item.value);
        } else if (item.type === 'CASCADER') {
          if (item.value !== '') {
            const arr = JSON.parse(item.value);
            if (arr && arr instanceof Array && arr.length > 0) {
              tmpObj[item.id] = arr[arr.length - 1];
            }
          }
        } else if (SINGLE_TYPE.includes(item.type)) {
          const multipleOptions = getOptionFromTemplate(
            currentCustomFields.value.find((filed: any) => item.id === filed.fieldId)
          );
          // 如果该值在选项中已经被删除掉
          const optionsIds = (multipleOptions || []).map((e: any) => e.value);
          tmpObj[item.id] = optionsIds.find((e: any) => item.value === e) || '';
        } else {
          tmpObj[item.id] = item.value;
        }
      });
    }
    // 初始化自定义字段
    platformSystemFields.value.forEach((item) => {
      item.defaultValue = tmpObj[item.fieldId];
    });

    getFormRules(
      customFieldsRes.customFields.filter((field: Record<string, any>) => !field.platformSystemField),
      tmpObj
    );
  }

  /**
   * 详情加载中
   */
  function setDetailLoading() {
    detailLoading.value = true;
  }

  /**
   * 获取 tab 的参数数量徽标
   */
  function getTabBadge(tabKey: string) {
    switch (tabKey) {
      case 'detail':
        return '';
      case 'case':
        return `${caseCount.value > 0 ? caseCount.value : ''}`;
      case 'comment':
        return '';
      case 'history':
        return '';
      default:
        return '';
    }
  }

  const editLoading = ref<boolean>(false);

  async function getDetail() {
    const res = await getBugDetail(props.detailId);
    loadedBug(res);
  }

  function updateSuccess() {
    getDetail();
    emit('submit');
  }

  const tabList = [
    {
      value: 'basicInfo',
      label: t('bugManagement.detail.basicInfo'),
    },
    {
      value: 'detail',
      label: t('bugManagement.detail.detail'),
    },
    {
      value: 'case',
      label: t('bugManagement.detail.case'),
    },
    {
      value: 'comment',
      label: t('bugManagement.detail.comment'),
    },
    {
      value: 'history',
      label: t('bugManagement.detail.changeHistory'),
    },
  ];

  /**
   * 如果模块没有开启用例管理
   */
  const contentTabList = computed(() => {
    return appStore.currentMenuConfig.includes('caseManagement')
      ? tabList
      : tabList.filter((item) => item.value !== 'case');
  });

  function updateHandler() {
    router.push({
      name: RouteEnum.BUG_MANAGEMENT_DETAIL,
      query: {
        id: detailInfo.value.id,
      },
      params: {
        mode: 'edit',
      },
    });
  }

  function loadList() {
    detailDrawerRef.value?.initDetail();
    emit('submit');
  }

  const shareLoading = ref<boolean>(false);

  function shareHandler() {
    const url = `${window.location.origin}#${
      router.resolve({ name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX }).fullPath
    }?
      id=${detailInfo.value.id}&orgId=${appStore.currentOrgId}&pId=${appStore.currentProjectId}`;
    if (isSupported) {
      copy(url);
      Message.info(t('bugManagement.detail.shareTip'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const followLoading = ref<boolean>(false);
  // 关注
  async function followHandler() {
    followLoading.value = true;
    try {
      await followBug(detailInfo.value.id, detailInfo.value.followFlag);
      Message.success(
        detailInfo.value.followFlag
          ? t('caseManagement.featureCase.cancelFollowSuccess')
          : t('caseManagement.featureCase.followSuccess')
      );
      detailInfo.value.followFlag = !detailInfo.value.followFlag;
      emit('submit');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }
  const deleteVisible = ref(false);

  // 删除用例
  function deleteHandler() {
    deleteVisible.value = true;
  }

  const handleSingleDelete = () => {
    emit('submit');
    if (!props.pagination && !props.tableData) {
      showDrawerVisible.value = false;
    } else {
      detailDrawerRef.value?.openNextDetail();
    }
  };
  // 复制bug
  function handleCopy() {
    router.push({
      name: RouteEnum.BUG_MANAGEMENT_DETAIL,
      query: {
        id: detailInfo.value.id,
      },
      params: {
        mode: 'copy',
      },
    });
  }

  const uploadFileIds = ref<string[]>([]);
  async function publishHandler(currentContent: string) {
    try {
      const params = {
        bugId: detailInfo.value.id,
        notifier: noticeUserIds.value.join(';'),
        replyUser: '',
        parentId: '',
        content: currentContent,
        event: noticeUserIds.value.join(';') ? 'AT' : 'COMMENT', // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLY‘;)
        uploadFileIds: uploadFileIds.value,
      };
      await createOrUpdateComment(params as CommentParams);
      Message.success(t('common.publishSuccessfully'));
      commentRef.value?.initData(detailInfo.value.id);
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

  async function updateSuccessHandler() {
    if (props.pagination) {
      detailDrawerRef.value?.initDetail();
    } else {
      updateSuccess();
    }
  }

  watch(
    () => showDrawerVisible.value,
    (val) => {
      if (val) {
        if (props.detailDefaultTab) {
          activeTab.value = props.detailDefaultTab;
        } else {
          activeTab.value = 'detail';
        }
      }
    }
  );
</script>

<style scoped lang="less">
  .leftWrapper {
    .header {
      padding: 0 16px;
      border-bottom: 1px solid var(--color-text-n8);
    }
  }
  .content-wrapper {
    @apply overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();
  }
  .rightWrapper {
    .baseItem {
      margin-bottom: 16px;
      height: 32px;
      line-height: 32px;
      @apply flex;
      .label {
        width: 84px;
        color: var(--color-text-3);
      }
    }
    :deep(.arco-form-item-layout-horizontal) {
      margin-bottom: 16px !important;
    }
    :deep(.arco-form-item-label-col > .arco-form-item-label) {
      color: var(--color-text-3) !important;
    }
    :deep(.arco-select-view-single) {
      border-color: transparent !important;
      .arco-select-view-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        .arco-select-view-suffix {
          visibility: visible !important;
        }
      }
      &:hover > .arco-input {
        font-weight: normal;
        text-decoration: none;
        color: var(--color-text-1);
      }
      & > .arco-input {
        font-weight: 500;
        text-decoration: underline;
        color: var(--color-text-1);
      }
    }
    :deep(.arco-input-tag) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-input-wrapper) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-select-view-multiple) {
      border-color: transparent !important;
      .arco-select-view-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        .arco-select-view-suffix {
          visibility: visible !important;
        }
      }
    }
    :deep(.arco-textarea-wrapper) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-input-number) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-picker) {
      border-color: transparent !important;
      .arco-picker-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        arco-picker-suffix {
          visibility: visible !important;
        }
      }
    }
  }
  .rightButtons {
    :deep(.ms-button--secondary):hover,
    :hover > .arco-icon {
      color: rgb(var(--primary-5)) !important;
      background: var(--color-bg-3);
      .arco-icon:hover {
        color: rgb(var(--primary-5)) !important;
      }
    }
  }
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
  :deep(.tags-class .arco-form-item-label-col) {
    justify-content: flex-start !important;
  }
  .tab-pane-container {
    @apply flex-1 overflow-y-auto p-4;
    .ms-scroll-bar();
  }
  :deep(.arco-form-item-content) {
    overflow-wrap: anywhere;
  }
</style>
