<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawerVisible"
    :width="1200"
    :footer="false"
    :title="t('bugManagement.detail.title', { id: detailInfo?.id, name: detailInfo?.title })"
    :detail-id="props.detailId"
    :detail-index="props.detailIndex"
    :get-detail-func="getBugDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
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
              <a-doption class="error-6 text-[rgb(var(--danger-6))]" @click="deleteHandler">
                <MsIcon type="icon-icon_delete-trash_outlined" class="font-[16px] text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </MsButton>
        <MsButton type="icon" status="secondary" class="!rounded-[var(--border-radius-small)]" @click="toggle">
          <MsIcon :type="isFullscreen ? 'icon-icon_off_screen' : 'icon-icon_full_screen_one'" class="mr-1" size="16" />
          {{ t('caseManagement.featureCase.fullScreen') }}
        </MsButton>
      </div>
    </template>
    <template #default>
      <div ref="wrapperRef" class="h-full bg-white">
        <MsSplitBox ref="wrapperRef" expand-direction="right" :max="0.7" :min="0.7" :size="900">
          <template #first>
            <div class="leftWrapper h-full">
              <div class="header h-[50px]">
                <a-tabs v-model:active-key="activeTab">
                  <a-tab-pane key="detail">
                    <BugDetailTab :detail-info="detailInfo" />
                  </a-tab-pane>
                  <a-tab-pane key="case">
                    <BugCaseTab :detail-info="detailInfo" />
                  </a-tab-pane>
                  <a-tab-pane key="comment">
                    <MsComment />
                  </a-tab-pane>
                </a-tabs>
              </div>
            </div>
          </template>
          <template #second>
            <div class="rightWrapper p-[24px]">
              <div class="mb-4 font-medium">{{ t('bugManagement.detail.basicInfo') }}</div>
              <div class="baseItem">
                <span class="label"> {{ t('bugManagement.detail.handleUser') }}</span>
                <MsUserSelector v-model:model-value="detailInfo.handleUser" />
              </div>
              <!-- 自定义字段开始 -->
              <MsFormCreate
                v-if="formRules.length"
                ref="formCreateRef"
                v-model:form-item="formItem"
                v-model:api="fApi"
                :form-rule="formRules"
                class="w-full"
                :option="options"
              />
              <!-- 自定义字段结束 -->
              <div class="baseItem">
                <span class="label"> {{ t('bugManagement.detail.tag') }}</span>
                <a-input-tag v-model:model-value="detailInfo.tag" />
              </div>
              <div class="baseItem">
                <span class="label"> {{ t('bugManagement.detail.creator') }}</span>
                <span>{{ detailInfo?.createUser }}</span>
              </div>
              <div class="baseItem">
                <span class="label"> {{ t('bugManagement.detail.createTime') }}</span>
                <span>{{ dayjs(detailInfo?.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
              </div>
            </div>
          </template>
        </MsSplitBox>
      </div>
    </template>
  </MsDetailDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useFullscreen } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsComment from '@/components/business/ms-comment';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import { MsUserSelector } from '@/components/business/ms-user-selector';
  import BugCaseTab from './bugCaseTab.vue';
  import BugDetailTab from './bugDetailTab.vue';

  import { deleteSingleBug, followBug, getBugDetail } from '@/api/modules/bug-management/index';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useUserStore from '@/store/modules/user';
  import { characterLimit, findNodeByKey } from '@/utils';

  import type { CaseManagementTable, CustomAttributes, TabItemType } from '@/models/caseManagement/featureCase';
  import { RouteEnum } from '@/enums/routeEnum';

  const router = useRouter();
  const detailDrawerRef = ref<InstanceType<typeof MsDetailDrawer>>();
  const wrapperRef = ref();
  const { isFullscreen, toggle } = useFullscreen(wrapperRef);
  const featureCaseStore = useFeatureCaseStore();
  const userStore = useUserStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const props = defineProps<{
    visible: boolean;
    detailId: string; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
  }>();

  const emit = defineEmits(['update:visible']);

  const userId = computed(() => userStore.userInfo.id);
  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const showDrawerVisible = ref<boolean>(false);

  const tabSettingList = computed(() => {
    return featureCaseStore.tabSettingList;
  });

  const tabSetting = ref<TabItemType[]>([...tabSettingList.value]);
  const activeTab = ref<string | number>('detail');

  const detailInfo = ref<Record<string, any>>({});
  const customFields = ref<CustomAttributes[]>([]);

  function loadedBug(detail: CaseManagementTable) {
    detailInfo.value = { ...detail };
    customFields.value = detailInfo.value.customFields;
  }

  const moduleName = computed(() => {
    return findNodeByKey<Record<string, any>>(featureCaseStore.caseTree, detailInfo.value?.moduleId as string, 'id')
      ?.name;
  });

  const editLoading = ref<boolean>(false);

  function updateSuccess() {
    detailDrawerRef.value?.initDetail();
  }

  function updateHandler() {
    router.push({
      name: RouteEnum.BUG_MANAGEMENT_DETAIL,
      query: {
        id: detailInfo.value.id,
      },
    });
  }

  const shareLoading = ref<boolean>(false);

  function shareHandler() {
    Message.info(t('bugManagement.detail.shareTip'));
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
          await deleteSingleBug(params);
          Message.success(t('common.deleteSuccess'));
          updateSuccess();
          detailDrawerRef.value?.openPrevDetail();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);

  const isDisabled = ref<boolean>(false);

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

  const fApi = ref(null);
  // 初始化表单
  function initForm() {
    formRules.value = customFields.value.map((item: any) => {
      return {
        type: item.type,
        name: item.fieldId,
        label: item.fieldName,
        value: JSON.parse(item.defaultValue),
        required: item.required,
        options: item.options || [],
        props: {
          modelValue: JSON.parse(item.defaultValue),
          disabled: isDisabled.value,
          options: item.options || [],
        },
      };
    }) as FormItem[];
  }

  watch(
    () => customFields.value,
    () => {
      initForm();
    },
    { deep: true }
  );

  watch(
    () => props.visible,
    (val) => {
      showDrawerVisible.value = val;
    }
  );

  watch(
    () => showDrawerVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  watch(
    () => tabSettingList.value,
    () => {
      tabSetting.value = featureCaseStore.getTab();
    },
    { deep: true, immediate: true }
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
  :deep(.active .arco-badge-number) {
    background: rgb(var(--primary-5));
  }
</style>
