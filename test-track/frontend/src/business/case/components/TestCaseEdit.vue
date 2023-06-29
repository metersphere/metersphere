<template>
  <div class="case-edit-wrap">
    <!-- since v2.6 -->
    <div class="case-edit-box">
      <!-- 创建 or 编辑用例 -->
      <div class="edit-header-container">
        <div class="header-content-row" :style="!isPublicShow ? 'width: 80%' : 'max-width: 65%'">
          <!-- 用例名称展示与编辑 -->
          <test-case-edit-name-view
            :is-add="isAdd"
            :editable-state="editableState"
            :is-name-edit.sync="isNameEdit"
            :is-public-show="isPublicShow || hasReadonlyPermission"
            :form="form"
            @save="saveCaseWithoutRefresh"
          />

          <div class="case-edit" v-show="!isNameEdit && !editable">
            <div class="case-level" v-if="!isPublicShow">
              <priority-table-item
                :value="form.priority"
                :priority-options="priorityOptions"
              />
            </div>
            <div>
              <!--  版本历史 v-xpack -->
              <mx-version-history
                v-xpack
                v-show="versionEnable"
                ref="versionHistory"
                :current-id="currentTestCaseInfo.id"
                :is-read="versionReadOnly"
                :is-public-show="isPublicShow"
                :current-version-id="form.versionId"
                @confirmOtherInfo="confirmOtherInfo"
                :current-project-id="projectId"
                :has-latest="hasLatest"
                @setLatest="setLatest"
                @compare="compare"
                @compareBranch="compareBranch"
                @checkout="checkout"
                @create="createVersion"
                @del="del"
                @setIsLastedVersion="setIsLastedVersion"
                @setCurrentVersionName="setCurrentVersionName"
                @setLatestVersionId="setLatestVersionId"
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
          <!-- 公共用例库头部按钮展示 -->
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
            class="edit-public-row head-opt"
            v-if="isPublicShow"
            @click="editPublicCase"
            :class="[!hasReadonlyPermission === true ? '' : 'div-readOnly']"
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
            :class="[hasCopyPermission === true ? '' : 'div-readOnly']"
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
          <!-- 功能用例库头部按钮展示 -->
          <div
            class="follow-row head-opt" v-if="!isPublicShow && !hasReadonlyPermission"
            @click="toEdit"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_edit_outlined.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("commons.edit") }}</div>
          </div>
          <div
            class="follow-row head-opt"
            v-if="!showFollow && !isPublicShow && !hasReadonlyPermission"
            @click="saveFollow"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_collection_outlined.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("case.follow") }}</div>
          </div>
          <div
            class="follow-row head-opt"
            v-if="showFollow && !isPublicShow && !hasReadonlyPermission"
            @click="saveFollow"
          >
            <div class="icon-row">
              <img src="/assets/module/figma/icon_collect_filled.svg" alt="" />
            </div>
            <div class="label-row">{{ $t("case.followed") }}</div>
          </div>
          <div class="more-row head-opt" v-if="!isPublicShow && !hasReadonlyPermission">
            <div class="icon-row" @mouseenter="$refs.headMoreOptPopover.doShow()" @mouseleave="$refs.headMoreOptPopover.doClose()">
              <img src="/assets/module/figma/icon_more_outlined.svg" alt="" />
            </div>
            <div class="label-row">
              <el-popover
                placement="bottom-end"
                trigger="hover"
                popper-class="more-opt-item-popover"
                :visible-arrow="false"
                ref="headMoreOptPopover"
              >
                <div class="opt-row">
                  <div
                    class="add-public-row sub-opt-row"
                    v-if="!casePublic"
                    @click="addPublic"
                  >
                    <svg-icon icon-class="icon_add-folder_outlined" class-name="opt-icon"/>
                    <span class="title">{{ $t("case.add_to_public_case") }}</span>
                  </div>
                  <div
                    class="add-public-row sub-opt-row"
                    v-if="!isPublicShow && casePublic"
                    @click="removePublic"
                  >
                    <svg-icon icon-class="icon_yes_outlined" class-name="opt-icon"/>
                    <span class="title">{{ $t("case.added_to_public_case") }}</span>
                  </div>
                  <div class="split"></div>
                  <div class="copy-row sub-opt-row" @click="copyRow">
                    <svg-icon :icon-class="!hasCopyPermission ? 'icon_copy_outlined_disable' : 'icon_copy_outlined'" class-name="opt-icon"/>
                    <span class="title" :style="!hasCopyPermission ? 'color: rgb(187, 187, 187); cursor: not-allowed;' : 'cursor: pointer;'">{{ $t("commons.copy") }}</span>
                  </div>
                  <div class="split"></div>
                  <div class="delete-row sub-opt-row" @click="deleteRow">
                    <svg-icon :icon-class="!hasDeletePermission ? 'icon_delete-trash_outlined_disable' : 'icon_delete-trash_outlined_red'" class-name="opt-icon"/>
                    <span class="title" :style="!hasDeletePermission ? 'color: rgb(187, 187, 187); cursor: not-allowed;' : 'cursor: pointer;'">{{ $t("commons.delete") }}</span>
                  </div>
                </div>
                <div slot="reference">{{ $t("case.more") }}</div>
              </el-popover>
            </div>
          </div>
        </div>
      </div>
      <!-- 检测版本 是否不是最新 -->
      <div class="diff-latest-container" v-if="!editable && versionEnable && !isLastedVersion && !isPublicShow && !hasReadonlyPermission">
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
      <div v-loading="loading" class="edit-content-container" :class="{'editable-edit-content-container' : editable}">
        <case-edit-info-component
          :editable="editable"
          :editable-state="editableState"
          :richTextDefaultOpen="richTextDefaultOpen"
          :formLabelWidth="formLabelWidth"
          :read-only="readOnly"
          :project-id="projectId"
          :form="form"
          :is-copy="isCopy"
          :copy-case-id="caseId"
          :label-width="formLabelWidth"
          :is-public-show="isPublicShow"
          :is-readonly-user="hasReadonlyPermission"
          :case-id="caseId"
          :type="!caseId ? 'add' : 'edit'"
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
              :editable-state="editableState"
              :case-id="form.id"
              :project-id="projectId"
              :form="form"
              :is-form-alive="isFormAlive"
              :is-loading="loading"
              :read-only="readOnly || !editable"
              :enable-default-module="!caseId"
              :public-enable="isPublicShow"
              :show-input-tag="showInputTag"
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
          <!-- 保存 -->
          <div
            class="save-btn-row"
            v-if="showAddBtn || editableState">
            <el-button
              v-prevent-re-click
              size="small"
              type="primary"
              :disabled="readOnly || loading"
              @click="handleCommand(1)">
              {{ $t("commons.save") }}
            </el-button>
          </div>
          <!-- 保存并新建 -->
          <div class="save-create-row">
            <el-button
              v-if="showAddBtn && !editableState"
              v-prevent-re-click
              size="small"
              :disabled="readOnly || loading"
              @click="handleCommand(2)">
              {{ $t("case.saveAndCreate") }}
            </el-button>
          </div>
          <!-- 保存并添加到公共用例库 -->
          <div
            v-if="showPublic && !editableState"
            class="save-add-pub-row">
            <el-button size="small"
                       v-prevent-re-click
                       :disabled="readOnly || loading"
                       @click="handleCommand(3)">
              {{ $t("test_track.case.save_add_public") }}
            </el-button>
          </div>
          <!-- 取消 -->
          <div class="cancel-row">
            <el-button
              v-if="editableState"
              size="small"
              :disabled="readOnly || loading"
              @click="handleCommand(4)">
              {{ $t("commons.cancel") }}
            </el-button>
          </div>
        </template>
      </div>
    </div>
  <!-- since v2.7 -->
  <case-diff-side-viewer ref="caseDiffViewerRef"/>
   <version-create-other-info-select
      @confirmOtherInfo="confirmOtherInfo"
      ref="selectPropDialog"/>

    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDeleteVersion"/>
  </div>
</template>

<script>
import {getProjectVersions} from "metersphere-frontend/src/api/version";
import { TokenKey } from "metersphere-frontend/src/utils/constants";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {
  getCurrentProjectID,
  getCurrentUser, setCurrentProjectID,
} from "metersphere-frontend/src/utils/token";
import {
  hasLicense,
  hasPermission,
  hasPermissionForProjectId
} from "metersphere-frontend/src/utils/permission";
import {
  getUUID,
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
import FormRichTextItem from "@/business/case/components/richtext/FormRichTextItem";
import TestCaseStepItem from "@/business/case/components/TestCaseStepItem";
import StepChangeItem from "@/business/case/components/StepChangeItem";
import MsChangeHistory from "metersphere-frontend/src/components/history/ChangeHistory";
import { getTestTemplate } from "@/api/custom-field-template";
import CustomFiledFormItem from "metersphere-frontend/src/components/form/CustomFiledFormItem";
import VersionCreateOtherInfoSelect from "@/business/case/components/VersionCreateOtherInfoSelect";
import TestCaseBaseInfo from "@/business/case/components/TestCaseBaseInfo";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {useStore, useUserStore} from "@/store";
import { getProjectApplicationConfig } from "@/api/project-application";
import {
  deleteTestCaseVersion,
  getTestCase,
  getTestCaseFollow,
  getTestCaseVersions,
  hasTestCaseOtherInfo,
  testCaseEditFollows,
  testCaseGetByVersionId,
  testCaseDeleteToGc,
  getTestCaseByVersionId,
  getEditSimpleTestCase,
  getSimpleTestCase,
} from "@/api/testCase";

import {
  getProjectListAll,
  getProjectMemberOption, parseCustomFilesForItem,
} from "@/business/utils/sdk-utils";
import { testCaseCommentList } from "@/api/test-case-comment";
import {
  setLatestVersionById,
} from "metersphere-frontend/src/api/version";
import CaseEditInfoComponent from "./case/CaseEditInfoComponent";
import CaseBaseInfo from "./case/CaseBaseInfo";
import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
import MxVersionHistory from "./common/CaseVersionHistory"
import {versionEnableByProjectId} from "@/api/project";
import {openCaseEdit} from "@/business/case/test-case";
import ListItemDeleteConfirm from "metersphere-frontend/src/components/ListItemDeleteConfirm";
import CaseDiffSideViewer from "./case/diff/CaseDiffSideViewer";
import TestCaseEditNameView from "@/business/case/components/head/TestCaseEditNameView";

const store = useStore();

export default {
  name: "TestCaseEdit",
  components: {
    TestCaseEditNameView,
    PriorityTableItem,
    CaseEditInfoComponent,
    CaseBaseInfo,
    CustomFiledFormItem,
    StepChangeItem,
    TestCaseStepItem,
    FormRichTextItem,
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
        num: '',
        nodePath: "/未规划用例",
        maintainer: getCurrentUser().id,
        priority: "P0",
        type: "",
        method: "",
        prerequisite: "",
        testId: "",
        nodeId: '',
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
        versionId: ""
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
        nodeId: [
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
      oldData: null,
      newData: null,
      selectedOtherInfo: null,
      casePublic: false,
      isClickAttachmentTab: false,
      latestVersionId: "",
      hasLatest: false,
      currentTestCaseInfo: {},
      currentVersionName: "",
      versionEnable: false,
      // 是否为最新版本
      isLastedVersion: true,
      // 1 表示是直接保存
      // 2 表示式保存并创建
      // 3 表示
      saveType: 1,
      projectId: null,
      createVersionId: null,
      editableState: false,
      isNameEdit: false,
      useUserStore: {},
      priorityOptions: [],
      initLatestVersionId: ""
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
    publicCaseId: String,
  },
  computed: {
    hasCopyPermission() {
      return hasPermission('PROJECT_TRACK_CASE:READ+COPY')
    },
    hasDeletePermission() {
      return hasPermission('PROJECT_TRACK_CASE:READ+DELETE')
    },
    hasReadonlyPermission() {
      return hasPermission('PROJECT_TRACK_CASE:READ') && !hasPermission('PROJECT_TRACK_CASE:READ+EDIT')
    },
    routeProjectId() {
      return this.$route.query.projectId;
    },
    isCustomNum() {
      return store.currentProjectIsCustomNum;
    },
    richTextDefaultOpen() {
      return this.type === "edit" ? "preview" : "edit";
    },
    readOnly() {
      if (this.isPublicShow || this.hasReadonlyPermission) {
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
    versionReadOnly() {
      if (this.isPublicShow || this.hasReadonlyPermission) {
        return true;
      }
      const { rowClickHasPermission } = this.currentTestCaseInfo;
      if (rowClickHasPermission !== undefined) {
        return !rowClickHasPermission;
      }
      return hasPermission('PROJECT_TRACK_CASE:READ');
    },
    caseId() {
      return !this.isPublicShow ? this.$route.params.caseId : this.publicCaseId;
    },
    editType() {
      return this.$route.query.type;
    },
    isAdd() {
      return !this.caseId || this.isCopy;
    },
    editable() {
      return this.isAdd || this.editableState;
    },
    isCopy() {
      return this.editType === 'copy' || this.isPublicCopy;
    },
    isPublicCopy() {
      return this.editType === 'publicCopy';
    },
    showPublic() {
      return this.isPublic && this.isXpack;
    },
    showAddBtn() {
      return this.isAdd || this.showPublic;
    },
    titleNum() {
      if (!this.form.num) {
        return '';
      }
      return '【' +  (this.isCustomNum ? this.form.customNum : this.form.num) + '】';
    }
  },
  watch: {
    isAdd() {
      this.type = this.isAdd ? 'add' : 'edit';
    },
    form: {
      handler(val) {
        if (val && store.testCaseMap && this.form.id) {
          let change = store.testCaseMap.get(this.form.id);
          change = change + 1;
          store.testCaseMap.set(this.form.id, change);
        }
      },
      deep: true,
    },
    customFieldForm: {
      handler(val) {
        if (val && store.testCaseMap && this.form.id) {
          let change = store.testCaseMap.get(this.form.id);
          change = change + 1;
          store.testCaseMap.set(this.form.id, change);
        }
      },
      deep: true,
    }
  },
  beforeDestroy() {
    this.removeListener();
    this.$EventBus.$off(
      "handleSaveCaseWithEvent",
      this.handleSaveCaseWithEvent
    );
  },
  beforeRouteLeave(to, from, next) {
    document.title = localStorage.getItem("default-document-title");
    next();
  },
  mounted() {
    this.getSelectOptions();

    // Cascader 级联选择器: 点击文本就让它自动点击前面的input就可以触发选择。
    setInterval(function () {
      document.querySelectorAll(".el-cascader-node__label").forEach((el) => {
        el.onclick = function () {
          if (this.previousElementSibling) this.previousElementSibling.click();
        };
      });
    }, 1000);

    if (!(store.testCaseMap instanceof Map)) {
      store.testCaseMap = new Map();
    }
    if (this.form.id) {
      store.testCaseMap.set(this.form.id, 0);
    }
  },
  activated() {
    // 用例跳转编辑走这里
    this.loadTestCase();
  },
  created(){
    this.useUserStore = useUserStore();
    this.$EventBus.$on("handleSaveCaseWithEvent", this.handleSaveCaseWithEvent);
    this.setInitialVal();
  },
  methods: {
    setInitialVal() {
      if (this.isAdd && hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then(
          (r) => {
            let latestVersion = r.data.filter(version => version.latest);
            if (latestVersion && latestVersion.length === 1 && this.editable) {
              this.initLatestVersionId = latestVersion[0].id
            }
          }
        );
      }
    },
    checkoutLatest(){
      //切换最新版本
      this.checkoutByVersionId(this.latestVersionId);
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
    setLatestVersionId(versionId) {
      this.latestVersionId = versionId;
    },
    setIsLastedVersion(isLastedVersion) {
      this.isLastedVersion = isLastedVersion;
    },
    async freshTestCase(caseId) {
      this.loading = true;
      this.routerToEdit(caseId);
      this.$nextTick(() => {
        this.initEdit();
      });
    },
    async loadTestCase() {

      if (this.isPublicShow) {
        this.resetForm();
      }

      let initFuc = this.initEdit;
      this.loading = true;

      // 校验路径中的项目ID
      await this.checkCurrentProject();

      getTestTemplate(this.projectId).then((template) => {
        this.testCaseTemplate = template;
        store.testCaseTemplate = this.testCaseTemplate;
        initFuc();
      });

      getProjectApplicationConfig('CASE_CUSTOM_NUM', this.projectId)
        .then(result => {
          let data = result.data;
          if (data && data.typeValue === 'true') {
            store.currentProjectIsCustomNum = true;
          } else {
            store.currentProjectIsCustomNum = false;
          }
        });

      this.addListener(); //  添加 ctrl s 监听
      if (!this.projectList || this.projectList.length === 0) {
        //没有项目数据的话请求项目数据
        getProjectListAll().then((response) => {
          this.projectList = response.data; //获取当前工作空间所拥有的项目,
        });
      }

      getTestCaseFollow(this.caseId).then((response) => {
        this.form.follows = response.data;
        for (let i = 0; i < response.data.length; i++) {
          if (response.data[i] === this.currentUser().id) {
            this.showFollow = true;
            break;
          }
        }
      });

      getProjectApplicationConfig("CASE_PUBLIC", this.projectId).then((res) => {
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

      //浏览器拉伸时窗口编辑窗口自适应
      this.$nextTick(() => {
        // 解决错位问题
        window.addEventListener("resize", this.resizeContainer);
      });

      this.checkVersionEnable();
    },
    editPublicCase() {
      // 这个接口会校验权限
      getEditSimpleTestCase(this.caseId)
        .then((r) => {
          openCaseEdit({caseId: this.caseId, projectId: r.data.projectId},  this);
        })
        .catch(() => {});
    },
    copyPublicCase() {
      // 这里复制使用当前项目，不使用 projectId ，可能没有权限
      openCaseEdit({caseId: this.caseId, type: 'publicCopy', projectId: getCurrentProjectID()},  this);
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
    openNewTab() {
      if (this.editable || !this.form.id || this.isPublicShow) {
        return;
      }
      openCaseEdit({caseId: this.form.id}, this);
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
        store.testCaseMap.set(this.form.id, 0);
      }
    },
    addPublic() {
      getProjectApplicationConfig('CASE_PUBLIC')
        .then(res => {
          let data = res.data;
          if (data && data.typeValue === 'true') {
            this.casePublic = true;
            this.saveCase(true);
          } else {
            this.$warning(this.$t('test_track.case.public_warning'), false);
          }
        });
    },
    removePublic() {
      getProjectApplicationConfig('CASE_PUBLIC')
        .then(res => {
          let data = res.data;
          if (data && data.typeValue === 'true') {
            this.casePublic = false;
            this.saveCase(true);
          } else {
            this.$warning(this.$t('test_track.case.public_warning'), false);
          }
        });
    },
    handleCommand(e) {
      this.saveType = e;
      if (e === 3) {
        this.casePublic = true;
      } else if (e === 4) {
        this.editableState = false;
        this.$refs.otherInfo.caseActiveName = 'detail';
        this.loadTestCase();
        return;
      }
      this.saveCase();
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
    },
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => {
        this.isStepTableAlive = true;
        if (this.form.id) {
          store.testCaseMap.set(this.form.id, 0);
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
        document.title = this.$t('test_track.case.create_case');

        let user = JSON.parse(localStorage.getItem(TokenKey));
        this.form.maintainer = user.id;
        this.form.tags = [];
        this.form.versionId = localStorage.getItem("latest-version") || this.initLatestVersionId;
        this.getSelectOptions();
        this.customFieldForm = parseCustomField(
          this.form,
          this.testCaseTemplate,
          this.customFieldRules
        );
        this.setSystemFieldDefault();
        this.reload();
        this.loading = false;
      }
      if (callback) {
        callback();
      }
    },
    setSystemFieldDefault() {
      this.form.name = this.testCaseTemplate.caseName;
      this.form.prerequisite = this.testCaseTemplate.prerequisite;
      this.form.stepDescription = this.testCaseTemplate.stepDescription;
      this.form.expectedResult = this.testCaseTemplate.expectedResult;
      if (this.testCaseTemplate.steps) {
        this.form.steps = JSON.parse(this.testCaseTemplate.steps);
      }
    },
    async checkCurrentProject() {
      if (this.isPublicShow) {
        // 用例库查看用例
        await getSimpleTestCase(this.caseId).then((response) => {
          let testCase = response.data;
          this.projectId = testCase.projectId;
        });
      } else {
        this.projectId = this.routeProjectId;
        if (this.projectId) {
          // 带了 routeProjectId 校验是否是当前项目
          if (!hasPermissionForProjectId('PROJECT_TRACK_CASE:READ', this.projectId)) {
            // 没有该项目权限，跳转到根目录
            this.$router.push({path: "/"});
            return;
          }
          if (getCurrentProjectID() !== this.projectId) {
            setCurrentProjectID(this.projectId);
            location.reload();
            return;
          }
        }
        // 创建和复制
        if (this.isCopy || this.isAdd) {
          if (!this.projectId) {
            // 没带 routeProjectId 则使用当前项目
            this.projectId = getCurrentProjectID();
          }
          if (this.caseId) {
            // copy
            await getSimpleTestCase(this.caseId).then((response) => {
              let testCase = response.data;
              // 重置用例的项目ID
              testCase.projectId = this.projectId;
            });
          }
        } else if (this.caseId) {
          // 接口会校验是否有改用例的编辑权限
          await getEditSimpleTestCase(this.caseId).then((response) => {
            let testCase = response.data;
            if (getCurrentProjectID() !== testCase.projectId) {
              // 如果不是当前项目，跳到列表页
              this.$router.push({path: '/track/case/all', query: {projectId: getCurrentProjectID()}});
            } else {
              this.projectId = testCase.projectId;
            }
          })
          .catch(() => {
            // 没有权限则跳转到根路径
            this.$router.push("/");
          });
        }
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
          testCase.versionId = localStorage.getItem("latest-version") || this.initLatestVersionId;
          this.form.id = null;
          testCase.casePublic = false;
        }
        if (!this.isPublicShow) {
          document.title = testCase.name;
        }
        this.currentTestCaseInfo = testCase;
        this.setFormData(testCase);
        this.setTestCaseExtInfo(testCase);
        this.getSelectOptions();
        this.reload();
        this.$nextTick(() => {
          this.showInputTag = true;
        });

        if (this.isCopy) {
          this.form.id = null;
        }
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

      if (this.isPublicCopy) {
        // 如果是 copy 用例库的用例，责任人设置成当前用户
        let user = JSON.parse(localStorage.getItem(TokenKey));
        this.form.maintainer = user.id;
      }

      testCase.fields.forEach(i => {
        parseCustomFilesForItem(i);
      });

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
      if (!this.caseId) {
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
          this.priorityOptions = field.options;
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
    saveCase(isAddPublic) {
      if (this.validateForm()) {
        this._saveCase(isAddPublic);
      } else {
        if (this.$refs.versionHistory) {
          this.$refs.versionHistory.loading = false;
        }
        if (this.$refs.selectPropDialog) {
          this.$refs.selectPropDialog.close();
        }
      }
    },
    saveCaseWithoutRefresh() {
      if (this.validateForm()) {
        let param = this.buildParam();
        if (this.validate(param)) {
          let option = this.getOption(param);
          this.$request(option);
        }
      }
    },
    _saveCase(isAddPublic) {
      let param = this.buildParam();
      if (this.validate(param)) {
        let option = this.getOption(param);
        if (!isAddPublic) {
          this.loading = true;
        }
        this.$request(option)
          .then((response) => {
            if (this.editableState) {
               // 如果是编辑态保存用例, 则直接reload页面
               this.editableState = false;
               this.$refs.otherInfo.caseActiveName = 'detail';
            }
            response = response.data;
            // 保存用例后刷新附件
            this.currentTestCaseInfo.isCopy = false;
            if (this.$refs.otherInfo) {
              this.$refs.otherInfo.getFileMetaData(response.data.id);
            }
            this.loading = false;
            if (isAddPublic) {
              this.$success(this.casePublic ? this.$t("commons.add_success") : this.$t("commons.cancel_add_success"), false);
            } else {
              this.$success(this.$t("commons.save_success"), false);
            }
            this.operationType = "edit";
            this.$emit("refreshTestCase");
            store.testCaseMap.set(this.form.id, 0);
            this.$emit("refresh", response.data);
            if (this.form.id) {
              this.$emit("caseEdit", param);
              if (this.createVersionId) {
                // 如果是创建版本，创建完跳转到对应的版本
                this.createVersionId = null;
                this.freshTestCase(response.data.id);
              }
            } else {
              param.id = response.data.id;
              this.close();
              if (this.saveType === 2) {
                // 保存并创建
                this.initLatestVersionId = param.versionId;
                this.resetForm();
                this.initEdit();
              } else {
                this.isLastedVersion = true;
                this.routerToEdit(response.data.id);
                this.form.num = response.data.num;
                this.form.customNum = response.data.customNum;
                setTimeout(() => this.initEdit(), 500);
              }
            }
            this.createVersionId = null;
          })
          .catch(() => {
            this.loading = false;
            this.createVersionId = null;
          });
      }
    },
    routerToEdit(id) {
      this.$router.push({path: '/track/case/edit/' + id});
    },
    buildParam() {
      let param = {};
      if (this.isAdd) {
        this.form.id = null;
      }
      Object.assign(param, this.form);
      param.steps = JSON.stringify(this.form.steps);
      param.copyCaseId = this.caseId;
      param.projectId = this.projectId;

      param.name = param.name.trim();
      if (this.form.tags instanceof Array) {
        param.tags = JSON.stringify(this.form.tags);
      } else {
        param.tags = this.form.tags
      }
      //当 testId 为其他信息的时候必须删除该字段避免后端反序列化报错
      if ("other" != this.form.selected) {
        param.testId = JSON.stringify(this.form.selected);
      } else {
        delete param.selected;
      }
      param.casePublic = this.casePublic;
      param.type = "functional";
      if (this.isCopy) {
        this.testCaseTemplate.customFields.forEach((item) => {
          item.isEdit = false;
        });
      }
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
        if (this.$refs.otherInfo.getFilterCopyFiles() && this.$refs.otherInfo.getFilterCopyFiles().length > 0) {
          param.filterCopyFileMetaIds = this.$refs.otherInfo.getFilterCopyFiles();
        }
      }
      if (this.createVersionId) {
        param.versionId = this.createVersionId;
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
      this.form.versionId = "";
      this.form.tags = [];
    },
    addListener() {
      document.addEventListener("keydown", this.createCtrlSHandle);
    },
    removeListener() {
      document.removeEventListener("keydown", this.createCtrlSHandle);
    },
    createCtrlSHandle(event) {
      let curTabId = store.curTabId;
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
        testCaseEditFollows(this.form.id, this.form.follows).then(() => {
          this.$success(this.$t("commons.cancel_follow_success"), false);
        });
      } else {
        this.showFollow = true;
        if (!this.form.follows) {
          this.form.follows = [];
        }
        this.form.follows.push(this.currentUser().id);

        testCaseEditFollows(this.form.id, this.form.follows).then(() => {
          this.$success(this.$t("commons.follow_success"), false);
        });
      }
    },
    toEdit() {
      this.editableState = true;
    },
    setSpecialPropForCompare: function (that) {
      that.newData.tags = JSON.parse(that.newData.tags || "{}");
      that.newData.steps = JSON.parse(that.newData.steps || "{}");
      that.oldData.tags = JSON.parse(that.oldData.tags || "{}");
      that.oldData.steps = JSON.parse(that.oldData.steps || "{}");
      that.newData.readOnly = true;
      that.oldData.readOnly = true;
    },
    setCurrentVersionName(versionName) {
      this.currentVersionName = versionName;
    },
    compareBranchWithVersionId(originId, targetId){
      let originVersion = this.$refs.versionHistory.findVersionById(originId);
      let targetVersion = this.$refs.versionHistory.findVersionById(targetId);
      this.compareBranchWithVersionInfo(originVersion, targetVersion, this.currentTestCaseInfo.id);
    },
    compareBranchWithVersionInfo(originVersion, targetVersion){
      // 打开对比
      this.$refs.caseDiffViewerRef.open(originVersion, targetVersion, this.currentTestCaseInfo.id)
    },
    compareBranch(t1, t2) {
       this.compareBranchWithVersionInfo(t1, t2);
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
            }
          });
        }
      );
    },
    checkoutByVersionId(versionId) {
      getTestCaseByVersionId(this.form.refId, versionId)
        .then((response) => {
          this.routerToEdit(response.data.id);
        });
    },
    checkout(testCase) {
      this.freshTestCase(testCase.id);
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
      if (!this.$refs.testCaseBaseInfo.validateWithTip()) {
        isValidate = false;
      }
      return isValidate;
    },
    async createVersion(row) {
      if (this.validateForm()) {
        // 创建新版本
        let hasOtherInfo = await this.hasOtherInfo();
        if (hasOtherInfo) {
          this.$refs.versionHistory.loading = false;
          this.$refs.selectPropDialog.open(row.id);
        } else {
          this.createVersionId = row.id;
          this.saveCase();
        }
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
                this.$refs.versionHistory.getVersionOptionList();
              });
            } else {
              that.$refs.versionHistory.loading = false;
            }
          },
        }
      );
    },
    setLatest(version) {
      let param = {
        projectId: this.projectId,
        type: "TEST_CASE",
        versionId: version.id,
        resourceId: this.caseId,
      };
      setLatestVersionById(param).then(() => {
        this.$success(this.$t("commons.modify_success"));
        this.checkoutByVersionId(version.id);
      }).catch(() => {
        this.$refs.versionHistory.loading = false;
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
      this.createVersionId = selectedOtherInfo.versionId;
      this.saveCase();
    },
    copyRow() {
      if (!hasPermission('PROJECT_TRACK_CASE:READ+COPY')) {
        return;
      }
      openCaseEdit({caseId: this.testCase.id, type: 'copy', projectId: this.projectId},  this);
    },
    deleteRow() {
      if (!hasPermission('PROJECT_TRACK_CASE:READ+DELETE')) {
        return;
      }
      getTestCaseVersions(this.testCase.id)
        .then(response => {
          if (hasLicense() && this.versionEnable && response.data.length > 1) {
            // 删除提供列表删除和全部版本删除
            this.$refs.apiDeleteConfirm.open(this.testCase, this.$t('test_track.case.delete_confirm'));
          } else {
            let title = this.$t('test_track.case.case_delete_confirm') + ": " + this.testCase.name + "?";
            this.$confirm(this.$t('test_track.case.batch_delete_soft_tip'), title, {
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
.div-readOnly{
  pointer-events: none;
}
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
      flex-grow: 1;
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
            padding: 0.1rem 0.4rem;

            .version-icon {
              width: 20.17px;
              height: 15.6px;
              margin-right: px2rem(5);

              img {
                width: 100%;
                height: 100%;
              }
            }

            .version-title {
              height: px2rem(22);
              line-height: px2rem(22);
              margin-top: px2rem(2);
            }
            .version-suffix {
              height: px2rem(22);
              line-height: px2rem(22);
              margin-left: px2rem(5);
              margin-top: px2rem(1);
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
          padding: 0.1rem 0.4rem 0.1rem 0;
        }

        .next-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
          padding: 0.1rem 0 0.1rem 0.4rem;
        }

        .follow-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(10);
          padding: 0.1rem 0.4rem;
        }

        .edit-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
          padding: 0.1rem 0.4rem;
        }

        .copy-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(20.67);
          padding: 0.1rem 0.4rem;
        }
        .close-row.head-opt {
          padding: 0.15rem;
        }
        .add-public-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(11);
          padding: 0 0.5rem;
        }

        .more-row.head-opt {
          .icon-row {
            img {
            }
          }

          .label-row {
          }
          margin-right: px2rem(24);
          padding: 0.1rem 0.4rem;
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
          padding: 0.3rem;
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
          padding: 0.3rem;
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
      justify-content: flex-start;
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
        margin: 0 0 0 px2rem(24);
        el-button {
        }
      }

      .save-create-row {
        margin-left: px2rem(12);
        el-button {
        }
      }

      .save-add-pub-row {
        margin-left: px2rem(12);
        el-button {
        }
      }
    }
  }
}

:deep(.el-button--small span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
}
</style>
<style>
/* 用例编辑头部操作更多popover样式 */
.more-opt-item-popover {
  padding: 0 !important;
  min-width: 118px !important;
}

.more-opt-item-popover .opt-row {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.more-opt-item-popover .sub-opt-row {
  display: flex;
  width: 100%;
  height: 32px;
  margin: 3px 0 4px;
  line-height: 32px;
  cursor: pointer;
}

.more-opt-item-popover .sub-opt-row:hover {
  background-color: rgba(31, 35, 41, 0.1);
}

.more-opt-item-popover .sub-opt-row .opt-icon {
  position: relative;
  top: 10px;
  left: 13px;
  margin-right: 23px;
  width: 1.2em;
  height: 1.2em;
}

.more-opt-item-popover .sub-opt-row .title {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  letter-spacing: -0.1px;
  color: #1f2329;
  margin-right: 10px;
}

.more-opt-item-popover .split {
  width: 170px;
  height: 1px;
  background-color: rgba(31, 35, 41, 0.15);
}

.more-opt-item-popover .delete-row {
  background-color: transparent;
  padding: 0;
}

.more-opt-item-popover .delete-row .title {
  color: #f54a45;
}

/* 附件上传popover样式 */
.attachment-popover {
  padding: 0 !important;
  height: 80px;
  min-width: 120px !important;
}
</style>
