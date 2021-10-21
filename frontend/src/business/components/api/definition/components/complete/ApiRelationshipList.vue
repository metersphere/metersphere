<template>
  <div>
   <ms-table
      v-loading="result.loading"
      :show-select-all="false"
      :data="data"
      :operators="operators"
      :enable-selection="false"
      ref="table"
      :screen-height="null"
      @refresh="getTableData">

      <ms-table-column
        prop="targetNum"
        :label="$t('commons.id')"
        min-width="80"/>

      <ms-table-column
        prop="targetName"
        :label="$t('commons.name')"
        min-width="120"/>

      <ms-table-column
        prop="creator"
        :label="$t('commons.create_user')"
        min-width="120">
      </ms-table-column>

    </ms-table>

    <api-relationship-relevance
      :api-definition-id="apiDefinitionId"
      @refresh="getTableData"
      :relationship-type="relationshipType"
      ref="testCaseRelevance"/>

  </div>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import RelationshipFunctionalRelevance
  from "@/business/components/track/case/components/RelationshipFunctionalRelevance";
import {getRelationshipApi} from "@/network/api";
import ApiRelationshipRelevance
  from "@/business/components/api/definition/components/complete/ApiRelationshipRelevance";
export default {
  name: "ApiRelationshipList",
  components: {ApiRelationshipRelevance, RelationshipFunctionalRelevance, MsTableSearchBar, MsTableColumn, MsTable},
  data() {
    return {
      result: {},
      data: [],
      operators: [
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_API_DEFINITION:READ+EDIT_API']
        }
      ],
      condition: {},
      options: [],
      value: ''
    }
  },
  props: {
    apiDefinitionId: String,
    readOnly: Boolean,
    relationshipType: String,
  },
  methods: {
    getTableData() {
      getRelationshipApi(this.apiDefinitionId, this.relationshipType, (data) => {
        this.data = data;
      });
    },
    openRelevance() {
      this.$refs.testCaseRelevance.open();
    },
    handleDelete(item) {
      this.$emit('deleteRelationship', item.sourceId, item.targetId);
    },
  }
}
</script>

<style scoped>
</style>
