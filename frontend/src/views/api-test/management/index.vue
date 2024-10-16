<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :not-show-first="isAdvancedSearchMode" :size="300" :max="0.5">
      <template #first>
        <div class="flex flex-col">
          <div class="p-[16px]">
            <moduleTree
              ref="moduleTreeRef"
              :active-node-id="activeNodeId"
              :doc-share-id="docShareId"
              @init="handleModuleInit"
              @new-api="newApi"
              @import="importDrawerVisible = true"
              @folder-node-select="handleNodeSelect"
              @click-api-node="handleApiNodeClick"
              @change-protocol="handleProtocolChange"
              @update-api-node="handleUpdateApiNode"
              @delete-node="handleDeleteApiFromModuleTree"
              @execute="handleExecute"
              @open-current-node="openCurrentNode"
            />
          </div>
          <div v-if="!docShareId" class="flex-1">
            <a-divider class="!my-0 !mb-0" />
            <div class="case h-[40px] !px-[24px]" @click="setActiveFolder('recycle')">
              <div class="flex items-center" :class="getActiveClass('recycle')">
                <MsIcon type="icon-icon_delete-trash_outlined1" class="folder-icon" />
                <div class="folder-name mx-[4px]">{{ t('common.recycle') }}</div>
              </div>
              <div class="folder-count">{{ recycleModulesCount || 0 }}</div>
            </div>
          </div>
        </div>
      </template>
      <template #second>
        <div class="relative flex h-full flex-col">
          <div
            v-if="!docShareId"
            id="managementContainer"
            :class="['absolute z-[102] h-full w-full', importDrawerVisible ? '' : 'invisible']"
            style="transition: all 0.3s"
          >
            <importApi
              v-model:visible="importDrawerVisible"
              :module-tree="folderTree"
              :active-module="activeModule"
              popup-container="#managementContainer"
              @done="handleImportDone"
            />
          </div>
          <management
            v-if="!docShareId"
            ref="managementRef"
            :module-tree="folderTree"
            :active-module="activeModule"
            :offspring-ids="offspringIds"
            :selected-protocols="selectedProtocols"
            @import="importDrawerVisible = true"
            @handle-adv-search="handleAdvSearch"
          />

          <ApiSharePreview
            v-if="docShareId"
            :selected-protocols="protocols"
            :api-info="currentNode"
            :previous-node="previousNode"
            :next-node="nextNode"
            @toggle-detail="toggleDetail"
          />
        </div>
      </template>
    </MsSplitBox>
    <!-- 分享密码校验 -->
    <a-modal
      v-model:visible="checkPsdModal"
      :mask-closable="false"
      :closable="false"
      :mask="true"
      title-align="start"
      class="ms-modal-upload ms-modal-medium ms-modal-share"
      :width="280"
      unmount-on-close
      @close="closeShareHandler"
    >
      <div class="no-resource-svg"></div>
      <a-form ref="formRef" :rules="rules" :model="checkForm" layout="vertical">
        <a-form-item
          class="password-form mb-0"
          field="password"
          :label="t('apiTestManagement.effectiveTime')"
          hide-asterisk
          hide-label
          :validate-trigger="['blur']"
        >
          <a-input-password
            v-model="checkForm.password"
            :max-length="6"
            :placeholder="t('apiTestManagement.sharePasswordPlaceholder')"
            allow-clear
            autocomplete="new-password"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button type="primary" :loading="checkLoading" :disabled="!checkForm.password" @click="handleCheckPsd">
          {{ t('common.confirm') }}
        </a-button>
      </template>
    </a-modal>
  </MsCard>
</template>

<script lang="ts" setup>
  /**
   * @description 接口测试-接口管理
   */
  import { provide } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import { RequestParam } from '../components/requestComposition/index.vue';
  import importApi from './components/import.vue';
  import management from './components/management/index.vue';
  import moduleTree from './components/moduleTree.vue';
  import ApiSharePreview from '@/views/api-test/management/components/management/api/apiSharePreview.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { checkSharePsd, getTrashModuleCount, shareDetail } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { NOT_FOUND_RESOURCE } from '@/router/constants';
  import { useUserStore } from '@/store';
  import useDocShareCheckStore from '@/store/modules/api/docShareCheck';
  import useAppStore from '@/store/modules/app';

  import { ApiDefinitionGetModuleParams, ShareDetailType } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const appStore = useAppStore();
  const route = useRoute();
  const { t } = useI18n();
  const router = useRouter();
  const docCheckStore = useDocShareCheckStore();
  const userStore = useUserStore();

  const activeModule = ref<string>('all');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const importDrawerVisible = ref(false);
  const offspringIds = ref<string[]>([]);
  const selectedProtocols = ref<string[]>([]);
  const activeNodeId = ref<string | number>('all');
  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const managementRef = ref<InstanceType<typeof management>>();
  function newApi() {
    importDrawerVisible.value = false;
    managementRef.value?.newTab();
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
    managementRef.value?.changeActiveApiTabToFirst();
  }

  function handleApiNodeClick(node: ModuleTreeNode) {
    managementRef.value?.newTab(node);
  }
  function setActiveApi(params: RequestParam) {
    if (params.id === 'all') {
      // 切换到全部 tab 时需设置为上次激活的 api 节点的模块
      activeNodeId.value = params.id;
    } else {
      activeNodeId.value = params.moduleId;
    }
  }

  const protocols = ref<any[]>([]);
  async function initProtocolList() {
    try {
      protocols.value = await getProtocolList(appStore.currentOrgId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initProtocolList();
  });

  function handleProtocolChange(val: string[]) {
    selectedProtocols.value = val;
  }

  const docShareId = ref<string>(route.query.docShareId as string);
  const recycleModulesCount = ref(0);
  async function selectRecycleCount() {
    if (!docShareId.value) {
      const res = await getTrashModuleCount({
        projectId: appStore.currentProjectId,
        keyword: '',
        moduleIds: [],
        protocols: selectedProtocols.value,
      });
      recycleModulesCount.value = res.all;
    }
  }

  function handleModuleInit(tree: ModuleTreeNode[], _protocols: string[], pathMap: Record<string, any>) {
    folderTree.value = tree;
    selectedProtocols.value = _protocols;
    folderTreePathMap.value = pathMap;
    selectRecycleCount();
  }

  async function refreshModuleTree() {
    await moduleTreeRef.value?.refresh();
  }

  function refreshModuleTreeCount(params: ApiDefinitionGetModuleParams) {
    moduleTreeRef.value?.initModuleCount(params);
  }

  async function handleImportDone() {
    await refreshModuleTree();
    managementRef.value?.refreshApiTable();
  }

  function handleUpdateApiNode(newInfo: { id: string; name: string; moduleId?: string; [key: string]: any }) {
    managementRef.value?.handleApiUpdateFromModuleTree(newInfo);
  }

  function handleDeleteApiFromModuleTree(id: string, isModule?: boolean) {
    managementRef.value?.handleDeleteApiFromModuleTree(id, isModule);
  }

  function handleExecute(id: string) {
    managementRef.value?.newTab(id, false, true);
  }

  onMounted(() => {
    if (route.query.dId) {
      // 携带 dId 参数，自动打开接口定义详情 tab
      managementRef.value?.newTab(route.query.dId as string);
    } else if (route.query.cId) {
      // 携带 cId 参数，自动打开接口用例详情 tab
      managementRef.value?.newCaseTab(route.query.cId as string);
    }
  });

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeModule.value === type ? 'folder-text case-active' : 'folder-text';
  };

  // 设置当前激活用例类型公共用例|全部用例|回收站
  const setActiveFolder = (type: string) => {
    if (type === 'recycle') {
      router.push({
        name: ApiTestRouteEnum.API_TEST_MANAGEMENT_RECYCLE,
      });
    }
  };

  const isAdvancedSearchMode = ref(false);
  function handleAdvSearch(isStartAdvance: boolean) {
    isAdvancedSearchMode.value = isStartAdvance;
    moduleTreeRef.value?.setActiveFolder('all');
  }

  const checkLoading = ref<boolean>(false);
  const checkPsdModal = ref<boolean>(false);
  const checkForm = ref({
    docShareId: route.query.docShareId as string,
    password: '',
  });

  const validatePassword = (value: string | undefined, callback: (error?: string) => void) => {
    const sixDigitRegex = /^\d{6}$/;

    if (value === undefined || value === '') {
      callback(t('apiTestManagement.enterPassword'));
    } else if (!sixDigitRegex.test(value)) {
      callback(t('apiTestManagement.enterPassword'));
    } else {
      callback();
    }
  };

  const rules = {
    password: [
      {
        required: true,
        message: t('apiTestManagement.sharePasswordPlaceholder'),
      },
      {
        validator: validatePassword,
      },
    ],
  };

  // 上一条|下一条
  function toggleDetail(type: string) {
    if (type === 'prev') {
      moduleTreeRef.value?.previousApi();
    } else {
      moduleTreeRef.value?.nextApi();
    }
  }
  const formRef = ref<FormInstance>();

  // 关闭分享
  function closeShareHandler() {
    checkPsdModal.value = false;
    formRef.value?.resetFields();
    checkForm.value.password = '';
  }

  const shareDetailInfo = ref<ShareDetailType>();
  const currentNode = ref();

  // 获取分享详情
  async function getShareDetail() {
    try {
      shareDetailInfo.value = await shareDetail(docShareId.value);
      // 资源无效
      if (shareDetailInfo.value.invalid) {
        router.push({
          name: NOT_FOUND_RESOURCE,
          query: {
            type: 'EXPIRED',
          },
        });
      }
      // 限制访问校验
      if (shareDetailInfo.value.isPrivate && !docCheckStore.isDocVerified(docShareId.value, userStore.id || '')) {
        checkPsdModal.value = true;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const previousNode = ref<ModuleTreeNode | null>();
  const nextNode = ref<ModuleTreeNode | null>();

  // 设置当前预览节点
  function openCurrentNode(node: ModuleTreeNode, apiNodes: ModuleTreeNode[]) {
    const index = apiNodes.indexOf(node);
    currentNode.value = node;
    previousNode.value = index > 0 ? apiNodes[index - 1] : null;
    nextNode.value = index < apiNodes.length - 1 ? apiNodes[index + 1] : null;
  }

  // 校验密码
  function handleCheckPsd() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          checkLoading.value = true;
          const res = await checkSharePsd(checkForm.value);
          if (res) {
            closeShareHandler();
            // 标记为已验证
            docCheckStore.markDocAsVerified(docShareId.value, userStore.id || '');
            checkPsdModal.value = false;
          } else {
            Message.error(t('apiTestManagement.apiSharePsdError'));
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          checkLoading.value = false;
        }
      }
    });
  }

  onBeforeMount(() => {
    if (docShareId.value) {
      getShareDetail();
    }
  });

  /** 向子孙组件提供方法和值 */
  provide('setActiveApi', setActiveApi);
  provide('refreshModuleTree', refreshModuleTree);
  provide('refreshModuleTreeCount', refreshModuleTreeCount);
  provide('folderTreePathMap', folderTreePathMap.value);
  provide('docShareId', docShareId.value);
  provide('shareDetailInfo', shareDetailInfo.value);
</script>

<style lang="less" scoped>
  .case {
    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    @apply flex cursor-pointer items-center justify-between;
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-icon {
      margin-right: 4px;
      color: var(--color-text-4);
    }
    .folder-name {
      color: var(--color-text-1);
    }
    .folder-count {
      margin-left: 4px;
      color: var(--color-text-4);
    }
    .case-active {
      .folder-icon,
      .folder-name,
      .folder-count {
        color: rgb(var(--primary-5));
      }
    }
    .back {
      margin-right: 8px;
      width: 20px;
      height: 20px;
      border: 1px solid #ffffff;
      background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
      box-shadow: 0 0 7px rgb(15 0 78 / 9%);
      .arco-icon {
        color: rgb(var(--primary-5));
      }

      @apply flex cursor-pointer items-center rounded-full;
    }
  }
  .recycle {
    @apply absolute bottom-0 bg-white pb-4;
    :deep(.arco-divider-horizontal) {
      margin: 8px 0;
    }
    .recycle-bin {
      @apply bottom-0 flex items-center bg-white;
      .recycle-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
  }
  .no-resource-svg {
    margin: 0 auto 24px;
    width: 160px;
    height: 98px;
    background: url('@/assets/svg/no_resource.svg');
    background-size: cover;
  }
  :deep(.ms-modal-share) {
    .arco-modal-mask {
      background: var(--color-text-1) !important;
    }
  }
  :deep(.password-form) {
    .arco-form-item-message {
      margin-bottom: 0 !important;
    }
  }
</style>
