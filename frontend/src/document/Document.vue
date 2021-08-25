<template>
  <div>
    <api-document-anchor :share-page="sharePage" :pageHeaderHeight="pageHeaderHeight" :project-id="projectId" :module-ids="moduleIds" :document-id="documentId" ref="apiDocumentAnchor"/>
  </div>

</template>

<script>

import ApiDocumentAnchor from "@/business/components/api/definition/components/document/ApiDocumentAnchor";
import {getShareId} from "@/common/js/utils";

export default {
  name: "ApiDocumentsPage",
  components: {
    ApiDocumentAnchor,
  },
  data() {
    return {
      documentId:"",
      projectId:"",
      pageHeaderHeight:100,
      sharePage:true,
      moduleIds:[],
    }
  },
  props: {
    activeDom:String,
    isApiListEnable: {
      type: Boolean,
      default: false,
    },
  },
  created: function () {
    this.selectDocumentInfo();
  },
  watch: {
  },
  computed: {

  },
  methods: {
    selectDocumentInfo(){
      this.documentId = getShareId();
      if(this.$refs.apiDocumentAnchor){
        this.$refs.apiDocumentAnchor.initApiDocSimpleList();
      }
    }
  },
}
</script>

<style scoped>

</style>
