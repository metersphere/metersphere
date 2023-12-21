<template>

  <div>

    <el-dialog v-loading="loading"
               :close-on-click-modal="false"
               :destroy-on-close="true"
               append-to-body
               :title="operationType === 'edit' ? $t('test_track.plan.edit_plan') : $t('test_track.plan.create_plan')"
               :visible.sync="dialogFormVisible"
               @close="close"
               top="8vh"
               width="60%">

      <el-form :model="form" :rules="rules" ref="planFrom" v-if="isStepTableAlive">
        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item
                :label="$t('test_track.plan.plan_name')"
                :label-width="formLabelWidth"
                prop="name">
              <el-input v-model="form.name" :placeholder="$t('test_track.plan.input_plan_name')"
                        :size="itemSize" maxlength="128" show-word-limit/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item prop="nodeId" :label="$t('test_track.case.module')" :label-width="formLabelWidth">
              <ms-select-tree
                  class="plan-node-tree"
                  :disabled="false"
                  :data="treeNodes"
                  :obj="moduleObj"
                  :default-key="form.nodeId"
                  checkStrictly
                  @getValue="setModule"
                  size="small"
                  ref="moduleTree"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('test_track.plan.plan_principal')" :label-width="formLabelWidth" prop="principals">
              <el-select v-model="form.principals" :placeholder="$t('test_track.plan.input_plan_principal')"
                         style="width: 100%;"
                         filterable multiple :size="itemSize">
                <el-option
                    v-for="(item) in principalOptions"
                    :key="item.id"
                    :label="item.name + '(' + item.id + ')'"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="$t('test_track.plan.plan_stage')" :label-width="formLabelWidth" prop="stage">
              <el-select v-model="form.stage" clearable :placeholder="$t('test_track.plan.input_plan_stage')"
                         style="width: 100%;" :size="itemSize">
                <el-option v-for="item in stageOption" :key="item.value" :label="$t(item.text)" :value="item.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!--start:xuxm增加自定义‘计划开始’，‘计划结束’时间字段-->
        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item
                :label="$t('test_track.plan.planned_start_time')"
                :label-width="formLabelWidth"
                prop="plannedStartTime">
              <el-date-picker :placeholder="$t('test_track.plan.planned_start_time')" v-model="form.plannedStartTime"
                              :size="itemSize"
                              type="datetime" value-format="timestamp" style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item
                :label="$t('test_track.plan.planned_end_time')"
                :label-width="formLabelWidth"
                prop="plannedEndTime">
              <el-date-picker :placeholder="$t('test_track.plan.planned_end_time')" v-model="form.plannedEndTime"
                              :size="itemSize"
                              type="datetime" value-format="timestamp" style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <!--end:xuxm增加自定义‘计划开始’，‘计划结束’时间字段-->

        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('commons.tag')" :label-width="formLabelWidth" prop="tag">
              <ms-input-tag :currentScenario="form" ref="tag" :size="itemSize"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item
                :label="$t('test_track.plan_view.automatically_update_status')"
                label-width="140px"
                prop="automaticStatusUpdate">
              <el-switch v-model="form.automaticStatusUpdate"/>
              <ms-instructions-icon :content="$t('test_track.plan_view.automatically_update_status_tip')"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                :label="$t('test_track.plan_view.allow_associated_repetitive_cases')"
                label-width="140px"
                prop="automaticStatusUpdate">
              <el-switch v-model="form.repeatCase"/>
              <ms-instructions-icon :content="$t('test_track.plan_view.allow_associated_repetitive_cases_tip')"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" justify="left" :gutter="20">
          <el-col :span="24">
            <el-form-item :label="$t('commons.description')" :label-width="formLabelWidth" prop="description">
              <el-input v-model="form.description"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 4}"
                        :rows="2"
                        :placeholder="$t('commons.input_content')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="operationType === 'edit'" type="flex" justify="left" :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('test_track.plan.plan_status')" :label-width="formLabelWidth" prop="status">
              <test-plan-status-button :status="form.status" @statusChange="statusChange"/>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>

      <template v-slot:footer>

        <div class="dialog-footer">
          <el-button
              v-prevent-re-click
              @click="dialogFormVisible = false">
            {{ $t('test_track.cancel') }}
          </el-button>
          <el-button
              type="primary"
              v-prevent-re-click
              @click="savePlan">
            {{ $t('test_track.confirm') }}
          </el-button>
          <el-button type="primary" v-prevent-re-click
                     @click="testPlanInfo">
            {{ $t('test_track.planning_execution') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>


</template>

<script>
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import TestPlanStatusButton from "../common/TestPlanStatusButton";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import MsInputTag from "metersphere-frontend/src/components/MsInputTag";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import {getPlanStageOption, testPlanAdd, testPlanEdit} from "@/api/remote/plan/test-plan";
import {buildTree, getProjectMemberOption} from "@/business/utils/sdk-utils";
import {getTestPlanNodes} from "@/api/test-plan-node";

export default {
  name: "TestPlanEdit",
  components: {MsInstructionsIcon, TestPlanStatusButton, MsInputTag, MsSelectTree},
  data() {
    return {
      isStepTableAlive: true,
      dialogFormVisible: false,
      itemSize: "small",
      loading: false,
      form: {
        name: '',
        projectIds: [],
        principals: [],
        stage: '',
        description: '',
        plannedStartTime: '',
        plannedEndTime: '',
        automaticStatusUpdate: false,
        repeatCase: false,
        follows: [],
        nodeId: '',
        nodePath: ''
      },
      rules: {
        name: [
          {required: true, message: this.$t('test_track.plan.input_plan_name'), trigger: 'blur'},
          {max: 128, message: this.$t('test_track.length_less_than') + '128', trigger: 'blur'}
        ],
        nodeId: [{required: true, message: this.$t("api_test.environment.module_warning"), trigger: "change"}],
        principals: [{required: true, message: this.$t('test_track.plan.input_plan_principal'), trigger: 'change'}],
        stage: [{required: true, message: this.$t('test_track.plan.input_plan_stage'), trigger: 'change'}],
        description: [{max: 200, message: this.$t('test_track.length_less_than') + '200', trigger: 'blur'}]
      },
      formLabelWidth: "100px",
      operationType: '',
      principalOptions: [],
      stageOption: [],
      defaultNode: null,
      treeNodes: null,
      moduleObj: {
        id: "id",
        label: "name",
      }
    };
  },
  created() {
    //设置“测试阶段”和“负责人”的默认值
    this.form.stage = 'smoke';
    getPlanStageOption()
        .then((r) => {
          this.stageOption = r.data;
        });
    this.getNodeTrees();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    getNodeTrees() {
      getTestPlanNodes(this.projectId, {})
          .then(r => {
            let treeNodes = r.data;
            treeNodes.forEach(node => {
              buildTree(node, {path: ''});
            });
            this.treeNodes = treeNodes;
            if (this.operationType === 'add') {
              this.setDefaultModule();
            }
          });
    },
    setDefaultModule() {
      if (this.defaultNode == null) {
        this.setUnplannedModule(this.treeNodes);
      } else {
        this.form.nodeId = this.defaultNode.data.id;
        let node = this.findTreeNode(this.treeNodes);
        if (node) {
          this.form.nodePath = node.path;
        } else {
          // 如果模块已删除，设置为未规划模块
          this.setUnplannedModule(this.treeNodes);
        }
      }
    },
    findTreeNode(nodeArray) {
      for (let i = 0; i < nodeArray.length; i++) {
        let node = nodeArray[i];
        if (node.id === this.form.nodeId) {
          return node;
        } else {
          if (node.children && node.children.length > 0) {
            let findNode = this.findTreeNode(node.children);
            if (findNode != null) {
              return findNode;
            }
          }
        }
      }
    },
    setUnplannedModule(treeNodes) {
      // 创建不带模块ID，设置成为规划模块
      this.form.nodeId = treeNodes[0].id;
      this.form.nodePath = treeNodes[0].path;
    },
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => (this.isStepTableAlive = true));
    },
    openTestPlanEditDialog(testPlan, selectDefaultNode) {
      this.resetForm();
      this.defaultNode = selectDefaultNode;
      this.getNodeTrees();
      this.setPrincipalOptions();
      this.operationType = 'add';
      if (testPlan) {
        //修改
        this.operationType = 'edit';
        let tmp = {};
        Object.assign(tmp, testPlan);
        Object.assign(this.form, tmp);
      } else {
        this.form.tags = [];
      }
      listenGoBack(this.close);
      this.setEmptyStage();
      this.dialogFormVisible = true;
      this.reload();
    },
    setEmptyStage() {
      // 如果测试阶段选项中没有当前值，则置空
      let hasOptions = false;
      this.stageOption.forEach(item => {
        if (item.value === this.form.stage) {
          hasOptions = true;
          return;
        }
      });
      if (!hasOptions) {
        this.form.stage = '';
      }
    },
    testPlanInfo() {
      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (!this.validate(param)) {
            return;
          }
          param.workspaceId = getCurrentWorkspaceId();
          if (this.form.tags instanceof Array) {
            this.form.tags = JSON.stringify(this.form.tags);
          }
          param.tags = this.form.tags;
          this.loading = true;
          let method = testPlanAdd;
          if (this.operationType === 'edit') {
            method = testPlanEdit;
          }
          method(param)
              .then(response => {
                this.loading = false;
                if (this.operationType === 'add') {
                  this.$success(this.$t('commons.save_success'));
                }
                this.dialogFormVisible = false;
                this.$router.push('/track/plan/view/' + response.data.id);
              }).catch(() => {
                this.loading = false;
              });
        } else {
          return false;
        }
      });
    },
    savePlan() {
      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (!this.validate(param)) {
            return;
          }
          param.workspaceId = getCurrentWorkspaceId();
          if (this.form.tags instanceof Array) {
            this.form.tags = JSON.stringify(this.form.tags);
          }
          param.tags = this.form.tags;

          this.loading = true;
          let method = testPlanAdd;
          if (this.operationType === 'edit') {
            method = testPlanEdit;
          }
          method(param)
              .then(() => {
                this.loading = false;
                this.$success(this.$t('commons.save_success'));
                this.dialogFormVisible = false;
                this.$emit("refresh");
              }).catch(() => {
                this.loading = false;
              });
        } else {
          return false;
        }
      });
    },
    validate(param) {
      if (param.name === '') {
        this.$warning(this.$t('test_track.plan.input_plan_name'));
        return false;
      }
      if (param.plannedStartTime > param.plannedEndTime) {
        this.$warning(this.$t('commons.date.data_time_error'));
        return false;
      }
      return true;
    },
    setPrincipalOptions() {
      getProjectMemberOption()
          .then(response => {
            this.principalOptions = response.data;
          });
    },
    statusChange(status) {
      this.form.status = status;
      this.$forceUpdate();
    },
    close() {
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
    },
    resetForm() {
      //防止点击修改后，点击新建触发校验
      if (this.$refs['planFrom']) {
        this.$refs['planFrom'].validate(() => {
          this.$refs['planFrom'].resetFields();
          this.form.name = '';
          this.form.projectIds = [];
          this.form.principals = [];
          this.form.follows = [];
          this.form.automaticStatusUpdate = false;
          this.form.stage = 'smoke';
          this.form.description = '';
          this.form.status = null;
          this.form.plannedStartTime = null;
          this.form.plannedEndTime = null;
          this.form.nodeId = '';
          this.form.nodePath = '';
          return true;
        });
      }
    },
    setModule(id, data) {
      if (data) {
        this.form.nodeId = id;
        this.form.nodePath = data.path;
      }
    }
  }
}
</script>

<style scoped>
.instructions-icon {
  margin-left: 10px;
}
</style>
