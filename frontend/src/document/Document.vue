<template>
  <div>
    <api-document-anchor :share-page="sharePage" :pageHeaderHeight="pageHeaderHeight" :project-id="projectId" :module-ids="moduleIds" :document-id="documentId" ref="apiDocumentAnchor"/>
  </div>

</template>

<script>

import ApiDocumentAnchor from "@/business/components/api/definition/components/document/ApiDocumentAnchor";

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
    getUrlParam(){
      let herfUrl = window.location.href;
      if(herfUrl.indexOf("?") > 0){
        let paramArr = herfUrl.split("?");
        if(paramArr.length > 1){
          let documentId = paramArr[1];
          if(documentId.indexOf("#") > 0){
            documentId = documentId.split("#")[0];
          }
          this.documentId = documentId;
        }
      }
    },
    selectDocumentInfo(){
      this.getUrlParam();
      if(this.$refs.apiDocumentAnchor){
        this.$refs.apiDocumentAnchor.initApiDocSimpleList();
      }
    }
  },
}
</script>

<style scoped>

</style>
