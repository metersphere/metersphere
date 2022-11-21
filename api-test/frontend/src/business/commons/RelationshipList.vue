<template>
  <el-main>
    <span>{{ title }}</span>
    <el-button
      class="add-btn"
      v-permission="
        relationshipType == 'TEST_CASE'
          ? ['PROJECT_TRACK_CASE:READ+EDIT']
          : ['PROJECT_API_DEFINITION:READ+EDIT_API']
      "
      :disabled="readOnly || !resourceId"
      type="primary"
      size="mini"
      @click="openRelevance"
    >
      {{ $t($t('commons.add')) }}
    </el-button>

    <api-relationship-list
      v-if="resourceType === 'API'"
      :read-only="readOnly"
      :api-definition-id="resourceId"
      :relationship-type="relationshipType"
      :version-enable="versionEnable"
      @setCount="setCount"
      @deleteRelationship="handleDelete"
      ref="testCaseRelationshipList"
    />
  </el-main>
</template>

<script>
import { deleteRelationshipEdge } from 'metersphere-frontend/src/api/relationship-edge';
import ApiRelationshipList from '@/business/definition/components/complete/ApiRelationshipList';

export default {
  name: 'RelationshipList',
  components: { ApiRelationshipList },
  data() {
    return {
      result: false,
      data: [],
      condition: {},
      options: [],
      value: '',
    };
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
      deleteRelationshipEdge(sourceId, targetId).then(() => {
        this.getTableData();
        this.$success(this.$t('commons.delete_success'));
      });
    },
    setCount(count) {
      this.$emit('setCount', count);
    },
  },
};
</script>

<style scoped>
.add-btn {
  margin-left: 20px;
}
</style>
