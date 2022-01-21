<template>
  <div>
    <el-row>
      <el-col :span="12">
        <el-tag>当前{{oldData.versionName }}</el-tag><span style="margin-left: 10px">{{oldData.userName}}</span><span style="margin-left: 10px">{{oldData.createTime | timestampFormatDate }}</span>
      </el-col>
      <el-col :span="12">
        <el-tag>{{ newData.versionName }}</el-tag><span style="margin-left: 10px">{{newData.userName}}</span><span style="margin-left: 10px">{{newData.createTime | timestampFormatDate }}</span>
      </el-col>
    </el-row>
    <div class="compare-class" v-loading="isReloadData">
      <el-card style="width: 50%;" ref="old">
        <el-form :model="oldData" ref="old" class="case-form" v-loading="oldLoading">
          <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
          <el-row>
            <el-col :span="8">
              <el-form-item
                :placeholder="$t('test_track.case.input_name')"
                :label="$t('test_track.case.name')"
                :label-width="oldData.formLabelWidth"
                prop="name">
                <el-input :disabled="oldData.readOnly" v-model="oldData.name" size="small"
                          class="ms-case-input"></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('test_track.case.module')" :label-width="oldData.formLabelWidth" prop="module" v-if="!isPublic">
                <ms-select-tree :disabled="oldData.readOnly" :data="treeNodes" :defaultKey="oldData.module"
                                :obj="moduleObj"
                                @getValue="setModule" clearable checkStrictly size="small"/>
              </el-form-item>
            </el-col>


            <el-col :span="8">
              <el-form-item :label="$t('test_track.case.project')" :label-width="oldData.formLabelWidth" prop="projectId"
                            v-if="isPublic" >
                <el-select v-model="oldData.projectId" filterable clearable :disabled="oldData.readOnly">
                  <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('commons.tag')" :label-width="oldData.formLabelWidth" prop="tag">
                <ms-input-tag :read-only="oldData.readOnly" :currentScenario="oldData" v-if="showInputTag" ref="tag"
                              class="ms-case-input"/>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 自定义字段 -->
          <el-form v-if="oldData.isFormAlive" :model="oldData.customFieldForm" :rules="oldData.customFieldRules"
                   ref="oldCustomFieldForm"
                   class="case-form">
            <custom-filed-form-item :form="oldData.customFieldForm" :form-label-width="oldData.formLabelWidth"
                                    :issue-template="oldData.testCaseTemplate" :is-public="isPublic"/>
          </el-form>

          <el-row v-if="oldData.isCustomNum">
            <el-col :span="7">
              <el-form-item label="ID" :label-width="oldData.formLabelWidth" prop="customNum">
                <el-input :disabled="oldData.readOnly" v-model.trim="oldData.customNum" size="small"
                          class="ms-case-input"></el-input>
              </el-form-item>
            </el-col>
          </el-row>


          <ms-form-divider :title="$t('test_track.case.step_info')"/>

          <form-rich-text-item :disabled="oldData.readOnly" :label-width="oldData.formLabelWidth"
                               :title="$t('test_track.case.prerequisite')" :data="oldData" prop="prerequisite"/>

          <step-change-item :label-width="oldData.formLabelWidth" :form="oldData"/>
          <form-rich-text-item :disabled="oldData.readOnly" :label-width="oldData.formLabelWidth"
                               v-if="oldData.stepModel === 'TEXT'"
                               :title="$t('test_track.case.step_desc')" :data="oldData" prop="stepDescription"/>
          <form-rich-text-item :disabled="oldData.readOnly" :label-width="oldData.formLabelWidth"
                               v-if="oldData.stepModel === 'TEXT'"
                               :title="$t('test_track.case.expected_results')" :data="oldData" prop="expectedResult"/>

          <test-case-step-item :label-width="oldData.formLabelWidth"
                               v-if="oldData.stepModel === 'STEP' || !oldData.stepModel"
                               :form="oldData" :read-only="oldData.readOnly"/>

          <ms-form-divider :title="$t('test_track.case.other_info')"/>

          <test-case-edit-other-info :read-only="oldData.readOnly" :project-id="projectIds" :form="oldData"
                                     :label-width="oldData.formLabelWidth" :case-id="oldData.id" ref="oldOtherInfo"/>

          <el-row style="margin-top: 10px">
            <el-col :span="20" :offset="1">{{ $t('test_track.review.comment') }}:
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="20" :offset="1">

              <review-comment-item v-for="(comment,index) in oldData.comments"
                                   :key="index"
                                   :comment="comment"
                                   @refresh="getComments" api-url="/test/case"/>
              <div v-if="oldData.comments && oldData.comments.length === 0" style="text-align: center">
                <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
                      <span style="font-size: 15px; color: #8a8b8d;">
                        {{ $t('test_track.comment.no_comment') }}
                      </span>
                </i>
              </div>
            </el-col>
          </el-row>
          <test-case-comment :case-id="oldData.id"
                             @getComments="getComments" ref="testCaseComment"/>

        </el-form>
      </el-card>
      <el-card style="width: 50%;" ref="new">
        <el-form :model="newData" ref="new" class="case-form" v-loading="newLoading">
          <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
          <el-row>
            <el-col :span="8">
              <el-form-item
                :placeholder="$t('test_track.case.input_name')"
                :label="$t('test_track.case.name')"
                :label-width="newData.formLabelWidth"
                prop="name">
                <el-input :disabled="newData.readOnly" v-model="newData.name" size="small"
                          class="ms-case-input"></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('test_track.case.module')" :label-width="newData.formLabelWidth" prop="module" v-if="!isPublic">
                <ms-select-tree :disabled="newData.readOnly" :data="treeNodes" :defaultKey="newData.module"
                                :obj="moduleObj"
                                @getValue="setModule" clearable checkStrictly size="small"/>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('test_track.case.project')" :label-width="newData.formLabelWidth" prop="projectId"
                            v-if="isPublic" >
                <el-select v-model="newData.projectId" filterable clearable :disabled="newData.readOnly">
                  <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('commons.tag')" :label-width="newData.formLabelWidth" prop="tag">
                <ms-input-tag :read-only="newData.readOnly" :currentScenario="newData" v-if="showInputTag" ref="tag"
                              class="ms-case-input"/>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 自定义字段 -->
          <el-form v-if="newData.isFormAlive" :model="newData.customFieldForm" :rules="newData.customFieldRules"
                   ref="newCustomFieldForm"
                   class="case-form">
            <custom-filed-form-item :form="newData.customFieldForm" :form-label-width="newData.formLabelWidth"
                                    :issue-template="newData.testCaseTemplate" :is-public="isPublic"/>
          </el-form>

          <el-row v-if="newData.isCustomNum">
            <el-col :span="7">
              <el-form-item label="ID" :label-width="newData.formLabelWidth" prop="customNum">
                <el-input :disabled="newData.readOnly" v-model.trim="newData.customNum" size="small"
                          class="ms-case-input"></el-input>
              </el-form-item>
            </el-col>
          </el-row>


          <ms-form-divider :title="$t('test_track.case.step_info')"/>

          <form-rich-text-item :disabled="newData.readOnly" :label-width="newData.formLabelWidth"
                               :title="$t('test_track.case.prerequisite')" :data="newData" prop="prerequisite"/>

          <step-change-item :label-width="newData.formLabelWidth" :form="newData"/>
          <form-rich-text-item :disabled="newData.readOnly" :label-width="newData.formLabelWidth"
                               v-if="newData.stepModel === 'TEXT'"
                               :title="$t('test_track.case.step_desc')" :data="newData" prop="stepDescription"/>
          <form-rich-text-item :disabled="newData.readOnly" :label-width="newData.formLabelWidth"
                               v-if="newData.stepModel === 'TEXT'"
                               :title="$t('test_track.case.expected_results')" :data="newData" prop="expectedResult"/>

          <test-case-step-item :label-width="newData.formLabelWidth"
                               v-if="newData.stepModel === 'STEP' || !newData.stepModel"
                               :form="newData" :read-only="newData.readOnly"/>

          <ms-form-divider :title="$t('test_track.case.other_info')"/>

          <test-case-edit-other-info :read-only="newData.readOnly" :project-id="projectIds" :form="newData"
                                     :label-width="newData.formLabelWidth" :case-id="newData.id" ref="newOtherInfo"/>

          <el-row style="margin-top: 10px">
            <el-col :span="20" :offset="1">{{ $t('test_track.review.comment') }}:
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="20" :offset="1">

              <review-comment-item v-for="(comment,index) in newData.comments"
                                   :key="index"
                                   :comment="comment"
                                   @refresh="getComments" api-url="/test/case"/>
              <div v-if="newData.comments && newData.comments.length === 0" style="text-align: center">
                <i class="el-icon-chat-line-square" style="font-size: 15px;color: #8a8b8d;">
                      <span style="font-size: 15px; color: #8a8b8d;">
                        {{ $t('test_track.comment.no_comment') }}
                      </span>
                </i>
              </div>
            </el-col>
          </el-row>
          <test-case-comment :case-id="newData.id"
                             @getComments="getComments" ref="testCaseComment"/>

        </el-form>

      </el-card>
    </div>
  </div>
</template>
<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import {getCurrentProjectID, getCurrentUser, removeGoBackListener} from "@/common/js/utils";
import {buildTestCaseOldFields, getTemplate, parseCustomField} from "@/common/js/custom_field";
import {STEP} from "@/business/components/api/automation/scenario/Setting";
import TestCaseEditOtherInfo from "@/business/components/track/case/components/TestCaseEditOtherInfo";
import TestCaseStepItem from "@/business/components/track/case/components/TestCaseStepItem";
import StepChangeItem from "@/business/components/track/case/components/StepChangeItem";
import TestCaseComment from "@/business/components/track/case/components/TestCaseComment";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import MsSelectTree from "../../../common/select-tree/SelectTree";
import CustomFiledFormItem from "@/business/components/common/components/form/CustomFiledFormItem";
import ReviewCommentItem from "@/business/components/track/review/commom/ReviewCommentItem";

const {diff} = require("@/business/components/performance/v_node_diff");

export default {
  name: "TestCaseVersionDiff",
  components: {
    MsFormDivider,
    MsInputTag,
    TestCaseEditOtherInfo,
    TestCaseStepItem,
    StepChangeItem,
    TestCaseComment,
    FormRichTextItem,
    MsSelectTree,
    CustomFiledFormItem,
    ReviewCommentItem,
  },
  props: {
    oldData: {
      type: Object
    },
    newData: {
      type: Object
    },
    treeNodes: [],
    isPublic: {
      type: Boolean,
      default() {
        return false;
      }
    }
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
    }
  },
  data() {
    return {
      path: "/test/case/add",
      isXpack: false,
      projectList: [],
      result: {},
      dialogFormVisible: false,
      workspaceId: '',
      formLabelWidth: "100px",
      operationType: '',
      methodOptions: [
        {value: 'auto', label: this.$t('test_track.case.auto')},
        {value: 'manual', label: this.$t('test_track.case.manual')}
      ],
      testCase: {},
      testCases: [],
      index: 0,
      showInputTag: true,
      tableType: "",
      stepFilter: new STEP,
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      versionData: [],
      dialogVisible: false,
      maintainerOptions: [],
      oldLoading: null,
      newLoading: null,
      isReloadData:true
    };
  },
  mounted() {
    this.oldLoading = true;
    this.oldLoading = true;
    this.getProjectList();
    this.getComments("newData");
    this.getComments("oldData");
    this.open("oldData");
    this.open("newData");
    this.$nextTick(function () {
      setTimeout(this.getDiff,(this.$refs.old.$children.length+1)*1000)
    })
  },
  methods: {
    getDiff() {
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      let oldColor = "";
      let newColor = "";
      if(this.oldData.createTime>this.newData.createTime){
        oldColor = "rgb(121, 225, 153,0.3)";
        newColor = "rgb(241,200,196,0.45)"
      }else{
        oldColor = "rgb(241,200,196,0.45)"
        newColor = "rgb(121, 225, 153,0.3)";
      }
      diff(oldVnode,vnode,oldColor,newColor);
      this.isReloadData = false
    },
    alert: alert,
    currentUser: () => {
      return getCurrentUser();
    },
    setModule(id, data) {
      this.form.module = id;
      this.form.nodePath = data.path;
    },
    setDefaultValue(prop) {
      if (!this[prop].prerequisite) {
        this[prop].prerequisite = "";
      }
      if (!this[prop].stepDescription) {
        this[prop].stepDescription = "";
      }
      if (!this[prop].expectedResult) {
        this[prop].expectedResult = "";
      }
      if (!this[prop].remark) {
        this[prop].remark = "";
      }
      this.$store.state.testCaseMap.set(this[prop].id, 0);
    },
    openComment() {
      this.$refs.testCaseComment.open()
    },
    getComments(prop, callback) {
      let id = '';
      if (this[prop]) {
        id = this[prop].id;
      } else {
        id = this.form.id;
      }
      this.result = this.$get('/test/case/comment/list/' + id, res => {
        this[prop].comments = res.data;
        if (callback) {
          callback();
        }
      })
    },
    reloadForm(prop) {
      this[prop].isFormAlive = false;
      this.$nextTick(() => {
        this[prop].isFormAlive = true;
        if (prop.indexOf("new") != -1) {
          this.newLoading = false;
        } else {
          this.oldLoading = false;
        }
      });
    },
    open(prop) {
      this[prop].projectId = this.projectIds;
      let initFuc = this.initEdit;
      this[prop].$get = this.$get;
      let that = this;
      getTemplate('field/template/case/get/relate/', this[prop])
        .then((template) => {
          this[prop].testCaseTemplate = template;
          initFuc(prop, () => {
            that.reloadForm(prop);
          });
        });
    },
    initEdit(prop, callback) {
      this.setFormData(prop);
      // this.setTestCaseExtInfo(prop);
      this.getSelectOptions();
      if (callback) {
        callback();
      }
    },
    setFormData(prop) {
      try {
        this[prop].selected = JSON.parse(this[prop].testId);
      } catch (error) {
        this[prop].selected = this[prop].testId
      }
      let tmp = {};
      Object.assign(tmp, this[prop]);
      if (!tmp.steps || tmp.steps.length < 1) {
        tmp.steps = [{
          num: 1,
          desc: '',
          result: ''
        }];
      }
      Object.assign(this[prop], tmp);
      if (!this[prop].stepModel) {
        this[prop].stepModel = "STEP";
      }
      this[prop].module = this[prop].nodeId;
      //设置自定义熟悉默认值
      this[prop].customFieldForm = parseCustomField(this[prop], this[prop].testCaseTemplate, null, this[prop] ? buildTestCaseOldFields(this[prop]) : null);
    },
    setTestCaseExtInfo(prop) {
      this[prop] = {};
      if (this[prop]) {
        // 复制 不查询评论
        this[prop] = this[prop].isCopy ? {} : this[prop];
      }
    }
    ,
    close() {
      //移除监听，防止监听其他页面
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
    }
    ,
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
      });
    }
    ,
    getSelectOptions() {
      this.getMaintainerOptions();
    }
    ,
    resetForm(prop) {
      this[prop].name = '';
      this[prop].module = '';
      this[prop].type = '';
      this[prop].method = '';
      this[prop].maintainer = '';
      this[prop].priority = '';
      this[prop].prerequisite = '';
      this[prop].remark = '';
      this[prop].testId = '';
      this[prop].testName = '';
      this[prop].steps = [{
        num: 1,
        desc: '',
        result: ''
      }];
      this[prop].customNum = '';
    }
    ,
    getProjectList() {
      if (!this.projectList || this.projectList.length === 0) {   //没有项目数据的话请求项目数据
        this.$get("/project/listAll", (response) => {
          this.projectList = response.data;  //获取当前工作空间所拥有的项目,
        })
      }
    }
  }
}
</script>
<style scoped>
.compare-class {
  display: flex;
  justify-content: space-between;
}
</style>
