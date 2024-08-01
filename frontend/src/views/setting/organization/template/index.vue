<template>
  <MsCard simple>
    <div style="display: flex !important" class="flex h-[100%] flex-col overflow-hidden">
      <a-alert v-if="isShowTip" class="mb-4 py-3">
        <div class="flex items-start justify-between">
          <span class="w-[80%]">{{ t('system.orgTemplate.templateDescription') }}</span>
          <span class="cursor-pointer text-[var(--color-text-2)]" @click="noRemindHandler">{{
            t('system.orgTemplate.noReminders')
          }}</span>
        </div>
      </a-alert>
      <MsCardList
        mode="static"
        :card-min-width="360"
        class="flex-1"
        :shadow-limit="50"
        :list="cardList"
        :is-proportional="false"
        :gap="16"
        padding-bottom-space="16px"
      >
        <template #item="{ item, index }">
          <TemplateItem
            :card-item="item"
            :index="index"
            mode="organization"
            @field-setting="fieldSetting"
            @template-management="templateManagement"
            @workflow-setup="workflowSetup"
            @update-state="updateState"
          />
        </template>
      </MsCardList>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统设置--组织--模版
   */
  import { useRoute, useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCardList from '@/components/business/ms-card-list/index.vue';
  import TemplateItem from './components/templateItem.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useTemplateStore from '@/store/modules/setting/template';

  import { SettingRouteEnum } from '@/enums/routeEnum';

  import { getCardList } from './components/fieldSetting';

  const templateStore = useTemplateStore();
  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();
  const visitedKey = 'notRemind';
  const { addVisited } = useVisit(visitedKey);
  const { getIsVisited } = useVisit(visitedKey);
  const isShowTip = ref<boolean>(true);
  const noRemindHandler = () => {
    isShowTip.value = false;
    addVisited();
  };

  // 不再提醒
  const doCheckIsTip = () => {
    isShowTip.value = !getIsVisited();
  };
  onBeforeMount(() => {
    templateStore.getStatus();
  });

  // 字段设置
  const fieldSetting = (key: string) => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
      query: {
        ...route.query,
        type: key,
      },
    });
  };

  // 模板管理
  const templateManagement = (key: string) => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
      query: {
        ...route.query,
        type: key,
      },
    });
  };

  // 工作流
  const workflowSetup = (key: string) => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW,
      query: {
        ...route.query,
        type: key,
      },
    });
  };

  const cardList = ref<Record<string, any>[]>([]);

  // 更新状态列表
  const updateState = () => {
    cardList.value = [...getCardList('organization')];
  };

  onBeforeMount(() => {
    doCheckIsTip();
    updateState();
  });
</script>

<style scoped lang="less"></style>
