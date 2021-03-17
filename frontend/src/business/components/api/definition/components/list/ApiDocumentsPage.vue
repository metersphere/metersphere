<template>
  <div>
      <api-document-item :project-id="projectId" :module-ids="moduleIds"/>
  </div>
</template>

<script>

import ApiDocumentItem from "@/business/components/api/definition/components/document/ApiDocumentItem";

export default {
  name: "ApiDocumentsPage",
  components: {
    ApiDocumentItem,
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
