<template>
  <div>
    <el-button class="add-btn" v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']" :disabled="readOnly" type="primary" size="mini" @click="appIssue">{{ $t('test_track.issue.add_issue') }}</el-button>
    <el-button class="add-btn" v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"  :disabled="readOnly" type="primary" size="mini" @click="relateIssue">{{ $t('test_track.case.relate_issue') }}</el-button>
    <el-tooltip class="item" v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"  effect="dark"
                :content="$t('test_track.issue.platform_tip')"
                placement="right">
      <i class="el-icon-info"/>
    </el-tooltip>

    <ms-table
      v-loading="page.result.loading"
      :show-select-all="false"
      :data="page.data"
      :fields.sync="fields"
      :operators="operators"
      :enable-selection="false"
      ref="table"
      @refresh="getIssues">
      <span v-for="(item) in fields" :key="item.key">
        <ms-table-column
          :label="$t('test_track.issue.id')"
          :field="item"
          prop="id" v-if="false">
        </ms-table-column>
        <ms-table-column
          :field="item"
          :label="$t('test_track.issue.id')"
          prop="num">
        </ms-table-column>

        <ms-table-column
          :field="item"
          :label="$t('test_track.issue.title')"
          prop="title">
        </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.platform_status')"
        :field="item"
        v-if="isThirdPart"
        prop="platformStatus">
        <template v-slot="scope">
          {{ scope.row.platformStatus ? scope.row.platformStatus : '--'}}
        </template>
      </ms-table-column>

      <ms-table-column
        v-else
        :field="item"
        :label="$t('test_track.issue.status')"
        prop="status">
        <template v-slot="scope">
          <span>{{ issueStatusMap[scope.row.status] ? issueStatusMap[scope.row.status] : scope.row.status }}</span>
        </template>
      </ms-table-column>

        <span v-for="field in issueTemplate.customFields" :key="field.id">
          <ms-table-column :field="item" :label="field.name" :prop="field.name" v-if="field.name === '状态'">
            <template v-slot="scope">
              <el-dropdown class="test-case-status" @command="statusChange" placement="bottom" trigger="click">
                <span class="el-dropdown-link">
                  {{getCustomFieldValue(scope.row, field) ? getCustomFieldValue(scope.row, field) : issueStatusMap[scope.row.status]}}
                </span>
                <el-dropdown-menu slot="dropdown" chang>
                  <span v-for="(item, index) in status" :key="index">
                      <el-dropdown-item :command="{id: scope.row.id, status: item.value}">
                        {{item.system ? $t(item.text) : item.text}}
                      </el-dropdown-item>
                    </span>
                </el-dropdown-menu>
              </el-dropdown>
            </template>

          </ms-table-column>
        </span>

        <ms-table-column
          :field="item"
          :label="$t('test_track.issue.platform')"
          prop="platform">
        </ms-table-column>

        <issue-description-table-item :field="item"/>
      </span>
    </ms-table>

    <test-plan-issue-edit :plan-id="planId" :case-id="caseId" @refresh="getIssues" ref="issueEdit"/>
    <IssueRelateList :is-third-part="isThirdPart" :case-id="caseId"  @refresh="getIssues" ref="issueRelate"/>
  </div>
</template>

<script>
import TestPlanIssueEdit from "@/business/components/track/case/components/TestPlanIssueEdit";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import {ISSUE_STATUS_MAP} from "@/common/js/table-constants";
import IssueRelateList from "@/business/components/track/case/components/IssueRelateList";
import {getIssuesByCaseId} from "@/network/Issue";
import {getIssueTemplate} from "@/network/custom-field-template";
import {getCustomFieldValue, getTableHeaderWithCustomFields} from "@/common/js/tableUtils";
export default {
  name: "TestCaseIssueRelate",
  components: {IssueRelateList, IssueDescriptionTableItem, MsTableColumn, MsTable, TestPlanIssueEdit},
  data() {
    return {
      page: {
        data: [],
        result: {},
      },
      isThirdPart: false,
      issueTemplate: {},
      fields: [],
      operators:[
        {
          tip: this.$t('test_track.case.unlink'),
          icon: "el-icon-unlock",
          type: "danger",
          exec: this.deleteIssue
        }
      ],
      status: []
    }
  },
  props: ['caseId', 'readOnly','planId'],
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
  },
  created() {
    getIssueTemplate()
      .then((template) => {
        this.issueTemplate = template;
        if (this.issueTemplate.platform === 'metersphere') {
          this.isThirdPart = false;
        } else {
          this.isThirdPart = true;
        }
        if (template) {
          let customFields = template.customFields;
          for (let fields of customFields) {
            if (fields.name === '状态') {
              this.status = fields.options;
              break;
            }
          }
        }
        this.fields = getTableHeaderWithCustomFields('ISSUE_LIST', this.issueTemplate.customFields);
        if (!this.isThirdPart) {
          for (let i = 0; i < this.fields.length; i++) {
            if (this.fields[i].id === 'platformStatus') {
              this.fields.splice(i, 1);
              break;
            }
          }
        }
        this.$refs.table.reloadTable();
      });
  },
  methods: {
    statusChange(param) {
      this.$post("/issues/change/status/", param, () => {
        this.getIssues();
        this.$success(this.$t('commons.modify_success'));
      });
    },
    getCustomFieldValue(row, field) {
      return getCustomFieldValue(row, field, this.members);
    },
    getIssues() {
      let result = getIssuesByCaseId(this.caseId, this.page);
      if (result) {
        this.page.result = result;
      }
    },
    appIssue() {
      if (!this.caseId) {
        this.$warning(this.$t('api_test.automation.save_case_info'));
        return;
      }
      this.$refs.issueEdit.open();
    },
    relateIssue() {
      if (!this.caseId) {
        this.$warning(this.$t('api_test.automation.save_case_info'));
        return;
      }
      this.$refs.issueRelate.open();
    },
    closeIssue(row) {
      if (row.status === 'closed') {
        this.$success(this.$t('test_track.issue.close_success'));
      } else {
        this.page.result = this.$get("/issues/close/" + row.id, () => {
          this.getIssues();
          this.$success(this.$t('test_track.issue.close_success'));
        });
      }
    },
    deleteIssue(row) {
      this.page.result = this.$post("/issues/delete/relate", {id: row.id, caseId: this.caseId}, () => {
        this.getIssues();
        this.$success(this.$t('commons.delete_success'));
      })
    },
  }
}
</script>

<style scoped>
.add-btn {
  display: inline-block;
  margin-right: 5px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #783887;
}
</style>
