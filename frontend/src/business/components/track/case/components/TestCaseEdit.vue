<template>
  <el-card :bodyStyle="{padding:'0px'}">
    <div class="card-content">
      <div class="ms-main-div" @click="showAll">
        <ms-container v-loading="result.loading" style="overflow: auto">
          <ms-aside-container :height="pageHight">
            <test-case-base-info
              :form="form"
              :is-form-alive="isFormAlive"
              :isloading="result.loading"
              :read-only="readOnly"
              :public-enable="publicEnable"
              :show-input-tag="showInputTag"
              :tree-nodes="treeNodes"
              :project-list="projectList"
              :custom-field-form="customFieldForm"
              :custom-field-rules="customFieldRules"
              :test-case-template="testCaseTemplate"
              :default-open="richTextDefaultOpen"
              ref="testCaseBaseInfo"
            />
          </ms-aside-container>
          <ms-main-container :style="{height: pageHight + 'px'}">
            <el-form :model="form" :rules="rules" ref="caseFrom" class="case-form">

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
                                    :is-read="currentTestCaseInfo.trashEnable"
                                    @confirmOtherInfo="confirmOtherInfo"
                                    :current-project-id="currentProjectId"
                                    @compare="compare" @checkout="checkout" @create="create" @del="del"/>
                <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand"
                             @command="handleCommand" size="small" style="float: right;margin-right: 20px"
                             v-if="(this.path ==='/test/case/add') || (this.isPublic && this.isXpack)"
                             :disabled="readOnly">
                  {{ $t('commons.save') }}
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="ADD_AND_CREATE" v-if="this.path =='/test/case/add'">{{
                        $t('test_track.case.save_create_continue')
                      }}
                    </el-dropdown-item>
                    <el-dropdown-item command="ADD_AND_PUBLIC" v-if="this.isPublic && this.isXpack">{{
                        $t('test_track.case.save_add_public')
                      }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
                <el-button v-else type="primary" class="ms-api-buttion" @click="handleCommand"
                           :disabled="readOnly"
                           @command="handleCommand" size="small" style="float: right;margin-right: 20px">
                  {{ $t('commons.save') }}
                </el-button>
              </div>
              <ms-form-divider :title="$t('test_track.case.step_info')"/>

              <form-rich-text-item :disabled="readOnly"
                                   :label-width="formLabelWidth"
                                   :title="$t('test_track.case.prerequisite')"
                                   :data="form"
                                   :default-open="richTextDefaultOpen"
                                   prop="prerequisite"/>

              <step-change-item :label-width="formLabelWidth" :form="form"/>
              <form-rich-text-item v-if="form.stepModel === 'TEXT'"
                                   prop="stepDescription"
                                   :disabled="readOnly"
                                   :label-width="formLabelWidth"
                                   :title="$t('test_track.case.step_desc')"
                                   :data="form"
                                   :default-open="richTextDefaultOpen"/>

              <form-rich-text-item v-if="form.stepModel === 'TEXT'"
                                   prop="expectedResult"
                                   :disabled="readOnly"
                                   :label-width="formLabelWidth"
                                   :title="$t('test_track.case.expected_results')"
                                   :data="form"
                                   :default-open="richTextDefaultOpen"/>

              <test-case-step-item v-if="form.stepModel === 'STEP' || !form.stepModel"
                                   :label-width="formLabelWidth"
                                   :form="form"
                                   :read-only="readOnly"/>

              <ms-form-divider :title="$t('test_track.case.other_info')"/>

              <test-case-edit-other-info :read-only="readOnly" :project-id="projectIds" :form="form"
                                         :is-copy="currentTestCaseInfo.isCopy"
                                         :copy-case-id="copyCaseId"
                                         :label-width="formLabelWidth" :case-id="form.id"
                                         :type="type" :comments.sync="comments"
                                         @openComment="openComment"
                                         :is-click-attachment-tab.sync="isClickAttachmentTab"
                                         :version-enable="versionEnable"
                                         :default-open="richTextDefaultOpen"
                                         ref="otherInfo"/>
              <test-case-comment :case-id="form.id"
                                 @getComments="getComments" ref="testCaseComment"/>
            </el-form>
          </ms-main-container>
        </ms-container>
      </div>
      <ms-change-history ref="changeHistory"/>
      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        width="100%"
      >
        <test-case-version-diff v-if="dialogVisible" :old-data="oldData" :new-data="newData"
                                :tree-nodes="treeNodes"></test-case-version-diff>

      </el-dialog>

      <version-create-other-info-select @confirmOtherInfo="confirmOtherInfo"
                                        ref="selectPropDialog"></version-create-other-info-select>
    </div>
  </el-card>


</template>

<script>
import {TokenKey} from '@/common/js/constants';
import MsDialogFooter from '../../../common/components/MsDialogFooter';
import {
  getCurrentProjectID,
  getCurrentUser,
  getNodePath,
  getUUID,
  handleCtrlSEvent,
  hasLicense,
  hasPermission,
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import CaseComment from "@/business/components/track/case/components/CaseComment";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import MsPreviousNextButton from "../../../common/components/MsPreviousNextButton";
import {STEP} from "@/business/components/api/automation/scenario/Setting";
import TestCaseComment from "@/business/components/track/case/components/TestCaseComment";
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";
import {API_STATUS, REVIEW_STATUS} from "@/business/components/api/definition/model/JsonData";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsSelectTree from "../../../common/select-tree/SelectTree";
import MsTestCaseStepRichText from "./MsRichText";
import CustomFiledComponent from "@/business/components/project/template/CustomFiledComponent";
import {buildCustomFields, buildTestCaseOldFields, getTemplate, parseCustomField} from "@/common/js/custom_field";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import TestCaseEditOtherInfo from "@/business/components/track/case/components/TestCaseEditOtherInfo";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import TestCaseStepItem from "@/business/components/track/case/components/TestCaseStepItem";
import StepChangeItem from "@/business/components/track/case/components/StepChangeItem";
import MsChangeHistory from "../../../history/ChangeHistory";
import {getTestTemplate} from "@/network/custom-field-template";
import CustomFiledFormItem from "@/business/components/common/components/form/CustomFiledFormItem";
import TestCaseVersionDiff from "@/business/components/track/case/version/TestCaseVersionDiff";
import VersionCreateOtherInfoSelect from "@/business/components/track/case/components/VersionCreateOtherInfoSelect";
import TestCaseBaseInfo from "@/business/components/track/case/components/TestCaseBaseInfo";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const versionHistory = requireComponent.keys().length > 0 ? requireComponent("./version/VersionHistory.vue") : {};

export default {
  name: "TestCaseEdit",
  components: {
    CustomFiledFormItem,
    StepChangeItem,
    TestCaseStepItem,
    FormRichTextItem,
    TestCaseEditOtherInfo,
    MsFormDivider,
    CustomFiledComponent,
    MsTableButton,
    MsSelectTree,
    ReviewCommentItem,
    TestCaseComment, MsPreviousNextButton, MsInputTag, CaseComment, MsDialogFooter, TestCaseAttachment,
    MsTestCaseStepRichText,
    MsChangeHistory,
    'MsVersionHistory': versionHistory.default,
    TestCaseVersionDiff,
    VersionCreateOtherInfoSelect,
    TestCaseBaseInfo,
    MsContainer,
    MsAsideContainer,
    MsMainContainer,
  },
  data() {
    return {
      // sysList: [],//一级选择框的数据
      path: "/test/case/add",
      isPublic: false,
      isXpack: false,
      testCaseTemplate: {},
      pageHight: document.documentElement.clientHeight - 150 + '',
      projectList: [],
      options: REVIEW_STATUS,
      statuOptions: API_STATUS,
      comments: [],
      result: {},
      dialogFormVisible: false,
      showFollow: false,
      isValidate: false,
      currentValidateName: "",
      type: "",
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
        // prerequisite: [{max: 500, message: this.$t('test_track.length_less_than') + '500', trigger: 'blur'}],
        // remark: [{max: 1000, message: this.$t('test_track.length_less_than') + '1000', trigger: 'blur'}]
      },
      customFieldRules: {},
      customFieldForm: null,
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
      copyCaseId: "",
      showInputTag: true,
      tableType: "",
      stepFilter: new STEP,
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      tabId: getUUID(),
      versionData: [],
      dialogVisible: false,
      oldData: null,
      newData: null,
      selectedOtherInfo: null,
      currentProjectId: "",
      casePublic: false,
      isClickAttachmentTab: false
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
    caseType: String,
    publicEnable: {
      type: Boolean,
      default: false,
    },
    activeName: String,
    versionEnable: Boolean,
  },
  computed: {
    projectIds() {
      return getCurrentProjectID();
    },
    moduleOptions() {
      return this.$store.state.testCaseModuleOptions;
    },
    isCustomNum() {
      return this.$store.state.currentProjectIsCustomNum;
    },
    richTextDefaultOpen() {
      return this.type === 'edit' ? 'preview' : 'edit';
    },
    readOnly() {
      const {rowClickHasPermission} = this.currentTestCaseInfo;
      if (rowClickHasPermission !== undefined) {
        return !rowClickHasPermission;
      }
      return !hasPermission('PROJECT_TRACK_CASE:READ+CREATE') &&
        !hasPermission('PROJECT_TRACK_CASE:READ+EDIT');
    }
  },
  watch: {
    form: {
      handler(val) {
        if (val && this.$store.state.testCaseMap && this.form.id) {
          let change = this.$store.state.testCaseMap.get(this.form.id);
          change = change + 1;
          this.$store.state.testCaseMap.set(this.form.id, change);
        }
      },
      deep: true
    },
    customFieldForm: {
      handler(val) {
        if (val && this.$store.state.testCaseMap && this.form.id) {
          let change = this.$store.state.testCaseMap.get(this.form.id);
          change = change + 1;
          this.$store.state.testCaseMap.set(this.form.id, change);
        }
      },
      deep: true
    }
  },
  beforeDestroy() {
    this.removeListener();
  },
  mounted() {
    this.getSelectOptions();
    if (this.type === 'edit' || this.type === 'copy') {
      this.open(this.currentTestCaseInfo);
    }
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
    if (!(this.$store.state.testCaseMap instanceof Map)) {
      this.$store.state.testCaseMap = new Map();
    }
    if (this.form.id) {
      this.$store.state.testCaseMap.set(this.form.id, 0);
    }

  },
  created() {
    this.type = this.caseType;
    if (!this.projectList || this.projectList.length === 0) {   //没有项目数据的话请求项目数据
      this.$get("/project/listAll", (response) => {
        this.projectList = response.data;  //获取当前工作空间所拥有的项目,
      })
    }
    this.projectId = this.projectIds;
    let initAddFuc = this.initAddFuc;
    getTestTemplate()
      .then((template) => {
        this.testCaseTemplate = template;
        this.$store.commit('setTestCaseTemplate', this.testCaseTemplate);
        initAddFuc();
      });
    if (this.selectNode && this.selectNode.data && !this.form.id) {
      this.form.module = this.selectNode.data.id;
      this.form.nodePath = this.selectNode.data.path;
    } else {
      this.form.module = this.treeNodes && this.length > 0 ? this.treeNodes[0].id : "";
    }
    if (this.type === 'edit' || this.type === 'copy') {
      this.form.module = this.currentTestCaseInfo.nodeId;
      this.form.nodePath = this.currentTestCaseInfo.nodePath;
    }
    if ((!this.form.module || this.form.module === "default-module" || this.form.module === "root") && this.treeNodes.length > 0) {
      this.form.module = this.treeNodes[0].id;
      this.form.nodePath = this.treeNodes[0].path;
    }
    this.$get('/test/case/follow/' + this.currentTestCaseInfo.id, response => {
      this.form.follows = response.data;
      for (let i = 0; i < response.data.length; i++) {
        if (response.data[i] === this.currentUser().id) {
          this.showFollow = true;
          break;
        }
      }
    }),
      this.result = this.$get('/project_application/get/config/' + this.projectId + "/CASE_PUBLIC", res => {
        let data = res.data;
        if (data && data.casePublic) {
          this.isPublic = true;
        }
      })
    if (hasLicense()) {
      this.isXpack = true;
    } else {
      this.isXpack = false;
    }
    if (hasLicense()) {
      this.getVersionHistory();
    }

    //浏览器拉伸时窗口编辑窗口自适应
    this.$nextTick(() => {
      // 解决错位问题
      window.addEventListener('resize', this.resizeContainer);
    });
  },
  methods: {
    alert: alert,
    currentUser: () => {
      return getCurrentUser();
    },
    resizeContainer() {
      this.pageHight = document.documentElement.clientHeight - 150 + '';
    },
    openHis() {
      this.$refs.changeHistory.open(this.form.id, ["测试用例", "測試用例", "Test case", "TRACK_TEST_CASE"]);
    },
    setModule(id, data) {
      this.form.module = id;
      this.form.nodePath = data.path;
    },
    initAddFuc() {
      // this.loadOptions();
      this.addListener(); //  添加 ctrl s 监听
      if (this.selectNode && this.selectNode.data && !this.form.id) {
        this.form.module = this.selectNode.data.id;
        this.form.nodePath = this.selectNode.data.path;
      } else {
        this.form.module = this.treeNodes && this.length > 0 ? this.treeNodes[0].id : "";
      }
      if (this.type === 'edit' || this.type === 'copy') {
        this.form.module = this.currentTestCaseInfo.nodeId;
        this.form.nodePath = this.currentTestCaseInfo.nodePath;
      }
      if ((!this.form.module || this.form.module === "default-module" || this.form.module === "root") && this.treeNodes.length > 0) {
        this.form.module = this.treeNodes[0].id;
        this.form.nodePath = this.treeNodes[0].path;
      }
      if (this.type === 'add') {
        //设置自定义熟悉默认值
        this.customFieldForm = parseCustomField(this.form, this.testCaseTemplate, this.customFieldRules);
        this.form.name = this.testCaseTemplate.caseName;
        this.form.stepDescription = this.testCaseTemplate.stepDescription;
        this.form.expectedResult = this.testCaseTemplate.expectedResult;
        this.form.prerequisite = this.testCaseTemplate.prerequisite;
        this.form.stepModel = this.testCaseTemplate.stepModel;
        if (this.testCaseTemplate.steps) {
          this.form.steps = JSON.parse(this.testCaseTemplate.steps);
        }
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
      if (this.form.id) {
        this.$store.state.testCaseMap.set(this.form.id, 0);
      }
    },
    handleCommand(e) {
      if (e === "ADD_AND_CREATE") {
        this.$refs['caseFrom'].validate((valid) => {
          if (!valid) {
            this.saveCase();
          } else {
            this.saveCase(function (t) {
              let tab = {};
              tab.name = 'add';
              t.$emit('addTab', tab);
            });
          }
        })
      } else if (e === 'ADD_AND_PUBLIC') {
        this.casePublic = true;
        this.saveCase();
      } else {
        this.saveCase();
      }
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
      this.result = this.$get('/test/case/comment/list/' + id, res => {
        this.comments = res.data;
      })
    },
    showAll() {
      if (!this.customizeVisible) {
        this.operatingElements = this.stepFilter.get("ALL");
        this.selectedTreeNode = undefined;
      }
      //this.reload();
    },
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => {
        this.isStepTableAlive = true;
        if (this.form.id) {
          this.$store.state.testCaseMap.set(this.form.id, 0);
        }
      });
    },
    reloadForm() {
      this.isFormAlive = false;
      this.$nextTick(() => (this.isFormAlive = true));
    },
    open(testCase) {
      /*
             this.form.selected=[["automation", "3edaaf31-3fa4-4a53-9654-320205c2953a"],["automation", "3aa58bd1-c986-448c-8060-d32713dbd4eb"]]
      */
      this.projectId = this.projectIds;
      let initFuc = this.initEdit;
      getTemplate('field/template/case/get/relate/', this)
        .then((template) => {
          this.testCaseTemplate = template;
          this.$store.commit('setTestCaseTemplate', this.testCaseTemplate);
          initFuc(testCase);
        });
    },
    initEdit(testCase, callback) {
      if (window.history && window.history.pushState) {
        history.pushState(null, null, document.URL);
        window.addEventListener('popstate', this.close);
      }
      this.resetForm();
      listenGoBack(this.close);
      this.operationType = 'add';
      if (testCase) {
        //修改
        this.operationType = 'edit';
        this.copyCaseId = '';
        //复制
        if (this.type === 'copy') {
          this.showInputTag = false;
          this.operationType = 'add';
          this.copyCaseId = testCase.copyId;
          this.setFormData(testCase);
          this.setTestCaseExtInfo(testCase);
          this.getSelectOptions();
          this.reload();
          this.$nextTick(() => {
            this.showInputTag = true;
          });
          this.form.id = null;
        } else {
          this.getTestCase(testCase.id);
        }
      } else {
        // add
        if (this.selectNode.data) {
          this.form.module = this.selectNode.data.id;
        } else {
          if (this.moduleOptions.length > 0) {
            this.form.module = this.moduleOptions[0].id;
          }
        }
        let user = JSON.parse(localStorage.getItem(TokenKey));
        this.copyCaseId = '';
        this.form.priority = 'P3';
        this.form.type = 'functional';
        this.form.method = 'manual';
        this.form.maintainer = user.id;
        this.form.tags = [];
        this.getSelectOptions();
        this.customFieldForm = parseCustomField(this.form, this.testCaseTemplate, this.customFieldRules);
        this.reload();
      }
      if (callback) {
        callback();
      }
      if (this.type !== 'copy') {
        this.getComments(this.currentTestCaseInfo);
      }
    },
    getTestCase(id) {
      this.showInputTag = false;
      if (!id) {
        id = this.currentTestCaseInfo.id;
      }
      this.result = this.$get('/test/case/get/' + id, response => {
        if (response.data) {
          this.path = "/test/case/edit";
          if (this.currentTestCaseInfo.isCopy) {
            this.path = "/test/case/add";
          }
        } else {
          this.path = "/test/case/add";
        }
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
      //设置自定义熟悉默认值
      this.customFieldForm = parseCustomField(this.form, this.testCaseTemplate, this.customFieldRules, testCase ? buildTestCaseOldFields(this.form) : null);
      this.setDefaultValue();
      this.resetSystemField();
      // 重新渲染，显示自定义字段的必填校验
      this.reloadForm();
    },
    resetSystemField() {
      if (this.operationType === 'add') {
        return;
      }
      // 用例等级等字段以表中对应字段为准，后端复杂操作直接改表中对应字段即可
      this.from;
      this.customFieldForm['用例等级'] = this.form.priority;
      this.customFieldForm['责任人'] = this.form.maintainer;
      this.customFieldForm['用例状态'] = this.form.status;
      this.testCaseTemplate.customFields.forEach(field => {
        if (field.name === '用例等级') {
          field.defaultValue = this.form.priority;
        } else if (field.name === '责任人') {
          field.defaultValue = this.form.maintainer;
        } else if (field.name === '用例状态') {
          field.defaultValue = this.form.status;
        }
      });
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
    saveCase(callback) {
      if (this.validateForm()) {
        this._saveCase(callback);
      } else {
        this.$refs.versionHistory.loading = false;
        this.$refs.selectPropDialog.close();
      }
    },
    _saveCase(callback) {
      let param = this.buildParam();
      if (this.validate(param)) {
        let option = this.getOption(param);
        this.result = this.$request(option, (response) => {
          // 保存用例后刷新附件
          this.currentTestCaseInfo.isCopy = false;
          this.$refs.otherInfo.getFileMetaData(response.data.id);
          this.$success(this.$t('commons.save_success'));
          this.path = "/test/case/edit";
          // this.operationType = "edit"
          this.$emit("refreshTestCase",);
          this.$store.state.testCaseMap.set(this.form.id, 0);
          //this.tableType = 'edit';
          this.$emit("refresh", response.data);
          if (this.form.id) {
            this.$emit("caseEdit", param);
          } else {
            param.id = response.data.id;
            this.$emit("caseCreate", param);
            this.type = 'edit';
            this.close();
          }
          this.form.id = response.data.id;
          this.currentTestCaseInfo.id = response.data.id;
          this.form.refId = response.data.refId;
          this.currentTestCaseInfo.refId = response.data.refId;
          if (this.currentTestCaseInfo.isCopy) {
            this.currentTestCaseInfo.isCopy = null;
          }
          if (callback) {
            callback(this);
          }
          //更新版本
          if (hasLicense()) {
            this.getVersionHistory();
          }
        });
      }
    },
    buildParam() {
      let param = {};
      Object.assign(param, this.form);
      param.steps = JSON.stringify(this.form.steps);
      param.nodeId = this.form.module;
      param.copyCaseId = this.copyCaseId
      if (!this.publicEnable) {
        param.nodePath = getNodePath(this.form.module, this.moduleOptions);
        if (this.projectId) {
          param.projectId = this.projectId;
        }
      }
      if (this.publicEnable) {
        this.casePublic = true;
      }
      param.name = param.name.trim();
      if (this.form.tags instanceof Array) {
        this.form.tags = JSON.stringify(this.form.tags);
      }
      //当 testId 为其他信息的时候必须删除该字段避免后端反序列化报错
      if ("other" != this.form.selected) {
        param.testId = JSON.stringify(this.form.selected);
      } else {
        delete param.selected;
      }
      param.tags = this.form.tags;
      param.casePublic = this.casePublic;
      param.type = 'functional';
      buildCustomFields(this.form, param, this.testCaseTemplate);
      this.parseOldFields(param);
      //配置多版本复制的时候是否要连带复制其他信息
      if (this.selectedOtherInfo) {
        param.otherInfoConfig = this.selectedOtherInfo;
      }
      if (this.$refs.otherInfo.relateFiles.length > 0) {
        param.relateFileMetaIds = this.$refs.otherInfo.relateFiles;
      }
      if (this.$refs.otherInfo.unRelateFiles.length > 0) {
        param.unRelateFileMetaIds = this.$refs.otherInfo.unRelateFiles;
      }
      return param;
    },
    parseOldFields(param) {
      let customFields = this.testCaseTemplate.customFields;
      customFields.forEach(item => {
        if (item.name === '用例等级') {
          param.priority = item.defaultValue;
        }
        if (item.name === '责任人') {
          param.maintainer = item.defaultValue;
        }
        if (item.name === '用例状态') {
          param.status = item.defaultValue;
        }
      });
    },
    getOption(param) {
      let formData = new FormData();
      let requestJson = JSON.stringify(param, function (key, value) {
        return key === "file" ? undefined : value
      });

      if (this.$refs.otherInfo.uploadFiles.length > 0) {
        this.$refs.otherInfo.uploadFiles.forEach(f => {
          formData.append("file", f);
        });
      }
      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));
      return {
        method: 'POST',
        url: this.path,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
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
      this.$get('/user/project/member/list', response => {
        this.maintainerOptions = response.data;
      });
    },
    getSelectOptions() {
      this.getMaintainerOptions();
    },
    resetForm() {
      //防止点击修改后，点击新建触发校验
      if (this.$refs['caseFrom']) {
        this.$refs['caseFrom'].validate(() => {
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
      this.form.tags = [];
    },
    addListener() {
      document.addEventListener("keydown", this.createCtrlSHandle);
    },
    removeListener() {
      document.removeEventListener("keydown", this.createCtrlSHandle);
    },
    createCtrlSHandle(event) {
      let curTabId = this.$store.state.curTabId;
      if (curTabId === this.tabId) {
        if (event.keyCode === 83 && event.ctrlKey && this.readOnly) {
          this.$warning(this.$t("commons.no_operation_permission"));
          return false;
        }
        handleCtrlSEvent(event, this.saveCase);
      }
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
        if (this.path === "/test/case/edit") {
          this.result.loading = true
          this.$post('/test/case/edit/follows/' + this.form.id, this.form.follows, () => {
            this.result.loading = false
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }

      } else {
        this.showFollow = true;
        if (!this.form.follows) {
          this.form.follows = [];
        }
        this.form.follows.push(this.currentUser().id)

        if (this.path === "/test/case/edit") {
          this.result.loading = true
          this.$post('/test/case/edit/follows/' + this.form.id, this.form.follows, () => {
            this.result.loading = false
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getVersionHistory(param) {
      this.$get('/test/case/versions/' + this.currentTestCaseInfo.id, response => {
        if (response.data.length > 0) {
          for (let i = 0; i < response.data.length; i++) {
            this.currentProjectId = response.data[i].projectId;
          }
        } else {
          this.currentProjectId = getCurrentProjectID();
        }
        this.versionData = response.data;
        if (this.$refs.versionHistory) {
          this.$refs.versionHistory.loading = false;
        }
      });
    },
    setSpecialPropForCompare: function (that) {
      that.newData.tags = JSON.parse(that.newData.tags || "{}");
      that.newData.steps = JSON.parse(that.newData.steps || "{}");
      that.oldData.tags = JSON.parse(that.oldData.tags || "{}");
      that.oldData.steps = JSON.parse(that.oldData.steps || "{}");
      that.newData.readOnly = true;
      that.oldData.readOnly = true;
    },
    compare(row) {
      this.$get('/test/case/get/' + row.id + "/" + this.currentTestCaseInfo.refId, response => {
        let p1 = this.$get('/test/case/get/' + response.data.id);
        let p2 = this.$get('/test/case/get/' + this.currentTestCaseInfo.id);
        let that = this;
        Promise.all([p1, p2]).then(data => {
          if (data[0] && data[1]) {
            that.newData = data[0].data.data;
            that.oldData = data[1].data.data;
            that.newData.createTime = row.createTime;
            that.oldData.createTime = this.$refs.versionHistory.versionOptions.filter(v => v.id === that.oldData.versionId)[0].createTime;
            that.newData.versionName = that.versionData.filter(v => v.id === that.newData.id)[0].versionName;
            that.oldData.versionName = that.versionData.filter(v => v.id === that.oldData.id)[0].versionName;
            that.newData.userName = response.data.createName
            that.oldData.userName = that.versionData.filter(v => v.id === that.oldData.id)[0].createName
            this.setSpecialPropForCompare(that);
            that.dialogVisible = true;
          }
        });
      });
    },
    checkout(row) {
      this.$refs.versionHistory.loading = true;
      let testCase = this.versionData.filter(v => v.versionId === row.id)[0];

      if (testCase) {
        this.$get('test/case/get/' + testCase.id, response => {
          let testCase = response.data;
          this.$emit("checkout", testCase);
          this.$refs.versionHistory.loading = false;
        });
      }
    },
    validateForm() {
      let isValidate = true;
      this.$refs['caseFrom'].validate((valid) => {
        if (!valid) {
          isValidate = false;
          return false;
        }
      });
      let baseInfoValidate = this.$refs.testCaseBaseInfo.validateForm();
      if (!baseInfoValidate) {
        return false;
      }
      let customValidate = this.$refs.testCaseBaseInfo.validateCustomForm();
      if (!customValidate) {
        let customFieldFormFields = this.$refs.testCaseBaseInfo.getCustomFields();
        for (let i = 0; i < customFieldFormFields.length; i++) {
          let customField = customFieldFormFields[i];
          if (customField.validateState === 'error') {
            if (this.currentValidateName) {
              this.currentValidateName = this.currentValidateName + "," + customField.label
            } else {
              this.currentValidateName = customField.label
            }
          }
        }
        this.isValidate = true;
        this.$warning(this.currentValidateName + this.$t('commons.cannot_be_null'));
        this.currentValidateName = '';
        return false;
      }
      return isValidate;
    },
    async create(row) {
      if (this.validateForm()) {
        // 创建新版本
        this.form.versionId = row.id;
        let hasOtherInfo = await this.hasOtherInfo();
        if (hasOtherInfo) {
          this.$refs.versionHistory.loading = false;
          this.$refs.selectPropDialog.open();
        } else {
          this.saveCase();
          if (this.$refs.versionHistory) {
            this.$refs.versionHistory.loading = false;
          }
        }
      } else {
        this.$refs.versionHistory.loading = false;
      }
    },
    del(row) {
      let that = this;
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/test/case/delete/' + row.id + '/' + this.form.refId, () => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
              this.$emit("refresh");
            });
          } else {
            that.$refs.versionHistory.loading = false;
          }
        }
      });
    },
    changeType(type) {
      this.type = type;
    },
    hasOtherInfo() {
      return new Promise((resolve) => {
          if (this.form.id) {
            this.$get("test/case/hasOtherInfo/" + this.form.id, (res) => {
              resolve(res.data);
            });
          } else {
            resolve();
          }
        }
      );
    },
    confirmOtherInfo(selectedOtherInfo) {
      this.selectedOtherInfo = selectedOtherInfo;
      this.saveCase();
    },
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

.case-dialog >>> .el-dialog__body {
  padding: 0 20px 10px 20px;
}

.container >>> .el-card__body {
  height: calc(100vh - 120px);
}

.comment-card >>> .el-card__header {
  padding: 27px 20px;
}

.comment-card >>> .el-card__body {
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
  z-index: 10;
}

.ms-case-input {
  width: 100%;
}

.ms-case {
  width: 100%;
}

/deep/ .el-button-group > .el-button:first-child {
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
