<template>
  <div class="h-full w-full">
    <MsSplitBox>
      <template #first>
        <div class="model-list-wrapper">
          <div class="model-config-title">{{ t('system.config.modelConfig.suppliers') }}</div>
          <div>
            <div
              v-for="item of modelList"
              :key="item.value"
              :class="`model-item flex gap-[8px] rounded ${item.value === activeModelType ? 'active' : ''}`"
              @click="changeModelType(item)"
            >
              <div class="model-item-img h-[24px] w-[24px]">
                <svg-icon width="24px" height="24px" :name="item.icon" />
              </div>
              <div>{{ item.name }}</div>
            </div>
          </div>
        </div>
      </template>
      <template #second>
        <div class="p-[16px]">
          <div
            :class="`mb-[16px] flex items-center ${
              hasAnyPermission(modelConfigPermissionMap) ? 'justify-between' : 'justify-end'
            }`"
          >
            <a-button v-permission="modelConfigPermissionMap" type="primary" @click="addModel">
              {{ t('system.config.modelConfig.addModel') }}
            </a-button>
            <a-input-search
              v-model:model-value="keyword"
              :placeholder="t('system.config.modelConfig.bySearchModelName')"
              class="w-[240px]"
              allow-clear
              @search="searchData"
              @press-enter="searchData"
              @clear="searchData"
            />
          </div>
          <div
            :class="`model-config-card-list-wrapper  ${
              props.modelKey === 'personal' ? 'h-[calc(100vh-138px)]' : 'h-[calc(100vh-274px)]'
            }`"
          >
            <a-spin class="block h-full w-full" :loading="loading">
              <div class="model-config-card-list relative">
                <MsCardList
                  ref="modelCardListRef"
                  mode="remote"
                  :remote-func="modelConfigListApiMap"
                  :remote-params="{
                    owner: props.modelKey === 'personal' ? userStore.id : '',
                    keyword,
                    providerName: activeModelType,
                  }"
                  :card-min-width="props.cardMinWidth || 230"
                  class="flex-1"
                  :shadow-limit="50"
                  :is-proportional="false"
                  :gap="16"
                >
                  <template #item="{ item }">
                    <div class="rounded-md bg-[var(--color-text-fff)] p-[24px]">
                      <div class="model-item-header mb-[16px] flex flex-nowrap items-center gap-[8px]">
                        <div class="model-item-img flex h-[40px] w-[40px] flex-shrink-0 items-center justify-center">
                          <svg-icon width="24px" height="24px" :name="getModelSvg(item)" />
                        </div>
                        <div class="one-line-text flex flex-1 flex-col">
                          <a-tooltip :content="item.name" :mouse-enter-delay="300">
                            <div class="one-line-text font-medium">{{ item.name }}</div>
                          </a-tooltip>

                          <div class="flex gap-[8px] text-[12px]">
                            <div class="text-[var(--color-text-4)]">
                              {{ t('system.config.modelConfig.modelCreateUser') }}
                            </div>
                            <a-tooltip :content="item.createUserName" :mouse-enter-delay="300">
                              <div class="one-line-text">{{ item.createUserName }}</div>
                            </a-tooltip>
                          </div>
                        </div>
                      </div>
                      <div class="model-item-body one-line-text flex items-center gap-[8px]">
                        <div class="model-item-body-label flex flex-col gap-[8px] text-[var(--color-text-4)]">
                          <div>
                            {{ t('system.config.modelConfig.modelType') }}
                          </div>
                          <div>
                            {{ t('system.config.modelConfig.baseModel') }}
                          </div>
                        </div>
                        <div class="one-line-text flex flex-col gap-[8px]">
                          <a-tooltip :content="getTypeName(item)" :mouse-enter-delay="300">
                            <div class="one-line-text"> {{ getTypeName(item) }}</div>
                          </a-tooltip>
                          <a-tooltip :content="item.baseName" :mouse-enter-delay="300">
                            <div class="one-line-text"> {{ item.baseName }}</div>
                          </a-tooltip>
                        </div>
                      </div>
                      <div class="model-item-footer mt-[24px] flex items-center justify-between">
                        <div class="flex items-center gap-[12px]">
                          <a-button
                            v-permission="modelConfigPermissionMap"
                            type="outline"
                            class="arco-btn-outline--secondary"
                            size="small"
                            @click="editModel(item)"
                          >
                            {{ t('common.edit') }}
                          </a-button>
                          <a-button
                            v-permission="modelConfigPermissionMap"
                            type="outline"
                            class="arco-btn-outline--secondary"
                            size="small"
                            @click="deleteModel(item)"
                          >
                            {{ t('common.delete') }}
                          </a-button>
                        </div>

                        <a-switch
                          v-model="item.status"
                          size="small"
                          :disabled="!hasAnyPermission(modelConfigPermissionMap)"
                          :before-change="(val) => changeStatus(val, item)"
                        />
                      </div>
                    </div>
                  </template>
                  <template #empty>
                    <div
                      class="absolute left-0 right-0 top-[30%] translate-y-[-60%] text-center text-[var(--color-text-4)]"
                    >
                      {{ t('system.config.modelConfig.noModelData') }}
                    </div>
                  </template>
                </MsCardList>
              </div>
            </a-spin>
          </div>
        </div>
        <modelEditDrawer
          v-model:visible="showModelConfigDrawer"
          :current-model-id="currentModelId"
          :supplier-model-item="supplierModelItem"
          :model-key="props.modelKey"
          @close="handleCancel"
          @refresh="refreshHandler"
        />
      </template>
    </MsSplitBox>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsCardList from '@/components/business/ms-card-list/index.vue';
  import modelEditDrawer from './modelEditDrawer.vue';

  import { deleteModelConfig, editModelConfig, getModelConfigList } from '@/api/modules/setting/config';
  import { deletePersonalModelConfig, editPersonalModelConfig, getPersonalModelConfigList } from '@/api/modules/user';
  import { modelTypeOptions } from '@/config/modelConfig';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useUserStore } from '@/store';
  import useAIStore from '@/store/modules/setting/ai';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { ModelConfigItem, SupplierModelItem } from '@/models/setting/modelConfig';
  import { ModelBaseTypeEnum } from '@/enums/modelEnum';

  const aiStore = useAIStore();

  const { openModal } = useModal();

  const { t } = useI18n();
  const userStore = useUserStore();

  const props = defineProps<{
    modelKey: 'personal' | 'system';
    cardMinWidth?: number;
  }>();

  const modelConfigListApiMap = {
    personal: getPersonalModelConfigList,
    system: getModelConfigList,
  }[props.modelKey];

  const modelConfigDeleteApiMap = {
    personal: deletePersonalModelConfig,
    system: deleteModelConfig,
  }[props.modelKey];

  const modelEditApiMap = {
    personal: editPersonalModelConfig,
    system: editModelConfig,
  }[props.modelKey];

  const modelConfigPermissionMap = {
    personal: [],
    system: ['SYSTEM_PARAMETER_SETTING_AI_MODEL:READ+UPDATE'],
  }[props.modelKey];

  const keyword = ref('');
  const modelCardListRef = ref<InstanceType<typeof MsCardList>>();

  function searchData() {
    nextTick(() => {
      modelCardListRef.value?.reload();
    });
  }

  const initSupplier = {
    value: ModelBaseTypeEnum.DeepSeek,
    name: 'DeepSeek',
    icon: 'deepSeek',
  };

  const modelList: SupplierModelItem[] = [
    initSupplier,
    {
      value: ModelBaseTypeEnum.OpenAI,
      name: 'OpenAI',
      icon: 'openAi',
    },
    {
      value: ModelBaseTypeEnum.ZhiPuAI,
      name: t('system.config.modelConfig.zhiPuAi'),
      icon: 'zhiPuAi',
    },
  ];

  const getTypeName = (item: ModelConfigItem) => t(modelTypeOptions.find((e) => e.value === item.type)?.label ?? '');

  const activeModelType = ref(ModelBaseTypeEnum.DeepSeek);

  const supplierModelItem = ref<SupplierModelItem>(initSupplier);

  function changeModelType(item: SupplierModelItem) {
    keyword.value = '';
    activeModelType.value = item.value;
    supplierModelItem.value = item;
    searchData();
  }

  function getModelSvg(item: ModelConfigItem) {
    switch (item.providerName) {
      case ModelBaseTypeEnum.DeepSeek:
        return 'deepSeek';
      case ModelBaseTypeEnum.ZhiPuAI:
        return 'zhiPuAi';
      default:
        return 'openAi';
    }
  }

  const loading = ref(false);
  async function enableModel(item: ModelConfigItem) {
    loading.value = true;
    try {
      await modelEditApiMap({
        ...item,
        status: true,
      });
      Message.success(t('common.enableSuccess'));
      searchData();
      aiStore.getAISourceNameList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function closedModel(item: ModelConfigItem) {
    openModal({
      type: 'warning',
      title: t('system.config.modelConfig.closedModelTitle', { name: characterLimit(item.name) }),
      content: t('system.config.modelConfig.closedModelTip'),
      okText: t('common.confirmClose'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'warning',
      },
      onBeforeOk: async () => {
        try {
          await modelEditApiMap({
            ...item,
            status: false,
          });
          Message.success(t('common.closeSuccess'));
          searchData();
          aiStore.getAISourceNameList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function changeStatus(newValue: string | number | boolean, item: ModelConfigItem) {
    if (newValue) {
      enableModel(item);
    } else {
      closedModel(item);
    }
    return false;
  }

  const showModelConfigDrawer = ref(false);
  const currentModelId = ref<string>('');

  function editModel(item: ModelConfigItem) {
    currentModelId.value = item.id;
    showModelConfigDrawer.value = true;
  }

  function handleCancel() {
    currentModelId.value = '';
  }

  function addModel() {
    showModelConfigDrawer.value = true;
  }

  function deleteModel(item: ModelConfigItem) {
    openModal({
      type: 'error',
      title: t('common.deleteConfirmTitle', { name: characterLimit(item.name) }),
      content: t('system.config.modelConfig.deleteModelTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await modelConfigDeleteApiMap(item.id);
          Message.success(t('common.deleteSuccess'));
          searchData();
          aiStore.getAISourceNameList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function refreshHandler() {
    searchData();
    aiStore.getAISourceNameList();
  }
</script>

<style scoped lang="less">
  .model-list-wrapper {
    padding: 24px;
    .model-config-title {
      @apply flex items-center font-medium;

      margin-bottom: 16px;
    }
    .model-item {
      padding: 12px 16px;
      border: 1px solid transparent;
      cursor: pointer;
      &:hover {
        background: var(--color-text-n9);
      }
      &.active {
        border: 1px solid rgb(var(--primary-5));
        color: rgb(var(--primary-5));
      }
    }
  }
  .model-config-card-list-wrapper {
    @apply overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();
    .model-config-card-list {
      padding: 16px;
      height: 100%;
      border-radius: var(--border-radius-medium);
      background: var(--color-text-n9);
    }
  }
  .model-item-img {
    border-radius: var(--border-radius-small);
    background: var(--color-text-n9);
  }
</style>
