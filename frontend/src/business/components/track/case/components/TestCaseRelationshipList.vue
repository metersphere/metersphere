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
       min-width="100px"
       v-if="relationshipType === 'POST'"
       :label="$t('commons.relationship.type')">
       <template>
         <span>{{ $t('commons.relationship.current_case') }}</span>
         <span class="type-type">{{ $t('commons.relationship.after_finish') }}</span>
         <font-awesome-icon class="type-type" :icon="['fas', 'arrow-right']" size="lg"/>
       </template>
     </ms-table-column>


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
       v-if="versionEnable"
       prop="versionId"
       :label="$t('commons.version')"
       min-width="120px">
       <template v-slot:default="scope">
         <span>{{ scope.row.versionName }}</span>
       </template>
     </ms-table-column>

      <ms-table-column
        prop="creator"
        :label="$t('commons.create_user')"
        min-width="120">
      </ms-table-column>

     <ms-table-column
       prop="status"
       min-width="100px"
       :label="$t('api_test.definition.api_case_status')">
       <template slot-scope="{row}">
         {{$t(statusMap.get(row.status))}}
       </template>
     </ms-table-column>

     <ms-table-column
       min-width="100px"
       v-if="relationshipType === 'PRE'"
       :label="$t('commons.relationship.type')">
       <template>
         <span class="type-type">{{ $t('commons.relationship.after_finish') }}</span>
         <font-awesome-icon class="type-type" :icon="['fas', 'arrow-right']" size="lg"/>
         <span>{{ $t('commons.relationship.current_case') }}</span>
       </template>
     </ms-table-column>

    </ms-table>

    <relationship-functional-relevance
      :case-id="caseId"
      :version-enable="versionEnable"
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
import {getRelationshipCase} from "@/network/testCase";
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
          isDisable: this.readOnly,
          permissions: ['PROJECT_TRACK_CASE:READ+DELETE']
        }
      ],
      condition: {},
      options: [],
      statusMap: new Map,
      value: ''
    }
  },
  props: {
    caseId: String,
    readOnly: Boolean,
    relationshipType: String,
    versionEnable: Boolean,
  },
  computed: {
    isCustomNum() {
      let template = this.$store.state.testCaseTemplate;
      if (template && template.customFields) {
        template.customFields.forEach(item => {
          if (item.name === '用例状态') {
            for (let i = 0; i < item.options.length; i++) {
              this.statusMap.set(item.options[i].value, item.options[i].text);
            }
          }
        });
      }
      return this.$store.state.currentProjectIsCustomNum;
    },
  },
  methods: {
    getTableData() {
      getRelationshipCase(this.caseId, this.relationshipType, (data) => {
        this.data = data;
        this.$emit('setCount', data.length);
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
.type-type {
  color: var(--primary_color);
  font-style: var(--primary_color);
}

.type-type:nth-child(2) {
  margin: 0px 10px;
}
</style>
