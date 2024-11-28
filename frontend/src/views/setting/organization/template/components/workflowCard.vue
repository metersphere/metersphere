<template>
  <div
    class="wrapper"
    :class="{
      ...styleClass.wrapper,
      '_hover_Wrapper': isEnableProjectState || isNotAllowCreate ? false : true,
      '_pointer': !isNotAllowCreate,
      'cursor-not-allowed': isNotAllowCreate,
      '_disabled_gray_bg': isEnableProjectState,
    }"
  >
    <a-trigger v-model:popup-visible="visible" trigger="click" align-point>
      <!-- 不允许状态流转 -->
      <img v-if="isNotAllowCreate" src="@/assets/images/notAllow_bg.png" class="h-[100%] w-[100%]" alt="" />
      <!-- 未创建 hover  禁用  选中 -->
      <div v-else-if="isUnCreateWorkFlow" class="action h-full" @click="createFlowStep">
        <icon-plus
          :style="{ 'font-size': '16px' }"
          class="_unSelect_SvgIcon"
          :class="{ ...styleClass.SvgIcon, _hover_SvgIcon: isEnableProjectState ? false : true }"
        />
        <span
          class="_unSelect_CreateStep"
          :class="{ ...styleClass.createStep, _hover_CreateStep: isEnableProjectState ? false : true }"
          >{{ t('system.orgTemplate.createFlowStep') }}</span
        >
      </div>
      <!-- 已创建 -->
      <div v-else-if="isCreated" class="created flex h-full w-full items-center justify-center" @click="createFlowStep">
        <icon-check :style="{ 'font-size': '16px' }" class="text-[rgb(var(--success-6))]" />
      </div>
      <template #content>
        <div class="w-[400px] rounded bg-[var(--color-text-fff)] p-[24px] shadow-[0_0_10px_rgba(100,100,102,0.15)]">
          <div class="flex items-center justify-between">
            <div class="font-medium text-[var(--color-text-1)]">{{ title }}</div>
            <div class="cursor-pointer text-[18px] text-[var(--color-text-2)]" @click="handleCancel">×</div>
          </div>
          <div class="my-[16px] flex w-[60%] items-center justify-between text-[var(--color-text-1)]">
            <div class="flex flex-col">
              <span class="mb-2">{{ t('system.orgTemplate.startState') }} </span>
              <MsTag>{{ startState }}</MsTag>
            </div>
            <icon-arrow-right class="mt-8 text-[16px] text-[var(--color-text-brand)]" />
            <div class="flex flex-col">
              <span class="mb-2"> {{ t('system.orgTemplate.endState') }}</span>
              <MsTag>{{ endState }}</MsTag>
            </div>
          </div>
          <div class="flex items-center justify-end gap-[8px]">
            <a-button size="small" @click="handleCancel">{{ t('common.cancel') }}</a-button>
            <a-button
              v-if="isUnCreateWorkFlow"
              size="small"
              type="primary"
              :loading="loading"
              @click="changeWorkFlow('create')"
            >
              {{ t('common.create') }}
            </a-button>
            <a-button
              v-else
              size="small"
              type="primary"
              status="danger"
              class="!bg-[rgb(var(--danger-7))]"
              @click="cancelFlowStep"
            >
              {{ t('system.orgTemplate.deleteSteps') }}
            </a-button>
          </div>
        </div>
      </template>
    </a-trigger>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 工作流table小卡片
   */
  import { ref } from 'vue';
  import { Message, TableColumnData } from '@arco-design/web-vue';

  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTemplateStore from '@/store/modules/setting/template';
  import { hasAnyPermission } from '@/utils/permission';

  import type { UpdateWorkFlowSetting, WorkFlowType } from '@/models/setting/template';

  import { getWorkFlowRequestApi } from '@/views/setting/organization/template/components/fieldSetting';

  const templateStore = useTemplateStore();

  const { t } = useI18n();
  const { openModal } = useModal();
  const props = defineProps<{
    mode: 'organization' | 'project';
    stateItem: WorkFlowType;
    columnItem: TableColumnData;
    cellCoordinates: { rowId: string; columnId: string };
    totalData: WorkFlowType[];
    deletePermission?: string[];
    createPermission?: string[];
    updatePermission?: string[];
  }>();

  const emit = defineEmits<{
    (e: 'ok'): void;
  }>();

  const stateInfo = ref<WorkFlowType>();
  const stateColumn = ref<TableColumnData>();

  watchEffect(() => {
    stateInfo.value = { ...props.stateItem };
    stateColumn.value = { ...props.columnItem };
  });

  // 不允许流转状态更改和新建状态 禁用状态
  const isNotAllowCreate = computed(() => {
    return props.stateItem.id === props.columnItem.dataIndex;
  });

  // 没有创建状态流默认+号
  const isUnCreateWorkFlow = computed(() => {
    if (props.columnItem.dataIndex)
      return (
        props.stateItem.statusFlowTargets.length < 1 ||
        !props.stateItem.statusFlowTargets.includes(props.columnItem.dataIndex)
      );
  });

  // 获取已经创建工作流
  const isCreated = computed(() => {
    if (props.columnItem.dataIndex) return props.stateItem.statusFlowTargets.includes(props.columnItem.dataIndex);
  });

  // 计算开始状态
  const startState = computed(() => {
    return props.totalData.find((item: WorkFlowType) => item.id === props.stateItem.id)?.name;
  });

  // 计算结束状态
  const endState = computed(() => {
    return props.totalData.find((item: WorkFlowType) => item.id === props.columnItem.dataIndex)?.name;
  });

  // 计算是否禁用状态
  const isEnableProjectState = computed(() => {
    return props.mode === 'project'
      ? !templateStore.projectStatus[props.stateItem.scene]
      : !templateStore.ordStatus[props.stateItem.scene];
  });

  const title = computed(() => {
    return isCreated.value ? t('system.orgTemplate.updateFlowStep') : t('system.orgTemplate.createFlowStep');
  });

  const isSelected = ref<boolean>(false);

  const styleClass = ref<Record<string, any>>({});

  // 计算当前是否选中class
  function setSelectClass() {
    if (!hasAnyPermission(props.updatePermission || [])) {
      return;
    }
    if (isUnCreateWorkFlow.value && isSelected.value) {
      styleClass.value = {
        wrapper: { _select_unCreate_Selected: true },
        createStep: { _selected_CreateStep: true },
        SvgIcon: { _selected_SvgIcon: true },
      };
    } else if (isCreated.value && !isNotAllowCreate.value && isSelected.value) {
      styleClass.value = {
        wrapper: { _select_created_Selected: true },
        createStep: { _selected_CreateStep: true },
        SvgIcon: { _selected_SvgIcon: true },
      };
    } else if (isUnCreateWorkFlow.value && !isSelected.value) {
      styleClass.value = {
        wrapper: { _select_unCreate_Selected: false },
        createStep: { _selected_CreateStep: false },
        SvgIcon: { _selected_SvgIcon: false },
      };
    } else if (isCreated.value && !isNotAllowCreate.value && !isSelected.value) {
      styleClass.value = {
        wrapper: { _select_created_Selected: false },
        createStep: { _selected_CreateStep: false },
        SvgIcon: { _selected_SvgIcon: false },
      };
    } else {
      styleClass.value = {};
    }
  }

  watch(
    () => props.cellCoordinates,
    () => {
      if (
        props.cellCoordinates.rowId === props.stateItem.id &&
        props.cellCoordinates.columnId === props.columnItem.dataIndex
      ) {
        isSelected.value = true;
      } else {
        isSelected.value = false;
      }

      setSelectClass();
    },
    { deep: true }
  );

  watch(
    () => isUnCreateWorkFlow.value,
    () => {
      setSelectClass();
    }
  );

  watch(
    () => isCreated.value,
    () => {
      setSelectClass();
    }
  );

  watch(
    () => isSelected.value,
    () => {
      setSelectClass();
    }
  );

  const visible = ref<boolean>(false);

  // 创建流转步骤
  function createFlowStep() {
    if (!hasAnyPermission(props.updatePermission || [])) {
      return;
    }
    if (isEnableProjectState.value) {
      return;
    }
    visible.value = true;
  }

  const loading = ref<boolean>(false);

  const updateOrdWorkStateFlow = getWorkFlowRequestApi(props.mode).updateFlow;
  // 创建工作流流转状态
  async function changeWorkFlow(type: string) {
    if (!hasAnyPermission(props.updatePermission || [])) {
      return;
    }
    try {
      loading.value = true;
      const params: UpdateWorkFlowSetting = {
        fromId: props.stateItem?.id,
        toId: props.columnItem?.dataIndex as string,
        enable: type === 'create',
      };
      await updateOrdWorkStateFlow(params);
      Message.success(
        type === 'delete' ? t('system.orgTemplate.deleteSuccess') : t('system.orgTemplate.createSuccess')
      );
      visible.value = false;
      emit('ok');
      isSelected.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  // 删除取消流转步骤
  function cancelFlowStep() {
    if (!hasAnyPermission(props.updatePermission || [])) {
      return;
    }
    if (isEnableProjectState.value) {
      return;
    }
    openModal({
      type: 'error',
      title: t('system.orgTemplate.deleteStateStepTitle', { name: stateInfo.value?.name }),
      content: t('system.orgTemplate.deleteStateStepContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await changeWorkFlow('delete');
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }
  function handleCancel() {
    visible.value = false;
  }
</script>

<style scoped lang="less">
  .wrapper {
    width: 100%;
    min-width: 112px;
    height: 100%;
    transition: 0.2;
    @apply flex items-center justify-center rounded;
    .img {
      object-fit: cover;
    }
    .action {
      width: 100%;
      @apply flex flex-col items-center justify-center;
    }
    &:hover {
      ._hover_CreateStep {
        opacity: 1;
      }
    }
    &:hover {
      ._hover_SvgIcon {
        margin-top: 0;
        color: rgb(var(--primary-5));
      }
    }
  }
  // 容器阴影 hover
  ._hover_Wrapper {
    &:hover {
      border-radius: 4px;
      box-shadow: 0 4px 15px -1px rgba(100 100 102 / 15%);
    }
  }
  // 未创建hover----
  ._hover_CreateStep {
    &:hover {
      opacity: 1;
    }
  }
  ._hover_SvgIcon {
    &:hover {
      margin-top: 0;
      color: rgb(var(--primary-5));
    }
  }
  // 已选择----
  ._unSelect_SvgIcon {
    margin-top: 30px;
    color: var(--color-text-brand);
  }
  ._unSelect_CreateStep {
    margin-top: 8px;
    color: rgb(var(--primary-5));
    opacity: 0;
  }
  ._selected_CreateStep {
    opacity: 1;
  }
  ._selected_SvgIcon {
    margin-top: 0;
    color: rgb(var(--primary-5));
  }
  // ----

  // 容器未创建已选择
  ._select_unCreate_Selected {
    background: var(--color-bg-3);
    box-shadow: none;
  }

  // 容器已创建已选择
  ._select_created_Selected {
    border: 1px solid rgb(var(--primary-5));
    background: none;
    box-shadow: none;
  }
  // 容器未创建禁用
  ._disabled_unCreate {
    background: var(--color-text-n8);
  }
  // 是否显示小手
  ._pointer {
    @apply cursor-pointer;
  }
  ._not_allowed {
    @apply cursor-not-allowed;
  }
  // 如果开启项目模板禁用状态
  ._disabled_gray_bg {
    background: var(--color-text-n8);
    @apply cursor-not-allowed;
  }
</style>
