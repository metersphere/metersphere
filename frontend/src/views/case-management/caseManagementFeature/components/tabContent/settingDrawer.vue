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
        >{{ t('caseManagement.featureCase.detailDisplaySetting') }}

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
        <a-switch v-model="detailEnable" size="small" :disabled="true" type="line" />
      </div>
      <a-divider orientation="center" class="non-sort"
        ><span class="one-line-text text-xs text-[var(--color-text-4)]">{{
          t('caseManagement.featureCase.nonClosableTab')
        }}</span></a-divider
      >
      <div v-for="item of tabSettingList" :key="item.key" class="itemTab">
        <span>{{ t(item.title) }}</span>
        <a-switch v-model="item.enable" size="small" type="line" />
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { postTabletList } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useLicenseStore from '@/store/modules/setting/license';

  import type { TabItemType } from '@/models/caseManagement/featureCase';

  const licenseStore = useLicenseStore();
  const { t } = useI18n();

  const featureCaseStore = useFeatureCaseStore();

  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);
  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showSettingVisible = ref<boolean>(false);
  const detailEnable = ref<boolean>(true);

  const moduleTab = ref<Record<string, TabItemType[]>>({
    bugManagement: [
      {
        key: 'requirement',
        title: 'caseManagement.featureCase.requirement',
        enable: true,
        total: 0,
      },
      {
        key: 'bug',
        title: 'caseManagement.featureCase.bug',
        enable: true,
        total: 0,
      },
    ],
    // testPlan: [
    //   {
    //     key: 'testPlan',
    //     title: 'caseManagement.featureCase.testPlan',
    //     enable: true,
    //   },
    // ],
  });

  let buggerTab: TabItemType[] = [];
  // let testPlanTab: TabItemType[] = [];

  function getTabList() {
    if (licenseStore.hasLicense()) {
      return [
        {
          key: 'changeHistory',
          title: 'caseManagement.featureCase.changeHistory',
          enable: true,
          total: 0,
        },
      ];
    }
    return [];
  }

  const tabDefaultSettingList = ref<TabItemType[]>([
    {
      key: 'case',
      title: 'caseManagement.featureCase.case',
      enable: true,
      total: 0,
    },
    {
      key: 'dependency',
      title: 'caseManagement.featureCase.dependency',
      enable: true,
      total: 0,
    },
    {
      key: 'caseReview',
      title: 'caseManagement.featureCase.caseReview',
      enable: true,
      total: 0,
    },
    {
      key: 'comments',
      title: 'caseManagement.featureCase.comments',
      enable: true,
      total: 0,
    },
    // TOTO Xpack 不上
    // ...getTabList(),
  ]);
  async function getTabModule() {
    buggerTab = [];
    // testPlanTab = [];
    const result = await postTabletList({ projectId: currentProjectId.value });
    // TODO 第一版不展示测试计划
    // const enableModuleArr = result.filter((item: any) => item.module === 'testPlan' || item.module === 'bugManagement');
    const enableModuleArr = result.filter((item: any) => item.module === 'bugManagement');
    enableModuleArr.forEach((item) => {
      if (item.module === 'bugManagement') {
        buggerTab.push(...moduleTab.value[item.module]);
      }
      // else if (item.module === 'testPlan') {
      //   testPlanTab.push(...moduleTab.value[item.module]);
      // }
    });
    const newTabDefaultSettingList = [
      tabDefaultSettingList.value[0],
      ...buggerTab,
      ...tabDefaultSettingList.value.slice(1, -2),
      // ...testPlanTab,
      tabDefaultSettingList.value[tabDefaultSettingList.value.length - 2],
      tabDefaultSettingList.value[tabDefaultSettingList.value.length - 1],
    ];
    featureCaseStore.setTab(newTabDefaultSettingList);
  }

  const tabList = computed(() => featureCaseStore.tabSettingList);

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

  defineExpose({
    getTabModule,
  });
</script>

<style scoped lang="less">
  .itemTab {
    height: 38px;
    @apply flex items-center justify-between p-3;
  }
</style>
