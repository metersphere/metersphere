<template>
  <div>
    <el-main>
      <api-document-anchor :is-template="true" />
    </el-main>
  </div>
</template>

<script>
import ApiDocumentAnchor from '@/business/definition/components/document/ApiDocumentAnchor';
export default {
  name: 'DocumentAnchorTemplate',
  components: { ApiDocumentAnchor },
  data() {},
};
</script>

<style scoped></style>
