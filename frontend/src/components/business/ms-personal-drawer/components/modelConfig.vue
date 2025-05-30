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
          <div class="mb-[16px] flex items-center justify-between">
            <a-button type="primary" @click="addModel">
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
            :class="`model-config-card-list-wrapper ${
              props.modelKey === 'personal' ? 'h-[calc(100vh-138px)]' : 'h-[calc(100vh-274px)]'
            }`"
          >
            <div class="model-config-card-list">
              <MsCardList
                mode="static"
                :card-min-width="props.cardMinWidth || 230"
                class="flex-1"
                :shadow-limit="50"
                :list="modelCardList"
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
                          <a-tooltip :content="item.creatorName" :mouse-enter-delay="300">
                            <div class="one-line-text">{{ item.creatorName }}</div>
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
                        <a-tooltip :content="item.modelType" :mouse-enter-delay="300">
                          <div class="one-line-text"> {{ item.modelType }}</div>
                        </a-tooltip>
                        <a-tooltip :content="item.baseModel" :mouse-enter-delay="300">
                          <div class="one-line-text"> {{ item.baseModel }}</div>
                        </a-tooltip>
                      </div>
                    </div>
                    <div class="model-item-footer mt-[24px] flex items-center justify-between">
                      <div class="flex items-center gap-[12px]">
                        <a-button
                          type="outline"
                          class="arco-btn-outline--secondary"
                          size="small"
                          @click="editModel(item)"
                        >
                          {{ t('common.edit') }}
                        </a-button>
                        <a-button
                          type="outline"
                          class="arco-btn-outline--secondary"
                          size="small"
                          @click="deleteModel(item)"
                        >
                          {{ t('common.delete') }}
                        </a-button>
                      </div>

                      <a-switch v-model="item.enable" size="small" :before-change="(val) => changeStatus(val, item)" />
                    </div>
                  </div>
                </template>
              </MsCardList>
            </div>
          </div>
        </div>
        <modelEditDrawer
          v-model:visible="showModelConfigDrawer"
          :current-model-id="currentModelId"
          :supplier-model-item="supplierModelItem"
          @close="handleCancel"
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

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { characterLimit } from '@/utils';

  import type { SupplierModelItem } from '@/models/setting/modelConfig';
  import { ModelBaseTypeEnum } from '@/enums/modelEnum';

  const { openModal } = useModal();

  const { t } = useI18n();

  const props = defineProps<{
    modelKey: 'personal' | 'system';
    cardMinWidth?: number;
  }>();

  const keyword = ref('');

  function searchData() {}

  const modelCardList = ref([
    {
      id: '1001',
      name: '模型名称',
      creatorName: '创建人名称',
      type: ModelBaseTypeEnum.DeepSeek,
      baseName: '基础模型',
      enable: true,
    },
    {
      id: '1002',
      name: '模型名称模型名称模型名称模型名称模型名称模型名称模型名称模型名称模型名称模型名称模型名称模型名称',
      creatorName:
        '创建人名称创建人名称创建人名称创建人名称创建人名称创建人名称创建人名称创建人名称创建人名称创建人名称创建人名称v',
      type: ModelBaseTypeEnum.ZhiPuAI,
      baseName: '基础模型基础模型基础模型基础模型基础模型基础模型基础模型基础模型基础模型基础模型v',
      enable: true,
    },
    {
      id: '1003',
      name: '模型名称',
      creatorName: '创建人名称',
      type: ModelBaseTypeEnum.OpenAI,
      baseName: '基础模型',
      enable: true,
    },
  ]);

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

  const activeModelType = ref(ModelBaseTypeEnum.DeepSeek);

  const supplierModelItem = ref<SupplierModelItem>(initSupplier);

  function changeModelType(item: SupplierModelItem) {
    activeModelType.value = item.value;
    supplierModelItem.value = item;
  }

  function getModelSvg(item: any) {
    switch (item.type) {
      case ModelBaseTypeEnum.DeepSeek:
        return 'deepSeek';
      case ModelBaseTypeEnum.ZhiPuAI:
        return 'zhiPuAi';
      default:
        return 'openAi';
    }
  }

  function enableModel(item: any) {
    Message.success(t('common.enableSuccess'));
  }

  function closedModel(item: any) {
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
          // TODO
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function changeStatus(newValue: string | number | boolean, item: any) {
    if (newValue) {
      enableModel(item);
    } else {
      closedModel(item);
    }
    return false;
  }

  const showModelConfigDrawer = ref(false);
  const currentModelId = ref<string>('');

  //  TODO 类型
  function editModel(item: any) {
    currentModelId.value = item.id;
    showModelConfigDrawer.value = true;
  }

  function handleCancel() {
    currentModelId.value = '';
  }

  function addModel() {
    showModelConfigDrawer.value = true;
  }

  function deleteModel(item: any) {
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
          // TODO
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
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
    border-radius: var(--border-radius-large);
    background: var(--color-text-n9);
  }
</style>
