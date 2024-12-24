<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="t('workbench.homePage.cardSetting')"
    :width="1200"
    :footer="false"
    no-content-padding
    unmount-on-close
    :closable="false"
    class="ms-drawer"
  >
    <template #tbutton>
      <div class="flex items-center justify-end gap-[8px]">
        <a-button type="secondary" @click="exitHandler">
          {{ t('workbench.homePage.exitEdit') }}
        </a-button>
        <a-button type="primary" :loading="confirmLoading" @click="saveHandler">
          {{ t('common.save') }}
        </a-button>
      </div>
    </template>
    <div class="setting-wrapper">
      <div class="setting-wrapper-config">
        <CardSettingList v-model:selectedIds="selectedIds" @add="addCard" />
      </div>
      <div class="setting-wrapper-content">
        <!-- 布局开始 -->
        <div v-if="selectedCardList.length" class="setting-content-grid">
          <VueDraggable
            v-model="selectedCardList"
            class="grid grid-cols-2 gap-4"
            ghost-class="ghost"
            handle=".card-item"
          >
            <div
              v-for="item of selectedCardList"
              :key="item.id"
              :class="`card-item ${item.fullScreen ? 'col-span-2' : ''}`"
            >
              <div class="card-item-text">
                <a-tooltip :content="t('workbench.homePage.sort')" :mouse-enter-delay="300">
                  <div class="flex hover:bg-[rgb(var(--primary-1))]">
                    <MsIcon
                      type="icon-icon_drag"
                      size="16"
                      class="cursor-move text-[var(--color-text-4)] hover:text-[rgb(var(--primary-7))]"
                    />
                  </div>
                </a-tooltip>
                <div>{{ t(item.label) }}</div>
              </div>
              <div class="card-item-text">
                <a-radio-group
                  v-if="!isFullScreenType.includes(item.key)"
                  v-model="item.fullScreen"
                  size="small"
                  type="button"
                >
                  <a-radio :disabled="item.isDisabledHalfScreen" :value="false">
                    {{ t('workbench.homePage.halfScreen') }}
                  </a-radio>
                  <a-radio :value="true">{{ t('workbench.homePage.fullScreen') }}</a-radio>
                </a-radio-group>
                <a-tooltip :content="t('common.delete')" :mouse-enter-delay="300">
                  <a-button
                    class="arco-btn-outline--secondary !p-[4px]"
                    type="outline"
                    size="small"
                    @click="deleteHandler(item)"
                  >
                    <MsIcon
                      size="16"
                      type="icon-icon_delete-trash_outlined1"
                      class="delete-icon text-[var(--color-text-4)] hover:text-[rgb(var(--danger-6))]"
                    />
                  </a-button>
                </a-tooltip>
              </div>
            </div>
          </VueDraggable>
        </div>
        <!-- 布局结束-->
        <NotData v-else is-dashboard />
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import NotData from '../../components/notData.vue';
  import CardSettingList from '@/views/workbench/homePage/components/cardSettingList.vue';

  import { editDashboardLayout } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { childrenWorkConfigItem, SelectedCardItem } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    list: SelectedCardItem[];
  }>();

  const emit = defineEmits<{
    (e: 'success'): void;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const selectedCardList = ref<SelectedCardItem[]>([]);
  const isFullScreenType = [
    WorkCardEnum.API_CHANGE,
    WorkCardEnum.REVIEWING_BY_ME,
    WorkCardEnum.PROJECT_VIEW,
    WorkCardEnum.PROJECT_MEMBER_VIEW,
    WorkCardEnum.CREATE_BY_ME,
    WorkCardEnum.PROJECT_PLAN_VIEW,
  ];

  const defaultAllProjectType = [WorkCardEnum.PROJECT_VIEW, WorkCardEnum.CREATE_BY_ME];

  const selectedIds = ref<WorkCardEnum[]>([]);

  // 添加卡片
  function addCard(item: childrenWorkConfigItem) {
    selectedIds.value.push(item.value);
    const newCard: SelectedCardItem = {
      id: getGenerateId(),
      fullScreen: !!isFullScreenType.includes(item.value),
      isDisabledHalfScreen: isFullScreenType.includes(item.value),
      label: item.label,
      key: item.value,
      projectIds: defaultAllProjectType.includes(item.value) ? [] : [appStore.currentProjectId],
      handleUsers: [],
      selectAll: [WorkCardEnum.PROJECT_VIEW, WorkCardEnum.CREATE_BY_ME, WorkCardEnum.PROJECT_MEMBER_VIEW].includes(
        item.value
      ),
      planId: '',
      groupId: '',
    };
    selectedCardList.value.push(newCard);
  }

  function exitHandler() {
    innerVisible.value = false;
  }

  // 删除卡片
  function deleteHandler(item: SelectedCardItem) {
    const index = selectedCardList.value.findIndex((e) => e.id === item.id);
    selectedCardList.value.splice(index, 1);
    selectedIds.value.splice(index, 1);
  }

  const confirmLoading = ref<boolean>(false);
  // 保存
  async function saveHandler() {
    try {
      confirmLoading.value = true;
      selectedCardList.value = selectedCardList.value.map((e, pos) => {
        return {
          ...e,
          pos,
        };
      });
      await editDashboardLayout(selectedCardList.value, appStore.currentOrgId);
      emit('success');
      innerVisible.value = false;
      Message.success(t('common.saveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        selectedCardList.value = cloneDeep(props.list);
        selectedIds.value = props.list.map((e) => e.key);
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less">
  .setting-wrapper {
    background: var(--color-text-n9);
    @apply flex h-full;
    .setting-wrapper-config {
      padding-left: 12px;
      width: 300px;
      background-color: var(--color-text-fff);
      box-shadow: 0 0 10px rgba(120 56 135/ 5%);
    }
    .setting-wrapper-content {
      padding: 16px;
      width: calc(100% - 300px);
      height: calc(100vh - 56px);
      @apply overflow-y-auto overflow-x-hidden;
      .ms-scroll-bar();
    }
  }
  .card-item {
    padding: 16px;
    border-radius: 4px;
    background-color: var(--color-text-fff);
    box-shadow: 0 0 10px rgba(120 56 135/ 5%);
    @apply flex items-center justify-between;
    .card-item-text {
      @apply flex items-center gap-2;
    }
  }
  .arco-btn-outline--secondary:hover {
    border-color: rgb(var(--danger-6)) !important;
    .delete-icon {
      color: rgb(var(--danger-6));
    }
  }
</style>
