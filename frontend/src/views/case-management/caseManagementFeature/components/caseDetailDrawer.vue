<template>
  <MsDetailDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawerVisible"
    :width="1200"
    :footer="false"
    :title="t('caseManagement.featureCase.caseDetailTitle', { id: detailInfo?.id, name: detailInfo?.name })"
    :detail-id="props.detailId"
    :detail-index="props.detailIndex"
    :get-detail-func="getCaseDetail"
    :pagination="props.pagination"
    :table-data="props.tableData"
    :page-change="props.pageChange"
    @loaded="loadedCase"
  >
    <template #titleRight="{ loading, detail }">
      <div class="rightButtons flex items-center">
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
          :disabled="loading"
          :loading="editLoading"
          @click="editHandler(detail)"
        >
          <MsIcon type="icon-icon_edit_outlined" class="mr-1 font-[16px] text-[var(--color-text-1)]" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
          :disabled="loading"
          :loading="shareLoading"
          @click="shareHandler(detail)"
        >
          <MsIcon type="icon-icon_share1" class="mr-1 font-[16px] text-[var(--color-text-1)]" />
          {{ t('caseManagement.featureCase.share') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
          :disabled="loading"
          :loading="followLoading"
          @click="followHandler(detail)"
        >
          <MsIcon
            :type="detail.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
            class="mr-1 font-[16px] text-[var(--color-text-1)]"
            :class="[detail.followFlag ? 'text-[rgb(var(--warning-6))]' : 'text-[var(--color-text-1)]']"
          />
          {{ t('caseManagement.featureCase.follow') }}
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
        >
          <a-dropdown position="br">
            <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect"></MsTableMoreAction>
            <span> 更多</span>
            <template #content>
              <a-doption> <a-switch class="mr-1" size="small" />添加公共组件库 </a-doption>
              <a-doption>
                <MsIcon type="icon-icon_copy_filled" class="font-[16px] text-[var(--color-text-1)]" />复制</a-doption
              >
              <a-doption class="error-6 text-[rgb(var(--danger-6))]">
                <MsIcon type="icon-icon_delete-trash_outlined" class="font-[16px] text-[rgb(var(--danger-6))]" />
                删除
              </a-doption>
            </template>
          </a-dropdown>
        </MsButton>
        <MsButton
          type="icon"
          status="secondary"
          class="!rounded-[var(--border-radius-small)] !text-[var(--color-text-1)]"
          @click="toggle"
        >
          <MsIcon
            :type="isFullscreen ? 'icon-icon_off_screen' : 'icon-icon_full_screen_one'"
            class="mr-1 text-[var(--color-text-1)]"
            size="16"
          />
          {{ t('caseManagement.featureCase.fullScreen') }}
        </MsButton>
      </div>
    </template>
    <template #default="{ detail }">
      <MsSplitBox expand-direction="right" :max="0.7" :min="0.7" :size="900">
        <template #left>
          <div class="leftWrapper h-full">
            <div class="header h-[49px]">
              <a-tabs @change="changeTabs">
                <a-tab-pane key="detail">
                  <template #title> {{ t('caseManagement.featureCase.detail') }}</template>
                  {{ detail }}
                </a-tab-pane>
                <a-tab-pane v-for="tab of tabSetting" :key="tab.key">
                  <template #title>
                    <div class="flex items-center">
                      <span>{{ tab.title }}</span
                      ><span class="round-label" :class="activeTab === tab.key ? 'active' : ''"
                        ><span class="label">99+</span></span
                      >
                    </div>
                  </template>
                  <Demand v-if="activeTab === 'requirement'" :case-id="props.detailId" />
                </a-tab-pane>
                <a-tab-pane key="setting">
                  <template #title>
                    <span @click="showMenuSetting">{{
                      t('caseManagement.featureCase.detailDisplaySetting')
                    }}</span></template
                  >
                </a-tab-pane>
              </a-tabs>
            </div>
          </div>
        </template>
        <template #right>
          <div class="rightWrapper p-[24px]">
            <div class="mb-4 font-medium">基本信息</div>
            <div class="baseItem">
              <span class="label"> {{ t('caseManagement.featureCase.tableColumnModule') }}</span>
              <span>{{ moduleName }}</span>
            </div>
            <div class="baseItem">
              <span class="label"> {{ t('caseManagement.featureCase.tableColumnCaseState') }}</span>
              <MsTag>未开始</MsTag>
            </div>
            <div class="baseItem">
              <span class="label"> 责任人</span>
              <span class="round-label">XXX</span>
            </div>
            <div class="baseItem">
              <span class="label"> {{ t('caseManagement.featureCase.tableColumnCreateUser') }}</span>
              <span>{{ detailInfo?.createUser }}</span>
            </div>
            <div class="baseItem">
              <span class="label"> {{ t('caseManagement.featureCase.tableColumnCreateTime') }}</span>
              <span>{{ dayjs(detailInfo?.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
            </div>
          </div>
        </template>
      </MsSplitBox>
    </template>
  </MsDetailDrawer>
  <SettingDrawer v-model:visible="showSettingDrawer" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useFullscreen } from '@vueuse/core';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsDetailDrawer from '@/components/business/ms-detail-drawer/index.vue';
  import Demand from './demand.vue';
  import SettingDrawer from './settingDrawer.vue';

  import { getCaseDetail } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { findNodeByKey } from '@/utils';

  import type { CaseManagementTable, TabItemType } from '@/models/caseManagement/featureCase';

  const detailDrawerRef = ref<HTMLElement | null>();
  const { isFullscreen, toggle } = useFullscreen(detailDrawerRef);

  const featureCaseStore = useFeatureCaseStore();

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    detailId: string; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
  }>();

  const emit = defineEmits(['update:visible']);

  const showDrawerVisible = ref<boolean>(false);

  const showSettingDrawer = ref<boolean>(false);
  function showMenuSetting() {
    showSettingDrawer.value = true;
  }
  const tabSettingList = computed(() => {
    return featureCaseStore.tabSettingList;
  });

  const tabSetting = ref<TabItemType[]>([...tabSettingList.value]);
  const activeTab = ref<string | number>('detail');
  function changeTabs(key: string | number) {
    activeTab.value = key;
    switch (activeTab.value) {
      case 'setting':
        showMenuSetting();
        break;
      default:
        break;
    }
  }
  const detailInfo = ref<CaseManagementTable>();
  function loadedCase(detail: CaseManagementTable) {
    detailInfo.value = { ...detail };
  }

  const moduleName = computed(() => {
    return findNodeByKey<Record<string, any>>(featureCaseStore.caseTree, detailInfo.value?.moduleId as string, 'id')
      ?.name;
  });

  const editLoading = ref<boolean>(false);

  function editHandler(detail: CaseManagementTable) {}

  const shareLoading = ref<boolean>(false);

  function shareHandler(detail: CaseManagementTable) {}
  const followLoading = ref<boolean>(false);

  function followHandler(detail: CaseManagementTable) {}

  const moreActions = ref<ActionsItem[]>([]);
  const handleMoreActionSelect = () => {};

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
      @apply mb-4 flex;
      .label {
        width: 40%;
        color: var(--color-text-3);
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
</style>
