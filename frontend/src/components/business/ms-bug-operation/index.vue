<template>
  <div class="flex items-center" @click="selectedStyle">
    <BugCountPopover
      :case-type="props.caseType"
      :bug-list="bugList || []"
      :bug-count="bugCount"
      :can-edit="props.canEdit"
      @load-list="loadList"
    />
    <a-dropdown
      v-if="hasAllPermission(['PROJECT_BUG:READ', ...(props.permission || [])])"
      position="bl"
      @select="handleSelect"
      @popup-visible-change="popupVisibleChange"
    >
      <a-button
        v-permission="['PROJECT_BUG:READ']"
        :class="`${isSelected ? 'selected-class' : 'operation-button'} arco-btn-outline--secondary ml-[8px] !p-[4px]`"
        type="outline"
        size="small"
      >
        <icon-plus class="text-[14px]" />
      </a-button>

      <template #content>
        <a-doption v-if="hasAnyPermission(['PROJECT_BUG:READ']) && props.existedDefect" value="linkBug">
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
  import BugCountPopover from './bugCountPopover.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import type { CaseBugItem } from '@/models/testPlan/testPlan';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  const { t } = useI18n();

  const props = defineProps<{
    resourceId: string; // 资源id: 功能用例id/接口用例id/场景用例id
    bugCount: number; // 缺陷数
    existedDefect: number; // 已经存在缺陷数
    caseType: CaseLinkEnum; // 用例类型
    canEdit: boolean;
    bugList?: CaseBugItem[];
    permission?: string[];
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

  const isSelected = ref<boolean>(false);
  function selectedStyle() {
    isSelected.value = true;
  }

  function popupVisibleChange(val: boolean) {
    if (!val) {
      isSelected.value = false;
    }
  }

  function loadList() {
    emit('loadList');
  }
</script>

<style scoped lang="less">
  :deep(.arco-btn-outline--secondary) {
    border-color: var(--color-text-n8) !important;
  }
  .selected-class {
    opacity: 1;
    &.arco-btn-outline--secondary {
      border-color: rgb(var(--primary-5)) !important;
    }
  }
</style>
