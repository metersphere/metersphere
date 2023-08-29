<template>
  <MsCard simple class="mb-[16px]" auto-height :loading="loading">
    <div class="outer-wrapper">
      <div class="mb-[16px] flex justify-between">
        <div class="font-medium text-[var(--color-text-000)]">{{ t('organization.service.integrationList') }}</div>
        <div>
          <a-input-search
            v-model="keyword"
            :placeholder="t('organization.service.searchService')"
            :max-length="250"
            allow-clear
            @search="searchHandler"
            @press-enter="searchHandler"
          />
        </div>
      </div>
      <div class="ms-card-wrap">
        <a-scrollbar
          :style="{
            overflow: 'auto',
            height: `calc(100vh - ${collapseHeight} - 230px)`,
          }"
        >
          <div class="list">
            <div v-for="item of filterList" :key="item.id" class="item">
              <div class="flex">
                <span class="icon float-left mr-2 h-[40px] w-[40px] rounded">
                  <img class="rounded" :src="`http://172.16.200.18:8081${item.logo}`" alt="log" />
                </span>
                <div class="flex flex-col justify-start">
                  <p>
                    <span class="mr-4 font-semibold">{{ item.title }}</span>
                    <span v-if="!item.config" class="ms-enable">{{ t('organization.service.unconfigured') }}</span>
                    <span
                      v-else
                      class="ms-enable active"
                      :style="{
                        background: 'rgb(var(--success-1))',
                        color: 'rgb(var(--success-6))',
                      }"
                      >{{ t('organization.service.configured') }}</span
                    >
                  </p>
                  <p class="mt-2 text-sm text-[var(--color-text-4)]">{{ item.description }}</p>
                </div>
              </div>
              <div class="flex justify-between">
                <a-space>
                  <a-tooltip v-if="!item.config" :content="t('organization.service.unconfiguredTip')" position="tl">
                    <span>
                      <a-button
                        v-if="!item.config"
                        type="outline"
                        class="arco-btn-outline--secondary"
                        size="mini"
                        :disabled="!item.config"
                        @click="getValidateHandler(item)"
                        >{{ t('organization.service.testLink') }}</a-button
                      ></span
                    >
                  </a-tooltip>
                  <a-button
                    v-else
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                    @click="getValidateHandler(item)"
                    >{{ t('organization.service.testLink') }}</a-button
                  >
                  <a-button type="outline" class="arco-btn-outline--secondary" size="mini" @click="editHandler(item)">{{
                    t('organization.service.edit')
                  }}</a-button>
                  <a-button
                    v-if="item.config"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                    @click="resetHandler(item)"
                    >{{ t('organization.service.reset') }}</a-button
                  >
                </a-space>
                <span>
                  <a-tooltip v-if="!item.config" :content="t('organization.service.unconfiguredTip')" position="br">
                    <span
                      ><a-switch
                        v-model="item.enable"
                        size="small"
                        :disabled="true"
                        @change="(v) => changeStatus(v, item.id)"
                    /></span>
                  </a-tooltip>
                  <a-switch v-else v-model="item.enable" size="small" @change="(v) => changeStatus(v, item.id)" />
                </span>
              </div>
            </div>
          </div>
        </a-scrollbar>
      </div>
    </div>
  </MsCard>
  <ConfigModal ref="ConfigRef" v-model:visible="serviceVisible" @success="loadList()" />
</template>

<script setup lang="ts">
  import { ref, onBeforeMount } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import { getServiceList, getValidate, resetService, addOrUpdate } from '@/api/modules/setting/serviceIntegration';
  import ConfigModal from './conifgModal.vue';
  import { useUserStore } from '@/store';
  import useModal from '@/hooks/useModal';
  import Message from '@arco-design/web-vue/es/message';
  import { characterLimit } from '@/utils';
  import type { ServiceList, ServiceItem } from '@/models/setting/serviceIntegration';

  const { t } = useI18n();
  const { openModal } = useModal();

  const userStore = useUserStore();
  const lastOrganizationId = userStore.$state?.lastOrganizationId as string;

  defineProps<{
    collapseHeight: string;
  }>();

  const keyword = ref<string>('');
  const filterList = ref<ServiceList>([]);
  const data = ref<ServiceList>([]);
  const loading = ref<boolean>(false);

  // 集成列表
  const loadList = async () => {
    loading.value = true;
    try {
      const result = await getServiceList(lastOrganizationId);
      data.value = result;
      filterList.value = result;
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  const searchHandler = () => {
    filterList.value = data.value.filter((item) => item.title?.includes(keyword.value));
  };

  const serviceVisible = ref<boolean>(false);
  const ConfigRef = ref();

  // 添加或配置
  const editHandler = (serviceItem: ServiceItem) => {
    serviceVisible.value = true;
    ConfigRef.value.addOrEdit(serviceItem);
    ConfigRef.value.title = serviceItem.title;
  };
  // 重置
  const resetHandler = async (serviceItem: ServiceItem) => {
    openModal({
      type: 'error',
      title: t('organization.service.resetServiceTip', { name: characterLimit(serviceItem.title) }),
      content: t('organization.service.resetServiceContentTip'),
      okText: t('organization.service.confirmReset'),
      cancelText: t('organization.service.Cancel'),
      okButtonProps: {
        status: 'normal',
      },
      onBeforeOk: async () => {
        try {
          await resetService(serviceItem.id as string);
          Message.success(t('organization.service.resetConfigTip'));
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  // 外部测试连接
  const getValidateHandler = async (serviceItem: ServiceItem) => {
    loading.value = true;
    try {
      await getValidate(serviceItem.id as string);
      Message.success(t('organization.service.testLinkStatusTip'));
      loadList();
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  // 切换状态
  const changeStatus = async (value: string | number | boolean, id: string | undefined) => {
    loading.value = true;
    const message = value ? 'organization.service.enableSuccess' : 'organization.service.closeSuccess';
    try {
      await addOrUpdate({ enable: value as boolean, id }, 'edit');
      Message.success(t(message));
      loadList();
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  onBeforeMount(() => {
    loadList();
  });
</script>

<style scoped lang="less">
  .ms-card-wrap {
    overflow: hidden;
    padding: 8px;
    height: calc(100% - 58px) !important;
    min-height: 300px;
    border-radius: var(--border-radius-small);
    background: var(--color-text-n9);
    .list {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-start;
      align-content: flex-start;
      width: 100%;
      .item {
        margin: 8px;
        padding: 24px;
        height: 144px;
        border-radius: 4px;
        background: white;
        @apply flex flex-col justify-between;
        .icon {
          border: 1px solid var(--color-text-n9);
        }
        .ms-enable {
          border-radius: var(--border-radius-small);
          background: var(--color-text-n9);
          @apply px-2 py-1 text-xs;
        }
      }
    }
    @media screen and (min-width: 800px) {
      .item {
        flex-basis: calc(50% - 16px);
      }
    }
    @media screen and (min-width: 1160px) {
      .item {
        flex-basis: calc(33.3% - 16px);
      }
    }
    @media screen and (min-width: 1800px) {
      .item {
        flex-basis: calc(25% - 16px);
      }
    }
  }
</style>
