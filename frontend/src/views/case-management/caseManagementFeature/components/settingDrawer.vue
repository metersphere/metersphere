<template>
  <MsDrawer
    v-model:visible="showSettingVisible"
    :mask="false"
    :title="t('caseManagement.featureCase.detailDisplaySetting')"
    :width="480"
    unmount-on-close
    :footer="false"
  >
    <div class="header mb-1 flex h-[22px] items-center justify-between">
      <div class="flex items-center text-[var(--color-text-4)]"
        >{{ t('caseManagement.featureCase.showSetting') }}

        <a-tooltip>
          <template #content>
            <div>{{ t('caseManagement.featureCase.tabShowSetting') }} </div>
            <div>{{ t('caseManagement.featureCase.closeModuleTab') }}</div>
            <div>{{ t('caseManagement.featureCase.enableModuleTab') }}</div>
          </template>
          <span class="inline-block align-middle">
            <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
          /></span>
        </a-tooltip>
      </div>
      <div class="cursor-pointer text-[rgb(var(--primary-5))]" @click="setDefault"
        >{{ t('caseManagement.featureCase.recoverDefault') }}
      </div>
    </div>
    <div>
      <div class="itemTab">
        <span>{{ t('caseManagement.featureCase.detail') }}</span>
        <a-switch v-model="detailEnable" size="small" :disabled="true" />
      </div>
      <a-divider orientation="center" class="non-sort"
        ><span class="one-line-text text-xs text-[var(--color-text-4)]">{{
          t('caseManagement.featureCase.nonClosableTab')
        }}</span></a-divider
      >
      <div v-for="item of tabSettingList" :key="item.key" class="itemTab">
        <span>{{ item.title }}</span>
        <a-switch v-model="item.enable" size="small" />
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import type { TabItemType } from '@/models/caseManagement/featureCase';

  const { t } = useI18n();

  const featureCaseStore = useFeatureCaseStore();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showSettingVisible = ref<boolean>(false);
  const detailEnable = ref<boolean>(true);

  const tabDefaultSettingList = ref<TabItemType[]>([
    {
      key: 'case',
      title: t('caseManagement.featureCase.case'),
      enable: true,
    },
    {
      key: 'requirement',
      title: t('caseManagement.featureCase.requirement'),
      enable: true,
    },
    {
      key: 'bug',
      title: t('caseManagement.featureCase.bug'),
      enable: true,
    },
    {
      key: 'dependency',
      title: t('caseManagement.featureCase.dependency'),
      enable: true,
    },
    {
      key: 'caseReview',
      title: t('caseManagement.featureCase.caseReview'),
      enable: true,
    },
    {
      key: 'testPlan',
      title: t('caseManagement.featureCase.testPlan'),
      enable: true,
    },
    {
      key: 'comments',
      title: t('caseManagement.featureCase.comments'),
      enable: true,
    },
    {
      key: 'changeHistory',
      title: t('caseManagement.featureCase.changeHistory'),
      enable: true,
    },
  ]);

  const tabList = computed(() => {
    return featureCaseStore.tabSettingList;
  });

  const tabSettingList = ref([...tabList.value]);

  function setDefault() {
    tabSettingList.value = tabSettingList.value.map((item: any) => {
      return {
        ...item,
        enable: true,
      };
    });
  }

  watch(
    () => props.visible,
    (val) => {
      showSettingVisible.value = val;
    }
  );

  watch(
    () => showSettingVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  watch(
    () => tabSettingList.value,
    (val) => {
      featureCaseStore.setTab(val as TabItemType[]);
    },
    {
      deep: true,
    }
  );

  onMounted(() => {
    if (tabList.value.length < 1) {
      featureCaseStore.setTab(tabDefaultSettingList.value);
    }
  });
</script>

<style scoped lang="less">
  .itemTab {
    height: 38px;
    @apply flex items-center justify-between p-3;
  }
</style>
