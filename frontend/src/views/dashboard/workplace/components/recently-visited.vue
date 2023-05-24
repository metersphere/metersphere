<template>
  <a-card
    class="general-card"
    :title="$t('workplace.recently.visited')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ paddingTop: '26px' }"
  >
    <div style="margin-bottom: -1rem">
      <a-row :gutter="8">
        <a-col v-for="link in links" :key="link.text" :span="8" class="wrapper">
          <div class="icon">
            <component :is="link.icon" />
          </div>
          <a-typography-paragraph class="text">
            {{ $t(link.text) }}
          </a-typography-paragraph>
        </a-col>
      </a-row>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  const links = [
    {
      text: 'workplace.contentManagement',
      icon: 'icon-storage',
    },
    {
      text: 'workplace.contentStatistical',
      icon: 'icon-file',
    },
    {
      text: 'workplace.advanced',
      icon: 'icon-settings',
    },
  ];
</script>

<style lang="less" scoped>
  :deep(.arco-card-header-title) {
    line-height: inherit;
  }
</style>
