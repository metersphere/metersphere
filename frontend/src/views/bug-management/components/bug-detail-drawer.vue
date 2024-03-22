<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawerVisible"
    :width="1200"
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
    @loaded="loadedBug"
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
          v-permission="['PROJECT_BUG:READ+UPDATE']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :loading="shareLoading"
          :disabled="loading"
          @click="shareHandler"
        >
          <MsIcon type="icon-icon_share1" class="mr-1 font-[16px]" />
          {{ t('caseManagement.featureCase.share') }}
        </MsButton>
        <MsButton
          v-permission="['PROJECT_BUG:READ+UPDATE']"
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
        <MsButton type="icon" status="secondary" class="!rounded-[var(--border-radius-small)]">
          <a-dropdown position="br" :hide-on-select="false">
            <div>
              <icon-more class="mr-1" />
              <span> {{ t('caseManagement.featureCase.more') }}</span>
            </div>
            <template #content>
              <a-doption :disabled="props.currentPlatform !== detailInfo.platform" @click="handleCopy">
                <MsIcon type="icon-icon_copy_filled" class="font-[16px]" />
                {{ t('common.copy') }}
              </a-doption>
              <a-doption class="error-6 text-[rgb(var(--danger-6))]" @click="deleteHandler">
                <MsIcon type="icon-icon_delete-trash_outlined" class="font-[16px] text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </MsButton>
      </div>
    </template>
    <template #default="{ loading }">
      <div ref="wrapperRef" class="h-full bg-white">
        <MsSplitBox
          ref="wrapperRef"
          expand-direction="right"
          :max="0.7"
          :min="0.7"
          :size="900"
          :class="{ 'left-bug-detail': activeTab === 'comment' }"
        >
          <template #first>
            <div class="leftWrapper h-full">
              <div class="header h-[50px]">
                <MsTab
                  v-model:active-key="activeTab"
                  :content-tab-list="contentTabList"
                  :get-text-func="getTabBadge"
                  class="no-content relative mb-[8px]"
                />
                <div class="tab-pane-container">
                  <BugDetailTab
                    v-if="activeTab === 'detail'"
                    ref="bugDetailTabRef"
                    :form-item="formItem"
                    :allow-edit="true"
                    :detail-info="detailInfo"
                    :is-platform-default-template="isPlatformDefaultTemplate"
                    :platform-system-fields="platformSystemFields"
                    :current-platform="props.currentPlatform"
                    @update-success="updateSuccess"
                  />

                  <BugCaseTab
                    v-else-if="activeTab === 'case'"
                    :bug-id="detailInfo.id"
                    @update-case-success="updateSuccess"
                  />

                  <CommentTab v-else-if="activeTab === 'comment'" ref="commentRef" :bug-id="detailInfo.id" />

                  <BugHistoryTab v-else-if="activeTab === 'history'" :bug-id="detailInfo.id" />
                </div>
              </div>
            </div>
          </template>
          <template #second>
            <a-spin :loading="rightLoading" class="w-full">
              <!-- 所属平台一致, 详情展示 -->
              <div v-if="props.currentPlatform === detailInfo.platform" class="rightWrapper p-[24px]">
                <!-- 自定义字段开始 -->
                <div class="inline-block w-full break-words">
                  <a-skeleton v-if="loading" class="w-full" :loading="loading" :animation="true">
                    <a-space direction="vertical" class="w-[100%]" size="large">
                      <a-skeleton-line :rows="14" :line-height="30" :line-spacing="30" />
                    </a-space>
                  </a-skeleton>
                  <div v-if="!loading" class="mb-4 font-medium">
                    <strong>
                      {{ t('bugManagement.detail.basicInfo') }}
                    </strong>
                  </div>
                  <MsFormCreate
                    v-if="!loading"
                    ref="formCreateRef"
                    v-model:form-item="formItem"
                    v-model:api="fApi"
                    :form-rule="formRules"
                    class="w-full"
                    :option="options"
                    @change="handelFormCreateChange"
                  />
                  <!-- 自定义字段结束 -->
                  <div
                    v-if="!isPlatformDefaultTemplate && hasAnyPermission(['PROJECT_BUG:READ+UPDATE']) && !loading"
                    class="baseItem"
                  >
                    <a-form
                      :model="{}"
                      :label-col-props="{
                        span: 9,
                      }"
                      :wrapper-col-props="{
                        span: 15,
                      }"
                      label-align="left"
                      content-class="tags-class"
                    >
                      <a-form-item field="tags" :label="t('system.orgTemplate.tags')">
                        <MsTagsInput
                          v-model:model-value="tags"
                          :disabled="!hasAnyPermission(['PROJECT_BUG:READ+UPDATE'])"
                        />
                      </a-form-item>
                    </a-form>

                    <!--                <span class="label"> {{ t('bugManagement.detail.tag') }}</span>-->
                    <!--                <span style="width: 200px">-->
                    <!--                  <MsTag v-for="item of tags" :key="item"> {{ item }} </MsTag>-->
                    <!--                </span>-->
                  </div>
                </div>

                <!-- 内置基础信息结束 -->
              </div>
              <!-- 所属平台不一致, 详情不展示, 展示空面板 -->
              <div v-else>
                <a-empty> 暂无内容 </a-empty>
              </div>
            </a-spin>
          </template>
        </MsSplitBox>
      </div>
      <CommentInput
        v-if="activeTab === 'comment' && hasAnyPermission(['PROJECT_BUG:READ+COMMENT'])"
        :content="commentContent"
        is-show-avatar
        :upload-image="handleUploadImage"
        is-use-bottom
        :notice-user-ids="noticeUserIds"
        :preview-url="EditorPreviewFileUrl"
        @publish="publishHandler"
      />
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { defineModel, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import CommentInput from '@/components/business/ms-comment/input.vue';
  import { CommentParams } from '@/components/business/ms-comment/types';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
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
  } from '@/api/modules/bug-management/index';
  import { EditorPreviewFileUrl } from '@/api/requrls/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import useUserStore from '@/store/modules/user';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { CustomFieldItem } from '@/models/bug-management';
  import { BugEditCustomField, BugEditFormObject, BugTemplateRequest } from '@/models/bug-management';
  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import { RouteEnum } from '@/enums/routeEnum';

  const router = useRouter();
  const route = useRoute();
  const detailDrawerRef = ref<InstanceType<typeof MsDetailDrawer>>();
  const wrapperRef = ref();

  const { t } = useI18n();
  const { openDeleteModal } = useModal();

  const emit = defineEmits<{
    (e: 'submit'): void;
  }>();

  const props = defineProps<{
    visible: boolean;
    detailId: string; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
    currentPlatform: string;
  }>();
  const caseCount = ref(0);
  const appStore = useAppStore();
  const commentContent = ref('');
  const commentRef = ref();
  const noticeUserIds = ref<string[]>([]); // 通知人ids
  const fApi = ref();
  const formRules = ref<FormItem[]>([]); // 表单规则
  const formItem = ref<FormRuleItem[]>([]); // 表单项
  const currentProjectId = computed(() => appStore.currentProjectId);
  const showDrawerVisible = defineModel<boolean>('visible', { default: false });
  const bugDetailTabRef = ref();
  const isPlatformDefaultTemplate = ref(false);
  const rightLoading = ref(false);
  const rowLength = ref<number>(0);
  const activeTab = ref<string>('detail');

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
          if (item.defaultValue === 'CREATE_USER' || item.defaultValue.includes('CREATE_USER')) {
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

  const templateChange = async (v: SelectValue, valueObj: BugEditFormObject, request: BugTemplateRequest) => {
    if (v) {
      try {
        const res = await getTemplateById({
          projectId: appStore.currentProjectId,
          id: v,
          fromStatusId: request.fromStatusId,
          platformBugKey: request.platformBugKey,
        });
        platformSystemFields.value = res.customFields.filter((field) => field.platformSystemField);
        platformSystemFields.value.forEach((item) => {
          item.defaultValue = valueObj[item.fieldId];
        });
        getFormRules(
          res.customFields.filter((field) => !field.platformSystemField),
          valueObj
        );
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  };
  const getOptionFromTemplate = (field: CustomFieldItem | undefined) => {
    if (field) {
      return field.options ? field.options : JSON.parse(field.platformOptionJson);
    }
    return [];
  };
  async function loadedBug(detail: BugEditFormObject) {
    detailInfo.value = { ...detail };
    const { templateId } = detailInfo.value;
    // 是否平台默认模板
    isPlatformDefaultTemplate.value = detail.platformDefault;
    // TAG 赋值
    tags.value = detail.tags || [];
    caseCount.value = detailInfo.value.linkCaseCount;
    const tmpObj = { status: detailInfo.value.status };
    // 初始化自定义字段
    const customFieldsRes = await getTemplateById({
      projectId: appStore.currentProjectId,
      id: templateId,
      fromStatusId: detail.status,
      platformBugKey: detail.platformBugId,
    });
    currentCustomFields.value = customFieldsRes.customFields || [];
    if (detailInfo.value.customFields && Array.isArray(detailInfo.value.customFields)) {
      const MULTIPLE_TYPE = ['MULTIPLE_SELECT', 'MULTIPLE_INPUT', 'CHECKBOX', 'MULTIPLE_MEMBER'];
      const SINGRADIO_TYPE = ['RADIO', 'SELECT', 'MEMBER'];
      detail.customFields.forEach((item) => {
        if (MULTIPLE_TYPE.includes(item.type)) {
          const multipleOptions = getOptionFromTemplate(
            currentCustomFields.value.find((filed: any) => item.id === filed.fieldId)
          );
          // 如果该值在选项中已经被删除掉
          const optionsIds = (multipleOptions || []).map((e: any) => e.value);
          if (item.type !== 'MULTIPLE_INPUT') {
            const currentDefaultValue = optionsIds.filter((e: any) => JSON.parse(item.value).includes(e));
            tmpObj[item.id] = currentDefaultValue;
          } else {
            tmpObj[item.id] = JSON.parse(item.value);
          }
        } else if (item.type === 'INT' || item.type === 'FLOAT') {
          tmpObj[item.id] = Number(item.value);
        } else if (item.type === 'CASCADER') {
          const arr = JSON.parse(item.value);
          if (arr && arr instanceof Array && arr.length > 0) {
            tmpObj[item.id] = arr[arr.length - 1];
          }
        } else if (SINGRADIO_TYPE.includes(item.type)) {
          const multipleOptions = getOptionFromTemplate(
            currentCustomFields.value.find((filed: any) => item.id === filed.fieldId)
          );
          // 如果该值在选项中已经被删除掉
          const optionsIds = (multipleOptions || []).map((e: any) => e.value);
          const currentDefaultValue = optionsIds.find((e: any) => item.value === e) || '';
          tmpObj[item.id] = currentDefaultValue;
        } else {
          tmpObj[item.id] = item.value;
        }
      });
    }
    // 初始化自定义字段
    await templateChange(templateId, tmpObj, { platformBugKey: detail.platformBugId, fromStatusId: detail.status });
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

  function updateSuccess() {
    rightLoading.value = false;
    detailDrawerRef.value?.initDetail();
    emit('submit');
  }

  const contentTabList = computed(() => {
    return [
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

  const shareLoading = ref<boolean>(false);

  function shareHandler() {
    const { origin } = window.location;
    const url = `${origin}/#${route.path}?id=${detailInfo.value.id}&pId=${appStore.currentProjectId}&orgId=${appStore.currentOrgId}`;
    if (navigator.clipboard) {
      navigator.clipboard.writeText(url).then(
        () => {
          Message.info(t('bugManagement.detail.shareTip'));
        },
        (e) => {
          Message.error(e);
        }
      );
    } else {
      const input = document.createElement('input');
      input.value = url;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      Message.info(t('bugManagement.detail.shareTip'));
    }
  }

  const followLoading = ref<boolean>(false);
  // 关注
  async function followHandler() {
    followLoading.value = true;
    try {
      await followBug(detailInfo.value.id, detailInfo.value.followFlag);
      updateSuccess();
      Message.success(
        detailInfo.value.followFlag
          ? t('caseManagement.featureCase.cancelFollowSuccess')
          : t('caseManagement.featureCase.followSuccess')
      );
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  // 删除用例
  function deleteHandler() {
    const { id, name } = detailInfo.value;
    openDeleteModal({
      title: t('bugManagement.detail.deleteTitle', { name: characterLimit(name) }),
      content: t('bugManagement.detail.deleteContent'),
      onBeforeOk: async () => {
        try {
          const params = {
            id,
            deleteAll: false,
            projectId: currentProjectId.value,
          };
          await deleteSingleBug(params);
          Message.success(t('common.deleteSuccess'));
          updateSuccess();
          detailDrawerRef.value?.openPrevDetail();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  }
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

  const handelFormCreateChange = debounce(() => {
    rightLoading.value = true;
    bugDetailTabRef.value?.handleSave();
  }, 300);

  // 表单配置项
  const options = {
    resetBtn: false, // 不展示默认配置的重置和提交
    submitBtn: false,
    on: false, // 取消绑定on事件
    form: {
      layout: 'horizontal',
      labelAlign: 'left',
      labelColProps: {
        span: 9,
      },
      wrapperColProps: {
        span: 15,
      },
    },
    // 暂时默认
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
      'hide-asterisk': true,
    },
  };

  async function publishHandler(currentContent: string) {
    const regex = /data-id="([^"]*)"/g;
    const matchesNotifier = currentContent.match(regex);
    let notifiers = '';
    if (matchesNotifier?.length) {
      notifiers = matchesNotifier.map((match) => match.replace('data-id="', '').replace('"', '')).join(';');
    }
    try {
      const params = {
        bugId: detailInfo.value.id,
        notifier: notifiers,
        replyUser: '',
        parentId: '',
        content: currentContent,
        event: notifiers ? 'AT' : 'COMMENT', // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLY‘;)
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
  watch(
    () => showDrawerVisible.value,
    (val) => {
      if (val) {
        activeTab.value = 'detail';
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
  :deep(.active .arco-badge-text) {
    background: rgb(var(--primary-5));
  }
  :deep(.tags-class .arco-form-item-label-col) {
    justify-content: flex-start !important;
  }
  .left-bug-detail {
    height: 88%;
  }
  //:deep(.w-full .arco-form-item-label) {
  //  display: inline-block;
  //  width: 100%;
  //  word-wrap: break-word;
  //}
</style>
