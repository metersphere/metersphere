<template>
  <!-- TODO:接口请求超过5S以上可展示取消请求按钮，避免用户过长等待 -->
  <MsCard :loading="loading" simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <div class="p-[24px]">
          <moduleTree
            ref="moduleTreeRef"
            @init="(val) => (folderTree = val)"
            @new-api="newApi"
            @click-api-node="handleApiNodeClick"
            @import="importDrawerVisible = true"
          />
        </div>
      </template>
      <template #second>
        <div class="flex h-full flex-col">
          <debug
            ref="debugRef"
            v-model:detail-loading="loading"
            :module-tree="folderTree"
            @add-done="handleDebugAddDone"
          />
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
  <MsDrawer
    v-model:visible="importDrawerVisible"
    :width="680"
    :ok-disabled="curlCode.trim() === ''"
    disabled-width-drag
    @cancel="curlCode = ''"
    @confirm="handleCurlImportConfirm"
  >
    <template #title>
      <a-tooltip position="right" :content="t('apiTestDebug.importByCURLTip')">
        {{ t('apiTestDebug.importByCURL') }}
        <icon-exclamation-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </template>
    <div class="h-full">
      <MsCodeEditor
        v-if="importDrawerVisible"
        v-model:model-value="curlCode"
        theme="MS-text"
        height="100%"
        language="plaintext"
        :show-theme-change="false"
        :show-full-screen="false"
      >
      </MsCodeEditor>
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import debug from './components/debug/index.vue';
  import moduleTree from './components/moduleTree.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { parseCurlScript } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import { RequestContentTypeEnum, RequestParamsType } from '@/enums/apiEnum';

  const { t } = useI18n();

  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const debugRef = ref<InstanceType<typeof debug>>();
  const folderTree = ref<ModuleTreeNode[]>([]);
  const importDrawerVisible = ref(false);
  const curlCode = ref('');
  const loading = ref(false);

  function newApi() {
    debugRef.value?.addDebugTab();
  }

  function handleCurlImportConfirm() {
    const { url, headers, queryParameters } = parseCurlScript(curlCode.value);
    debugRef.value?.addDebugTab({
      url,
      headers: headers?.map((e) => ({
        contentType: RequestContentTypeEnum.TEXT,
        description: '',
        enable: true,
        ...e,
      })),
      query: queryParameters?.map((e) => ({
        paramType: RequestParamsType.STRING,
        description: '',
        required: false,
        maxLength: undefined,
        minLength: undefined,
        encode: false,
        enable: true,
        ...e,
      })),
    });
    curlCode.value = '';
    importDrawerVisible.value = false;
  }

  function handleApiNodeClick(node: ModuleTreeNode) {
    debugRef.value?.openApiTab(node);
  }

  function handleDebugAddDone() {
    moduleTreeRef.value?.initModules();
    moduleTreeRef.value?.initModuleCount();
  }
</script>

<style lang="less" scoped></style>
