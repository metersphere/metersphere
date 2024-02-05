<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <div class="p-[24px]">
          <moduleTree
            @init="(val) => (folderTree = val)"
            @new-api="newApi"
            @change="(val) => (activeModule = val)"
            @import="importDrawerVisible = true"
          />
        </div>
      </template>
      <template #second>
        <div class="flex h-full flex-col">
          <debug ref="debugRef" :module="activeModule" :module-tree="folderTree" />
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
  import { RequestContentTypeEnum } from '@/enums/apiEnum';

  const { t } = useI18n();

  const debugRef = ref<InstanceType<typeof debug>>();
  const activeModule = ref<string>('root');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const importDrawerVisible = ref(false);
  const curlCode = ref('');

  function newApi() {
    debugRef.value?.addDebugTab();
  }

  function handleCurlImportConfirm() {
    const { url, headers, queryParameters } = parseCurlScript(curlCode.value);
    debugRef.value?.addDebugTab({
      url,
      headerParams: headers?.map((e) => ({
        required: false,
        type: 'string',
        min: undefined,
        max: undefined,
        contentType: RequestContentTypeEnum.TEXT,
        tag: [],
        desc: '',
        encode: false,
        enable: false,
        mustContain: false,
        ...e,
      })),
      value: '',
      queryParams: queryParameters?.map((e) => ({
        required: false,
        type: 'string',
        min: undefined,
        max: undefined,
        contentType: RequestContentTypeEnum.TEXT,
        tag: [],
        desc: '',
        encode: false,
        enable: false,
        mustContain: false,
        ...e,
      })),
    });
    curlCode.value = '';
    importDrawerVisible.value = false;
  }
</script>

<style lang="less" scoped></style>
