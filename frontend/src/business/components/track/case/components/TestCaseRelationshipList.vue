<template>
  <el-main>
    <span>{{ title }}</span>
    <el-button class="add-btn"
               :disabled="readOnly" type="primary" size="mini" @click="openRelevance">{{ $t('添加') }}</el-button>

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
        prop="targetCustomNum"
        v-if="isCustomNum"
        :label="$t('commons.id')"
        min-width="80"/>

      <ms-table-column
        prop="targetNum"
        v-else
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

    <relationship-functional-relevance
      :case-id="caseId"
      @refresh="getTableData"
      :relationship-type="relationshipType"
      ref="testCaseRelevance"/>
  </el-main>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import RelationshipFunctionalRelevance
  from "@/business/components/track/case/components/RelationshipFunctionalRelevance";
import {getRelationshipCase} from "@/network/testCase";
import {deleteRelationshipEdge} from "@/network/relationship-edge";
export default {
  name: "TestCaseRelationshipList",
  components: {RelationshipFunctionalRelevance, MsTableSearchBar, MsTableColumn, MsTable},
  data() {
    return {
      result: {},
      data: [],
      operators: [
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          // permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
        }
      ],
      condition: {},
      options: [],
      value: ''
    }
  },
  props: {
    tip: {
      type: String,
      default() {
        return this.$t('commons.search_by_name_or_id');
      }
    },
    caseId: String,
    readOnly: Boolean,
    relationshipType: String,
    title: String,
  },
  computed: {
    isCustomNum() {
      return this.$store.state.currentProjectIsCustomNum;
    },
  },
  methods: {
    getTableData() {
      getRelationshipCase(this.caseId, this.relationshipType, (data) => {
        this.data = data;
      });
    },
    openRelevance() {
      this.$refs.testCaseRelevance.open();
    },
    handleDelete(item) {
      deleteRelationshipEdge(item.sourceId, item.targetId, () => {
        this.getTableData();
        this.$success(this.$t('commons.delete_success'));
      });
    }
  }
}
</script>

<style scoped>
.add-btn {
  margin-left: 20px;
}

.el-main:nth-child(2) {
  margin-top: 20px;
}
</style>
