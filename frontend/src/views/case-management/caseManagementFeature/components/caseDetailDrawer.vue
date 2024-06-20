<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawerVisible"
    :width="880"
    :footer="false"
    :mask="false"
    :title="t('caseManagement.featureCase.caseDetailTitle', { id: detailInfo?.num, name: detailInfo?.name })"
    :detail-id="props.detailId"
    :detail-index="props.detailIndex"
    :get-detail-func="getCaseDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
    :mask-closable="true"
    :edit-name="true"
    show-full-screen
    :unmount-on-close="true"
    @loaded="loadedCase"
  >
    <template #titleLeft>
      <div class="flex items-center"><caseLevel :case-level="caseLevels" /></div>
    </template>
    <template #titleRight="{ loading }">
      <div class="rightButtons flex items-center">
        <MsButton
          v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="editLoading"
          @click="updateHandler('edit')"
        >
          <MsIcon type="icon-icon_edit_outlined" class="mr-2 font-[16px]" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="shareLoading"
          @click="shareHandler"
        >
          <MsIcon type="icon-icon_share1" class="mr-2 font-[16px]" />
          {{ t('caseManagement.featureCase.share') }}
        </MsButton>
        <MsButton
          v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          :disabled="loading"
          :loading="followLoading"
          @click="followHandler"
        >
          <MsIcon
            :type="detailInfo.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
            class="mr-2 font-[16px]"
            :class="[detailInfo.followFlag ? 'text-[rgb(var(--warning-6))]' : '']"
          />
          {{ t('caseManagement.featureCase.follow') }}
        </MsButton>
        <MsButton type="icon" status="secondary" class="mr-2 !rounded-[var(--border-radius-small)]">
          <a-dropdown position="br" :hide-on-select="false">
            <div class="flex items-center">
              <icon-more class="mr-2" />
              <span> {{ t('caseManagement.featureCase.more') }}</span>
            </div>

            <template #content>
              <!-- TOTO公共用例先不上 -->
              <!-- <a-doption>
                <a-switch class="mr-1" size="small" type="line" />{{ t('caseManagement.featureCase.addToPublic') }}
              </a-doption> -->
              <a-doption @click="updateHandler('copy')">
                <MsIcon type="icon-icon_copy_filled" class="font-[16px]" />{{ t('common.copy') }}</a-doption
              >
              <a-doption class="error-6 text-[rgb(var(--danger-6))]" @click="deleteHandler">
                <MsIcon type="icon-icon_delete-trash_outlined" class="font-[16px] text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </MsButton>
      </div>
    </template>
    <template #default="{ detail, loading }">
      <div
        ref="wrapperRef"
        :class="[`${!commentInputIsActive ? 'h-[calc(100%-72px)]' : 'h-[calc(100%-286px)]'}`, 'bg-white']"
      >
        <div class="header relative h-[48px] pl-2">
          <MsTab
            v-model:active-key="activeTab"
            :content-tab-list="tabSetting"
            :get-text-func="getTotal"
            class="no-content relative border-b"
            @change="clickMenu"
          />
          <span class="display-setting h-full text-[var(--color-text-2)]" @click="showMenuSetting">{{
            t('caseManagement.featureCase.detailDisplaySetting')
          }}</span>
        </div>
        <div class="leftWrapper h-full">
          <div class="w-full p-[16px] pt-4">
            <template v-if="activeTab === 'detail'">
              <TabDetail :form="detailInfo" :allow-edit="true" :form-rules="formItem" @update-success="updateSuccess" />
            </template>
            <template v-if="activeTab === 'basicInfo'">
              <BasicInfo :loading="loading" :detail="detail" @update-success="updateSuccess" />
            </template>
            <template v-if="activeTab === 'requirement'">
              <TabDemand :case-id="detail.id" />
            </template>
            <template v-if="activeTab === 'case'">
              <TabCaseTable :case-id="detail.id" />
            </template>
            <template v-if="activeTab === 'bug'">
              <TabDefect :case-id="detail.id" />
            </template>
            <template v-if="activeTab === 'dependency'">
              <TabDependency :case-id="detail.id" />
            </template>
            <template v-if="activeTab === 'caseReview'">
              <TabCaseReview :case-id="detail.id" />
            </template>
            <template v-if="activeTab === 'testPlan'">
              <TabTestPlan :case-id="detail.id" />
            </template>
            <template v-if="activeTab === 'comments'">
              <TabComment ref="commentRef" :case-id="detail.id" :comment-value="detail.commentList" />
            </template>
            <template v-if="activeTab === 'changeHistory'">
              <TabChangeHistory :case-id="detail.id" />
            </template>
          </div>
        </div>
        <inputComment
          v-if="activeTab !== 'detail'"
          ref="commentInputRef"
          v-model:content="content"
          v-model:notice-user-ids="noticeUserIds"
          v-model:filed-ids="uploadFileIds"
          v-permission="['FUNCTIONAL_CASE:READ+COMMENT']"
          :preview-url="PreviewEditorImageUrl"
          :is-active="isActive"
          is-show-avatar
          is-use-bottom
          :upload-image="handleUploadImage"
          @publish="publishHandler"
          @cancel="cancelPublish"
        />
      </div>
    </template>
  </MsDetailDrawer>
  <SettingDrawer ref="settingDrawerRef" v-model:visible="showSettingDrawer" @init-data="initTab" />
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import inputComment from '@/components/business/ms-comment/input.vue';
  import { CommentParams } from '@/components/business/ms-comment/types';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import BasicInfo from './tabContent/basicInfo.vue';
  import SettingDrawer from './tabContent/settingDrawer.vue';

  import {
    createCommentList,
    deleteCaseRequest,
    editorUploadFile,
    followerCaseRequest,
    getCaseDetail,
    getCaseModuleTree,
  } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { defaultCaseDetail } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useUserStore from '@/store/modules/user';
  import { characterLimit } from '@/utils';

  import type { CustomAttributes, DetailCase, TabItemType } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import { getCaseLevels, initFormCreate } from './utils';
  // 异步加载组件
  const TabDefect = defineAsyncComponent(() => import('./tabContent/tabBug/tabDefect.vue'));
  const TabCaseTable = defineAsyncComponent(() => import('./tabContent/tabCase/tabCaseTable.vue'));
  const TabCaseReview = defineAsyncComponent(() => import('./tabContent/tabCaseReview.vue'));
  const TabChangeHistory = defineAsyncComponent(() => import('./tabContent/tabChangeHistory.vue'));
  const TabComment = defineAsyncComponent(() => import('./tabContent/tabComment/tabCommentIndex.vue'));
  const TabDemand = defineAsyncComponent(() => import('./tabContent/tabDemand/demand.vue'));
  const TabDependency = defineAsyncComponent(() => import('./tabContent/tabDependency/tabDependency.vue'));
  const TabDetail = defineAsyncComponent(() => import('./tabContent/tabDetail.vue'));
  const TabTestPlan = defineAsyncComponent(() => import('./tabContent/tabTestPlan.vue'));

  const router = useRouter();
  const detailDrawerRef = ref<InstanceType<typeof MsDetailDrawer>>();
  const wrapperRef = ref();
  const featureCaseStore = useFeatureCaseStore();
  const userStore = useUserStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const { copy, isSupported } = useClipboard({ legacy: true });

  const props = defineProps<{
    visible: boolean;
    detailId: string; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
  }>();

  const emit = defineEmits(['update:visible', 'success']);
  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const showDrawerVisible = ref<boolean>(false);

  const showSettingDrawer = ref<boolean>(false);
  function showMenuSetting() {
    showSettingDrawer.value = true;
  }

  const commentInputRef = ref<InstanceType<typeof inputComment>>();
  const commentInputIsActive = computed(() => commentInputRef.value?.isActive);

  const tabSetting = ref<TabItemType[]>([]);
  const activeTab = ref<string>('detail');

  function clickMenu(key: string) {
    activeTab.value = key;
    featureCaseStore.setActiveTab(key);
    switch (activeTab.value) {
      case 'setting':
        activeTab.value = 'detail';
        showMenuSetting();
        break;
      default:
        showSettingDrawer.value = false;
        break;
    }
  }

  const caseTree = ref<ModuleTreeNode[]>([]);

  async function getCaseTree() {
    try {
      caseTree.value = await getCaseModuleTree({ projectId: currentProjectId.value });
    } catch (error) {
      console.log(error);
    }
  }
  const route = useRoute();
  const detailInfo = ref<DetailCase>(cloneDeep(defaultCaseDetail));
  const customFields = ref<CustomAttributes[]>([]);
  const caseLevels = ref<CaseLevel>('P0');

  // 初始化count
  function setCount(detail: DetailCase) {
    const {
      bugCount,
      caseCount,
      caseReviewCount,
      demandCount,
      relateEdgeCount,
      testPlanCount,
      commentCount,
      historyCount,
    } = detail;
    const countMap: Record<string, any> = {
      case: caseCount || '0',
      dependency: relateEdgeCount || '0',
      caseReview: caseReviewCount || '0',
      testPlan: testPlanCount || '0',
      bug: bugCount || '0',
      requirement: demandCount || '0',
      comments: commentCount || '0',
      changeHistory: historyCount || '0',
    };
    featureCaseStore.initCountMap(countMap);
  }

  function loadedCase(detail: DetailCase) {
    getCaseTree();
    detailInfo.value = { ...detail };
    setCount(detail);
    customFields.value = detailInfo.value?.customFields as CustomAttributes[];
    caseLevels.value = getCaseLevels(customFields.value) as CaseLevel;
  }

  const editLoading = ref<boolean>(false);

  async function updateSuccess() {
    detailDrawerRef.value?.initDetail();
    emit('success');
  }

  function updateHandler(type: string) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
      query: {
        id: detailInfo.value.id,
      },
      params: {
        mode: type,
      },
    });
  }

  const shareLoading = ref<boolean>(false);

  function shareHandler() {
    const { origin } = window.location;
    const url = `${origin}/#${route.path}?id=${detailInfo.value.id}&pId=${appStore.currentProjectId}&orgId=${appStore.currentOrgId}`;
    if (isSupported) {
      copy(url);
      Message.info(t('bugManagement.detail.shareTip'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  // 关注
  const followLoading = ref<boolean>(false);
  async function followHandler() {
    followLoading.value = true;
    try {
      if (detailInfo.value.id) {
        await followerCaseRequest({ userId: userStore.id as string, functionalCaseId: detailInfo.value.id });
        updateSuccess();
        Message.success(
          detailInfo.value.followFlag
            ? t('caseManagement.featureCase.cancelFollowSuccess')
            : t('caseManagement.featureCase.followSuccess')
        );
      }
    } catch (error) {
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  // 删除用例
  function deleteHandler() {
    const { id, name } = detailInfo.value;
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.deleteCaseTitle', { name: characterLimit(name) }),
      content: t('caseManagement.featureCase.beforeDeleteCase'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const params = {
            id,
            deleteAll: false,
            projectId: currentProjectId.value,
          };
          await deleteCaseRequest(params);
          Message.success(t('common.deleteSuccess'));
          showDrawerVisible.value = false;
          emit('success');
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);

  const fApi = ref(null);
  // 初始化表单
  function initForm() {
    formRules.value = initFormCreate(customFields.value, ['FUNCTIONAL_CASE:READ+UPDATE']);
  }

  function getTotal(key: string) {
    switch (key) {
      case 'detail':
        return '';
      default:
        break;
    }
    const count = featureCaseStore.countMap[key] ?? 0;
    return featureCaseStore.countMap[key] > 99 ? '99+' : `${count > 0 ? count : ''}`;
  }

  watch(
    () => customFields.value,
    () => {
      initForm();
    },
    { deep: true }
  );

  const settingDrawerRef = ref();

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        showDrawerVisible.value = val;
        activeTab.value = 'detail';
        featureCaseStore.setActiveTab(activeTab.value);
      } else {
        activeTab.value = '';
      }
    }
  );

  watch(
    () => showDrawerVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  const content = ref('');
  const commentRef = ref();
  const isActive = ref<boolean>(false);

  const noticeUserIds = ref<string[]>([]);
  const uploadFileIds = ref<string[]>([]);
  async function publishHandler(currentContent: string) {
    try {
      const params: CommentParams = {
        caseId: detailInfo.value.id,
        notifier: noticeUserIds.value.join(';'),
        replyUser: '',
        parentId: '',
        content: currentContent,
        event: noticeUserIds.value.join(';') ? 'AT' : 'COMMENT', // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
        uploadFileIds: uploadFileIds.value,
      };
      await createCommentList(params);
      if (activeTab.value === 'comments') {
        commentRef.value.getAllCommentList();
      }

      Message.success(t('common.publishSuccessfully'));
    } catch (error) {
      console.log(error);
    }
  }

  function cancelPublish() {
    isActive.value = !isActive.value;
  }

  const tabDefaultSettingList: TabItemType[] = [
    {
      value: 'basicInfo',
      label: t('caseManagement.featureCase.basicInfo'),
      canHide: false,
      isShow: true,
    },
    {
      value: 'detail',
      label: t('caseManagement.featureCase.detail'),
      canHide: false,
      isShow: true,
    },

    {
      value: 'case',
      label: t('caseManagement.featureCase.case'),
      canHide: true,
      isShow: true,
    },
  ];

  const caseTab: TabItemType[] = [
    {
      value: 'dependency',
      label: t('caseManagement.featureCase.dependency'),
      canHide: true,
      isShow: true,
    },
    {
      value: 'caseReview',
      label: t('caseManagement.featureCase.caseReview'),
      canHide: true,
      isShow: true,
    },
    {
      value: 'testPlan',
      label: t('caseManagement.featureCase.testPlan'),
      canHide: true,
      isShow: true,
    },
    {
      value: 'comments',
      label: t('caseManagement.featureCase.comments'),
      canHide: true,
      isShow: true,
    },
    {
      value: 'changeHistory',
      label: t('caseManagement.featureCase.changeHistory'),
      canHide: true,
      isShow: true,
    },
  ];

  const buggerTab: TabItemType[] = [
    {
      value: 'requirement',
      label: t('caseManagement.featureCase.requirement'),
      canHide: true,
      isShow: true,
    },
    {
      value: 'bug',
      label: t('caseManagement.featureCase.bug'),
      canHide: true,
      isShow: true,
    },
  ];

  // 计算模块开启是否展示缺陷和需求
  const newTabDefaultSettingList = computed(() => {
    if (appStore.currentMenuConfig.includes('bugManagement')) {
      return [...tabDefaultSettingList, ...buggerTab, ...caseTab];
    }
    return [...tabDefaultSettingList, ...caseTab];
  });

  await featureCaseStore.initContentTabList([...newTabDefaultSettingList.value]);

  async function initTabConfig() {
    const result = (await featureCaseStore.getContentTabList()) || [];
    if (result.length) {
      return result.filter((item) => item.isShow);
    }
    return newTabDefaultSettingList.value;
  }

  tabSetting.value = await initTabConfig();

  const initTab = async () => {
    showSettingDrawer.value = false;
    const tmpArr = (await featureCaseStore.getContentTabList()) || [];
    tabSetting.value = tmpArr.filter((item) => item.isShow);
    activeTab.value = 'detail';
  };

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }
</script>

<style scoped lang="less">
  .display-setting {
    position: absolute;
    top: 0;
    right: 16px;
    height: 48px;
    line-height: 48px;
  }
  :deep(.arco-menu-light) {
    height: 50px;
    background: none !important;
    .arco-menu-inner {
      overflow: hidden;
      padding: 14px 0;
      height: 50px;
    }
  }
  .leftWrapper {
    .header {
      padding: 0 16px;
      border-bottom: 1px solid var(--color-text-n8);
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
</style>
