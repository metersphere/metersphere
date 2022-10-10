<template>
  <el-card>
    <div class="card-content">
      <div class="ms-main-div" @click="showAll">

        <!--操作按钮-->
        <div class="ms-opt-btn">
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off"
               style="color: var(--primary_color); font-size: 25px;  margin-right: 15px;cursor: pointer;position: relative;top: 5px "
               @click="saveFollow"/>
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i class="el-icon-star-on"
               style="color: var(--primary_color); font-size: 28px; margin-right: 15px;cursor: pointer;position: relative;top: 5px "
               @click="saveFollow"/>
          </el-tooltip>
          <el-link type="primary" style="margin-right: 20px" @click="openHis" v-if="form.id">
            {{ $t('operating_log.change_history') }}
          </el-link>
          <!--  版本历史 -->
          <ms-version-history v-xpack
                              ref="versionHistory"
                              :version-data="versionData"
                              :current-id="currentTestCaseInfo.id"
                              :current-project-id="currentProjectId"
                              :is-read="true"/>
          <ms-table-button id="inputDelay"
                           type="primary"
                           :content="$t('commons.copy')"
                           size="small" @click="handleCopyPublic"
                           icon=""
          />
        </div>
        <el-form :model="form" :rules="rules" ref="caseFrom" v-loading="loading" class="case-form">
          <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
          <el-row>
            <el-col :span="8">
              <el-form-item
                :placeholder="$t('test_track.case.input_name')"
                :label="$t('test_track.case.name')"
                :label-width="formLabelWidth"
                prop="name">
                <el-input :disabled="readOnly" v-model="form.name" size="small" class="ms-case-input"></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('test_track.case.project')" :label-width="formLabelWidth" prop="projectId">
                <el-select v-model="form.projectId" filterable clearable :disabled="readOnly">
                  <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('commons.tag')" :label-width="formLabelWidth" prop="tag">
                <ms-input-tag :read-only="readOnly" :currentScenario="form" v-if="showInputTag" ref="tag"
                              :disabled="true"
                              class="ms-case-input"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row v-if="isCustomNum">
            <el-col :span="7">
              <el-form-item label="ID" :label-width="formLabelWidth" prop="customNum">
                <el-input :disabled="readOnly" v-model.trim="form.customNum" size="small"
                          class="ms-case-input"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <ms-form-divider :title="$t('test_track.case.step_info')"/>

          <form-rich-text-item :disabled="readOnly" :label-width="formLabelWidth"
                               :title="$t('test_track.case.prerequisite')" :data="form" prop="prerequisite"/>

          <step-change-item :label-width="formLabelWidth" :form="form"/>
          <form-rich-text-item :disabled="readOnly" :label-width="formLabelWidth" v-if="form.stepModel === 'TEXT'"
                               :title="$t('test_track.case.step_desc')" :data="form" prop="stepDescription"/>
          <form-rich-text-item :disabled="readOnly" :label-width="formLabelWidth" v-if="form.stepModel === 'TEXT'"
                               :title="$t('test_track.case.expected_results')" :data="form" prop="expectedResult"/>

          <test-case-step-item :label-width="formLabelWidth" v-if="form.stepModel === 'STEP' || !form.stepModel"
                               :form="form" :read-only="readOnly"/>

          <ms-form-divider :title="$t('test_track.case.other_info')"/>

          <test-case-edit-other-info :read-only="readOnly" :project-id="projectIds" :form="form"
                                     :comments.sync="comments"
                                     :label-width="formLabelWidth" :case-id="form.id" ref="otherInfo"/>
          <test-case-comment :case-id="form.id"
                             @getComments="getComments" ref="testCaseComment"/>

        </el-form>

      </div>
      <ms-change-history ref="changeHistory"/>

      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        width="100%">

        <test-case-version-diff v-if="dialogVisible" :old-data="oldData" :new-data="newData"
                                :tree-nodes="treeNodes" :is-public="publicEnable"></test-case-version-diff>

      </el-dialog>


    </div>
    <batch-move ref="testBatchMove" :public-enable="publicEnable"
                @copyPublic="copyPublic"/>
  </el-card>


</template>

<script>
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import {getCurrentProjectID, getCurrentUser} from "metersphere-frontend/src/utils/token";
import {
  getUUID,
  handleCtrlSEvent,
  listenGoBack,
  removeGoBackListener
} from "metersphere-frontend/src/utils"
import {hasLicense} from "metersphere-frontend/src/utils/permission"
import TestCaseAttachment from "@/business/case/components/TestCaseAttachment";
import CaseComment from "@/business/case/components/CaseComment";
import MsInputTag from "metersphere-frontend/src/components/MsInputTag";
import MsPreviousNextButton from "metersphere-frontend/src/components/MsPreviousNextButton";
import TestCaseComment from "@/business/case/components/TestCaseComment";
import ReviewCommentItem from "@/business/review/commom/ReviewCommentItem";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import MsTestCaseStepRichText from "./MsRichText";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import TestCaseEditOtherInfo from "@/business/case/components/TestCaseEditOtherInfo";
import FormRichTextItem from "metersphere-frontend/src/components/FormRichTextItem";
import TestCaseStepItem from "@/business/case/components/TestCaseStepItem";
import StepChangeItem from "@/business/case/components/StepChangeItem";
import MsChangeHistory from "metersphere-frontend/src/components/history/ChangeHistory";
import BatchMove from "@/business/case/components/BatchMove";
import TestCaseVersionDiff from "@/business/case/version/TestCaseVersionDiff";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {useStore} from "@/store";
import {
  getTestCase,
  getTestCaseFollow,
  getTestCaseVersions,
  testCaseEditFollows,
  testCasePublicBatchCopy
} from "@/api/testCase";
import {testCaseCommentList} from "@/api/test-case-comment";
import {getProjectListAll, getProjectMemberOption} from "@/business/utils/sdk-utils";

export default {
  name: "TestCaseEditShow",
  components: {
    StepChangeItem,
    TestCaseStepItem,
    FormRichTextItem,
    TestCaseEditOtherInfo,
    MsFormDivider,
    MsTableButton,
    MsSelectTree,
    ReviewCommentItem,
    TestCaseComment, MsPreviousNextButton, MsInputTag, CaseComment, MsDialogFooter, TestCaseAttachment,
    MsTestCaseStepRichText,
    MsChangeHistory,
    BatchMove,
    'MsVersionHistory': MxVersionSelect,
    TestCaseVersionDiff
  },
  data() {
    return {
      selectIds: [],
      projectList: [],
      isPublic: false,
      isXpack: false,
      testCaseTemplate: {},
      comments: [],
      loading: false,
      dialogFormVisible: false,
      showFollow: false,
      form: {
        name: '',
        module: 'default-module',
        nodePath: '/未规划用例',
        maintainer: getCurrentUser().id,
        priority: 'P0',
        type: '',
        method: '',
        prerequisite: '',
        testId: '',
        otherTestName: '',
        steps: [{
          num: 1,
          desc: '',
          result: ''
        }],
        stepDesc: '',
        stepResult: '',
        selected: [],
        remark: '',
        tags: [],
        demandId: '',
        demandName: '',
        status: 'Prepare',
        reviewStatus: 'Prepare',
        stepDescription: '',
        expectedResult: '',
        stepModel: 'STEP',
        customNum: '',
        followPeople: '',
      },
      maintainerOptions: [],
      // testOptions: [],
      workspaceId: '',
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 255, message: this.$t('test_track.length_less_than') + '255', trigger: 'blur'}
        ],
        module: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        customNum: [
          {required: true, message: "ID必填", trigger: 'blur'},
          {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
        ],
        demandName: [{required: true, message: this.$t('test_track.case.input_demand_name'), trigger: 'change'}],
        maintainer: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        priority: [{required: true, message: this.$t('test_track.case.input_priority'), trigger: 'change'}],
        method: [{required: true, message: this.$t('test_track.case.input_method'), trigger: 'change'}],
      },
      formLabelWidth: "100px",
      operationType: '',
      isCreateContinue: false,
      isStepTableAlive: true,
      isFormAlive: true,
      methodOptions: [
        {value: 'auto', label: this.$t('test_track.case.auto')},
        {value: 'manual', label: this.$t('test_track.case.manual')}
      ],
      testCase: {},
      showInputTag: true,
      tableType: "",
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      tabId: getUUID(),
      versionData: [],
      currentProjectId: "",
      dialogVisible: false,
      oldData: null,
      newData: null,
      readOnly: true
    };
  },
  props: {
    treeNodes: {
      type: Array
    },
    currentTestCaseInfo: {},
    selectNode: {
      type: Object
    },
    selectCondition: {
      type: Object
    },
    type: String,
    publicEnable: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    projectIds() {
      return getCurrentProjectID();
    },
    moduleOptions() {
      return useStore().testCaseModuleOptions;
    },
    isCustomNum() {
      return useStore().currentProjectIsCustomNum;
    },
  },
  mounted() {
    this.getSelectOptions();
    this.open(this.currentTestCaseInfo)
    this.getComments(this.currentTestCaseInfo)
    // Cascader 级联选择器: 点击文本就让它自动点击前面的input就可以触发选择。
    setInterval(function () {
      document.querySelectorAll('.el-cascader-node__label').forEach(el => {
        el.onclick = function () {
          if (this.previousElementSibling) this.previousElementSibling.click();
        };
      });
    }, 1000);
    if (this.selectNode && this.selectNode.data && !this.form.id) {
      this.form.module = this.selectNode.data.id;
      this.form.nodePath = this.selectNode.data.path;
    }
    if ((!this.form.module || this.form.module === "default-module" || this.form.module === "root") && this.treeNodes.length > 0) {
      this.form.module = this.treeNodes[0].id;
      this.form.nodePath = this.treeNodes[0].path;
    }
    if (!(useStore().testCaseMap instanceof Map)) {
      useStore().testCaseMap = new Map();
    }
    useStore().testCaseMap.set(this.form.id, 0);
  },
  created() {
    if (!this.projectList || this.projectList.length === 0) {   //没有项目数据的话请求项目数据
      getProjectListAll()
        .then((response) => {
          this.projectList = response.data;  //获取当前工作空间所拥有的项目,
        });
    }
    this.projectId = this.projectIds;
    this.initAddFuc();
    getTestCaseFollow(this.currentTestCaseInfo.id)
      .then(response => {
        this.form.follows = response.data;
        for (let i = 0; i < response.data.length; i++) {
          if (response.data[i] === this.currentUser().id) {
            this.showFollow = true;
            break;
          }
        }
      });
    if (hasLicense()) {
      this.isXpack = true;
    } else {
      this.isXpack = false;
    }
    if (hasLicense()) {
      this.getVersionHistory();
    }
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    openHis() {
      this.$refs.changeHistory.open(this.form.id, ["测试用例", "測試用例", "Test case", "TRACK_TEST_CASE"]);
    },
    setModule(id, data) {
      this.form.module = id;
      this.form.nodePath = data.path;
    },
    initAddFuc() {
      if (this.selectNode && this.selectNode.data && !this.form.id) {
        this.form.module = this.selectNode.data.id;
        this.form.nodePath = this.selectNode.data.path;
      } else {
        this.form.module = this.treeNodes && this.length > 0 ? this.treeNodes[0].id : "";
      }
      this.form.module = this.currentTestCaseInfo.nodeId;
      this.form.nodePath = this.currentTestCaseInfo.nodePath;

      if ((!this.form.module || this.form.module === "default-module" || this.form.module === "root") && this.treeNodes.length > 0) {
        this.form.module = this.treeNodes[0].id;
        this.form.nodePath = this.treeNodes[0].path;
      }
    },
    setDefaultValue() {
      if (!this.form.prerequisite) {
        this.form.prerequisite = "";
      }
      if (!this.form.stepDescription) {
        this.form.stepDescription = "";
      }
      if (!this.form.expectedResult) {
        this.form.expectedResult = "";
      }
      if (!this.form.remark) {
        this.form.remark = "";
      }
      useStore().testCaseMap.set(this.form.id, 0);
    },
    openComment() {
      this.$refs.testCaseComment.open()
    },
    getComments(testCase) {
      let id = '';
      if (testCase) {
        id = testCase.id;
      } else {
        id = this.form.id;
      }
      testCaseCommentList(id)
        .then(res => {
          this.comments = res.data;
        });
    },
    showAll() {
      if (!this.customizeVisible) {
        this.selectedTreeNode = undefined;
      }
    },
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => {
        this.isStepTableAlive = true;
        useStore().testCaseMap.set(this.form.id, 0);
      });
    },
    reloadForm() {
      this.isFormAlive = false;
      this.$nextTick(() => (this.isFormAlive = true));
    },
    open(testCase) {
      this.projectId = this.projectIds;
      this.initEdit(testCase);
    },
    initEdit(testCase) {
      if (window.history && window.history.pushState) {
        history.pushState(null, null, document.URL);
        window.addEventListener('popstate', this.close);
      }
      this.resetForm();
      listenGoBack(this.close);
      this.getTestCase(testCase.id);
    },
    getTestCase(id) {
      this.showInputTag = false;
      if (!id) {
        id = this.currentTestCaseInfo.id;
      }
      this.loading = true;
      getTestCase(id)
        .then(response => {
          this.loading = false;
          let testCase = response.data;
          this.setFormData(testCase);
          this.setTestCaseExtInfo(testCase);
          this.getSelectOptions();
          this.reload();
          this.$nextTick(() => {
            this.showInputTag = true;
          });
        });
    },
    async setFormData(testCase) {
      try {
        testCase.selected = JSON.parse(testCase.testId);
      } catch (error) {
        testCase.selected = testCase.testId
      }
      let tmp = {};
      Object.assign(tmp, testCase);
      tmp.steps = JSON.parse(testCase.steps);
      if (!tmp.steps || tmp.steps.length < 1) {
        tmp.steps = [{
          num: 1,
          desc: '',
          result: ''
        }];
      }
      tmp.tags = JSON.parse(tmp.tags);
      Object.assign(this.form, tmp);
      if (!this.form.stepModel) {
        this.form.stepModel = "STEP";
      }
      this.casePublic = tmp.casePublic;
      this.form.module = testCase.nodeId;
      this.setDefaultValue();
      // 重新渲染，显示自定义字段的必填校验
      this.reloadForm();
    },
    setTestCaseExtInfo(testCase) {
      this.testCase = {};
      if (testCase) {
        // 复制 不查询评论
        this.testCase = testCase.isCopy ? {} : testCase;
      }
    },
    close() {
      //移除监听，防止监听其他页面
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
    },
    handleCopyPublic(testCase) {
      this.selectIds.push(this.form.id);
      this.$refs.testBatchMove.open(this.treeNodes, this.selectIds, this.moduleOptions);

    },
    copyPublic(param) {
      param.condition = this.condition;
      this.loading = true;
      testCasePublicBatchCopy(param)
        .then(() => {
          this.loading = false;
          this.$success(this.$t('commons.save_success'));
          this.$refs.testBatchMove.close();
          this.$emit("refresh", this.form);
        });
    },
    validate(param) {
      for (let i = 0; i < param.steps.length; i++) {
        if ((param.steps[i].desc && param.steps[i].desc.length > 300) ||
          (param.steps[i].result && param.steps[i].result.length > 300)) {
          this.$warning(this.$t('test_track.case.step_desc') + ","
            + this.$t('test_track.case.expected_results') + this.$t('test_track.length_less_than') + '300');
          return false;
        }
      }
      if (param.name === '') {
        this.$warning(this.$t('test_track.case.input_name'));
        return false;
      }
      return true;
    },
    typeChange() {
      this.form.testId = '';
    },
    getMaintainerOptions() {
      getProjectMemberOption()
        .then(response => {
          this.maintainerOptions = response.data;
        });
    },
    getSelectOptions() {
      this.getMaintainerOptions();
    },
    resetForm() {
      //防止点击修改后，点击新建触发校验
      if (this.$refs['caseFrom']) {
        this.$refs['caseFrom'].validate((valid) => {
          this.$refs['caseFrom'].resetFields();
          this._resetForm();
          return true;
        });
      } else {
        this._resetForm();
      }
    },
    _resetForm() {
      this.form.name = '';
      this.form.module = '';
      this.form.type = '';
      this.form.method = '';
      this.form.maintainer = '';
      this.form.priority = '';
      this.form.prerequisite = '';
      this.form.remark = '';
      this.form.testId = '';
      this.form.testName = '';
      this.form.steps = [{
        num: 1,
        desc: '',
        result: ''
      }];
      this.form.customNum = '';
    },
    createCtrlSHandle(event) {
      let curTabId = useStore().curTabId;
      if (curTabId === this.tabId)
        handleCtrlSEvent(event, this.saveCase);
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.form.follows.length; i++) {
          if (this.form.follows[i] === this.currentUser().id) {
            this.form.follows.splice(i, 1)
            break;
          }
        }
        this.loading = true;
        testCaseEditFollows(this.form.id, this.form.follows)
          .then(() => {
            this.loading = false
            this.$success(this.$t('commons.cancel_follow_success'));
          });
      } else {
        this.showFollow = true;
        if (!this.form.follows) {
          this.form.follows = [];
        }
        this.form.follows.push(this.currentUser().id)

        this.loading = true;
        testCaseEditFollows(this.form.id, this.form.follows)
          .then(() => {
            this.loading = false
            this.$success(this.$t('commons.follow_success'));
          });
      }
    },
    getVersionHistory() {
      getTestCaseVersions(this.currentTestCaseInfo.id)
        .then(response => {
          for (let i = 0; i < response.data.length; i++) {
            this.currentProjectId = response.data[i].projectId
          }
          this.versionData = response.data;
          this.$refs.versionHistory.loading = false;
        });
    },
    setSpecialPropForCompare: function (that) {
      that.newData.tags = JSON.parse(that.newData.tags || "");
      that.newData.steps = JSON.parse(that.newData.steps || "");
      that.oldData.tags = JSON.parse(that.oldData.tags || "");
      that.oldData.steps = JSON.parse(that.oldData.steps || "");
      that.newData.readOnly = true;
      that.oldData.readOnly = true;
    },
    changeType(type) {
      this.type = type;
    }
  }
}
</script>

<style scoped>

.el-switch {
  margin-bottom: 10px;
}

.case-name {
  width: 194px;
}

.container {
  height: 100vh;
}

.case-form {
  height: 95%;
  overflow: auto;
}

.case-dialog :deep(.el-dialog__body) {
  padding: 0 20px 10px 20px;
}

.container :deep(.el-card__body) {
  height: calc(100vh - 120px);
}

.comment-card :deep(.el-card__header) {
  padding: 27px 20px;
}

.comment-card :deep(.el-card__body) {
  height: calc(100vh - 120px);
}

.head-right {
  text-align: right;
}

.ms-main-div {
  background-color: white;
}

.ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 9;
}

.ms-case-input {
  width: 100%;
}

.ms-case {
  width: 100%;
}

:deep(.el-button-group > .el-button:first-child) {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  height: 32px;
  width: 56px;
}

.other-info-tabs {
  padding-left: 60px;
  margin-left: 40px;
}
</style>
