<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawerVisible"
    :width="1200"
    :footer="false"
    :title="t('bugManagement.detail.title', { id: detailInfo?.num, name: detailInfo?.title })"
    :detail-id="props.detailId"
    :detail-index="props.detailIndex"
    :get-detail-func="getBugDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
    show-full-screen
    @loaded="loadedBug"
  >
    <template #titleRight="{ loading }">
      <div class="rightButtons flex items-center">
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="editLoading"
          @click="updateHandler"
        >
          <MsIcon type="icon-icon_edit_outlined" class="mr-1 font-[16px]" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="shareLoading"
          @click="shareHandler"
        >
          <MsIcon type="icon-icon_share1" class="mr-1 font-[16px]" />
          {{ t('caseManagement.featureCase.share') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="followLoading"
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
              <a-doption @click="handleCopy">
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
    <template #default>
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
                  class="no-content relative mb-[8px] border-b border-[var(--color-text-n8)]"
                />
                <div class="tab-pane-container">
                  <BugDetailTab
                    v-if="activeTab === 'detail'"
                    ref="bugDetailTabRef"
                    :form-item="formItem"
                    :allow-edit="true"
                    :detail-info="detailInfo"
                    @update-success="updateSuccess"
                  />

                  <BugCaseTab
                    v-else-if="activeTab === 'case'"
                    :bug-id="detailInfo.id"
                    @updateCaseSuccess="updateSuccess"
                  />

                  <CommentTab v-else-if="activeTab === 'comment'" ref="commentRef" :bug-id="detailInfo.id" />

                  <BugHistoryTab v-else-if="activeTab === 'history'" :bug-id="detailInfo.id" />
                </div>
              </div>
            </div>
          </template>
          <template #second>
            <div class="rightWrapper p-[24px]">
              <div class="mb-4 font-medium">{{ t('bugManagement.detail.basicInfo') }}</div>
              <!-- 自定义字段开始 -->
              <MsFormCreate
                v-if="formRules.length"
                ref="formCreateRef"
                v-model:form-item="formItem"
                v-model:api="fApi"
                :form-rule="formRules"
                class="w-full"
                :option="options"
                @change="handelFormCreateChange"
              />
              <!-- 自定义字段结束 -->
              <div class="baseItem">
                <span class="label"> {{ t('bugManagement.detail.tag') }}</span>
                <span style="width: 200px">
                  <MsTag v-for="item of tags" :key="item"> {{ item }} </MsTag>
                </span>
              </div>
            </div>
          </template>
        </MsSplitBox>
      </div>
      <CommentInput
        v-if="activeTab === 'comment'"
        :content="commentContent"
        is-show-avatar
        is-use-bottom
        :notice-user-ids="noticeUserIds"
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
  import { CommentInput } from '@/components/business/ms-comment';
  import { CommentParams } from '@/components/business/ms-comment/types';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import BugCaseTab from './bugCaseTab.vue';
  import BugDetailTab from './bugDetailTab.vue';
  import BugHistoryTab from './bugHistoryTab.vue';
  import CommentTab from './commentTab.vue';

  import {
    createOrUpdateComment,
    deleteSingleBug,
    followBug,
    getBugDetail,
    getTemplateById,
  } from '@/api/modules/bug-management/index';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import { characterLimit } from '@/utils';

  import { BugEditCustomField, BugEditFormObject, BugTemplateRequest } from '@/models/bug-management';
  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import { RouteEnum } from '@/enums/routeEnum';

  const router = useRouter();
  const route = useRoute();
  const detailDrawerRef = ref<InstanceType<typeof MsDetailDrawer>>();
  const wrapperRef = ref();

  const { t } = useI18n();
  const { openDeleteModal } = useModal();

  const props = defineProps<{
    visible: boolean;
    detailId: string; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
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

  const activeTab = ref<string>('detail');

  const detailInfo = ref<Record<string, any>>({ match: [] }); // 存储当前详情信息，通过loadBug 获取
  const tags = ref([]);

  // 处理表单格式
  const getFormRules = (arr: BugEditCustomField[], valueObj: BugEditFormObject) => {
    formRules.value = [];
    if (Array.isArray(arr) && arr.length) {
      formRules.value = arr.map((item) => {
        return {
          type: item.type,
          name: item.fieldId,
          label: item.fieldName,
          value: valueObj[item.fieldId],
          options: item.platformOptionJson ? JSON.parse(item.platformOptionJson) : item.options,
          required: item.required as boolean,
          props: {
            modelValue: valueObj[item.fieldId],
            options: item.platformOptionJson ? JSON.parse(item.platformOptionJson) : item.options,
          },
        };
      });
    }
  };

  const templateChange = async (v: SelectValue, valueObj: BugEditFormObject, request: BugTemplateRequest) => {
    if (v) {
      try {
        const res = await getTemplateById({
          projectId: appStore.currentProjectId,
          id: v,
          fromStatusId: request.fromStatusId,
          platformBugKey: request.platformBugKey,
        });
        getFormRules(res.customFields, valueObj);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  };
  async function loadedBug(detail: BugEditFormObject) {
    detailInfo.value = { ...detail };
    const { templateId } = detail;
    // tag 赋值
    tags.value = detail.tags || [];
    caseCount.value = detail.linkCaseCount;
    const tmpObj = {};
    if (detail.customFields && Array.isArray(detail.customFields)) {
      detail.customFields.forEach((item) => {
        if (item.type === 'MULTIPLE_SELECT') {
          tmpObj[item.id] = JSON.parse(item.value);
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
    detailDrawerRef.value?.initDetail();
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
    const url = `${origin}/#${route.path}?id=${detailInfo.value.id}&projectId=${appStore.currentProjectId}&organizationId=${appStore.currentOrgId}`;
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
        width: 38%;
        color: var(--color-text-3);
      }
    }
    :deep(.arco-form-item-layout-horizontal) {
      margin-bottom: 16px !important;
    }
    :deep(.arco-form-item-label-col > .arco-form-item-label) {
      color: var(--color-text-3) !important;
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
  .left-bug-detail {
    height: 88%;
  }
</style>
