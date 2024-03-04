<template>
  <div class="page">
    <MsSplitBox>
      <template #first>
        <div class="p-[24px]">
          <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
            <a-radio value="PROJECT">{{ t('project.environmental.env') }}</a-radio>
            <a-radio value="PROJECT_GROUP">{{ t('project.environmental.envGroup') }}</a-radio>
          </a-radio-group>
          <template v-if="showType === 'PROJECT'">
            <a-input-search
              v-model="keyword"
              :placeholder="t('project.environmental.searchHolder')"
              allow-clear
              @press-enter="searchData"
              @search="searchData"
            />
            <!-- 全局参数-->
            <div class="p-[8px] text-[var(--color-text-4)]">
              {{ t('project.environmental.allParam') }}
            </div>
            <div
              class="env-item justify-between font-medium text-[var(--color-text-1)] hover:bg-[rgb(var(--primary-1))]"
              :class="{ 'bg-[rgb(var(--primary-1))] !text-[rgb(var(--primary-5))]': activeKey === ALL_PARAM }"
              @click="handleListItemClick({ id: 'allParam', name: 'allParam' })"
            >
              {{ t('project.environmental.allParam') }}
              <div class="node-extra">
                <MsMoreAction
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
                    :list="allMoreAction"
                    @select="(value) => handleMoreAction(value, 'all', EnvAuthTypeEnum.ENVIRONMENT)"
                  />
                </div>
                <MsButton type="icon" class="!mr-0 p-[2px]" @click="handleCreateEnv">
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
                    class="env-item hover:bg-[rgb(var(--primary-1))]"
                    @click="handleListItemClick(element)"
                  >
                    <RenamePop
                      :list="envList"
                      :type="(showType as EnvAuthScopeEnum)"
                      v-bind="popVisible[element.id]"
                      @cancel="handleRenameCancel(element)"
                      @submit="handleRenameCancel(element, true)"
                    >
                      <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                        <a-tooltip :content="element.name">
                          <div
                            class="one-line-text"
                            :class="{ 'font-medium text-[rgb(var(--primary-5))]': element.id === activeKey }"
                            >{{ element.name }}</div
                          >
                        </a-tooltip>
                        <div class="node-extra">
                          <div class="flex flex-row items-center gap-[8px]">
                            <MsButton type="icon" class="drag-handle !mr-0 p-[2px]">
                              <MsIcon
                                type="icon-icon_drag"
                                size="16"
                                class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                              />
                            </MsButton>
                            <MsMoreAction
                              :list="envMoreAction"
                              @select="
                                (value) => handleMoreAction(value, element.id, EnvAuthTypeEnum.ENVIRONMENT_PARAM)
                              "
                            />
                          </div>
                        </div>
                      </div>
                    </RenamePop>
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
                <MsButton type="icon" class="!mr-0 p-[2px]" @click="handleCreateGroup">
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
                  class="env-item hover:bg-[rgb(var(--primary-1))]"
                  @click="handleListItemClickGroup(element)"
                >
                  <RenamePop
                    :list="evnGroupList"
                    :type="(showType as EnvAuthScopeEnum)"
                    v-bind="groupPopVisible[element.id]"
                    @cancel="handleRenameCancelGroup(element)"
                    @submit="handleRenameCancelGroup(element, true)"
                  >
                    <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                      <a-tooltip :content="element.name">
                        <div
                          class="one-line-text"
                          :class="{ 'font-medium text-[rgb(var(--primary-5))]': element.id === activeGroupKey }"
                          >{{ element.name }}</div
                        >
                      </a-tooltip>
                      <div class="node-extra">
                        <div class="flex flex-row items-center gap-[8px]">
                          <MsButton type="icon" class="drag-handle !mr-0 p-[2px]">
                            <MsIcon
                              type="icon-icon_drag"
                              size="16"
                              class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                            />
                          </MsButton>
                          <MsMoreAction
                            :list="groupMoreAction"
                            @select="(value) => handleMoreAction(value, element.id, EnvAuthTypeEnum.ENVIRONMENT_GROUP)"
                          />
                        </div>
                      </div>
                    </div>
                  </RenamePop>
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
        <AllParamBox v-if="showType === 'PROJECT' && activeKey === ALL_PARAM" />
        <!-- 环境变量 -->
        <EnvParamBox v-else-if="showType === 'PROJECT' && activeKey !== ALL_PARAM" @ok="initData()" />
        <!-- 环境组 -->
        <EnvGroupBox v-else-if="showType === 'PROJECT_GROUP'" @save-or-update="handleUpdateEnvGroup" />
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
    @confirm="(v) => handleEnvExport(v.map((item) => item.id))"
  />
</template>

<script lang="ts" setup>
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
  import { useAppStore } from '@/store';
  import useProjectEnvStore, {
    ALL_PARAM,
    NEW_ENV_GROUP,
    NEW_ENV_PARAM,
  } from '@/store/modules/setting/useProjectEnvStore';
  import { downloadByteFile } from '@/utils';

  import { EnvListItem } from '@/models/projectManagement/environmental';
  import { PopVisible } from '@/models/setting/usergroup';
  import { EnvAuthScopeEnum, EnvAuthTypeEnum } from '@/enums/envEnum';

  import { SortableEvent } from 'sortablejs';

  const { t } = useI18n();
  const store = useProjectEnvStore();

  const envList = ref<EnvListItem[]>([]); // 环境列表
  const evnGroupList = ref<EnvListItem[]>([]); // 环境组列表

  const showType = ref<EnvAuthScopeEnum>(EnvAuthScopeEnum.PROJECT); // 展示类型

  const activeKey = computed(() => store.currentId); // 当前选中的id

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

  // 默认环境MoreAction
  const envMoreAction: ActionsItem[] = [
    {
      label: t('common.export'),
      eventTag: 'export',
    },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  // 全局参数/环境 MoreAction
  const allMoreAction: ActionsItem[] = [
    {
      label: t('common.import'),
      eventTag: 'import',
    },
    {
      label: t('common.export'),
      eventTag: 'export',
    },
  ];

  // 环境组moreAction
  const groupMoreAction: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
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
  // 处理环境变量导出
  const handleEnvExport = async (id: string | string[]) => {
    try {
      const blob = await exportEnv(Array.isArray(id) ? id : [id]);
      downloadByteFile(blob, 'EnvParam.json');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const handleSubmit = (shouldSearch: boolean) => {
    if (shouldSearch) {
      if (importAuthType.value === EnvAuthTypeEnum.GLOBAL && store.currentId === ALL_PARAM) {
        store.initEnvDetail();
      } else if (importAuthType.value === EnvAuthTypeEnum.ENVIRONMENT && store.currentId !== ALL_PARAM) {
        store.initEnvDetail();
      }
    }
  };
  // 处理环境导入
  const handleEnvImport = () => {
    importVisible.value = true;
    importAuthType.value = EnvAuthTypeEnum.ENVIRONMENT;
  };

  // 创建环境变量
  const handleCreateEnv = () => {
    const tmpArr = envList.value;
    tmpArr.unshift({
      id: NEW_ENV_PARAM,
      name: t('project.environmental.newEnv'),
    });
    store.setCurrentId(NEW_ENV_PARAM);
    envList.value = tmpArr;
  };
  // 创建环境组
  const handleCreateGroup = () => {
    const tmpArr = evnGroupList.value;
    tmpArr.unshift({
      id: NEW_ENV_GROUP,
      name: t('project.environmental.newEnv'),
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
        targetId: type === EnvAuthTypeEnum.ENVIRONMENT ? envList.value[_newIndex].id : evnGroupList.value[_newIndex].id,
        moveId: '',
        moveMode: 'AFTER',
      };
      if (type === EnvAuthTypeEnum.ENVIRONMENT) {
        if (envList.value[_newIndex + 1].id) {
          params.moveMode = 'BEFORE';
          params.moveId = envList.value[_newIndex + 1].id;
        }
        if (envList.value[_newIndex - 1].id) {
          params.moveMode = 'AFTER';
          params.moveId = envList.value[_newIndex - 1].id;
        }
      }
      if (type === EnvAuthTypeEnum.ENVIRONMENT_GROUP) {
        if (evnGroupList.value[_newIndex + 1].id) {
          params.moveMode = 'AFTER';
          params.moveId = evnGroupList.value[_newIndex + 1].id;
        }
        if (evnGroupList.value[_newIndex - 1].id) {
          params.moveMode = 'BEFORE';
          params.moveId = evnGroupList.value[_newIndex - 1].id;
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
    try {
      if (store.currentId === NEW_ENV_PARAM) {
        // 删除id为newEnvParam的环境
        envList.value = envList.value.filter((item) => item.id !== id);
      } else {
        await deleteEnv(id);
      }
      if (store.currentId === id) {
        store.setCurrentId('');
      }
      searchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 处理删除环境组
  const handleDeleteEnvGroup = async (id: string) => {
    try {
      if (store.currentGroupId === NEW_ENV_GROUP) {
        // 删除id为newEnvParam的环境
        evnGroupList.value = evnGroupList.value.filter((item) => item.id !== id);
      } else {
        await deleteEnvGroup(id);
      }
      await initGroupList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  function changeShowType(value: string | number | boolean) {
    if (value === 'PROJECT_GROUP') {
      initGroupList();
      store.setCurrentGroupId('');
    }
  }

  const handleRenameCancel = (element: EnvListItem, shouldSearch?: boolean) => {
    if (shouldSearch) {
      initData();
    }
    popVisible.value[element.id].visible = false;
  };

  const handleRenameCancelGroup = (element: EnvListItem, shouldSearch?: boolean) => {
    if (shouldSearch) {
      initGroupList();
    }
    groupPopVisible.value[element.id].visible = false;
  };

  const handleListItemClick = (element: EnvListItem) => {
    const { id } = element;
    store.setCurrentId(id);
  };

  const handleListItemClickGroup = (element: EnvListItem) => {
    const { id } = element;
    store.setCurrentGroupId(id);
  };

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
      default:
        break;
    }
  };
  onMounted(() => {
    initData(keyword.value, true);
  });
</script>

<style lang="less" scoped>
  .page {
    @apply bg-white;

    height: calc(100vh - 88px);
    border-radius: var(--border-radius-large);
  }
  .env-item {
    display: flex;
    align-items: center;
    padding: 7px 8px;
    height: 38px;
    box-sizing: border-box;
    border-radius: var(--border-radius-base);
    cursor: pointer;
    .node-extra {
      opacity: 0;
      &:hover {
        opacity: 1;
      }
    }
    &:hover {
      .node-extra {
        opacity: 1;
      }
    }
    :active {
      color: rgb(var(--primary-5));
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
    @apply grid grid-cols-2;

    margin-bottom: 8px;
    :deep(.arco-radio-button-content) {
      @apply text-center;
    }
  }
</style>
