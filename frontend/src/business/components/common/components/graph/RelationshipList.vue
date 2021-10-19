<template>
  <el-main>
    <span>{{ title }}</span>
    <el-button class="add-btn"
               :disabled="readOnly" type="primary" size="mini" @click="openRelevance">{{ $t('添加') }}</el-button>

    <test-case-relationship-list
      v-if="resourceType === 'TEST_CASE'"
      :case-id="resourceId"
      :relationship-type="relationshipType"
      @deleteRelationship="handleDelete"
      ref="testCaseRelationshipList"/>

    <api-relationship-list
      v-if="resourceType === 'API'"
      :api-definition-id="resourceId"
      :relationship-type="relationshipType"
      @deleteRelationship="handleDelete"
      ref="testCaseRelationshipList"/>

  </el-main>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import RelationshipFunctionalRelevance
  from "@/business/components/track/case/components/RelationshipFunctionalRelevance";
import {deleteRelationshipEdge} from "@/network/relationship-edge";
import TestCaseRelationshipList from "@/business/components/track/case/components/TestCaseRelationshipList";
import ApiRelationshipList from "@/business/components/api/definition/components/complete/ApiRelationshipList";
export default {
  name: "RelationshipList",
  components: {
    ApiRelationshipList,
    TestCaseRelationshipList, RelationshipFunctionalRelevance, MsTableSearchBar, MsTableColumn, MsTable},
  data() {
    return {
      result: {},
      data: [],
      condition: {},
      options: [],
      value: ''
    }
  },
  props: {
    resourceId: String,
    readOnly: Boolean,
    relationshipType: String,
    title: String,
    resourceType: String
  },
  methods: {
    getTableData() {
      this.$refs.testCaseRelationshipList.getTableData();
    },
    openRelevance() {
      this.$refs.testCaseRelationshipList.openRelevance();
    },
    handleDelete(sourceId, targetId) {
      deleteRelationshipEdge(sourceId, targetId, () => {
        this.getTableData();
        this.$success(this.$t('commons.delete_success'));
      });
    },
  }
}
</script>

<style scoped>
.add-btn {
  margin-left: 20px;
}
</style>
