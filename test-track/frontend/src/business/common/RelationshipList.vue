<template>
  <el-main>
    <span>{{ title }}</span>
    <el-button class="add-btn" v-permission="['PROJECT_TRACK_CASE:READ+EDIT']"
               :disabled="readOnly || !resourceId" type="primary" size="mini" @click="openRelevance">
      {{ $t($t('commons.add')) }}
    </el-button>

    <test-case-relationship-list
      v-if="resourceType === 'TEST_CASE'"
      :case-id="resourceId"
      :read-only="readOnly"
      :relationship-type="relationshipType"
      :version-enable="versionEnable"
      @setCount="setCount"
      @deleteRelationship="handleDelete"
      ref="testCaseRelationshipList"/>

  </el-main>
</template>

<script>
import TestCaseRelationshipList from "../case/components/TestCaseRelationshipList";
import {deleteRelationshipEdge} from "@/business/utils/sdk-utils";

export default {
  name: "RelationshipList",
  components: {TestCaseRelationshipList},
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
    resourceType: String,
    versionEnable: Boolean,
  },
  methods: {
    getTableData() {
      this.$refs.testCaseRelationshipList.getTableData();
    },
    openRelevance() {
      this.$refs.testCaseRelationshipList.openRelevance();
    },
    handleDelete(sourceId, targetId) {
      deleteRelationshipEdge(sourceId, targetId)
        .then(() => {
          this.getTableData();
          this.$success(this.$t('commons.delete_success'));
        });
    },
    setCount(count) {
      this.$emit('setCount', count);
    }
  }
}
</script>

<style scoped>
.add-btn {
  margin-left: 20px;
}
</style>
