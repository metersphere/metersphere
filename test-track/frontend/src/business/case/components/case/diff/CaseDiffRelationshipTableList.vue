<template>
  <div>
    <ms-table
      :show-select-all="false"
      :data="tableData"
      :enable-selection="false"
      ref="table"
      :screen-height="null"
    >
      <ms-table-column
        min-width="200px"
        width="200px"
        v-if="relationshipType === 'POST'"
        :label="$t('commons.relationship.type')"
      >
        <template>
          <div class="pos-label">
            {{ $t("commons.relationship.current_case") }}
          </div>
          <div class="pos-type pos-left-margin">
            {{ $t("commons.relationship.after_finish") }}
          </div>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="targetCustomNum"
        v-if="isCustomNum"
        :label="$t('commons.id')"
        sortable
        min-width="100px"
        width="100px"
      >
        <template v-slot:default="{ row }">
          <div :class="row.diffStatus == 2 ? 'line-through' : ''">
            {{ row.targetCustomNum }}
          </div>
          <div style="width: 32px" v-if="row.diffStatus > 0">
            <case-diff-status :diffStatus="row.diffStatus"></case-diff-status>
          </div>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="targetNum"
        v-else
        :label="$t('commons.id')"
        sortable
        min-width="100px"
        width="100px"
      >
        <template v-slot:default="{ row }">
          <div :class="row.diffStatus == 2 ? 'line-through' : ''">
            {{ row.targetNum }}
          </div>
          <div style="width: 32px" v-if="row.diffStatus > 0">
            <case-diff-status :diffStatus="row.diffStatus"></case-diff-status>
          </div>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="targetName"
        :label="$t('case.case_name')"
        sortable
        min-width="256px"
        width="256px"
      >
        <template v-slot:default="{ row }">
          <div :class="row.diffStatus == 2 ? 'line-through' : ''">
            {{ row.targetName }}
          </div>
        </template>
      </ms-table-column>

      <ms-table-column
        v-xpack
        prop="versionId"
        :label="$t('commons.version')"
        sortable
        min-width="100px"
        width="100px"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="creator"
        :label="$t('commons.create_user')"
        min-width="120"
      >
      </ms-table-column>

      <ms-table-column
        prop="status"
        min-width="100px"
        width="100px"
        :label="$t('api_test.definition.api_case_status')"
      >
        <template slot-scope="{ row }">
          <status-table-item :value="$t(statusMap.get(row.status))" />
        </template>
      </ms-table-column>
      <ms-table-column
        width="200px"
        min-width="200px"
        v-if="relationshipType === 'PRE'"
        :label="$t('commons.relationship.type')"
      >
        <template>
          <div class="pos-type pos-right-margin">
            {{ $t("commons.relationship.after_finish") }}
          </div>
          <div class="pos-label">
            {{ $t("commons.relationship.current_case") }}
          </div>
        </template>
      </ms-table-column>
    </ms-table>
  </div>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import RelationshipFunctionalRelevance from "../../common/CaseRelationshipFunctionalRelevance";
import { getRelationshipCase } from "@/api/testCase";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import { useStore } from "@/store";
import { operationConfirm } from "metersphere-frontend/src/utils";
import CaseDiffStatus from "./CaseDiffStatus";

export default {
  name: "CaseDiffRelationshipTableList",
  components: {
    RelationshipFunctionalRelevance,
    MsTableSearchBar,
    MsTableColumn,
    MsTable,
    StatusTableItem,
    CaseDiffStatus,
  },
  data() {
    return {
      result: {},
      data: [],
      operators: [
        {
          tip: this.$t("case.relieve"),
          isTextButton: true,
          exec: this.handleDelete,
          isDisable: this.readOnly,
          permissions: ["PROJECT_TRACK_CASE:READ+DELETE"],
        },
      ],
      condition: {},
      options: [],
      statusMap: new Map(),
      value: "",
    };
  },
  props: {
    tableData: [],
    readOnly: Boolean,
    relationshipType: String,
  },
  computed: {
    isCustomNum() {
      let template = useStore().testCaseTemplate;
      if (template && template.customFields) {
        template.customFields.forEach((item) => {
          if (item.name === "用例状态") {
            for (let i = 0; i < item.options.length; i++) {
              this.statusMap.set(item.options[i].value, item.options[i].text);
            }
          }
        });
      }
      return useStore().currentProjectIsCustomNum;
    },
  },
  methods: {
    getTableData() {
      getRelationshipCase(this.caseId, this.relationshipType).then((r) => {
        this.data = r.data;
        this.$emit("setCount", this.data.length);
      });
    },
    openRelevance() {
      this.$refs.testCaseRelevance.open();
    },
    handleDelete(item) {
      operationConfirm(
        this,
        this.$t("case.dependency_remove_confirm"),
        () => {
          this.$emit("deleteRelationship", item.sourceId, item.targetId);
        }
      );
    },
  },
};
</script>

<style scoped>
.type-type {
  color: var(--primary_color);
  font-style: var(--primary_color);
}

.type-type:nth-child(2) {
  margin: 0px 10px;
}
.pos-type {
  width: auto;
  height: 22px;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  color: #2ea121;
  padding: 0 6px;
  display: inline-block;
  background: rgba(52, 199, 36, 0.1);
  border-radius: 2px;
}
.pos-label {
  display: inline-block;
  height: 22px;
  font-size: 14px;
  line-height: 22px;
  color: #000000;
}
.pos-left-margin {
  margin-left: 4px;
}
.pos-right-margin {
  margin-right: 4px;
}
.line-through {
  text-decoration-line: line-through;
  color: #8f959e !important;
  font-size: 14px;
  font-weight: 400;
  font-size: 14px;
}
</style>
