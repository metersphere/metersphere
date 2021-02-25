<template>
  <div>
    <api-document-item :pageHeaderHeight="pageHeaderHeight" :project-id="projectId" :module-ids="moduleIds" :document-id="documentId" ref="apiDocumentItem"/>
<!--    <test-scroll></test-scroll>-->
  </div>

</template>

<script>

import ApiDocumentItem from "@/business/components/api/definition/components/document/ApiDocumentItem";
import TestScroll from "@/business/components/api/definition/components/document/TestScroll";

export default {
  name: "ApiDocumentsPage",
  components: {
    TestScroll,
    ApiDocumentItem,
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
      if(this.$refs.apiDocumentItem){
        this.$refs.apiDocumentItem.initApiDocSimpleList();
      }
    }
  },
}
</script>

<style scoped>

</style>
