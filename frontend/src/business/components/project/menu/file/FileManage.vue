<template>
  <ms-container>
    <ms-aside-container>
      <ms-file-module @nodeSelectEvent="change" @myFile="myFile" @setNodeTree="setNodeTree" ref="module"/>
    </ms-aside-container>
    <ms-main-container>
      <resource-manage ref="resourceManage" :moduleId="moduleId" :module-type="moduleType" :nodeTree="nodeTree"
                       @refreshModule="refreshModule"/>
    </ms-main-container>
  </ms-container>
</template>

<script>

import ResourceManage from "@/business/components/project/menu/file/list/FileMetadataList";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsJarConfigList from "@/business/components/api/test/components/jar/JarConfigList";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import MsFileModule from "./module/FileModule";

export default {
  name: "FileManager",
  components: {
    MsFileModule,
    MsMainContainer,
    MsContainer,
    ResourceManage,
    MsJarConfigList,
    MsAsideContainer,
  },
  data() {
    return {
      moduleId: "",
      moduleType: "module",
      nodeTree: []
    }
  },
  methods: {
    change(node, nodeIds, pNodes) {
      this.moduleId = node.data.id;
      this.moduleType = node.data.moduleType;
      this.$refs.resourceManage.moduleChange(nodeIds);
    },
    myFile() {
      this.$refs.resourceManage.myFile();
    },
    setNodeTree(data) {
      this.nodeTree = data;
    },
    refreshModule() {
      this.$refs.module.refresh();
    }
  }
}
</script>

<style scoped>
.file-manage-jar {
  height: calc(50vh - 60px);
}

.file-manage-resource {
  height: calc(50vh - 4px);
}
</style>
