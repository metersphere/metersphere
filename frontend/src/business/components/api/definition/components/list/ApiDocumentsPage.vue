<template>
  <div>
<!--      <api-document-item :project-id="projectId" :module-ids="moduleIds"/>-->
    <api-document-anchor :project-id="projectId" :module-ids="moduleIds"></api-document-anchor>
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
    }
  },
  props: {
    projectId:String,
    moduleIds:Array,
    activeDom:String,
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
