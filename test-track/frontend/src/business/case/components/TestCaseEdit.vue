<template>
  <div class="case-edit-wrap">
    <!-- since v2.6 -->
    <div class="case-edit-box">
      <!-- 创建 or 编辑用例 -->
      <div class="edit-header-container">
        <div class="header-content-row">
          <div class="back" @click="back" v-if="!isPublicShow">
            <img src="/assets/module/figma/icon_arrow-left_outlined.svg" alt="" />
          </div>
          <div
            @click.stop="openNewTab"
            :class="editable ? 'case-name' : ['case-name-hover', 'case-name']"
          >
            {{ !editable ? form.name : $t('test_track.case.create_case') }}
          </div>
          <div class="case-edit" v-if="!editable">
            <div class="case-level" v-if="!isPublicShow">
              <priority-table-item :value="form.priority" />
            </div>
            <div>
              <!--  版本历史 v-xpack -->
              <mx-version-history
                ref="versionHistory"
                :version-data="versionData"
                :current-id="currentTestCaseInfo.id"
                :is-read="readOnly"
                @confirmOtherInfo="confirmOtherInfo"
                :current-project-id="currentProjectId"
                :has-latest="hasLatest"
                @setLatest="setLatest"
                @compare="compare"
                @compareBranch="compareBranch"
                @checkout="checkout"
                @create="create"
                @del="del"
              >
                <div class="version-box case-version" slot="versionLabel">
                  <div class="version-icon">
                    <img
                      src="/assets/module/figma/icon_moments-categories_outlined.svg"
                      alt=""
                    />
                  </div>
                  <div class="version-title">{{ currentVersionName }}</div>
                  <div class="version-suffix">{{ $t("commons.version") }}</div>
                </div>
              </mx-version-history>
            </div>
          </div>
        </div>
        <div class="header-opt-row" v-if="!editable">
          <div
            class="previous-public-row head-opt"
            :class="{'disable-row': isFirstPublic}"
            v-if="isPublicShow"
            @click="showPreviousPublicCase"
          >
            <div class="icon-row">
              <span class="el-icon-arrow-left"></span>
            </div>
            <div class="label-row">{{ $t("case.previous_public_case") }}</div>
          </div>
          <div
            class="next-public-row head-opt"
            :class="{'disable-row': isLastPublic}"
            v-if="isPublicShow"
            @click="showNextPublicCase"
          >
            <div class="label-row">{{ $t("case.next_public_case") }}</div>
            <div class="icon-row">
              <span class="el-icon-arrow-right"></span>
            </div>
          </div>
          <div v-if="isPublicShow">
            <span class="separator-row">|</span>
          </div>
          <div
            class="follow-row head-opt"
            v-if="!showFollow"
            @click="saveFollow"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_collection_outlined.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("case.follow") }}</div>
          </div>
          <div
            class="follow-row head-opt"
            v-if="showFollow"
            @click="saveFollow"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_collect_filled.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("case.followed") }}</div>
          </div>
          <div
            class="add-public-row head-opt"
            v-if="!isPublicShow && !casePublic"
            @click="addPublic"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_add-folder_outlined.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("case.add_to_public_case") }}</div>
          </div>
          <div
            class="add-public-row head-opt"
            v-if="!isPublicShow && casePublic"
            @click="removePublic"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_yes_outlined.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("case.added_to_public_case") }}</div>
          </div>
          <div class="more-row head-opt" v-if="!isPublicShow">
            <div class="icon-row">
              <img src="/assets/module/figma/icon_more_outlined.svg" alt="" />
            </div>
            <div class="label-row">
              <el-popover
                placement="bottom-start"
                trigger="hover"
                popper-class="case-step-item-popover"
                :visible-arrow="false"
              >
                <div class="opt-row">
                  <div class="copy-row sub-opt-row" @click="copyRow">
                    <div class="icon">
                      <i class="el-icon-copy-document"></i>
                    </div>
                    <div class="title">{{ $t("commons.copy") }}</div>
                  </div>
                  <div class="split"></div>
                  <div class="delete-row sub-opt-row" @click="deleteRow">
                    <div class="icon">
                      <i class="el-icon-delete"></i>
                    </div>
                    <div class="title">{{ $t("commons.delete") }}</div>
                  </div>
                </div>
                <div slot="reference">{{ $t("case.more") }}</div>
              </el-popover>
            </div>
          </div>
          <div
            class="edit-public-row head-opt"
            v-if="isPublicShow"
            @click="editPublicCase"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_edit_outlined.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("commons.edit") }}</div>
          </div>
          <div
            class="copy-public-row head-opt"
            v-if="isPublicShow"
            @click="copyPublicCase"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_copy_outlined.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("commons.copy") }}</div>
          </div>
          <div v-if="isPublicShow">
            <span class="separator-row">|</span>
          </div>
          <div
            class="close-row head-opt"
            v-if="isPublicShow"
            @click="closePublicCase"
          >
            <span class="el-icon-close"></span>
          </div>
        </div>
      </div>
      <!-- 检测版本 是否不是最新 -->
      <div class="diff-latest-container" v-if="!editable && versionEnable && !isLastedVersion">
        <div class="left-view-row">
          <div class="view-icon"><img src="/assets/module/figma/icon_warning_colorful.svg" alt=""></div>
          <div class="view-content">{{$t("case.current_display_history_version")}}</div>
        </div>
        <div class="right-diff-opt">
          <div class="diff-latest" @click="diffWithLatest">{{$t("case.compare_with_the_latest_version")}}</div>
          <div class="show-latest" @click="checkoutLatest">{{$t("case.view_the_latest_version")}}</div>
        </div>
      </div>
      <!-- 正文 -->
      <div class="edit-content-container" :class="{'editable-edit-content-container' : editable}">
        <case-edit-info-component
          :editable="editable"
          :richTextDefaultOpen="richTextDefaultOpen"
          :formLabelWidth="formLabelWidth"
          :read-only="readOnly"
          :project-id="projectId"
          :form="form"
          :is-copy="isCopy"
          :copy-case-id="caseId"
          :label-width="formLabelWidth"
          :case-id="caseId"
          :type="type"
          :comments.sync="comments"
          @openComment="openComment"
          @getComments="getComments"
          :version-enable="versionEnable"
          :default-open="richTextDefaultOpen"
          ref="otherInfo"
        >
        </case-edit-info-component>
        <!-- 基础信息 -->
        <div class="content-base-info-wrap" :class="{'editable-content-base-info-wrap' : editable}">
          <el-scrollbar>
            <case-base-info
              :editable="editable"
              :case-id="form.id"
              :project-id="projectId"
              :form="form"
              :is-form-alive="isFormAlive"
              :isloading="loading"
              :read-only="readOnly"
              :public-enable="publicEnable"
              :show-input-tag="showInputTag"
              :tree-nodes="treeNodes"
              :project-list="projectList"
              :custom-field-form="customFieldForm"
              :custom-field-rules="customFieldRules"
              :test-case-template="testCaseTemplate"
              :default-open="richTextDefaultOpen"
              :version-enable="versionEnable"
              ref="testCaseBaseInfo"
            ></case-base-info>
          </el-scrollbar>
        </div>
      </div>
      <!-- 底部操作按钮 -->
      <div class="edit-footer-container" v-if="editable">
        <template>
          <!-- 保存并新建 -->
          <div class="save-create-row">
            <el-button
              size="small"
              @click="handleCommand('ADD_AND_CREATE')"
              v-if="showAddBtn"
              :disabled="readOnly">
              {{ $t("case.saveAndCreate") }}
            </el-button>
          </div>
          <!-- 保存并添加到公共用例库 -->
          <div
          class="save-add-pub-row"
          v-if="showPublic"
          @click="handleCommand('ADD_AND_PUBLIC')">
          <el-button size="small" :disabled="readOnly">
            {{ $t("test_track.case.save_add_public") }}
          </el-button>
        </div>
          <!-- 保存 -->
          <div
            class="save-btn-row"
            v-if="showAddBtn">
            <el-button size="small" @click="handleCommand" :disabled="readOnly" type="primary">
              {{ $t("commons.save") }}
            </el-button>
          </div>
        </template>
      </div>
    </div>
    <!-- <el-dialog
    :fullscreen="true"
    :visible.sync="dialogVisible"
    :destroy-on-close="true"
    width="100%"
  >
    <test-case-version-diff
      v-if="dialogVisible"
      :old-data="oldData"
      :new-data="newData"
      :tree-nodes="treeNodes"
    ></test-case-version-diff>
  </el-dialog>
   -->
  <!-- since v2.7 -->
  <case-diff-side-viewer ref="caseDiffViewerRef" ></case-diff-side-viewer>
   <version-create-other-info-select
      @confirmOtherInfo="confirmOtherInfo"
      ref="selectPropDialog"/>

    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDeleteVersion"/>
  </div>
</template>

<script>
import { TokenKey } from "metersphere-frontend/src/utils/constants";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {
  getCurrentProjectID,
  getCurrentUser, setCurrentProjectID,
} from "metersphere-frontend/src/utils/token";
import {
  hasLicense,
  hasPermission,
} from "metersphere-frontend/src/utils/permission";
import {
  getUUID,
  getNodePath,
  listenGoBack,
  removeGoBackListener,
  handleCtrlSEvent,
} from "metersphere-frontend/src/utils";
import TestCaseAttachment from "@/business/case/components/TestCaseAttachment";
import CaseComment from "@/business/case/components/CaseComment";
import MsInputTag from "metersphere-frontend/src/components/MsInputTag";
import MsPreviousNextButton from "metersphere-frontend/src/components/MsPreviousNextButton";
import TestCaseComment from "@/business/case/components/TestCaseComment";
import ReviewCommentItem from "@/business/review/commom/ReviewCommentItem";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import MsTestCaseStepRichText from "./MsRichText";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import {
  buildCustomFields,
  buildTestCaseOldFields,
  parseCustomField,
} from "metersphere-frontend/src/utils/custom_field";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import TestCaseEditOtherInfo from "@/business/case/components/TestCaseEditOtherInfo";
import FormRichTextItem from "@/business/case/components/richtext/FormRichTextItem";
import TestCaseStepItem from "@/business/case/components/TestCaseStepItem";
import StepChangeItem from "@/business/case/components/StepChangeItem";
import MsChangeHistory from "metersphere-frontend/src/components/history/ChangeHistory";
import { getTestTemplate } from "@/api/custom-field-template";
import CustomFiledFormItem from "metersphere-frontend/src/components/form/CustomFiledFormItem";
import TestCaseVersionDiff from "@/business/case/version/TestCaseVersionDiff";
import VersionCreateOtherInfoSelect from "@/business/case/components/VersionCreateOtherInfoSelect";
import TestCaseBaseInfo from "@/business/case/components/TestCaseBaseInfo";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import { useStore } from "@/store";
import { getProjectApplicationConfig } from "@/api/project-application";
import {
  deleteTestCaseVersion,
  getTestCase,
  getTestCaseFollow,
  getTestCaseVersions,
  hasTestCaseOtherInfo,
  testCaseEditFollows,
  testCaseGetByVersionId,
  testCaseDeleteToGc, getTestCaseNodesByCaseFilter,
} from "@/api/testCase";

import {
  getProjectListAll,
  getProjectMemberOption,
} from "@/business/utils/sdk-utils";
import { testCaseCommentList } from "@/api/test-case-comment";
import {
  getDefaultVersion,
  setLatestVersionById,
} from "metersphere-frontend/src/api/version";
import CaseEditInfoComponent from "./case/CaseEditInfoComponent";
import CaseBaseInfo from "./case/CaseBaseInfo";
import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
import MxVersionHistory from "./common/CaseVersionHistory"
import {
  getProjectVersions,
} from "metersphere-frontend/src/api/version";
import {buildTree} from "metersphere-frontend/src/model/NodeTree";
import {versionEnableByProjectId} from "@/api/project";
import {openCaseEdit} from "@/business/case/test-case";
import ListItemDeleteConfirm from "metersphere-frontend/src/components/ListItemDeleteConfirm";
import CaseDiffSideViewer from "./case/diff/CaseDiffSideViewer";

export default {
  name: "TestCaseEdit",
  components: {
    PriorityTableItem,
    CaseEditInfoComponent,
    CaseBaseInfo,
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
    TestCaseComment,
    MsPreviousNextButton,
    MsInputTag,
    CaseComment,
    MsDialogFooter,
    TestCaseAttachment,
    MsTestCaseStepRichText,
    MsChangeHistory,
    TestCaseVersionDiff,
    VersionCreateOtherInfoSelect,
    TestCaseBaseInfo,
    MsContainer,
    MsAsideContainer,
    MsMainContainer,
    MxVersionHistory,
    ListItemDeleteConfirm,
    CaseDiffSideViewer
  },
  data() {
    return {
      // origin
      isPublic: false,
      isXpack: false,
      testCaseTemplate: {},
      pageHeight: document.documentElement.clientHeight - 150 + "",
      projectList: [],
      comments: [],
      loading: false,
      dialogFormVisible: false,
      showFollow: false,
      isValidate: false,
      currentValidateName: "",
      type: "",
      form: {
        name: "",
        module: "default-module",
        nodePath: "/未规划用例",
        maintainer: getCurrentUser().id,
        priority: "P0",
        type: "",
        method: "",
        prerequisite: "",
        testId: "",
        steps: [
          {
            num: 1,
            desc: "",
            result: "",
          },
        ],
        stepDesc: "",
        stepResult: "",
        selected: [],
        remark: "",
        tags: [],
        demandId: "",
        demandName: "",
        status: "Prepare",
        reviewStatus: "Prepare",
        stepDescription: "",
        expectedResult: "",
        stepModel: "STEP",
        customNum: "",
        followPeople: "",
      },
      maintainerOptions: [],
      workspaceId: "",
      rules: {
        name: [
          {
            required: true,
            message: this.$t("test_track.case.input_name"),
            trigger: "blur",
          },
          {
            max: 255,
            message: this.$t("test_track.length_less_than") + "255",
            trigger: "blur",
          },
        ],
        module: [
          {
            required: true,
            message: this.$t("test_track.case.input_module"),
            trigger: "change",
          },
        ],
        customNum: [
          { required: true, message: "ID必填", trigger: "blur" },
          {
            max: 50,
            message: this.$t("test_track.length_less_than") + "50",
            trigger: "blur",
          },
        ],
        demandName: [
          {
            required: true,
            message: this.$t("test_track.case.input_demand_name"),
            trigger: "change",
          },
        ],
        maintainer: [
          {
            required: true,
            message: this.$t("test_track.case.input_maintainer"),
            trigger: "change",
          },
        ],
        priority: [
          {
            required: true,
            message: this.$t("test_track.case.input_priority"),
            trigger: "change",
          },
        ],
        method: [
          {
            required: true,
            message: this.$t("test_track.case.input_method"),
            trigger: "change",
          },
        ],
      },
      customFieldRules: {},
      customFieldForm: null,
      formLabelWidth: "100px",
      isCreateContinue: false,
      isStepTableAlive: true,
      isFormAlive: true,
      methodOptions: [
        { value: "auto", label: this.$t("test_track.case.auto") },
        { value: "manual", label: this.$t("test_track.case.manual") },
      ],
      testCase: {},
      showInputTag: true,
      tableType: "",
      moduleObj: {
        id: "id",
        label: "name",
      },
      tabId: getUUID(),
      versionData: [],
      dialogVisible: false,
      oldData: null,
      newData: null,
      selectedOtherInfo: null,
      currentProjectId: "",
      casePublic: false,
      isClickAttachmentTab: false,
      latestVersionId: "",
      hasLatest: false,
      treeNodes: [],
      currentTestCaseInfo: {},
      versionOptions: [],
      currentVersionName: "",
      versionEnable: false,
      // 是否为最新版本
      isLastedVersion: true,
      caseId: ""
    };
  },
  props: {
    isPublicShow: {
      type: Boolean,
      default: false
    },
    isFirstPublic: {
      type: Boolean,
      default: false
    },
    isLastPublic: {
      type: Boolean,
      default: false
    },
    publicCaseId: String
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    moduleOptions() {
      return useStore().testCaseModuleOptions;
    },
    isCustomNum() {
      return useStore().currentProjectIsCustomNum;
    },
    richTextDefaultOpen() {
      return this.type === "edit" ? "preview" : "edit";
    },
    readOnly() {
      if (this.isPublicShow) {
        return true;
      }
      const { rowClickHasPermission } = this.currentTestCaseInfo;
      if (rowClickHasPermission !== undefined) {
        return !rowClickHasPermission;
      }
      return (
        !hasPermission("PROJECT_TRACK_CASE:READ+CREATE") &&
        !hasPermission("PROJECT_TRACK_CASE:READ+EDIT")
      );
    },
    // caseId: {
    //   get: function(){
    //     return !this.isPublicShow ? this.$route.params.caseId : this.publicCaseId;
    //   },
    //   set: function(val){
    //     this.$route.params.caseId = val;
    //   }
    // },
    editType() {
      return this.$route.query.type;
    },
    isAdd() {
      return !this.caseId || this.isCopy;
    },
    editable() {
      return this.isAdd;
    },
    isCopy() {
      return this.editType == 'copy';
    },
    publicEnable() {
      return this.editType == 'public';
    },
    showPublic() {
      return this.isPublic && this.isXpack;
    },
    showAddBtn() {
      return this.isAdd || this.showPublic;
    }
  },
  watch: {
    isAdd() {
      this.type = this.isAdd ? 'add' : 'edit';
    },
    form: {
      handler(val) {
        if (val && useStore().testCaseMap && this.form.id) {
          let change = useStore().testCaseMap.get(this.form.id);
          change = change + 1;
          useStore().testCaseMap.set(this.form.id, change);
        }
        if(val.versionId && !this.currentVersionName){
          this.fetchVersionName();
        }

        if(!this.editable && val.versionId){
          // 检测是否为最新版本
          this.checkIsLatestVersion(val.versionId);
        }
      },
      deep: true,
    },
    customFieldForm: {
      handler(val) {
        if (val && useStore().testCaseMap && this.form.id) {
          let change = useStore().testCaseMap.get(this.form.id);
          change = change + 1;
          useStore().testCaseMap.set(this.form.id, change);
        }
      },
      deep: true,
    },
  },
  beforeDestroy() {
    this.removeListener();
    this.$EventBus.$off(
      "handleSaveCaseWithEvent",
      this.handleSaveCaseWithEvent
    );
  },
  mounted() {
    this.caseId = !this.isPublicShow ? this.$route.params.caseId : this.publicCaseId;

    this.getSelectOptions();

    // Cascader 级联选择器: 点击文本就让它自动点击前面的input就可以触发选择。
    setInterval(function () {
      document.querySelectorAll(".el-cascader-node__label").forEach((el) => {
        el.onclick = function () {
          if (this.previousElementSibling) this.previousElementSibling.click();
        };
      });
    }, 1000);

    if (!(useStore().testCaseMap instanceof Map)) {
      useStore().testCaseMap = new Map();
    }
    if (this.form.id) {
      useStore().testCaseMap.set(this.form.id, 0);
    }

    //获取版本信息
    this.getVersionOptionList();
  },
  activated() {
    this.loadTestCase();
  },
  created(){
    this.$EventBus.$on("handleSaveCaseWithEvent", this.handleSaveCaseWithEvent);
  },
  methods: {
    checkoutLatest(){
      //切换最新版本
      this.checkout({id: this.latestVersionId})
    },
    //与最新版本比较
    diffWithLatest(){
      if(!this.latestVersionId){
        return;
      }
      if(!this.currentTestCaseInfo){
        return;
      }
      this.compareBranchWithVersionId(this.latestVersionId, this.currentTestCaseInfo.versionId);
    },
    async checkIsLatestVersion(id){
      let allCaseVersions = await getTestCaseVersions(this.currentTestCaseInfo.id);
      if (allCaseVersions.data) {
        this.isLastedVersion = allCaseVersions.data.length === 1;
        return true;
      }
      if(!this.versionOptions || this.versionOptions.length <= 0){
        this.isLastedVersion = true;
        return true;
      }
      let version =  this.versionOptions.filter(v => v.id == id);
      if(!version || version.length <= 0){
        this.isLastedVersion = true;
        return true;
      }
      this.isLastedVersion = version[0].latest;
      return version[0].latest;
    },
    loadTestCase() {
      let initFuc = this.initEdit;
      this.loading = true;
      getTestTemplate().then((template) => {
        this.testCaseTemplate = template;
        useStore().testCaseTemplate = this.testCaseTemplate;
        initFuc();
      });

      this.addListener(); //  添加 ctrl s 监听
      if (!this.projectList || this.projectList.length === 0) {
        //没有项目数据的话请求项目数据
        getProjectListAll().then((response) => {
          this.projectList = response.data; //获取当前工作空间所拥有的项目,
        });
      }

      getTestCaseNodesByCaseFilter(this.projectId, {})
        .then(r => {
          this.treeNodes = r.data;
          this.treeNodes.forEach(node => {
            node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
            buildTree(node, {path: ''});
            this.setNodeModule();
          });
        });

      getTestCaseFollow(this.caseId).then((response) => {
        this.form.follows = response.data;
        for (let i = 0; i < response.data.length; i++) {
          if (response.data[i] === this.currentUser().id) {
            this.showFollow = true;
            break;
          }
        }
      });
      getProjectApplicationConfig("CASE_PUBLIC").then((res) => {
        let data = res.data;
        if (data && data.typeValue === "true") {
          this.isPublic = true;
        } else {
          this.isPublic = false;
        }
      });
      if (hasLicense()) {
        this.isXpack = true;
      } else {
        this.isXpack = false;
      }
      if (hasLicense()) {
        this.getDefaultVersion();
      }

      //浏览器拉伸时窗口编辑窗口自适应
      this.$nextTick(() => {
        // 解决错位问题
        window.addEventListener("resize", this.resizeContainer);
      });

      this.checkVersionEnable();
    },
    editPublicCase() {
      openCaseEdit(this.caseId, "", this)
    },
    copyPublicCase() {
      openCaseEdit(this.caseId, "copy", this)
    },
    closePublicCase() {
      this.$emit("close");
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        versionEnableByProjectId(this.projectId)
          .then(response => {
            this.versionEnable = response.data;
          });
      }
    },
    back() {
      this.$router.push('/track/case/all');
    },
    openNewTab() {
      if (this.editable || !this.form.id || this.isPublicShow) {
        return;
      }
      let TestCaseData = this.$router.resolve({
        path: "/track/case/edit/" + this.form.id
      });
      window.open(TestCaseData.href, "_blank");
    },
    handleSaveCaseWithEvent(formData) {
      this.saveCase();
    },
    alert: alert,
    currentUser: () => {
      return getCurrentUser();
    },
    resizeContainer() {
      this.pageHeight = document.documentElement.clientHeight - 150 + "";
    },
    openHis() {
      this.$refs.changeHistory.open(this.form.id, [
        "测试用例",
        "測試用例",
        "Test case",
        "TRACK_TEST_CASE",
      ]);
    },
    setNodeModule() {
      if (this.caseId) {
        this.form.module = this.currentTestCaseInfo.nodeId;
        this.form.nodePath = this.currentTestCaseInfo.nodePath;
      }
      if ((!this.form.module ||
          this.form.module === "default-module" ||
          this.form.module === "root")
        && this.treeNodes.length > 0) {
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
      if (this.form.id) {
        useStore().testCaseMap.set(this.form.id, 0);
      }
    },
    addPublic() {
      this.casePublic = true;
      this.saveCase();
    },
    removePublic() {
      this.casePublic = false;
      this.saveCase();
    },
    handleCommand(e) {
      if (e === "ADD_AND_CREATE") {
        if (!this.validateForm()) {
          this.saveCase();
        } else {
          this.saveCase(function (t) {
            if(t){
              t.$router.push({path: "/track/case/create",});
              location.reload();
            }
          });
        }
      } else if (e === "ADD_AND_PUBLIC") {
        this.casePublic = true;
        this.saveCase();
      } else {
        this.saveCase();
      }
    },
    openComment() {
      this.$refs.testCaseComment.open();
    },
    getComments() {
      if (!this.caseId) {
        return;
      }
      this.loading = true;
      testCaseCommentList(this.caseId).then((res) => {
        this.loading = false;
        this.comments = res.data.filter(comment => comment.description);
      });
    },
    showAll() {
      if (!this.customizeVisible) {
        this.selectedTreeNode = undefined;
      }
      //this.reload();
    },
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => {
        this.isStepTableAlive = true;
        if (this.form.id) {
          useStore().testCaseMap.set(this.form.id, 0);
        }
      });
    },
    reloadForm() {
      this.isFormAlive = false;
      this.$nextTick(() => (this.isFormAlive = true));
    },
    initEdit(testCase, callback) {

      if (window.history && window.history.pushState) {
        history.pushState(null, null, document.URL);
        window.addEventListener("popstate", this.close);
      }
      this.resetForm();
      listenGoBack(this.close);

      if (this.caseId) {
        this.operationType = 'edit';
        if (this.isCopy) {
          this.operationType = 'add';
        } else {
          this.getComments();
        }
        this.getTestCase();
      } else {
        this.operationType = 'add';

        // add
        if (this.moduleOptions.length > 0) {
          this.form.module = this.moduleOptions[0].id;
        }
        let user = JSON.parse(localStorage.getItem(TokenKey));
        this.form.priority = "P3";
        this.form.type = "functional";
        this.form.method = "manual";
        this.form.maintainer = user.id;
        this.form.tags = [];
        this.getSelectOptions();
        this.customFieldForm = parseCustomField(
          this.form,
          this.testCaseTemplate,
          this.customFieldRules
        );
        this.reload();
        this.loading = false;
      }
      if (callback) {
        callback();
      }
    },
    getTestCase() {
      if (!this.caseId) {
        return;
      }
      this.showInputTag = false;
      this.loading = true;
      getTestCase(this.caseId).then((response) => {
        let testCase = response.data;
        this.operationType = "edit";

        if (this.isCopy) {
          this.operationType = "add";
          testCase.name = 'copy_' + testCase.name;
          //复制的时候只复制当前版本
          testCase.id = getUUID();
          testCase.refId = null;
          testCase.versionId = null;

          this.testCaseTemplate.customFields.forEach((item) => {
            item.isEdit = false;
          });
          this.form.id = null;
        } else {
          // 如果不是当前项目，先切项目
          if (this.projectId !== testCase.projectId) {
            setCurrentProjectID(testCase.projectId);
            location.reload();
          }
        }
        this.currentTestCaseInfo = testCase;
        this.setFormData(testCase);
        this.setTestCaseExtInfo(testCase);
        this.getSelectOptions();
        this.reload();
        this.$nextTick(() => {
          this.showInputTag = true;
        });
        this.loading = false;
      });
    },
    async setFormData(testCase) {
      try {
        testCase.selected = JSON.parse(testCase.testId);
      } catch (error) {
        testCase.selected = testCase.testId;
      }
      let tmp = {};
      Object.assign(tmp, testCase);
      tmp.steps = JSON.parse(testCase.steps);
      if (!tmp.steps || tmp.steps.length < 1) {
        tmp.steps = [
          {
            num: 1,
            desc: "",
            result: "",
          },
        ];
      }
      tmp.tags = JSON.parse(tmp.tags);
      Object.assign(this.form, tmp);
      if (!this.form.stepModel) {
        this.form.stepModel = "STEP";
      }
      this.casePublic = tmp.casePublic;
      this.form.module = testCase.nodeId;
      //设置自定义熟悉默认值
      this.customFieldForm = parseCustomField(
        this.form,
        this.testCaseTemplate,
        this.customFieldRules,
        testCase ? buildTestCaseOldFields(this.form) : null
      );
      this.setDefaultValue();
      this.resetSystemField();
      // 重新渲染，显示自定义字段的必填校验
      this.reloadForm();
    },
    resetSystemField() {
      if (this.operationType === "add") {
        return;
      }
      // 用例等级等字段以表中对应字段为准，后端复杂操作直接改表中对应字段即可
      this.from;
      this.customFieldForm["用例等级"] = this.form.priority;
      this.customFieldForm["责任人"] = this.form.maintainer;
      this.customFieldForm["用例状态"] = this.form.status;
      this.testCaseTemplate.customFields.forEach((field) => {
        if (field.name === "用例等级") {
          field.defaultValue = this.form.priority;
        } else if (field.name === "责任人") {
          field.defaultValue = this.form.maintainer;
        } else if (field.name === "用例状态") {
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
        if (this.$refs.versionHistory) {
          this.$refs.versionHistory.loading = false;
        }
        if (this.$refs.selectPropDialog) {
          this.$refs.selectPropDialog.close();
        }
      }
    },
    _saveCase(callback) {
      let param = this.buildParam();
      if (this.validate(param)) {
        let option = this.getOption(param);
        this.loading = true;
        this.$request(option)
          .then((response) => {
            response = response.data;
            // 保存用例后刷新附件
            this.currentTestCaseInfo.isCopy = false;
            if (this.$refs.otherInfo) {
              this.$refs.otherInfo.getFileMetaData(response.data.id);
            }
            this.loading = false;
            this.$success(this.$t("commons.save_success"), false);
            this.operationType = "edit";
            this.$emit("refreshTestCase");
            useStore().testCaseMap.set(this.form.id, 0);
            this.$emit("refresh", response.data);
            if (this.form.id) {
              this.$emit("caseEdit", param);
            } else {
              param.id = response.data.id;
              this.$emit("caseCreate", param);
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

              return;
            }
            // 保存用例后刷新附件

            //更新版本
            if (hasLicense()) {
              this.getDefaultVersion();
            }

            this.$router.push({path: "/track/case/edit/" + this.form.id})
          })
          .catch(() => {
            this.loading = false;
          });
      }
    },
    buildParam() {
      let param = {};
      Object.assign(param, this.form);
      param.steps = JSON.stringify(this.form.steps);
      param.nodeId = this.form.module;
      param.copyCaseId = this.caseId;
      if (!this.publicEnable) {
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
      param.type = "functional";
      buildCustomFields(this.form, param, this.testCaseTemplate);
      this.parseOldFields(param);
      //配置多版本复制的时候是否要连带复制其他信息
      if (this.selectedOtherInfo) {
        param.otherInfoConfig = this.selectedOtherInfo;
      }
      if (this.$refs.otherInfo) {
        if (this.$refs.otherInfo.getRelateFiles() && this.$refs.otherInfo.getRelateFiles().length > 0) {
          param.relateFileMetaIds = this.$refs.otherInfo.getRelateFiles();
        }
        if (this.$refs.otherInfo.getUnRelateFiles() && this.$refs.otherInfo.getUnRelateFiles().length > 0) {
          param.unRelateFileMetaIds = this.$refs.otherInfo.getUnRelateFiles();
        }
      }
      return param;
    },
    parseOldFields(param) {
      let customFields = this.testCaseTemplate.customFields;
      customFields.forEach((item) => {
        if (item.name === "用例等级") {
          param.priority = item.defaultValue;
          this.form.priority = item.defaultValue;
        }
        if (item.name === "责任人") {
          param.maintainer = item.defaultValue;
          this.form.maintainer = item.defaultValue;
        }
        if (item.name === "用例状态") {
          param.status = item.defaultValue;
          this.form.status = item.defaultValue;
        }
      });
    },
    getOption(param) {
      let formData = new FormData();
      let requestJson = JSON.stringify(param, function (key, value) {
        return key === "file" ? undefined : value;
      });
      if (this.$refs.otherInfo.getUploadFiles() && this.$refs.otherInfo.getUploadFiles().length > 0) {
        this.$refs.otherInfo.getUploadFiles().forEach((f) => {
          formData.append("file", f);
        });
      }
      formData.append(
        "request",
        new Blob([requestJson], {
          type: "application/json",
        })
      );
      let path = '/test/case/edit';
      if (this.isAdd || this.isCopy) {
        path = '/test/case/add';
      }
      return {
        method: "POST",
        url: path,
        data: formData,
        headers: {
          "Content-Type": undefined,
        },
      };
    },
    validate(param) {
      for (let i = 0; i < param.steps.length; i++) {
        if (
          (param.steps[i].desc && param.steps[i].desc.length > 300) ||
          (param.steps[i].result && param.steps[i].result.length > 300)
        ) {
          this.$warning(
            this.$t("test_track.case.step_desc") +
              "," +
              this.$t("test_track.case.expected_results") +
              this.$t("test_track.length_less_than") +
              "300", false
          );
          return false;
        }
      }
      if (param.name === "") {
        this.$warning(this.$t("test_track.case.input_name"), false);
        return false;
      }
      return true;
    },
    typeChange() {
      this.form.testId = "";
    },
    getMaintainerOptions() {
      getProjectMemberOption().then((response) => {
        this.maintainerOptions = response.data;
      });
    },
    getSelectOptions() {
      this.getMaintainerOptions();
    },
    resetForm() {
      //防止点击修改后，点击新建触发校验
      if (this.$refs["caseFrom"]) {
        this.$refs["caseFrom"].validate(() => {
          this.$refs["caseFrom"].resetFields();
          this._resetForm();
          return true;
        });
      } else {
        this._resetForm();
      }
    },
    _resetForm() {
      this.form.name = "";
      this.form.module = "";
      this.form.type = "";
      this.form.method = "";
      this.form.maintainer = "";
      this.form.priority = "";
      this.form.prerequisite = "";
      this.form.remark = "";
      this.form.testId = "";
      this.form.testName = "";
      this.form.steps = [
        {
          num: 1,
          desc: "",
          result: "",
        },
      ];
      this.form.customNum = "";
      this.form.tags = [];
    },
    addListener() {
      document.addEventListener("keydown", this.createCtrlSHandle);
    },
    removeListener() {
      document.removeEventListener("keydown", this.createCtrlSHandle);
    },
    createCtrlSHandle(event) {
      let curTabId = useStore().curTabId;
      if (curTabId === this.tabId) {
        if (event.keyCode === 83 && event.ctrlKey && this.readOnly) {
          this.$warning(this.$t("commons.no_operation_permission"), false);
          return false;
        }
        handleCtrlSEvent(event, this.saveCase);
      }
    },
    showPreviousPublicCase() {
      this.$emit("previousCase", this.caseId)
    },
    showNextPublicCase() {
      this.$emit("nextCase", this.caseId)
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.form.follows.length; i++) {
          if (this.form.follows[i] === this.currentUser().id) {
            this.form.follows.splice(i, 1);
            break;
          }
        }
        this.loading = true;
        testCaseEditFollows(this.form.id, this.form.follows).then(() => {
          this.loading = false;
          this.$success(this.$t("commons.cancel_follow_success"), false);
        });
      } else {
        this.showFollow = true;
        if (!this.form.follows) {
          this.form.follows = [];
        }
        this.form.follows.push(this.currentUser().id);

        this.loading = true;
        testCaseEditFollows(this.form.id, this.form.follows).then(() => {
          this.loading = false;
          this.$success(this.$t("commons.follow_success"), false);
        });
      }
    },
    getDefaultVersion() {
      getDefaultVersion(this.projectId).then((response) => {
        this.latestVersionId = response.data;
        this.getVersionHistory();
      });
    },
    getVersionHistory() {
      getTestCaseVersions(this.caseId).then((response) => {
        if (response.data.length > 0) {
          for (let i = 0; i < response.data.length; i++) {
            this.currentProjectId = response.data[i].projectId;
          }
        } else {
          this.currentProjectId = getCurrentProjectID();
        }
        this.versionData = response.data;
        let latestVersionData = response.data.filter(
          (v) => v.versionId === this.latestVersionId
        );
        if (latestVersionData.length > 0) {
          this.hasLatest = false;
        } else {
          this.hasLatest = true;
        }
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
    async fetchVersionName(){
      if(this.form.versionName){
        this.currentVersionName = this.form.versionName;
        return;
      }
      if(this.currentVersionName){
        return;
      }
      //查询版本名称
      await this.getVersionOptionList();
      this.currentVersionName = this.findVersionNameByID(this.form.versionId)
    },
    async getVersionOptionList() {
      if (!hasLicense()) {
        return;
      }
      let res = await getProjectVersions(getCurrentProjectID());
      this.versionOptions = res.data ?? [];
    },
    findVersionNameByID(versionId){
      let versionName = "";
      let version = this.versionOptions.filter(v => v.id === versionId);
      if(Array.isArray(version) && version.length > 0){
        return version[0].name;
      }
      return versionName;
    },
    compareBranchWithVersionId(originId, targetId){
       // 打开对比
       this.dialogVisible = true;
       this.$refs.caseDiffViewerRef.open(originId, targetId, this.currentTestCaseInfo.id)
    },
    compareBranch(t1, t2) {
       this.compareBranchWithVersionId(t1.id, t2.id);
      // let t1Case = await testCaseGetByVersionId(t1.id, this.currentTestCaseInfo.id);
      // let t2Case = await testCaseGetByVersionId(t2.id, this.currentTestCaseInfo.id);

      // let p1 = getTestCase(t1Case.data.id);
      // let p2 = getTestCase(t2Case.data.id);
      // let that = this;
      // Promise.all([p1, p2]).then((r) => {
      //   if (r[0] && r[1]) {
      //     that.newData = r[0].data;
      //     that.oldData = r[1].data;
      //     that.newData.createTime = t1.createTime;
      //     that.oldData.createTime = t2.createTime;
      //     that.newData.versionName = t1.name;
      //     that.oldData.versionName = t2.name;
      //     that.newData.userName = t1Case.data.createName;
      //     that.oldData.userName = t2Case.data.createName;
      //     this.setSpecialPropForCompare(that);
      //     that.dialogVisible = true;
      //   }
      // });
    },
    compare(row) {
      testCaseGetByVersionId(row.id, this.currentTestCaseInfo.refId).then(
        (response) => {
          let p1 = getTestCase(response.data.id);
          let p2 = getTestCase(this.caseId);
          let that = this;
          Promise.all([p1, p2]).then((r) => {
            if (r[0] && r[1]) {
              that.newData = r[0].data;
              that.oldData = r[1].data;
              that.newData.createTime = row.createTime;
              that.oldData.createTime =
                this.$refs.versionHistory.versionOptions.filter(
                  (v) => v.id === that.oldData.versionId
                )[0].createTime;
              that.newData.versionName = that.versionData.filter(
                (v) => v.id === that.newData.id
              )[0].versionName;
              that.oldData.versionName = that.versionData.filter(
                (v) => v.id === that.oldData.id
              )[0].versionName;
              that.newData.userName = response.data.createName;
              that.oldData.userName = that.versionData.filter(
                (v) => v.id === that.oldData.id
              )[0].createName;
              this.setSpecialPropForCompare(that);
              that.dialogVisible = true;
            }
          });
        }
      );
    },
    checkout(row) {
      this.getVersionHistory();
      this.$refs.versionHistory.loading = true;
      let testCase = this.versionData.filter((v) => v.versionId === row.id)[0];

      if (testCase) {
        getTestCase(testCase.id).then((response) => {
          let testCase = response.data;
          this.currentTestCaseInfo = testCase;
          this.form = testCase;
          this.caseId = testCase.id;
          //版本切换展示
          this.currentVersionName = this.findVersionNameByID(this.form.versionId)
          this.checkIsLatestVersion(this.form.versionId);
          this.$emit("checkout", testCase);
          this.$refs.versionHistory.loading = false;
        });
      }
    },
    validateForm() {
      let isValidate = true;
      if (this.$refs["caseFrom"]) {
        this.$refs["caseFrom"].validate((valid) => {
          if (!valid) {
            isValidate = false;
            return false;
          }
        });
      }
      let detailForm = this.$refs.otherInfo.validateForm();
      let baseInfoValidate = this.$refs.testCaseBaseInfo.validateForm();
      let customValidate = this.$refs.testCaseBaseInfo.validateCustomForm();
      if (!detailForm || !baseInfoValidate) {
        return false;
      }
      if (!customValidate) {
        let customFieldFormFields =
          this.$refs.testCaseBaseInfo.getCustomFields();
        for (let i = 0; i < customFieldFormFields.length; i++) {
          let customField = customFieldFormFields[i];
          if (customField.validateState === "error") {
            if (this.currentValidateName) {
              this.currentValidateName =
                this.currentValidateName + "," + customField.label;
            } else {
              this.currentValidateName =
                customField.label || customField.labelFor;
            }
          }
        }
        this.isValidate = true;
        this.$warning(this.currentValidateName + this.$t("commons.cannot_be_null"), false);
        this.currentValidateName = "";
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
          this.saveCase(() => {
            if (this.$refs.versionHistory) {
              this.$refs.versionHistory.loading = false;
            }
          });
        }
        setTimeout(() => {
          this.checkout(row);
        }, 3000);
      } else {
        this.$refs.versionHistory.loading = false;
      }
    },
    del(row) {
      let that = this;
      this.$confirm(
        this.$t("test_track.case.delete_confirm") + " " + row.name + " ？",
        "",
        {
          confirmButtonText: this.$t("commons.confirm"),
          customClass: "custom-confirm-delete",
          callback: (action) => {
            if (action === "confirm") {
              deleteTestCaseVersion(row.id, this.form.refId).then(() => {
                this.$success(this.$t("commons.delete_success"));
                this.getVersionHistory();
                this.$emit("refresh");
              });
            } else {
              that.$refs.versionHistory.loading = false;
            }
          },
        }
      );
    },
    setLatest(row) {
      let param = {
        projectId: getCurrentProjectID(),
        type: "TEST_CASE",
        versionId: row.id,
        resourceId: this.caseId,
      };
      setLatestVersionById(param).then(() => {
        this.$success(this.$t("commons.modify_success"));
        this.checkout(row);
      });
    },
    hasOtherInfo() {
      return new Promise((resolve) => {
        if (this.form.id) {
          hasTestCaseOtherInfo(this.form.id).then((res) => {
            resolve(res.data);
          });
        } else {
          resolve();
        }
      });
    },
    confirmOtherInfo(selectedOtherInfo) {
      this.selectedOtherInfo = selectedOtherInfo;
      this.saveCase();
    },
    copyRow() {
      openCaseEdit(this.testCase.id, "copy", this);
    },
    deleteRow() {
      getTestCaseVersions(this.testCase.id)
        .then(response => {
          if (hasLicense() && this.versionEnable && response.data.length > 1) {
            // 删除提供列表删除和全部版本删除
            this.$refs.apiDeleteConfirm.open(this.testCase, this.$t('test_track.case.delete_confirm'));
          } else {
            let title = this.$t('test_track.case.case_delete_confirm') + ": " + this.testCase.name + "?";
            this.$confirm(this.$t('test_track.case.batch_delete_tip'), title, {
                cancelButtonText: this.$t("commons.cancel"),
                confirmButtonText: this.$t("commons.delete"),
                customClass: 'custom-confirm-delete',
                callback: action => {
                  if (action === "confirm") {
                    this._handleDeleteVersion(this.testCase, false);
                  }
                }
              }
            );
          }
        })
    },
    _handleDeleteVersion(testCase, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        deleteTestCaseVersion(testCase.versionId, testCase.refId)
          .then(() => {
            this.$success(this.$t('commons.delete_success'), false);
            this.$refs.apiDeleteConfirm.close();
            this.$emit("refreshAll");
          });
      } else {
        // 删除全部版本
        this._handleDeleteToGc(testCase);
        this.$refs.apiDeleteConfirm.close();
      }
    },
    _handleDeleteToGc(testCase) {
      let testCaseId = testCase.id;
      testCaseDeleteToGc(testCaseId)
        .then(() => {
          this.$success(this.$t('commons.delete_success'), false);
          this.$router.push('/track/case/all');
        });
    },
  },
};
</script>

<style scoped>
.el-switch {
  margin-bottom: 10px;
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
  z-index: 10;
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

.editable-edit-content-container {
  height: calc(100vh - 190px) !important;
}

.editable-content-base-info-wrap {
  height: calc(100vh - 190px) !important;
}

.el-scrollbar {
  height: 100%;
}
</style>

<style scoped lang="scss">
@import "@/business/style/index.scss";

.case-edit-wrap {
  padding: 12px 24px 0px;
  box-sizing: border-box;

  :deep(.el-form-item__content) {
    line-height: px2rem(32);
  }
  .case-edit-box {
    /* margin-left: px2rem(34); */
    background-color: #fff;
    .edit-header-container {
      height: 56px;
      width: 100%;
      // border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      justify-content: space-between;
      .header-content-row {
        display: flex;
        align-items: center;
        .back {
          margin-left: px2rem(24);
          width: px2rem(20);
          height: px2rem(20);
          cursor: pointer;
          img {
            width: 100%;
            height: 100%;
          }
        }

        .case-name {
          height: px2rem(24);
          font-size: 16px;
          font-family: "PingFang SC";
          font-style: normal;
          font-weight: 500;
          line-height: px2rem(24);
          color: #1f2329;
          margin-left: px2rem(8);
          margin-right: px2rem(8);
          cursor: pointer;
        }
        .case-name-hover:hover {
          cursor: pointer;
          background: rgba(31, 35, 41, 0.1);
          border-radius: 4px;
        }

        .case-edit {
          display: flex;
          align-items: center;
          .case-level {
          }

          .case-version:hover {
            cursor: pointer;
            background: rgba(31, 35, 41, 0.1);
            border-radius: 4px;
          }
          .case-version {
            display: flex;
            color: #646a73;
            align-items: center;
            margin-left: px2rem(8);

            .version-icon {
              width: 14.17px;
              height: 12.6px;
              margin-right: px2rem(4.95);

              img {
                width: 100%;
                height: 100%;
              }
            }

            .version-title {
              height: px2rem(22);
              line-height: px2rem(22);
            }
            .version-suffix {
              height: px2rem(22);
              line-height: px2rem(22);
              margin-left: px2rem(5);
            }
          }
        }
      }
      .separator-row {
        margin-right: px2rem(20);
        position: relative;
        bottom: px2rem(1);
        color: #BBBFC4;
      }
      .el-icon-close:before {
        font-size: 20px;
      }
      .el-icon-arrow-left:before {
        font-size: 16px;
        float: left;
      }
      .el-icon-arrow-right:before {
        font-size: 16px;
        float: right;
      }
      .header-opt-row {
        display: flex;
        align-items: center;
        // 公共处理
        .head-opt:hover {
          background: rgba(31, 35, 41, 0.1);
          border-radius: 4px;
          cursor: pointer;
        }
        .head-opt {
          display: flex;
          align-items: center;
          .icon-row {
            width: 14px;
            height: 14px;
            margin-right: px2rem(5);
            img {
              width: 100%;
              height: 100%;
            }
          }

          .label-row {
            height: px2rem(22);
            font-family: "PingFang SC";
            font-style: normal;
            font-weight: 400;
            font-size: 14px;
            line-height: px2rem(22);
            text-align: center;
            color: #1f2329;
          }
        }

        .disable-row{
          color: #BBBFC4;
        }

        .disable-row .label-row {
          color: #BBBFC4!important;
        }

        .previous-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
        }

        .next-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
        }

        .follow-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
        }

        .edit-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
        }

        .copy-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
        }

        .add-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(22.33);
        }

        .more-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(24);
        }
      }
    }
    .diff-latest-container {
      background: linear-gradient(0deg, rgba(255, 136, 0, 0.15), rgba(255, 136, 0, 0.15)), #FFFFFF;
      border-radius: 4px;
      height: 40px;
      margin-left: 12px;
      margin-right: 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      .left-view-row {
        display: flex;
        .view-icon {
          width: 14.67px;
          height: 14.67px;
          margin-left: 16.67px;
          margin-right: 8.67px;
          img {
            width: 100%;
            height: 100%;
          }
        }

        .view-content {
          color: #1F2329;
          font-weight: 400;
          font-size: 14px;
        }
      }

      .right-diff-opt {
        display: flex;
        .diff-latest {
          font-weight: 400;
          font-size: 14px;
          color: #783887;
          cursor: pointer;
        }
        .diff-latest:hover{
          background: rgba(120, 56, 135, 0.1);
          border-radius: 4px;
        }
        .show-latest:hover{
          background: rgba(120, 56, 135, 0.1);
          border-radius: 4px;
        }
        .show-latest {
          margin: 0 16px;
          font-weight: 400;
          font-size: 14px;
          color: #783887;
          cursor: pointer;
        }
      }
    }
    .edit-content-container {
      width: 100%;
      height: 100%;
      display: flex;
      justify-content: space-between;
      background-color: #fff;
      border-top: 1px solid rgba(31, 35, 41, 0.15);
      .required-item:after {
        content: "*";
        color: #f54a45;
        margin-left: px2rem(4);
        width: px2rem(8);
        height: 22px;
        font-weight: 400;
        font-size: 14px;
        line-height: 22px;
      }
      .content-body-wrap {
        .case-title-wrap {
          display: flex;
          margin-top: px2rem(24);
          margin-bottom: px2rem(8);
          .title-wrap {
            font-family: "PingFang SC";
            font-style: normal;
            font-weight: 500;
            font-size: 14px;
            color: #1f2329;
          }
        }
        :deep(.el-tabs__nav-scroll) {
          padding-left: 6px;
          height: 45px;
        }
        :deep(.el-tabs__nav) {
          height: 45px;
          line-height: 45px;
        }
        :deep(.el-tabs__item) {
          padding: 0 14px !important;
        }
        //公共样式
        .content-wrap {
          :deep(.v-note-op) {
            background-color: #f8f9fa !important;
            border-bottom: 1px solid #bbbfc4;
          }
          :deep(.v-note-wrapper) {
            box-sizing: border-box;
            border: 1px solid #bbbfc4 !important;
            border-radius: 4px;
            box-shadow: none !important;
          }
          :deep(.v-note-show) {
            min-height: 65px;
          }
          :deep(.v-left-item) {
            flex: none !important;
          }
        }

        .case-name-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            .opt-row {
              width: 100%;
              height: 32px;
            }
          }
        }
        .pre-condition-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            min-height: 100px;
            .opt-row {
              :deep(.el-form-item) {
                margin: 0;
              }
              width: 100%;
            }
          }
        }
        .remark-row {
          .content-wrap {
            display: flex;
            justify-content: center;
            width: 100%;
            min-height: 100px;
            .opt-row {
              width: 100%;
              :deep(.el-form-item) {
                margin: 0;
              }
            }
          }
        }
        .attachment-row {
          .attachment-name.case-title-wrap {
            .name.title-wrap {
            }
          }

          .content-wrap {
            .opt-btn {
            }

            .opt-tip {
              font-family: "PingFang SC";
              font-style: normal;
              font-weight: 400;
              font-size: 14px;
              line-height: 22px;
              /* identical to box height, or 157% */

              color: #8f959e;
            }
          }
        }
      }

      .content-base-info-wrap {
        width: px2rem(304);
        height: calc(100vh - 240px + 130px);
        border-left: 1px solid rgba(31, 35, 41, 0.15);
        .case-wrap {
          margin-left: px2rem(24);
          margin-top: px2rem(24);
        }
        .case-title-wrap {
          display: flex;
          .title-wrap {
            font-weight: 500;
            height: 22px;
            font-size: 14px;
            line-height: 22px;
            color: #1f2329;
          }
          margin-bottom: px2rem(8);
        }
        .side-content {
          width: px2rem(256);
          height: 32px;
          :deep(.el-select) {
            width: 100%;
          }
        }
      }
    }

    .edit-footer-container {
      display: flex;
      width: 100%;
      height: 80px;
      background: #ffffff;
      box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
      align-items: center;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 22px;
      text-align: center;
      justify-content: flex-end;
      // 底部按钮激活样式
      .opt-active-primary {
        background: #783887;
        color: #ffffff;
      }
      .opt-disable-primary {
        background: #bbbfc4;
        color: #ffffff;
      }
      .opt-active {
        background: #ffffff;
        color: #1f2329;
      }
      .opt-disable {
        background: #ffffff;
        color: #bbbfc4;
      }

      .save-btn-row {
        margin: 0 24px 0 12px;
        el-button {
        }
      }

      .save-add-pub-row {
        margin-left: px2rem(12);
        el-button {
        }
      }

      .save-create-row {
        margin-left: px2rem(12);
        el-button {
        }
      }
    }
  }
}
</style>
<style>
.attachment-popover {
  padding: 0 !important;
  height: 80px;
  min-width: 120px !important;
}
</style>
