<template>
  <div>
    <api-document-anchor :pageHeaderHeight="pageHeaderHeight" :project-id="projectId" :module-ids="moduleIds" :document-id="documentId" ref="apiDocumentAnchor"/>
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
      let queryParams =this.$route.query;
      let documentIdParam = queryParams['documentId'];
      this.documentId = queryParams['documentId'];
      return documentIdParam;
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
