<template>
  <div>
<!--      <api-document-item :project-id="projectId" :module-ids="moduleIds"/>-->
    <api-document-anchor :is-share-page="isSharePage" :trash-enable="trashEnable" :project-id="projectId" :module-ids="moduleIds"></api-document-anchor>
  </div>
</template>

<script>

import ApiDocumentAnchor from "@/business/components/api/definition/components/document/ApiDocumentAnchor";

export default {
  name: "ApiDocumentsPage",
  components: {
    ApiDocumentAnchor
  },
  data() {
    return {
      isSharePage:false,
    }
  },
  props: {
    projectId:String,
    moduleIds:Array,
    activeDom:String,
    trashEnable: {
      type: Boolean,
      default: false,
    },
  },
  created: function () {
  },
  watch: {
  },
  computed: {

  },
  methods: {
  },
}
</script>

<style scoped>

</style>
