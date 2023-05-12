<template>
  <div class="issue-wrap">
    <ms-table
      :show-select-all="false"
      :data="tableData"
      :fields.sync="fields"
      :enable-selection="false"
      ref="table"
    >
      <span v-for="item in fields" :key="item.key">
        <!-- <ms-table-column
          :label="$t('test_track.issue.id')"
          :field="item"
          prop="id"
          v-if="false"
        >
        </ms-table-column> -->
        <ms-table-column
          :field="item"
          :label="$t('ID')"
          :sortable="true"
          prop="num"
        >
          <template v-slot:default="{ row }">
            <div :class="row.diffStatus == 2 ? 'line-through' : ''">
              {{ row.num }}
            </div>
            <div style="width: 32px" v-if="row.diffStatus > 0">
              <case-diff-status :diffStatus="row.diffStatus"></case-diff-status>
            </div>
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :sortable="true"
          :label="$t('test_track.issue.title')"
          prop="title"
        >
          <template v-slot:default="{ row }">
            <div :class="row.diffStatus == 2 ? 'line-through' : ''">
              {{ row.title }}
            </div>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.issue.platform_status')"
          :field="item"
          v-if="isThirdPart"
          prop="platformStatus"
        >
          <template v-slot="scope">
            {{ scope.row.platformStatus ? scope.row.platformStatus : "--" }}
          </template>
        </ms-table-column>

        <ms-table-column
          v-else
          :field="item"
          :label="$t('test_track.issue.status')"
          prop="status"
        >
          <template v-slot="scope">
            <span>{{
              issueStatusMap[scope.row.status]
                ? issueStatusMap[scope.row.status]
                : scope.row.status
            }}</span>
          </template>
        </ms-table-column>

        <span v-for="field in issueTemplate.customFields" :key="field.id">
          <ms-table-column
            :field="item"
            :label="field.name"
            :prop="field.name"
            v-if="field.name === '状态'"
          >
            <template v-slot="scope">
              <el-dropdown
                class="test-case-status"
                @command="statusChange"
                placement="bottom"
                trigger="click"
              >
                <span class="el-dropdown-link">
                  {{ getStatus(scope.row, field) }}
                </span>
                <el-dropdown-menu slot="dropdown" chang>
                  <span v-for="(item, index) in status" :key="index">
                    <el-dropdown-item
                      :command="{ id: scope.row.id, status: item.value }"
                    >
                      {{ item.system ? $t(item.text) : item.text }}
                    </el-dropdown-item>
                  </span>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </ms-table-column>
          <ms-table-column
            :field="item"
            :label="field.name"
            :prop="field.name"
            v-if="field.name === '处理人'">
            <template v-slot="scope">
              <span>{{ getCustomMember(scope.row, field) }}</span>
            </template>
          </ms-table-column>
        </span>

        <ms-table-column
          :field="item"
          :label="$t('test_track.issue.platform')"
          prop="platform"
        >
        </ms-table-column>

        <issue-description-table-item :field="item" />
      </span>
    </ms-table>
  </div>
</template>
<script>
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import CaseDiffStatus from "./CaseDiffStatus";
import {
  getIssuePartTemplateWithProject,
  getIssuesByCaseId,
  getIssuesByCaseIdWithSearch,
} from "@/api/issue";
import {
  getCustomFieldValue,
  getTableHeaderWithCustomFields,
} from "metersphere-frontend/src/utils/tableUtils";
import { LOCAL } from "metersphere-frontend/src/utils/constants";
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import { ISSUE_STATUS_MAP } from "metersphere-frontend/src/utils/table-constants";
import { issueStatusChange } from "@/api/issue";
import {getProjectMember} from "@/api/user";

export default {
  name: "CaseDiffIssueRelate",
  components: {
    MsTableColumn,
    MsTable,
    CaseDiffStatus,
    IssueDescriptionTableItem,
  },
  props: {
    tableData: Array,
  },
  data() {
    return {
      isThirdPart: false,
      issueTemplate: {},
      status: [],
      issueRelateVisible: false,
      fields: [],
      members: [],
    };
  },
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
  },
  created() {
    getIssuePartTemplateWithProject((template, project) => {
      this.currentProject = project;
      this.issueTemplate = template;
      if (this.issueTemplate.platform === LOCAL) {
        this.isThirdPart = false;
      } else {
        this.isThirdPart = true;
      }
      if (template) {
        let customFields = template.customFields;
        for (let fields of customFields) {
          if (fields.name === "状态") {
            this.status = fields.options;
            break;
          }
        }
      }
      this.fields = getTableHeaderWithCustomFields(
        "ISSUE_LIST",
        this.issueTemplate.customFields
      );
      if (!this.isThirdPart) {
        for (let i = 0; i < this.fields.length; i++) {
          if (this.fields[i].id === "platformStatus") {
            this.fields.splice(i, 1);
            break;
          }
        }
      }
      if (this.$refs.table) {
        this.$refs.table.reloadTable();
      }
    });
    this.getMembers();
  },
  methods: {
    getMembers() {
      getProjectMember().then((res) => {
        this.members = res.data;
      });
    },
    getCustomMember(row, field) {
      return getCustomFieldValue(row, field, this.members);
    },
    getStatus(row, field) {
      return getCustomFieldValue(row, field, this.members)
        ? getCustomFieldValue(row, field, this.members)
        : this.issueStatusMap[row.status];
    },
    statusChange(param) {
      issueStatusChange(param).then(() => {
        this.getIssues();
        this.$success(this.$t("commons.modify_success"), false);
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.issue-wrap {
  margin-top: 22px;
  min-height: 300px;
}
</style>
