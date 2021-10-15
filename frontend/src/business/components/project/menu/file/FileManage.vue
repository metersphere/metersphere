<template>
  <ms-container>
    <ms-main-container>
      <jar-manage ref="jarManage" class="file-manage-item"/>
      <div style="margin-top: 6px;">
        <resource-manage ref="resourceManage" class="file-manage-item"/>
      </div>
    </ms-main-container>
  </ms-container>
</template>

<script>

import JarManage from "@/business/components/project/menu/file/JarManage";
import ResourceManage from "@/business/components/project/menu/file/ResourceManage";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

export default {
  name: "FileManager",
  components: {
    MsMainContainer,
    MsContainer,
    ResourceManage,
    JarManage,
  },
  data() {
    return {

    }
  },
}
</script>

<style scoped>
.file-manage-item {
  height: calc(50vh - 60px);
}
</style>
