<template>
  <div class="page">
    <MsSplitBox>
      <template #first>
        <div class="mr-[6px] p-[16px]">
          <!-- TODO:这个版本环境不展示默认项目环境 -->
          <!-- <div
            ><a-radio-group
              v-model:model-value="showType"
              type="button"
              class="file-show-type w-[50%]"
              @change="changeShowType"
            >
              <a-radio v-permission="['PROJECT_ENVIRONMENT:READ']" value="PROJECT">{{
                t('project.environmental.env')
              }}</a-radio>
              <a-radio v-permission="['PROJECT_ENVIRONMENT:READ']" value="PROJECT_GROUP">{{
                t('project.environmental.envGroup')
              }}</a-radio>
            </a-radio-group></div
          > -->
          <template v-if="showType === 'PROJECT'">
            <a-input-search
              v-model="keyword"
              :placeholder="t('project.environmental.searchHolder')"
              allow-clear
              @press-enter="searchData"
              @search="searchData"
              @clear="searchData"
            />
            <!-- 全局参数-->
            <div class="p-[8px] text-[var(--color-text-4)]">
              {{ t('project.environmental.allParam') }}
            </div>
            <div
              class="env-item justify-between font-medium text-[var(--color-text-1)] hover:bg-[rgb(var(--primary-1))]"
              :class="{ 'bg-[rgb(var(--primary-1))] !text-[rgb(var(--primary-5))]': activeKey === ALL_PARAM }"
              @click="handleListItemClick({ id: 'allParam', name: 'allParam', description: '' })"
            >
              {{ t('project.environmental.requestHeader') }}
              <div class="node-extra">
                <MsMoreAction
                  v-permission="['PROJECT_ENVIRONMENT:READ+IMPORT', 'PROJECT_ENVIRONMENT:READ+EXPORT']"
                  :list="allMoreAction"
                  @select="(value) => handleMoreAction(value, 'allParams', EnvAuthTypeEnum.GLOBAL)"
                />
              </div>
            </div>
            <a-divider :margin="6" />
            <!-- 环境-->
            <div class="env-row p-[8px] hover:bg-[rgb(var(--primary-1))]">
              <div class="text-[var(--color-text-4)]">{{ t('project.environmental.env') }}</div>
              <div class="flex flex-row items-center">
                <div class="env-row-extra">
                  <MsMoreAction
                    v-permission="['PROJECT_ENVIRONMENT:READ+IMPORT', 'PROJECT_ENVIRONMENT:READ+EXPORT']"
                    trigger="click"
                    :list="allMoreAction"
                    @select="(value) => handleMoreAction(value, 'all', EnvAuthTypeEnum.ENVIRONMENT)"
                  />
                </div>
                <MsButton
                  v-permission="['PROJECT_ENVIRONMENT:READ+ADD']"
                  type="icon"
                  :disabled="!!hasUnSaveData"
                  class="!mr-0 p-[2px]"
                  @click="handleCreateEnv"
                >
                  <MsIcon
                    type="icon-icon_create_planarity"
                    size="18"
                    class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                  />
                </MsButton>
              </div>
            </div>
            <div>
              <!-- 环境list-->
              <div v-if="envList.length">
                <VueDraggable
                  v-model="envList"
                  ghost-class="ghost"
                  handle=".drag-handle"
                  @update="handleEnvGroupPosChange($event, EnvAuthTypeEnum.ENVIRONMENT)"
                >
                  <div
                    v-for="element in envList"
                    :key="element.id"
                    class="env-item"
                    :class="[activeKey === element.id ? 'env-item-focus' : '']"
                    @click="handleListItemClick(element)"
                  >
                    <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                      <a-tooltip :content="element.name">
                        <div
                          class="one-line-text"
                          :class="{ 'font-medium text-[rgb(var(--primary-5))]': element.id === activeKey }"
                          >{{ element.name }}</div
                        >
                      </a-tooltip>
                      <div class="env-item-actions">
                        <RenamePop
                          :list="envList"
                          :type="(showType as EnvAuthScopeEnum)"
                          v-bind="popVisible[element.id]"
                          @cancel="handleRenameCancel(element)"
                          @success="envSuccessHandler"
                        >
                          <div class="flex flex-row items-center gap-[8px]">
                            <icon-drag-dot-vertical
                              v-if="
                                hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE']) &&
                                !excludeActionType.includes(element.id)
                              "
                              class="drag-handle env-item-drag-icon"
                            />
                            <MsMoreAction
                              v-permission="['PROJECT_ENVIRONMENT:READ+DELETE', 'PROJECT_ENVIRONMENT:READ+EXPORT']"
                              trigger="click"
                              :list="envMoreAction(element)"
                              @select="
                                (value) => handleMoreAction(value, element.id, EnvAuthTypeEnum.ENVIRONMENT_PARAM)
                              "
                            >
                              <MsButton type="icon" size="mini" class="env-item-actions-btn">
                                <MsIcon type="icon-icon_more_outlined" size="14" class="text-[var(--color-text-4)]" />
                              </MsButton>
                            </MsMoreAction>
                          </div>
                        </RenamePop>
                      </div>
                    </div>
                  </div>
                </VueDraggable>
              </div>
              <!-- 环境无数据 -->
              <div v-else class="bg-[var(--color-text-n9)] p-[8px] text-[12px] text-[var(--color-text-4)]">
                {{ t('project.environmental.envListIsNull') }}
              </div>
            </div>
          </template>
          <template v-else>
            <!-- 环境组 -->
            <div class="env-row mt-[8px] p-[8px]">
              <div class="text-[var(--color-text-4)]">{{ t('project.environmental.group.envGroup') }}</div>
              <div class="flex flex-row items-center">
                <MsButton
                  v-permission="['PROJECT_ENVIRONMENT:READ+ADD']"
                  type="icon"
                  class="!mr-0 p-[2px]"
                  @click="handleCreateGroup"
                >
                  <MsIcon
                    type="icon-icon_create_planarity"
                    size="18"
                    class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                  />
                </MsButton>
              </div>
            </div>
            <!-- 环境组list-->
            <div v-if="evnGroupList.length">
              <VueDraggable
                v-model="evnGroupList"
                ghost-class="ghost"
                handle=".drag-handle"
                @update="handleEnvGroupPosChange($event, EnvAuthTypeEnum.ENVIRONMENT_GROUP)"
              >
                <div
                  v-for="element in evnGroupList"
                  :key="element.id"
                  class="env-item"
                  :class="[activeGroupKey === element.id ? 'env-item-focus' : '']"
                  @click="handleListItemClickGroup(element)"
                >
                  <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                    <a-tooltip :content="element.name">
                      <div
                        class="one-line-text"
                        :class="{ 'font-medium text-[rgb(var(--primary-5))]': element.id === activeGroupKey }"
                        >{{ element.name }}</div
                      >
                    </a-tooltip>
                    <div class="env-item-actions">
                      <RenamePop
                        :list="evnGroupList"
                        :type="(showType as EnvAuthScopeEnum)"
                        v-bind="groupPopVisible[element.id]"
                        @cancel="handleRenameCancelGroup(element)"
                        @success="envSuccessCroupHandler(element)"
                      >
                        <div class="flex flex-row items-center gap-[8px]">
                          <icon-drag-dot-vertical
                            v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']"
                            class="env-item-drag-icon drag-handle"
                          />
                          <MsMoreAction
                            v-permission="['PROJECT_ENVIRONMENT:READ+DELETE', 'PROJECT_ENVIRONMENT:READ+EXPORT']"
                            :list="groupMoreAction"
                            @select="(value) => handleMoreAction(value, element.id, EnvAuthTypeEnum.ENVIRONMENT_GROUP)"
                          >
                            <MsButton type="icon" size="mini" class="env-item-actions-btn">
                              <MsIcon type="icon-icon_more_outlined" size="14" class="text-[var(--color-text-4)]" />
                            </MsButton>
                          </MsMoreAction>
                        </div>
                      </RenamePop>
                    </div>
                  </div>
                </div>
              </VueDraggable>
            </div>
            <!-- 环境无数据 -->
            <div v-else class="bg-[var(--color-text-n9)] p-[8px] text-[12px] text-[var(--color-text-4)]">
              {{ t('project.environmental.envListIsNull') }}
            </div>
          </template>
        </div>
      </template>
      <template #second>
        <!-- 全局参数 -->
        <AllParamBox v-if="showType === 'PROJECT' && activeKey === ALL_PARAM" ref="globalEnvRef" />
        <!-- 环境变量 -->
        <EnvParamBox
          v-else-if="showType === 'PROJECT' && activeKey !== ALL_PARAM"
          ref="envParamBoxRef"
          @reset-env="resetHandler"
          @ok="successHandler"
        />
        <!-- 环境组 -->
        <EnvGroupBox
          v-else-if="showType === 'PROJECT_GROUP'"
          ref="envGroupBoxRef"
          @save-or-update="handleUpdateEnvGroup"
        />
      </template>
    </MsSplitBox>
  </div>
  <CommonImportPop v-model:visible="importVisible" :type="importAuthType" @submit="handleSubmit" />
  <MsExportDrawer
    v-model:visible="envExportVisible"
    :all-data="exportOptionData"
    :default-selected-keys="[]"
    is-array-column
    :array-column="envList"
    :title-props="{
      selectableTitle: t('project.environmental.env.selectableTitle'),
      systemTitle: t('project.environmental.env.systemTitle'),
      selectedTitle: t('project.environmental.env.selectedTitle'),
    }"
    :export-loading="exportLoading"
    @confirm="(v) => handleEnvExport(v.map((item) => item.key))"
  />
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';
  import { isEqual } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsExportDrawer from '@/components/pure/ms-export-drawer/index.vue';
  import { MsExportDrawerMap } from '@/components/pure/ms-export-drawer/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AllParamBox from './components/AllParamBox.vue';
  import CommonImportPop from './components/common/CommonImportPop.vue';
  import EnvGroupBox from './components/EnvGroupBox.vue';
  import EnvParamBox from './components/EnvParamBox.vue';
  import RenamePop from './components/RenamePop.vue';

  import {
    deleteEnv,
    deleteEnvGroup,
    editPosEnv,
    exportEnv,
    exportGlobalParam,
    groupEditPosEnv,
    groupListEnv,
    listEnv,
  } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import useProjectEnvStore, {
    ALL_PARAM,
    NEW_ENV_GROUP,
    NEW_ENV_PARAM,
    NEW_ENV_PARAM_COPY,
  } from '@/store/modules/setting/useProjectEnvStore';
  import { characterLimit, downloadByteFile } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { EnvListItem, PopVisible } from '@/models/projectManagement/environmental';
  import { EnvAuthScopeEnum, EnvAuthTypeEnum } from '@/enums/envEnum';

  import { SortableEvent } from 'sortablejs';

  const { openModal } = useModal();

  const { t } = useI18n();
  const store = useProjectEnvStore();

  const envList = ref<EnvListItem[]>([]); // 环境列表
  const evnGroupList = ref<EnvListItem[]>([]); // 环境组列表

  const showType = ref<EnvAuthScopeEnum>(EnvAuthScopeEnum.PROJECT); // 展示类型

  const activeKey = computed({
    get() {
      return store.currentId;
    },
    set(val) {
      activeKey.value = val;
    },
  }); // 当前选中的id

  const activeGroupKey = computed(() => store.currentGroupId); // 当前选中的group id

  const keyword = ref<string>(''); // 搜索关键字
  const appStore = useAppStore();

  // 气泡弹窗
  const popVisible = ref<PopVisible>({});
  // group 气泡弹窗
  const groupPopVisible = ref<PopVisible>({});
  // 导入弹窗
  const importVisible = ref<boolean>(false);
  // 导入类型
  const importAuthType = ref<EnvAuthTypeEnum>(EnvAuthTypeEnum.GLOBAL);
  // 环境变量导出Drawer
  const envExportVisible = ref<boolean>(false);
  // 环境变量导出option
  const exportOptionData = ref<MsExportDrawerMap>({
    systemColumns: {},
  });
  const excludeActionType = [NEW_ENV_PARAM_COPY, NEW_ENV_PARAM];

  // 默认环境MoreAction
  const envMoreAction = (item: EnvListItem) => {
    const allowAction = excludeActionType.includes(item.id)
      ? []
      : [
          {
            label: t('common.rename'),
            eventTag: 'rename',
            disabled: item.mock || false,
            permission: ['PROJECT_ENVIRONMENT:READ+UPDATE'],
          },
          {
            label: t('common.copy'),
            eventTag: 'copy',
            permission: ['PROJECT_ENVIRONMENT:READ+ADD'],
          },
          {
            label: t('common.export'),
            eventTag: 'export',
            permission: ['PROJECT_ENVIRONMENT:READ+EXPORT'],
          },
          {
            isDivider: true,
          },
        ];
    return [
      ...allowAction,
      {
        label: t('common.delete'),
        danger: true,
        eventTag: 'delete',
        disabled: item.mock || false,
        permission: ['PROJECT_ENVIRONMENT:READ+DELETE'],
      },
    ];
  };

  // 全局参数/环境 MoreAction
  const allMoreAction: ActionsItem[] = [
    {
      label: t('common.import'),
      eventTag: 'import',
      permission: ['PROJECT_ENVIRONMENT:READ+IMPORT'],
    },
    {
      label: t('common.export'),
      eventTag: 'export',
      permission: ['PROJECT_ENVIRONMENT:READ+EXPORT'],
    },
  ];

  // 环境组moreAction
  const groupMoreAction: ActionsItem[] = [
    {
      label: t('common.rename'),
      eventTag: 'rename',
      permission: ['PROJECT_ENVIRONMENT:READ+UPDATE'],
    },
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
      permission: ['PROJECT_ENVIRONMENT:READ+DELETE'],
    },
  ];

  // 处理全局参数导入
  const handleGlobalImport = () => {
    importVisible.value = true;
    importAuthType.value = EnvAuthTypeEnum.GLOBAL;
  };

  // 处理全局参数导出
  const handleGlobalExport = async () => {
    try {
      const blob = await exportGlobalParam(appStore.currentProjectId);
      downloadByteFile(blob, 'globalParam.json');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };
  const exportLoading = ref<boolean>(false);
  // 处理环境变量导出
  const handleEnvExport = async (id: string | string[]) => {
    exportLoading.value = true;
    try {
      const blob = await exportEnv(Array.isArray(id) ? id : [id]);
      downloadByteFile(blob, 'EnvParam.json');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      exportLoading.value = false;
    }
  };
  const globalEnvRef = ref();
  const handleSubmit = (shouldSearch: boolean) => {
    if (shouldSearch) {
      if (importAuthType.value === EnvAuthTypeEnum.GLOBAL && store.currentId === ALL_PARAM) {
        globalEnvRef.value.initEnvDetail();
      } else if (importAuthType.value === EnvAuthTypeEnum.ENVIRONMENT && store.currentId !== ALL_PARAM) {
        // eslint-disable-next-line no-use-before-define
        initData(keyword.value, true);
      }
    }
  };
  // 处理环境导入
  const handleEnvImport = () => {
    importVisible.value = true;
    importAuthType.value = EnvAuthTypeEnum.ENVIRONMENT;
  };
  // 创建环境组
  const handleCreateGroup = () => {
    const tmpArr = evnGroupList.value;
    tmpArr.unshift({
      id: NEW_ENV_GROUP,
      name: t('project.environmental.newEnv'),
      description: '',
    });
    store.setCurrentGroupId(NEW_ENV_GROUP);
    evnGroupList.value = tmpArr;
  };
  const initGroupList = async (keywordStr = '', initNode = false) => {
    try {
      evnGroupList.value = await groupListEnv({ projectId: appStore.currentProjectId, keyword: keywordStr });
      if (initNode && evnGroupList.value.length) {
        store.setCurrentGroupId(evnGroupList.value[0].id);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 更新环境组
  const handleUpdateEnvGroup = async (id: string) => {
    await initGroupList();
    store.setCurrentGroupId(id);
  };

  const initData = async (keywordStr = '', initNode = false) => {
    try {
      envList.value = await listEnv({ projectId: appStore.currentProjectId, keyword: keywordStr });
      if (initNode && envList.value.length) {
        store.setCurrentId(envList.value[0].id);
      }
      if (showType.value === 'PROJECT_GROUP') {
        initGroupList(keywordStr, initNode);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 排序更新
  const handleEnvGroupPosChange = async (event: SortableEvent, type: EnvAuthTypeEnum) => {
    try {
      const { oldIndex, newIndex } = event;
      if (oldIndex === newIndex) {
        return;
      }
      const _oldIndex = oldIndex as number;
      const _newIndex = newIndex as number;

      const params = {
        projectId: appStore.currentProjectId,
        moveId: type === EnvAuthTypeEnum.ENVIRONMENT ? envList.value[_newIndex].id : evnGroupList.value[_newIndex].id,
        targetId: '',
        moveMode: 'AFTER',
      };
      if (type === EnvAuthTypeEnum.ENVIRONMENT) {
        if (envList.value[_newIndex + 1].id) {
          params.moveMode = 'BEFORE';
          params.targetId = envList.value[_newIndex + 1].id;
        } else if (envList.value[_newIndex - 1].id && !envList.value[_newIndex + 1].id) {
          params.moveMode = 'AFTER';
          params.targetId = envList.value[_newIndex - 1].id;
        }
      }
      if (type === EnvAuthTypeEnum.ENVIRONMENT_GROUP) {
        if (evnGroupList.value[_newIndex + 1].id) {
          params.moveMode = 'AFTER';
          params.targetId = evnGroupList.value[_newIndex + 1].id;
        } else if (evnGroupList.value[_newIndex - 1].id && !evnGroupList.value[_newIndex + 1].id) {
          params.moveMode = 'BEFORE';
          params.targetId = evnGroupList.value[_newIndex - 1].id;
        }
      }

      if (type === EnvAuthTypeEnum.ENVIRONMENT) {
        await editPosEnv(params);
      } else if (type === EnvAuthTypeEnum.ENVIRONMENT_GROUP) {
        await groupEditPosEnv(params);
      }
      initData();
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
    }
  };

  function searchData() {
    initData(keyword.value);
  }

  // 处理删除环境
  const handleDeleteEnv = async (id: string) => {
    const isNewEnvParam = id === NEW_ENV_PARAM || id === NEW_ENV_PARAM_COPY;
    const isCurrentIdNewEnvParam = store.currentId === NEW_ENV_PARAM || store.currentId === NEW_ENV_PARAM_COPY;

    if (isCurrentIdNewEnvParam) {
      envList.value = envList.value.filter((item) => item.id !== id);
      store.setCurrentId(envList.value[0].id);
    }

    if (isNewEnvParam) {
      envList.value = envList.value.filter((item) => item.id !== id);
      store.setCurrentId(envList.value[0].id);
      return;
    }

    const matchingItem = envList.value.find((item) => item.id === id);
    const itemName = matchingItem?.name ?? '';
    openModal({
      type: 'error',
      title: t('project.environmental.env.delete', { name: characterLimit(itemName) }),
      content: t('project.environmental.env.deleteTip'),
      okText: t('project.fileManagement.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteEnv(id);
          Message.success(t('common.deleteSuccess'));
          initData(keyword.value, true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  // 处理删除环境组
  const handleDeleteEnvGroup = async (id: string) => {
    if (store.currentId === NEW_ENV_GROUP) {
      evnGroupList.value = evnGroupList.value.filter((item) => item.id !== id);
      store.setCurrentId(envList.value[0].id);
    }
    if (id === NEW_ENV_GROUP) {
      evnGroupList.value = evnGroupList.value.filter((item) => item.id !== id);
      store.setCurrentId(envList.value[0].id);
      return;
    }
    const matchingItem = evnGroupList.value.find((item) => item.id === id);
    const itemName = matchingItem?.name ?? '';
    openModal({
      type: 'error',
      title: t('project.environmental.env.deleteGroup', { name: characterLimit(itemName) }),
      content: t('project.environmental.env.deleteGroupTip'),
      okText: t('project.fileManagement.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteEnvGroup(id);
          Message.success(t('common.deleteSuccess'));
          await initGroupList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  function changeShowType(value: string | number | boolean) {
    if (value === 'PROJECT_GROUP') {
      initGroupList(keyword.value, true);
    }
  }

  const handleRenameCancel = async (element: EnvListItem) => {
    popVisible.value[element.id].visible = false;
  };

  async function envSuccessHandler() {
    initData();
    store.initEnvDetail();
  }

  const envGroupBoxRef = ref<InstanceType<typeof EnvGroupBox>>();

  const handleRenameCancelGroup = async (element: EnvListItem) => {
    groupPopVisible.value[element.id].visible = false;
  };

  const envSuccessCroupHandler = async (element: EnvListItem) => {
    await initGroupList();
    envGroupBoxRef.value?.initDetail(element.id);
  };

  const envParamBoxRef = ref<InstanceType<typeof EnvParamBox>>();
  function openModalTip(id: string, isNew = false) {
    const tipContent = isNew ? t('project.environmental.env.existNewEnvTip') : t('apiTestDebug.unsavedLeave');
    const confirmText = isNew ? t('common.save') : t('common.leave');
    openModal({
      type: 'warning',
      title: t('common.tip'),
      content: tipContent,
      hideCancel: isNew,
      okText: confirmText,
      onBeforeOk: async () => {
        if (isNew) {
          try {
            const isNewEnv = envList.value.some((item) => item.id === NEW_ENV_PARAM);
            await envParamBoxRef.value?.saveCallBack(isNewEnv);
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        } else {
          store.setCurrentId(id);
        }
      },
    });
  }

  const hasUnSaveData = computed(() => envList.value.find((item) => excludeActionType.includes(item.id)));

  const handleListItemClickGroup = (element: EnvListItem) => {
    const { id } = element;
    store.setCurrentGroupId(id);
  };

  function checkHasNewEnv() {
    if (hasUnSaveData.value) {
      openModalTip(hasUnSaveData.value.id, true);
      return true;
    }
    return false;
  }

  const handleListItemClick = (element: EnvListItem) => {
    if (checkHasNewEnv()) {
      return;
    }
    const { id } = element;
    // 校验是否切换
    if (store.currentId !== id) {
      if (!hasAnyPermission(['PROJECT_ENVIRONMENT:READ+ADD', 'PROJECT_ENVIRONMENT:READ+UPDATE'])) {
        store.setCurrentId(id);
        return;
      }
      const isChangeEnvValue =
        store.currentId === ALL_PARAM
          ? isEqual(store.allParamDetailInfo, store.backupAllParamDetailInfo)
          : isEqual(store.currentEnvDetailInfo, store.backupEnvDetailInfo);
      if (isChangeEnvValue) {
        store.setCurrentId(id);
      } else if (hasUnSaveData.value) {
        openModalTip(id, true);
      } else {
        openModalTip(id);
      }
    }
  };

  // 创建环境变量
  const handleCreateEnv = () => {
    const tmpArr = envList.value;
    const unSaveEnv = envList.value.filter((item) => item.id === NEW_ENV_PARAM).length < 1;
    if (unSaveEnv) {
      tmpArr.unshift({
        id: NEW_ENV_PARAM,
        name: t('project.environmental.newEnv'),
        description: '',
      });
      store.setCurrentId(NEW_ENV_PARAM);
      envList.value = tmpArr;
    }
  };

  // 复制
  function copyEnvHandler(id: string) {
    if (checkHasNewEnv()) {
      return;
    }
    const currentItem = envList.value.find((item) => item.id === id);
    const tmpArr = envList.value;
    if (currentItem) {
      let copyName = `copy_${currentItem.name}`;
      if (copyName.length > 255) {
        copyName = copyName.slice(0, 255);
      }
      tmpArr.unshift({
        ...currentItem,
        name: copyName,
        id: NEW_ENV_PARAM_COPY,
      });
      store.setCurrentId(NEW_ENV_PARAM_COPY);
      store.initEnvDetail(id);
    }
  }

  // 处理MoreAction
  const handleMoreAction = (item: ActionsItem, id: string, scopeType: EnvAuthTypeEnum) => {
    const { eventTag } = item;
    switch (eventTag) {
      case 'export':
        if (scopeType === EnvAuthTypeEnum.GLOBAL) {
          // 全局参数导出
          handleGlobalExport();
        } else if (scopeType === EnvAuthTypeEnum.ENVIRONMENT_PARAM) {
          // 环境变量导出
          handleEnvExport(id);
        } else if (scopeType === EnvAuthTypeEnum.ENVIRONMENT) {
          envExportVisible.value = true;
        }
        break;
      case 'delete':
        if (scopeType === EnvAuthTypeEnum.ENVIRONMENT_GROUP) {
          handleDeleteEnvGroup(id);
        } else if (scopeType === EnvAuthTypeEnum.ENVIRONMENT_PARAM) {
          handleDeleteEnv(id);
        }
        break;
      case 'import':
        if (scopeType === EnvAuthTypeEnum.GLOBAL) {
          handleGlobalImport();
        } else if (scopeType === EnvAuthTypeEnum.ENVIRONMENT) {
          handleEnvImport();
        }
        break;
      case 'rename':
        if (scopeType === EnvAuthTypeEnum.ENVIRONMENT_GROUP) {
          const tmpObj = evnGroupList.value.filter((ele) => ele.id === id)[0];
          const visibleItem = { visible: true, defaultName: tmpObj.name, id };
          groupPopVisible.value[id] = visibleItem;
        } else if (scopeType === EnvAuthTypeEnum.ENVIRONMENT_PARAM) {
          const tmpObj = envList.value.filter((ele) => ele.id === id)[0];
          const visibleItem = { visible: true, defaultName: tmpObj.name, id };
          popVisible.value[id] = visibleItem;
        }
        break;
      case 'copy':
        copyEnvHandler(id);
        break;
      default:
        break;
    }
  };

  function resetHandler() {
    const unSaveEnv = envList.value.filter((item) => item.id === NEW_ENV_PARAM).length;
    // @desc: 未保存环境存在环境为 NEW_ENV_PARAM 类型初次新增
    if (unSaveEnv) {
      envList.value = envList.value.filter((item: any) => item.id !== NEW_ENV_PARAM);
      const excludeMock = envList.value.filter((item) => !item.mock);
      // @desc: 如果没有MOCK环境默认为全局参数
      if (showType.value === 'PROJECT' && !excludeMock.length) {
        store.setCurrentId(ALL_PARAM);
        // @desc: 如果没有MOCK环境默认为MOCK环境
      } else if (excludeMock.length) {
        store.setCurrentId(excludeMock[0].id);
      }
      // @desc: 已经创建恢复最初数据
    } else {
      store.initEnvDetail();
    }
  }

  function successHandler(envId: string | undefined) {
    if (!envId) {
      initData(keyword.value, true);
    } else {
      initData(keyword.value, false);
      store.initEnvDetail();
    }
  }

  onMounted(() => {
    initData(keyword.value, true);
  });
</script>

<style lang="less" scoped>
  .page {
    @apply h-full;

    border-radius: var(--border-radius-large);
    background-color: var(--color-text-fff);
  }
  .env-item {
    display: flex;
    align-items: center;
    padding: 8px 4px;
    box-sizing: border-box;
    border-radius: var(--border-radius-small);
    cursor: pointer;
    &:hover {
      background-color: rgb(var(--primary-1));
      .ms-list-drag-icon {
        @apply visible;
      }
      .env-item-actions {
        @apply visible;
      }
    }
    .ms-list-drag-icon {
      @apply invisible cursor-move;
    }
    .env-item-actions {
      @apply invisible flex items-center justify-end;
      .env-item-actions-btn {
        @apply !mr-0;

        padding: 4px;
        border-radius: var(--border-radius-mini);
      }
    }
  }
  .env-item-focus {
    background-color: rgb(var(--primary-1));
    .env-item-actions {
      @apply visible;
    }
  }
  .env-row {
    @apply flex flex-row justify-between;
    &-extra {
      @apply relative;

      opacity: 0;
    }
    &:hover {
      .env-row-extra {
        opacity: 1;
      }
    }
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
  }
  .file-show-type {
    @apply grid grid-cols-1;

    margin-bottom: 8px;
    :deep(.arco-radio-button-content) {
      @apply text-center;
    }
  }
</style>
