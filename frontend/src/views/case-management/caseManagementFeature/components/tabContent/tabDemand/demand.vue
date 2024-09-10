<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button
          v-if="caseEnable"
          v-permission="['FUNCTIONAL_CASE:READ+ADD', 'FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE']"
          type="primary"
          class="mr-2"
          @click="associatedDemand"
        >
          {{ t('caseManagement.featureCase.associatedDemand') }}
        </a-button>
        <a-button
          v-permission="['FUNCTIONAL_CASE:READ+ADD', 'FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE']"
          type="outline"
          @click="addDemand"
        >
          {{ t('caseManagement.featureCase.addDemand') }}
        </a-button>
      </div>

      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByName')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="searchList"
      />
    </div>
    <AssociatedDemandTable
      ref="demandRef"
      :fun-params="{ caseId: props.caseId, keyword, projectId: currentProjectId }"
      :show-empty="true"
      :case-enable="caseEnable"
      @update="updateDemand"
      @create="addDemand"
      @cancel="cancelLink"
      @open="openDemandUrl"
      @associate="linkDemandDrawer = true"
    />
    <AddDemandModal
      ref="demandModalRef"
      v-model:visible="showAddModel"
      v-model:form="modelForm"
      :case-id="props.caseId"
      :loading="confirmLoading"
      @save="saveHandler"
      @success="searchList()"
    />
    <ThirdDemandDrawer
      v-model:visible="linkDemandDrawer"
      :case-id="caseId"
      :drawer-loading="drawerLoading"
      :platform-info="platformInfo"
      @save="handleDrawerConfirm"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import AddDemandModal from './addDemandModal.vue';
  import AssociatedDemandTable from './associatedDemandTable.vue';
  import ThirdDemandDrawer from './thirdDemandDrawer.vue';

  import {
    addDemandRequest,
    cancelAssociationDemand,
    updateDemandReq,
  } from '@/api/modules/case-management/featureCase';
  import { getCaseRelatedInfo } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { CreateOrUpdateDemand, DemandItem } from '@/models/caseManagement/featureCase';

  const { t } = useI18n();
  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const props = defineProps<{
    caseId: string;
  }>();

  const keyword = ref<string>('');
  const demandRef = ref();

  const searchList = debounce(() => {
    demandRef.value.initData();
  }, 100);

  const showAddModel = ref<boolean>(false);

  const caseEnable = ref<boolean>(false);

  const initModelForm: DemandItem = {
    id: '',
    caseId: '', // 功能用例ID
    demandId: '', // 需求ID
    demandName: '', // 需求标题
    demandUrl: '', // 需求地址
    demandPlatform: '', // 需求所属平台
    createTime: '',
    updateTime: '',
    createUser: '',
    updateUser: '',
    children: [], // 平台下对应的需求
  };

  const modelForm = ref<DemandItem>({
    ...initModelForm,
  });

  // 更新需求
  function updateDemand(record: DemandItem) {
    showAddModel.value = true;
    modelForm.value = { ...record };
  }

  const drawerLoading = ref<boolean>(false);

  // 关联需求
  const linkDemandDrawer = ref<boolean>(false);
  function associatedDemand() {
    linkDemandDrawer.value = true;
  }

  function openDemandUrl(record: DemandItem) {
    if (record.demandUrl) {
      window.open(record.demandUrl);
    }
  }

  const platformInfo = ref<Record<string, any>>({});

  async function handleDrawerConfirm(associatedParams: CreateOrUpdateDemand) {
    try {
      drawerLoading.value = true;
      const { demandPlatform, demandList, functionalDemandBatchRequest } = associatedParams;
      const params = {
        id: JSON.parse(platformInfo.value.demand_platform_config).zentaoId,
        caseId: props.caseId,
        demandPlatform,
        demandList,
        functionalDemandBatchRequest,
      };
      await addDemandRequest(params);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      linkDemandDrawer.value = false;
      demandRef.value.initData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  // 取消关联
  async function cancelLink(record: DemandItem) {
    try {
      await cancelAssociationDemand(record.id);
      Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
      demandRef.value.initData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function initPlatform() {
    try {
      const result = await getCaseRelatedInfo(currentProjectId.value);
      if (result && result.platform_key) {
        platformInfo.value = { ...result };
        caseEnable.value = platformInfo.value.case_enable !== 'false';
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const confirmLoading = ref<boolean>(false);
  const demandModalRef = ref();

  async function saveHandler(param: CreateOrUpdateDemand, isContinue: boolean) {
    try {
      confirmLoading.value = true;
      if (param.id) {
        await updateDemandReq(param);
        Message.success(t('common.updateSuccess'));
      } else {
        await addDemandRequest(param);
        Message.success(t('common.addSuccess'));
      }
      if (!isContinue) {
        showAddModel.value = false;
      }
      demandModalRef.value.resetForm();
      demandRef.value.initData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  function addDemand() {
    showAddModel.value = true;
    modelForm.value = { ...initModelForm };
  }

  onMounted(async () => {
    initPlatform();
  });
</script>

<style scoped lang="less"></style>
