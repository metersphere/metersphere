<template>
  <ms-base-table ref="bugTableRef" v-bind="linkPropsRes" v-on="linkTableEvent">
    <template #num="{ record }">
      <span type="text" class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">{{ record.num }}</span>
    </template>
    <template #name="{ record }">
      <span class="one-line-text max-w-[150px]"> {{ characterLimit(record.name) }}</span>
      <a-popover title="" position="right" style="width: 480px">
        <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
        <template #content>
          <div v-dompurify-html="record.content" class="markdown-body" style="margin-left: 48px"> </div>
        </template>
      </a-popover>
    </template>
    <template #severityFilter="{ columnConfig }">
      <TableFilter
        v-model:visible="severityFilterVisible"
        v-model:status-filters="severityFilterValue"
        :title="(columnConfig.title as string)"
        :list="severityFilterOptions"
        value-key="value"
        @search="searchData()"
      >
        <template #item="{ item }">
          {{ item.text }}
        </template>
      </TableFilter>
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
          {{ t('caseManagement.featureCase.createDefect') }}
        </MsButton>
      </div>
    </template>
  </ms-base-table>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

  import { getLinkedCaseBugList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugOptionItem } from '@/models/bug-management';

  const appStore = useAppStore();
  const { t } = useI18n();
  const props = defineProps<{
    caseId: string;
    keyword: string;
    bugColumns: MsTableColumn;
    bugTotal: number; // 平台缺陷总数决定是否新建还是关联
  }>();

  const emit = defineEmits<{
    (e: 'link'): void;
    (e: 'new'): void;
    (e: 'cancelLink', bugId: string): void;
  }>();

  const severityFilterVisible = ref(false);
  const severityFilterOptions = ref<BugOptionItem[]>([]);
  const bugTableRef = ref();
  const {
    propsRes: linkPropsRes,
    propsEvent: linkTableEvent,
    loadList: loadLinkList,
    setLoadListParams: setLinkListParams,
  } = useTable(getLinkedCaseBugList, {
    columns: props.bugColumns,
    scroll: { x: 'auto' },
    heightUsed: 340,
    enableDrag: false,
  });
  const severityColumnId = ref('');
  const severityFilterValue = ref<string[]>([]);
  function initTableParams() {
    const filterParams: Record<string, any> = {
      // status: statusFilterValue.value,
      // handleUser: handleUserFilterValue.value,
    };
    // TODO 要和后台同学确认一下
    filterParams[severityColumnId.value] = severityFilterValue.value;
    return {
      keyword: props.keyword,
      caseId: props.caseId,
      projectId: appStore.currentProjectId,
      condition: {
        keyword: props.keyword,
        filter: linkPropsRes.value.filter,
      },
    };
  }
  function searchData() {
    setLinkListParams(initTableParams());
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
    searchData();
  });

  watch(
    () => props.bugColumns,
    (val) => {
      if (val) {
        bugTableRef.value.initColumn(val);
      }
    }
  );

  defineExpose({
    searchData,
  });
</script>

<style scoped></style>
