<template>
  <MsCard simple>
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
          mode="project"
          @field-setting="fieldSetting"
          @template-management="templateManagement"
          @workflow-setup="workflowSetup"
          @update-state="updateState"
        />
      </template>
    </MsCardList>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 项目设置--模板
   */

  import { useRoute, useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCardList from '@/components/business/ms-card-list/index.vue';
  import TemplateItem from '@/views/setting/organization/template/components/templateItem.vue';

  import useTemplateStore from '@/store/modules/setting/template';

  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  import { getCardList } from '@/views/setting/organization/template/components/fieldSetting';

  const router = useRouter();
  const route = useRoute();
  const templateStore = useTemplateStore();
  // 字段设置
  const fieldSetting = (key: string) => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
      query: {
        ...route.query,
        type: key,
      },
    });
  };

  // 模板管理
  const templateManagement = (key: string) => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
      query: {
        ...route.query,
        type: key,
      },
    });
  };

  // 工作流
  const workflowSetup = (key: string) => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_WORKFLOW,
      query: {
        ...route.query,
        type: key,
      },
    });
  };

  const cardList = ref<Record<string, any>[]>([]);

  // 更新状态列表
  const updateState = () => {
    cardList.value = [...getCardList('project')];
  };
  onBeforeMount(() => {
    templateStore.getStatus();
    updateState();
  });
</script>

<style scoped lang="less"></style>
