<template>
  <slot name="header" :project-name="shareDetailInfo?.projectName"></slot>
  <MsCard simple no-content-padding auto-height>
    <div class="h-[calc(100vh-80px)]">
      <MsSplitBox :size="300" :max="0.5">
        <template #first>
          <div class="flex flex-col">
            <div class="p-[16px]">
              <moduleTree
                ref="moduleTreeRef"
                :active-node-id="activeNodeId"
                :doc-share-id="docShareId"
                @init="handleModuleInit"
                @folder-node-select="handleNodeSelect"
                @change-protocol="handleProtocolChange"
                @open-current-node="openCurrentNode"
                @export-share="handleExportShare"
              />
            </div>
          </div>
        </template>
        <template #second>
          <ApiSharePreview
            :selected-protocols="protocols"
            :api-info="currentNode"
            :previous-node="previousNode"
            :next-node="nextNode"
            @toggle-detail="toggleDetail"
            @export-share="handleExportShare"
          />
        </template>
      </MsSplitBox>
    </div>

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
      :esc-to-close="false"
      @close="closeShareHandler"
    >
      <div class="no-permission-svg-bright"></div>
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
    <ApiExportModal
      v-model:visible="showExportModal"
      :batch-params="batchParams"
      :condition-params="getConditionParams"
      is-share
    />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import ApiExportModal from '@/views/api-test/management/components/management/api/apiExportModal.vue';
  import ApiSharePreview from '@/views/api-test/management/components/management/api/apiSharePreview.vue';
  import moduleTree from '@/views/api-test/management/components/moduleTree.vue';

  import { checkSharePsd, shareDetail } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { NOT_FOUND_RESOURCE } from '@/router/constants';
  import { useUserStore } from '@/store';
  import useDocShareCheckStore from '@/store/modules/api/docShareCheck';
  import useAppStore from '@/store/modules/app';

  import { ShareDetailType } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';

  const appStore = useAppStore();
  const route = useRoute();
  const { t } = useI18n();
  const router = useRouter();
  const docCheckStore = useDocShareCheckStore();
  const userStore = useUserStore();

  const activeNodeId = ref<string | number>('all');
  const activeModule = ref<string>('all');
  const offspringIds = ref<string[]>([]);

  const docShareId = ref<string>(route.query.docShareId as string);

  const folderTree = ref<ModuleTreeNode[]>([]);
  const selectedProtocols = ref<string[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});

  function handleModuleInit(tree: ModuleTreeNode[], _protocols: string[], pathMap: Record<string, any>) {
    folderTree.value = tree;
    selectedProtocols.value = _protocols;
    folderTreePathMap.value = pathMap;
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function handleProtocolChange(val: string[]) {
    selectedProtocols.value = val;
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
  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();

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

  const protocols = ref<any[]>([]);
  const currentNode = ref();

  const showExportModal = ref(false);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
  });

  // 导出全部|导出单个
  function handleExportShare(all: boolean) {
    batchParams.value.selectAll = all;
    batchParams.value.selectedIds = all ? [] : [currentNode.value?.id];
    showExportModal.value = true;
  }

  function getConditionParams() {
    return {
      condition: {
        keyword: '',
        filter: {},
        viewId: '',
      },
      projectId: appStore.currentProjectId,
      protocols: [],
      moduleIds: [],
      orgId: appStore.currentOrgId,
      shareId: docShareId.value,
    };
  }

  const shareDetailInfo = ref<ShareDetailType>({
    invalid: false,
    allowExport: false,
    isPrivate: false,
    projectName: '',
  });

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

  provide('docShareId', docShareId.value);
  provide('shareDetailInfo', shareDetailInfo);
</script>

<style scoped lang="less">
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
