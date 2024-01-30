<template>
  <MsCard has-breadcrumb simple>
    <FieldSetting
      mode="project"
      :delete-permission="['PROJECT_TEMPLATE:READ+DELETE']"
      :update-permission="['PROJECT_TEMPLATE:READ+UPDATE']"
      :create-permission="['PROJECT_TEMPLATE:READ+ADD']"
  /></MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-模板-字段设置-首页
   */
  import { useRoute } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import FieldSetting from '@/views/setting/organization/template/components/fieldSetting.vue';

  import { useAppStore } from '@/store';

  import { getCardList } from '@/views/setting/organization/template/components/fieldSetting';

  const route = useRoute();
  const appStore = useAppStore();

  // 更新面包屑
  const updateBreadcrumbList = () => {
    const { breadcrumbList } = appStore;
    const breadTitle = getCardList('project').find((item: any) => item.key === route.query.type);
    if (breadTitle) {
      breadcrumbList[0].locale = breadTitle.name;
      appStore.setBreadcrumbList(breadcrumbList);
    }
  };

  onMounted(() => {
    updateBreadcrumbList();
  });
</script>

<style scoped></style>
