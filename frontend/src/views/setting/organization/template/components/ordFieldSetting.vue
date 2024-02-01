<template>
  <MsCard has-breadcrumb simple>
    <FieldSetting
      mode="organization"
      :delete-permission="['ORGANIZATION_TEMPLATE:READ+DELETE']"
      :update-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']"
      :create-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"
  /></MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-字段列表首页
   */
  import { useRoute } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import FieldSetting from '@/views/setting/organization/template/components/fieldSetting.vue';

  import { useAppStore } from '@/store';

  import { getCardList } from '@/views/setting/organization/template/components/fieldSetting';

  const route = useRoute();
  const appStore = useAppStore();

  // 更新面包屑根据不同的模板
  const updateBreadcrumbList = () => {
    const { breadcrumbList } = appStore;
    const breadTitle = getCardList('organization').find((item: any) => item.key === route.query.type);
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
