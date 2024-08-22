<template>
  <div class="flex items-center">
    <BugCountPopover :bug-list="bugList || []" :bug-count="bugCount" :can-edit="props.canEdit" @load-list="loadList" />
    <a-dropdown
      v-if="hasAllPermission(['PROJECT_BUG:READ', ...(props.linkBugPermission || [])])"
      position="bl"
      @select="handleSelect"
    >
      <a-button
        v-permission="['PROJECT_BUG:READ+ADD']"
        class="arco-btn-outline--secondary ml-[8px] !p-[4px]"
        type="outline"
        size="small"
      >
        <icon-plus class="text-[14px]" />
      </a-button>

      <template #content>
        <a-doption v-if="hasAnyPermission(props.linkBugPermission || []) && props.bugCount" value="linkBug">
          {{ t('caseManagement.featureCase.linkDefect') }}
        </a-doption>
        <a-doption v-if="hasAnyPermission(['PROJECT_BUG:READ+ADD'])" value="newBug">
          {{ t('testPlan.featureCase.noBugDataNewBug') }}
        </a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import BugCountPopover from './bugCountPopover.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import type { CaseBugItem } from '@/models/testPlan/testPlan';

  const { t } = useI18n();

  const props = defineProps<{
    resourceId: string; // 资源id: 功能用例id/接口用例id/场景用例id
    bugCount: number; // 缺陷数
    canEdit: boolean;
    bugList?: CaseBugItem[];
    linkBugPermission?: string[];
  }>();

  const emit = defineEmits<{
    (e: 'associated'): void;
    (e: 'create'): void;
    (e: 'loadList'): void;
  }>();

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'newBug':
        emit('create');
        break;
      default:
        emit('associated');
        break;
    }
  }

  function loadList() {
    emit('loadList');
  }
</script>

<style scoped lang="less">
  :deep(.arco-btn-outline--secondary) {
    &:hover {
      border-color: rgb(var(--primary-5)) !important;
    }
  }
</style>
