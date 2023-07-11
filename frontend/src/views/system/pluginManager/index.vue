/* stylelint-disable order/properties-order */
<template>
  <div>
    <MSAlert :closable="true" type="primary" :banner="false">
      <template #content>
        <div>
          MeterSphereV2.10LTS版本支持DevOps、API导入、请求、项目管理、协议类型的插件，具体支持插件请
          <a class="mx-1" href="javascript:;">查看表格</a>更多开源插件，请在此下载
          <a class="mx-1" href="javascript:;">去下载 </a>
        </div>
      </template>
    </MSAlert>
    <div class="mt-4">
      <pluginTable />
    </div>
  </div>
</template>

<script setup lang="ts">
  import MSAlert from '@/components/pure/ms-alert/index.vue';
  import pluginTable from './components/pluginTable.vue';
</script>

<style scoped>
  a {
    color: rgb(var(--primary-5));
  }
</style>
