<template>
  <a-modal
    v-model:visible="showModalVisible"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    @close="handleMoveCaseModalCancel"
  >
    <template #title>
      <div class="flex w-full items-center justify-between">
        <div>
          {{ props.mode === 'move' ? t('common.batchMove') : t('common.batchCopy') }}
          <span class="ml-[4px] text-[var(--color-text-4)]">
            {{ t('common.selectedCount', { count: props.currentSelectCount }) }}
          </span>
        </div>
      </div>
    </template>
    <div v-if="props.type === testPlanTypeEnum.TEST_PLAN" class="mb-[16px] flex items-center">
      <span class="mr-2 text-[var(--color-text-1)]"
        >{{ props.mode === 'move' ? t('msTable.batch.moveTo') : t('msTable.batch.copyTo') }}:
      </span>
      <a-radio-group v-model="form.moveType" class="file-show-type mr-2">
        <a-radio value="MODULE" class="show-type-icon p-[2px]">{{ t('testPlan.testPlanGroup.module') }}</a-radio>
        <a-radio value="GROUP" class="show-type-icon p-[2px]">{{ t('testPlan.testPlanIndex.testPlanGroup') }}</a-radio>
      </a-radio-group>
    </div>
    <a-form
      v-if="form.moveType === 'GROUP' && props.type === testPlanTypeEnum.TEST_PLAN"
      ref="formRef"
      :model="form"
      layout="vertical"
      class="flex items-center"
    >
      <a-form-item
        :rules="[{ required: true, message: t('testPlan.testPlanGroup.selectTestPlanGroupPlaceHolder') }]"
        field="targetId"
        :label="t('testPlan.testPlanIndex.testPlanGroup')"
      >
        <a-select v-model="form.targetId" :placeholder="t('common.pleaseSelect')" allow-search>
          <a-option v-for="item of groupList" :key="item.id" :value="item.id">
            {{ item.name }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <div v-if="form.moveType === 'MODULE'">
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
        allow-clear
        :max-length="255"
        class="mb-4"
      />
      <a-spin class="min-h-[300px] w-full" :loading="loading">
        <MsTree
          v-model:focus-node-key="focusNodeKey"
          v-model:selected-keys="innerSelectedModuleKeys"
          :data="treeData"
          :keyword="moduleKeyword"
          :default-expand-all="props.isExpandAll"
          :expand-all="isExpandAll"
          :empty-text="t(props.emptyText)"
          :draggable="false"
          :virtual-list-props="virtualListProps"
          :field-names="{
            title: 'name',
            key: 'id',
            children: 'children',
            count: 'count',
          }"
          block-node
          title-tooltip-position="top"
          @select="nodeSelect"
        >
          <template #title="nodeData">
            <div class="inline-flex w-full">
              <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            </div>
          </template>
        </MsTree>
      </a-spin>
    </div>
    <template #footer>
      <a-button type="secondary" @click="handleMoveCaseModalCancel">{{ t('common.cancel') }}</a-button>
      <a-button
        class="ml-[12px]"
        type="primary"
        :loading="props.okLoading"
        :disabled="innerSelectedModuleKeys.length === 0 && form.moveType === 'MODULE'"
        @click="handleCaseMoveOrCopy"
      >
        {{ props.mode === 'move' ? t('common.move') : t('common.copy') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { SelectOptionData } from '@arco-design/web-vue';

  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getPlanGroupOptions } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { mapTree } from '@/utils';

  import type { TableQueryParams } from '@/models/common';
  import { ModuleTreeNode } from '@/models/common';
  import type { moduleForm } from '@/models/testPlan/testPlan';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const appStore = useAppStore();
  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      mode: 'move' | 'copy';
      currentSelectCount: number;
      visible: boolean;
      isExpandAll?: boolean;
      getModuleTreeApi: (params: TableQueryParams) => Promise<ModuleTreeNode[]>; // 模块树接口
      selectedNodeKeys: (string | number)[];
      okLoading: boolean;
      emptyText?: string;
      type: keyof typeof testPlanTypeEnum;
    }>(),
    {
      isExpandAll: false,
      emptyText: 'common.noData',
    }
  );

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:selectedNodeKeys', val: string[]): void;
    (e: 'save', form: moduleForm): void;
  }>();

  const showModalVisible = useVModel(props, 'visible', emit);
  const innerSelectedModuleKeys = useVModel(props, 'selectedNodeKeys', emit);

  const moduleKeyword = ref<string>('');

  const form = ref<moduleForm>({
    moveType: 'MODULE',
    targetId: '',
  });

  const groupList = ref<SelectOptionData>([]);

  const focusNodeKey = ref<string>('');
  const formRef = ref<FormInstance | null>(null);
  // 批量移动和复制
  async function handleCaseMoveOrCopy() {
    if (form.value.moveType === 'GROUP') {
      formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
        if (!errors) {
          emit('save', form.value);
        }
      });
    } else {
      emit('save', form.value);
    }
  }

  function handleMoveCaseModalCancel() {
    showModalVisible.value = false;
    innerSelectedModuleKeys.value = [];
    moduleKeyword.value = '';
    form.value = {
      moveType: 'MODULE',
      targetId: '',
    };
  }

  const loading = ref<boolean>(false);

  const treeData = ref<ModuleTreeNode[]>([]);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await props.getModuleTreeApi({ projectId: appStore.currentProjectId });
      treeData.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: true,
          draggable: false,
          disabled: false,
        };
      });
      if (isSetDefaultKey) {
        innerSelectedModuleKeys.value = [treeData.value[0].id];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const virtualListProps = computed(() => {
    return {
      height: 'calc(60vh - 190px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15,
    };
  });

  // 节点选中事件
  const nodeSelect = (selectedKeys: (string | number)[], node: MsTreeNodeData) => {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    innerSelectedModuleKeys.value = selectedKeys;
  };

  async function initGroupOptions() {
    try {
      groupList.value = await getPlanGroupOptions(appStore.currentProjectId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => showModalVisible.value,
    (val) => {
      if (val) {
        initModules();
        initGroupOptions();
      }
    }
  );
</script>

<style scoped lang="less"></style>
