<template>
  <ms-container>
    <ms-main-container>
      <div>
        <ms-jar-config-list  ref="jarConfigList" class="file-manage-jar"/>
      </div>

      <div style="margin-top: 6px;">
        <resource-manage ref="resourceManage" class="file-manage-resource"/>
      </div>
    </ms-main-container>
  </ms-container>
</template>

<script>

import ResourceManage from "@/business/components/project/menu/file/ResourceManage";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsJarConfigList from "@/business/components/api/test/components/jar/JarConfigList";

export default {
  name: "FileManager",
  components: {
    MsMainContainer,
    MsContainer,
    ResourceManage,
    MsJarConfigList,
  },
  data() {
    return {

    }
  },
}
</script>

<style scoped>
.file-manage-jar {
  height: calc(50vh - 60px);
}

.file-manage-resource {
  height: calc(50vh - 44px);
}
</style>
