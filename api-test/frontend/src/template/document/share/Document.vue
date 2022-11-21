<template>
  <div>
    <api-document-anchor
      :share-page="sharePage"
      :pageHeaderHeight="pageHeaderHeight"
      :project-id="projectId"
      :module-ids="moduleIds"
      :document-id="documentId"
      ref="documentAnchor"
    />
  </div>
</template>

<script>
import ApiDocumentAnchor from "@/business/definition/components/document/ApiDocumentAnchor";
import { getShareId } from "@/api/share";

export default {
  name: "Document",
  components: {
    ApiDocumentAnchor,
  },
  data() {
    return {
      documentId: "",
      projectId: "",
      pageHeaderHeight: 100,
      sharePage: true,
      moduleIds: [],
    };
  },
  props: {
    activeDom: String,
    isApiListEnable: {
      type: Boolean,
      default: false,
    },
  },
  created() {
    this.selectDocumentInfo();
  },
  watch: {},
  computed: {},
  methods: {
    selectDocumentInfo() {
      this.documentId = getShareId();
      if (this.$refs.documentAnchor) {
        this.$refs.documentAnchor.initApiDocSimpleList();
      }
    },
  },
};
</script>

<style scoped></style>
