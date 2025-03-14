<template>
  <div class="card-container">
    <ms-table
        :table-is-loading="loading"
        :data="page.data"
        :enableSelection="false"
        :condition="condition"
        :total="page.total"
        :page-size.sync="page.pageSize"
        :screen-height="screenHeight"
        :remember-order="true"
        row-key="id"
        :row-order-group-id="projectId"
        :row-order-func="editTestCaseOrder"
        @handlePageChange="initTableData"
        @handleRowClick="handleEdit"
        :fields.sync="fields"
        :field-key="tableHeaderKey"
        @refresh="initTableData"
        ref="table"
    >
      <ms-table-column
          prop="deleteTime"
          sortable
          v-if="this.trashEnable"
          :fields-width="fieldsWidth"
          :label="$t('commons.delete_time')"
          min-width="150px"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.deleteTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
          prop="deleteUserId"
          :fields-width="fieldsWidth"
          v-if="this.trashEnable"
          :label="$t('commons.delete_user')"
          min-width="120"
      />

      <span v-for="item in fields" :key="item.key">
        <ms-table-column
            v-if="!customNum"
            :field="item"
            :fields-width="fieldsWidth"
            prop="num"
            sortable
            :label="$t('commons.id')"
            min-width="80"
        />

        <ms-table-column
            v-if="item.id === 'num' && customNum"
            :fields-width="fieldsWidth"
            prop="customNum"
            sortable
            :label="$t('commons.id')"
            min-width="80"
        />

        <ms-table-column
            prop="name"
            sortable
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.name')"
            min-width="120"
        />
        <ms-table-column
            prop="nodePath"
            :field="item"
            v-if="isShowAllColumn"
            :fields-width="fieldsWidth"
            :label="$t('test_track.case.module')"
            min-width="150px"
        >
          <template v-slot:default="scope">
            <span>{{ nodePathMap.get(scope.row.nodeId) }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
            prop="projectName"
            :field="item"
            v-if="!isShowAllColumn"
            :fields-width="fieldsWidth"
            :label="$t('report.project_name')"
            min-width="150px"
        >
        </ms-table-column>

        <ms-table-column
            :label="$t('project.version.name')"
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100px"
            prop="versionId"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
            :label="$t('test_track.case.case_desc')"
            prop="desc"
            :field="item"
            min-width="120px"
            v-if="isShowAllColumn"
        >
          <template v-slot:default="scope">
            <el-link
                @click.stop="getCase(scope.row.id)"
                style="color: #783887"
            >{{ $t("commons.preview") }}</el-link
            >
          </template>
        </ms-table-column>

        <ms-table-column
            sortable
            prop="createUser"
            min-width="120"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.create_user')"
            :filters="userFilter"
        >
          <template v-slot:default="scope">
            {{ getCreateUserName(scope.row.createUser) }}
          </template>
        </ms-table-column>

        <test-case-review-status-table-item
            :field="item"
            :fields-width="fieldsWidth"
        />

        <test-plan-case-status-table-item
            prop="lastExecuteResult"
            :field="item"
            :fields-width="fieldsWidth"
        />

        <ms-table-column
            prop="tags"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.tag')"
            min-width="80"
        >
          <template v-slot:default="scope">
            <ms-tag
                v-for="(itemName, index) in scope.row.tags"
                :key="index"
                type="success"
                effect="plain"
                :show-tooltip="
                scope.row.tags.length === 1 && itemName.length * 12 <= 80
              "
                :content="itemName"
                style="margin-left: 0px; margin-right: 2px"
            />
            <span/>
          </template>
        </ms-table-column>

        <ms-table-column
            prop="updateTime"
            sortable
            v-if="isShowAllColumn"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.update_time')"
            min-width="150px"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
            prop="createTime"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.create_time')"
            sortable
            min-width="150px"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
            v-for="field in testCaseTemplate.customFields"
            :key="field.id"
            :filters="getCustomFieldFilter(field)"
            :field="item"
            :fields-width="fieldsWidth"
            :label="field.system ? $t(systemFiledMap[field.name]) : field.name"
            :min-width="120"
            :prop="field.name"
        >
          <template v-slot="scope">
            <span v-if="field.name === '用例等级'">
              <priority-table-item
                  :value="
                  getCustomFieldValue(scope.row, field)
                    ? getCustomFieldValue(scope.row, field)
                    : scope.row.priority
                "
              />
            </span>
            <span v-else>
              {{ getCustomFieldValue(scope.row, field) }}
            </span>
          </template>
        </ms-table-column>
      </span>
    </ms-table>

    <ms-table-pagination
        :change="initTableData"
        :current-page.sync="page.currentPage"
        :page-size.sync="page.pageSize"
        :total="page.total"
    />

    <test-case-preview ref="testCasePreview" :loading="rowCaseResult.loading"/>
  </div>
</template>

<script>
import {TEST_CASE_LIST} from "metersphere-frontend/src/utils/constants";

import {
  getCustomFieldFilter,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  getPageInfo,
  getTableHeaderWithCustomFields,
  initCondition,
  parseCustomFilesForList,
} from "metersphere-frontend/src/utils/tableUtils";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import {getCurrentProjectID, getCurrentUserId,} from "metersphere-frontend/src/utils/token";

import {getProjectMember, getProjectMemberUserFilter} from "@/api/user";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {SYSTEM_FIELD_NAME_MAP} from "metersphere-frontend/src/utils/table-constants";
import {editTestCaseOrder, getTestCaseListById, getTestCaseStep, testCaseList,} from "@/api/test-case";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {TEST_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import PriorityTableItem from "@/business/module/track/PriorityTableItem";
import StatusTableItem from "@/business/module/track/StatusTableItem";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getProject} from "@/api/project";
import {getProjectVersions} from "metersphere-frontend/src/api/version";
import useStore, {useApiStore} from "@/store";
import {getTestTemplate} from "metersphere-frontend/src/api/custom-field-template";
import {getAdvSearchCustomField} from "metersphere-frontend/src/components/search/custom-component";
import TestCaseReviewStatusTableItem from "@/business/othermodule/track/TestCaseReviewStatusTableItem";
import TestPlanCaseStatusTableItem from "@/business/othermodule/track/TestPlanCaseStatusTableItem";
import TestCasePreview from "@/business/othermodule/track/TestCasePreview";
import {parseTag} from "metersphere-frontend/src/utils";
import {getCustomFieldValueForTrack} from "@/business/component/js/table-head-util";
import {getTestCaseNodes} from "@/api/test-case-node";
import {buildNodePath, buildTree} from "metersphere-frontend/src/model/NodeTree";

export default {
  name: "TableList",
  components: {
    MsTableColumn,
    MsTable,
    HeaderLabelOperate,
    MsTablePagination,
    PriorityTableItem,
    StatusTableItem,
    MsTag,
    TestCaseReviewStatusTableItem,
    TestPlanCaseStatusTableItem,
    TestCasePreview,
  },
  data() {
    return {
      projectName: "",
      type: TEST_CASE_LIST,
      tableHeaderKey: "TRACK_TEST_CASE",
      tableLabel: [],
      condition: {
        components: TEST_CASE_CONFIGS,
        combine: {
          creator: {
            operator: "current user",
            value: "current user",
          },
        },
      },
      loading: false,
      versionFilters: [],
      statusFilters: [
        {text: this.$t("test_track.case.status_prepare"), value: "Prepare"},
        {text: this.$t("test_track.case.status_running"), value: "Underway"},
        {
          text: this.$t("test_track.case.status_finished"),
          value: "Completed",
        },
      ],
      priorityFilters: [
        {text: "P0", value: "P0"},
        {text: "P1", value: "P1"},
        {text: "P2", value: "P2"},
        {text: "P3", value: "P3"},
      ],
      typeArr: [],
      valueArr: {},
      selectDataRange: "all",
      testCaseTemplate: {},
      members: [],
      page: getPageInfo(),
      fields: getCustomTableHeader("TRACK_TEST_CASE"),
      fieldsWidth: getCustomTableWidth("TRACK_TEST_CASE"),
      memberMap: new Map(),
      rowCase: {},
      rowCaseResult: {},
      store: {},
      userFilter: [],
      nodePathMap: new Map()
    };
  },
  props: {
    treeNodes: {
      type: Array,
    },
    trashEnable: {
      type: Boolean,
      default: false,
    },
    isFocus: {
      type: Boolean,
      default: false,
    },
    isShowAllColumn: {
      type: Boolean,
      default: true,
    },
    isSelectAll: {
      type: Boolean,
      default: false,
    },
    isCreation: {
      type: Boolean,
      default: false,
    },
    currentVersion: String,
    screenHeight: {
      type: [Number, String],
      default() {
        return "calc(100vh - 218px)";
      },
    }, //屏幕高度
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    selectNodeIds() {
      return this.store.testCaseSelectNodeIds;
    },
    moduleOptions() {
      return this.store.testCaseModuleOptions;
    },
    systemFiledMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    editTestCaseOrder() {
      return editTestCaseOrder;
    },
    customNum() {
      return this.store.currentProjectIsCustomNum;
    },
  },
  created() {
    getProjectMemberUserFilter((data) => {
      this.userFilter = data;
    });
    this.getNodePathMap();
    if (this.isFocus) {
      if (this.condition.filters) {
        delete this.condition.filters["user_id"];
      }
      if (this.condition.userId) {
        delete this.condition.userId;
      }
      this.condition.combine = {
        followPeople: {operator: "current user", value: "current user"},
      };
    } else if (this.isCreation) {
      if (this.condition.filters) {
        delete this.condition.filters["user_id"];
      }
      this.condition.userId = getCurrentUserId();
    }
    this.getTemplateField();
    this.store = useApiStore();
    if (!this.isCreation && !this.isFocus) {
      this.condition.unComing = true;
    }
    this.$emit("setCondition", this.condition);
    if (this.trashEnable) {
      if (this.condition.filters) {
        this.condition.filters.status = ["Trash"];
      } else {
        this.condition.filters = {status: ["Trash"]};
      }
    }
    this.condition.versionId = this.currentVersion;
    this.initTableData();
    let redirectParam = this.$route.query.dataSelectRange;
    this.checkRedirectEditPage(redirectParam);

    if (!this.projectName || this.projectName === "") {
      this.getProjectName();
    }
    this.getVersionOptions();
  },
  activated() {
    this.getTemplateField();
    let ids = this.$route.params.ids;
    if (ids) {
      this.condition.ids = ids;
    }
    if (!this.isCreation && !this.isFocus) {
      this.condition.unComing = true;
    }
    this.initTableData();
    this.condition.ids = null;
  },
  watch: {
    selectNodeIds() {
      this.page.currentPage = 1;
      if (!this.trashEnable) {
        if (this.condition.filters) {
          this.condition.filters.status = [];
        } else {
          this.condition.filters = {status: []};
        }
      }
      initCondition(this.condition, false);
      this.initTableData();
    },
    condition() {
      this.$emit("setCondition", this.condition);
    },
    trashEnable() {
      if (this.trashEnable) {
        //更改查询条件
        if (this.condition.filters) {
          this.condition.filters.status = ["Trash"];
        } else {
          this.condition.filters = {status: ["Trash"]};
        }
        this.condition.moduleIds = [];
        initCondition(this.condition, false);
        this.initTableData();
      } else {
        if (this.condition.filters) {
          this.condition.filters.status = [];
        } else {
          this.condition.filters = {status: []};
        }
      }
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTableData();
      this.getVersionOptions(this.currentVersion);
    },
  },
  methods: {
    getCreateUserName(userId) {
      let user = this.userFilter.filter((item) => item.value === userId);
      return user.length > 0 ? user[0].text : "";
    },
    getTemplateField() {
      this.loading = true;
      let p1 = getProjectMember((data) => {
        this.members = data;
        this.members.forEach((item) => {
          this.memberMap.set(item.id, item.name);
        });
      });
      let p2 = getTestTemplate();
      Promise.all([p1, p2]).then((data) => {
        let template = data[1];
        this.testCaseTemplate = template;
        this.fields = getTableHeaderWithCustomFields(
            this.tableHeaderKey,
            this.testCaseTemplate.customFields,
            this.members
        );
        // todo 处理高级搜索自定义字段部分
        this.condition.components = this.condition.components.filter(
            (item) => item.custom !== true
        );
        let comp = getAdvSearchCustomField(
            this.condition,
            this.testCaseTemplate.customFields
        );
        // 系统字段国际化处理
        comp.filter((element) => {
          if (element.label === "责任人") {
            element.label = this.$t("custom_field.case_maintainer");
          }
          if (element.label === "用例等级") {
            element.label = this.$t("custom_field.case_priority");
          }
          if (element.label === "用例状态") {
            element.label = this.$t("custom_field.case_status");
            // 回收站TAB页处理高级搜索用例状态字段
            if (this.trashEnable) {
              element.options = [
                {
                  text: this.$t("test_track.plan.plan_status_trash"),
                  value: "Trash",
                },
              ];
            } else {
              element.options.forEach((option) => {
                option.text = this.$t(option.text);
              });
            }
          }
        });
        this.condition.components.push(...comp);
        this.setTestCaseDefaultValue(template);
        this.$nextTick(() => {
          if (this.$refs.table) {
            this.$refs.table.resetHeader();
          }
          this.loading = false;
        });
      });
    },
    getNodePathMap() {
      if (!this.projectId) {
        return;
      }
      getTestCaseNodes(this.projectId)
          .then((r) => {
            let treeNodes = r.data;
            treeNodes.forEach(node => {
              node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
              buildTree(node, {path: ''});
            });
            let moduleOptions = [];
            treeNodes.forEach(node => {
              buildNodePath(node, {path: ''}, moduleOptions);
            });
            let map = new Map();
            if (moduleOptions) {
              moduleOptions.forEach((item) => {
                map.set(item.id, item.path);
              });
            }
            this.nodePathMap = map;
          });
    },
    setTestCaseDefaultValue(template) {
      let testCaseDefaultValue = {};
      template.customFields.forEach((item) => {
        if (item.system) {
          if (item.defaultValue) {
            testCaseDefaultValue[item.name] = JSON.parse(item.defaultValue);
          } else {
            testCaseDefaultValue[item.name] = "";
          }
        }
        if (item.name === "用例等级") {
          item.columnKey = "priority";
        } else if (item.name === "责任人") {
          item.columnKey = "maintainer";
        } else if (item.name === "用例状态") {
          item.columnKey = "status";
        }
      });
      useStore().testCaseDefaultValue = testCaseDefaultValue;
    },

    getCustomFieldValue(row, field) {
      let value = getCustomFieldValueForTrack(row, field, this.members);
      if (!value) {
        if (field.name === "用例等级") {
          return row.priority;
        }
        if (field.name === "责任人") {
          return row.maintainerName;
        }
        if (field.name === "用例状态") {
          return row.status;
        }
      }
      return value;
    },
    getCustomFieldFilter(field) {
      if (field.name === "用例状态") {
        let option = null;
        if (!this.trashEnable) {
          option = [];
          field.options.forEach((item) => {
            option.push({
              text: this.$t(item.text),
              value: item.value,
            });
          });
        }
        return option;
      }
      return getCustomFieldFilter(field, this.userFilter);
    },
    checkRedirectEditPage(redirectParam) {
      if (redirectParam != null) {
        getTestCaseListById(id).then((response) => {
          let testCase = response.data;
          testCase.label = "redirect";
          this.$emit("testCaseEdit", testCase);
        });
      }
    },
    getProjectName() {
      getProject(this.projectId).then((response) => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    getSelectDataRange() {
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      this.selectDataRange = dataType === "case" ? dataRange : "all";
    },
    initTableData() {
      this.condition.planId = "";
      this.condition.nodeIds = [];
      //initCondition(this.condition);
      initCondition(this.condition, this.condition.selectAll);
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);

      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (!this.trashEnable) {
        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          // param.nodeIds = this.selectNodeIds;
          this.condition.nodeIds = this.selectNodeIds;
        }
      }

      this.getData();
    },
    getData() {
      this.getSelectDataRange();
      this.condition.selectThisWeedData = false;
      this.condition.selectThisWeedRelevanceData = false;
      this.condition.caseCoverage = null;
      switch (this.selectDataRange) {
        case "thisWeekCount":
          this.condition.selectThisWeedData = true;
          break;
        case "thisWeekRelevanceCount":
          this.condition.selectThisWeedRelevanceData = true;
          break;
        case "uncoverage":
          this.condition.caseCoverage = "uncoverage";
          break;
        case "coverage":
          this.condition.caseCoverage = "coverage";
          break;
        case "notReviewed":
          this.condition.filters.review_status = ["Prepare"];
          break;
        case "reviewSuccess":
          this.condition.filters.review_status = ["Pass"];
          break;
        case "reviewFail":
          this.condition.filters.review_status = ["UnPass"];
          break;
      }
      if (this.trashEnable) {
        //支持回收站查询版本
        let versionIds = this.condition.filters.version_id;
        this.condition.filters.status = ["Trash"];
        if (versionIds) {
          this.condition.filters.version_id = versionIds;
        }
      }
      if (this.projectId && this.projectId !== "no_such_project") {
        this.condition.projectId = this.projectId;
        this.$emit("setCondition", this.condition);
        this.loading = true;
        testCaseList(
            {pageNum: this.page.currentPage, pageSize: this.page.pageSize},
            this.condition
        ).then((response) => {
          this.loading = false;
          let data = response.data;
          this.page.total = data.itemCount;
          this.page.data = data.listObject;
          parseCustomFilesForList(this.page.data);
          parseTag(this.page.data);
          this.page.data.forEach((item) => {
            let nodePath = item.nodePath;
            if (item.customFields) {
              item.customFields = JSON.parse(item.customFields);
            }
            if (nodePath.startsWith("/未规划用例", "0")) {
              item.nodePath = nodePath.replaceAll(
                  "/未规划用例",
                  "/" + this.$t("api_test.unplanned_case")
              );
            }
          });
        });
      }
    },
    search() {
      this.initTableData();
    },
    handleEdit(testCase, column) {
      let query = {
        projectId: testCase.projectId,
      };
      let path = "/track/case/edit/" + testCase.id;
      let TestCaseData = this.$router.resolve({
        path,
        query,
      });
      window.open(TestCaseData.href, "_blank");
    },
    refresh() {
      this.$refs.table.clear();
      this.$emit("refresh");
    },
    refreshAll() {
      this.$refs.table.clear();
      this.$emit("refreshAll");
    },
    getCase(id) {
      this.$refs.testCasePreview.open();
      this.rowCaseResult.loading = true;

      getTestCaseStep(id).then((response) => {
        this.rowCaseResult.loading = false;
        this.rowCase = response.data;
        this.rowCase.steps = JSON.parse(this.rowCase.steps);
        if (!this.rowCase.steps || this.rowCase.length < 1) {
          this.rowCase.steps = [
            {
              num: 1,
              desc: "",
              result: "",
            },
          ];
        }
        if (!this.rowCase.stepModel) {
          this.rowCase.stepModel = "STEP";
        }
        this.$refs.testCasePreview.setData(this.rowCase);
      });
    },
    getVersionOptions() {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then((response) => {
          this.versionFilters = response.data.map((u) => {
            return {text: u.name, value: u.id};
          });
        });
      }
    },
  },
};
</script>

<style scoped>
.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}
</style>
