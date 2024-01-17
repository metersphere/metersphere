<template>
  <div class="page">
    <MsSplitBox>
      <template #first>
        <div class="p-[24px]">
          <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
            <a-radio value="PROJECT">{{ t('project.environmental.project') }}</a-radio>
            <a-radio value="PROJECT_GROUP">{{ t('project.environmental.projectGroup') }}</a-radio>
          </a-radio-group>
          <template v-if="showType === 'PROJECT'">
            <a-input-search
              :placeholder="t('project.environmental.searchHolder')"
              allow-clear
              @press-enter="enterData"
              @search="searchData"
            />
            <!-- 全局参数-->
            <div class="p-[8px] text-[var(--color-text-4)]">
              {{ t('project.environmental.allParam') }}
            </div>
            <div
              class="env-item justify-between font-medium text-[rgb(var(--primary-5))] hover:bg-[rgb(var(--primary-1))]"
              :class="{ 'bg-[rgb(var(--primary-1))]': activeKey === ALL_PARAM }"
              @click="handleListItemClick({ id: 'allParam', name: 'allParam' })"
            >
              {{ t('project.environmental.allParam') }}
              <div class="node-extra">
                <MsMoreAction
                  :list="allMoreAction"
                  @select="(value) => handleMoreAction(value, 'all', EnvAuthTypeEnum.GLOBAL)"
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
                <MsButton type="icon" class="!mr-0 p-[2px]">
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
                <VueDraggable v-model="envList" ghost-class="ghost">
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
                      @submit="handleRenameCancel(element, element.id)"
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
                            <MsButton type="icon" class="!mr-0 p-[2px]">
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
                <MsButton type="icon" class="!mr-0 p-[2px]">
                  <MsIcon
                    type="icon-icon_create_planarity"
                    size="18"
                    class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                  />
                </MsButton>
              </div>
            </div>
            <!-- 环境list-->
            <div v-if="envList.length">
              <VueDraggable v-model="envList" ghost-class="ghost">
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
                    @submit="handleRenameCancel(element, element.id)"
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
                          <MsButton type="icon" class="!mr-0 p-[2px]">
                            <MsIcon
                              type="icon-icon_drag"
                              size="16"
                              class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                            />
                          </MsButton>
                          <MsMoreAction
                            :list="envMoreAction"
                            @select="(value) => handleMoreAction(value, element.id, EnvAuthTypeEnum.ENVIRONMENT_PARAM)"
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
        <EnvParamBox v-else-if="showType === 'PROJECT' && activeKey !== ALL_PARAM" />
        <!-- 环境组 -->
        <EnvGroupBox v-else-if="showType === 'PROJECT_GROUP'" />
      </template>
    </MsSplitBox>
  </div>
</template>

<script lang="ts" setup>
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AllParamBox from './components/AllParamBox.vue';
  import EnvGroupBox from './components/EnvGroupBox.vue';
  import EnvParamBox from './components/EnvParamBox.vue';
  import RenamePop from './components/RenamePop.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useProjectEnvStore, { ALL_PARAM } from '@/store/modules/setting/useProjectEnvStore';

  import { EnvListItem } from '@/models/projectManagement/environmental';
  import { PopVisible } from '@/models/setting/usergroup';
  import { EnvAuthScopeEnum, EnvAuthTypeEnum } from '@/enums/envEnum';

  const { t } = useI18n();
  const store = useProjectEnvStore();

  const envList = ref<EnvListItem[]>([]); // 环境列表

  const showType = ref<EnvAuthScopeEnum>(EnvAuthScopeEnum.PROJECT); // 展示类型

  const activeKey = computed(() => store.currentId); // 当前选中的id

  // 气泡弹窗
  const popVisible = ref<PopVisible>({});

  // 默认环境MoreAction
  const envMoreAction: ActionsItem[] = [
    {
      label: t('common.rename'),
      eventTag: 'rename',
    },
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

  // 处理MoreAction
  const handleMoreAction = (item: ActionsItem, id: string, scopeType: EnvAuthTypeEnum) => {
    const { eventTag } = item;
    switch (eventTag) {
      case 'rename':
        break;
      case 'export':
        break;
      case 'delete':
        break;
      case 'import':
        break;
      default:
        break;
    }
    console.log(item, id, scopeType);
  };

  function changeShowType(value: string | number | boolean) {
    console.log(value);
  }

  // 用户组数据初始化
  const initData = async (id?: string, isSelect = true) => {
    const tmpArr: EnvListItem[] = [];
    for (let i = 0; i < 100; i++) {
      tmpArr.push({
        id: `${i + 1}`,
        name: `环境${i + 1}`,
      });
    }
    envList.value = tmpArr;
    console.log(id, isSelect);
  };

  const handleRenameCancel = (element: EnvListItem, id?: string) => {
    if (id) {
      initData(id, true);
    }
    popVisible.value[element.id].visible = false;
  };

  const handleListItemClick = (element: EnvListItem) => {
    const { id } = element;
    store.setCurrentId(id);
  };

  function enterData(eve: Event) {
    if (!(eve.target as HTMLInputElement).value) {
      return;
    }
    const keyword = (eve.target as HTMLInputElement).value;
    const tmpArr = envList.value.filter((ele) => ele.name.includes(keyword));
    envList.value = tmpArr;
  }
  function searchData(value: string) {
    if (!value) {
      // initData('', false);
      return;
    }
    const keyword = value;
    const tmpArr = envList.value.filter((ele) => ele.name.includes(keyword));
    envList.value = tmpArr;
  }
  onMounted(() => {
    initData();
  });
</script>

<style lang="less" scoped>
  .page {
    @apply bg-white;

    min-width: 1000px;
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
      @apply relative hidden;
      &:hover {
        @apply block;
      }
    }
    &:hover {
      .node-extra {
        @apply block;
      }
    }
    :active {
      color: rgb(var(--primary-5));
    }
  }
  .env-row {
    @apply flex  flex-row justify-between;
    &-extra {
      @apply relative hidden;
    }
    &:hover {
      .env-row-extra {
        @apply block;
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
