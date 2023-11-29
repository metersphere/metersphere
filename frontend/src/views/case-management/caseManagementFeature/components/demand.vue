<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button type="primary" @click="associatedDemand"> 关联需求</a-button>
        <a-button class="mx-3" type="outline" @click="addDemand"> 添加需求</a-button>
      </div>

      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
    </div>
    <AssociatedDemandTable
      ref="demandRef"
      :fun-params="{ caseId: props.caseId, keyword }"
      @update="updateDemand"
    ></AssociatedDemandTable>
    <AddDemandModal v-model:visible="showAddModel" :case-id="props.caseId" :form="modelForm" @success="searchList()" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { debounce } from 'lodash-es';

  import AddDemandModal from './addDemandModal.vue';
  import AssociatedDemandTable from './associatedDemandTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { DemandItem } from '@/models/caseManagement/featureCase';

  const { t } = useI18n();

  const props = defineProps<{
    caseId: string;
  }>();

  const keyword = ref<string>('');
  const demandRef = ref();

  const searchList = debounce(() => {
    demandRef.value.initData();
  }, 100);

  const showAddModel = ref<boolean>(false);

  function addDemand() {
    showAddModel.value = true;
  }
  const modelForm = ref<DemandItem>({
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
  });

  // 更新需求
  function updateDemand(record: DemandItem) {
    showAddModel.value = true;
    modelForm.value = { ...record };
  }
  // 关联需求(暂无接口)
  function associatedDemand() {}
</script>

<style scoped></style>
