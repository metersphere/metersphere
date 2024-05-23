<template>
  <ms-base-table ref="bugTableRef" v-bind="linkPropsRes" v-on="linkTableEvent">
    <template #num="{ record }">
      <span type="text" class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">{{ record.num }}</span>
    </template>
    <template #name="{ record }">
      <div class="one-line-text flex max-w-[150px] flex-nowrap items-center"> {{ characterLimit(record.name) }}</div>
      <a-popover title="" position="right" style="width: 480px">
        <div class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</div>
        <template #content>
          <div v-dompurify-html="record.content" class="markdown-body" style="margin-left: 48px"> </div>
        </template>
      </a-popover>
    </template>
    <template #statusName="{ record }">
      <div class="one-line-text">{{ record.statusName || '-' }}</div>
    </template>
    <template #handleUserName="{ record }">
      <a-tooltip :content="record.handleUserName">
        <div class="one-line-text max-w-[200px]">{{ characterLimit(record.handleUserName) || '-' }}</div>
      </a-tooltip>
    </template>

    <template #operation="{ record }">
      <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" @click="cancelLink(record.id)">{{
        t('caseManagement.featureCase.cancelLink')
      }}</MsButton>
    </template>
    <template v-if="(keyword || '').trim() === ''" #empty>
      <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
        {{ t('caseManagement.featureCase.tableNoDataWidthComma') }}
        <span v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE', 'PROJECT_BUG:READ+ADD'])">{{
          t('caseManagement.featureCase.please')
        }}</span>
        <MsButton
          v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE'])"
          :disabled="!props.bugTotal"
          class="ml-[8px]"
          @click="linkDefect"
        >
          {{ t('caseManagement.featureCase.linkDefect') }}
        </MsButton>
        <span v-if="hasAnyPermission(['PROJECT_BUG:READ+ADD'])">{{ t('caseManagement.featureCase.or') }}</span>
        <MsButton v-permission="['PROJECT_BUG:READ+ADD']" class="ml-[8px]" @click="createDefect">
          {{ t('testPlan.featureCase.noBugDataNewBug') }}
        </MsButton>
      </div>
    </template>
  </ms-base-table>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { CommonList, TableQueryParams } from '@/models/common';

  const appStore = useAppStore();
  const { t } = useI18n();
  const props = defineProps<{
    caseId: string;
    keyword: string;
    bugColumns: MsTableColumn;
    bugTotal: number; // 平台缺陷总数决定是否新建还是关联
    loadParams?: Record<string, any>;
    loadBugListApi: (params: TableQueryParams) => Promise<CommonList<Record<string, any>>>; // 获取列表请求函数
  }>();

  const emit = defineEmits<{
    (e: 'link'): void;
    (e: 'new'): void;
    (e: 'cancelLink', bugId: string): void;
    (e: 'update:keyword'): void;
  }>();

  const bugTableRef = ref();
  const {
    propsRes: linkPropsRes,
    propsEvent: linkTableEvent,
    loadList: loadLinkList,
    setLoadListParams: setLinkListParams,
  } = useTable(props.loadBugListApi, {
    columns: props.bugColumns,
    scroll: { x: 'auto' },
    heightUsed: 340,
    enableDrag: false,
  });

  const innerKeyword = useVModel(props, 'keyword', emit);
  function searchData(keyword?: string) {
    setLinkListParams({
      ...props.loadParams,
      keyword,
      projectId: appStore.currentProjectId,
      condition: {
        keyword: innerKeyword.value,
        filter: linkPropsRes.value.filter,
      },
    });
    loadLinkList();
  }

  function linkDefect() {
    emit('link');
  }

  function createDefect() {
    emit('new');
  }

  function cancelLink(id: string) {
    emit('cancelLink', id);
  }

  onBeforeMount(() => {
    searchData(innerKeyword.value);
  });

  watch(
    () => props.bugColumns,
    (val) => {
      if (val) {
        bugTableRef.value.initColumn(val);
      }
    }
  );

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        searchData(innerKeyword.value);
      }
    }
  );

  defineExpose({
    searchData,
  });
</script>

<style scoped></style>
