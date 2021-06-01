<template>
  <el-dialog :close-on-click-modal="false" :title="$t('test_track.case.create')" :visible.sync="visible"
             width="45%"
             :destroy-on-close="true">
    <el-form :model="testCaseForm" label-position="right" label-width="80px" size="small" :rules="rule"
             ref="testCaseForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="testCaseForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
      </el-form-item>

      <el-form-item :label="$t('api_test.automation.scenario.principal')" prop="maintainer">
        <el-select v-model="testCaseForm.maintainer"
                   :placeholder="$t('api_test.automation.scenario.principal')" filterable size="small"
                   style="width: 100%">
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('api_test.automation.scenario.follow_people')" prop="followPeople">
        <el-select v-model="testCaseForm.followPeople"
                   :placeholder="$t('api_test.automation.scenario.follow_people')" filterable size="small"
                   style="width: 100%">
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description" style="margin-bottom: 29px">
        <el-input class="ms-http-textarea" v-model="testCaseForm.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="visible = false"
        :isShow="true"
        title="编辑详情"
        @saveAsEdit="saveTestCase(true)"
        @confirm="saveTestCase">
      </ms-dialog-footer>

    </template>

  </el-dialog>

</template>

<script>
import {getCurrentProjectID, getCurrentUser} from "@/common/js/utils";
import {WORKSPACE_ID} from "@/common/js/constants";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {buildNodePath} from "@/business/components/api/definition/model/NodeTree";

export default {
  name: "TestCaseCreate",
  components: {MsDialogFooter},
  data() {
    return {
      testCaseForm: {},
      visible: false,
      currentModule: {},
      userOptions: [],
      rule: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
        maintainer: [{
          required: true,
          message: this.$t('api_test.automation.scenario.select_principal'),
          trigger: 'change'
        }],


      },
    }
  },
  props: {
    treeNodes: {
      type: Array
    },
  },
  watch: {
    treeNodes() {
      this.getModuleOptions();
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    moduleOptions() {
      return this.$store.state.testCaseModuleOptions;
    }
  },
  methods: {
    saveTestCase(saveAs) {
      this.$refs['testCaseForm'].validate((valid) => {
        if (valid) {
          let path = "/test/case/save";
          this.testCaseForm.projectId = this.projectId;
          this.testCaseForm.type = "";
          this.testCaseForm.priority = "P0";
          if (this.currentModule && this.currentModule !== 0 && this.currentModule.path && this.currentModule.path !== 0) {
            this.testCaseForm.nodePath = this.currentModule.path;
            this.testCaseForm.nodeId = this.currentModule.id;
          } else {
            this.testCaseForm.nodePath = "/默认模块"
            this.testCaseForm.nodeId = "default-module"
          }
          this.result = this.$post(path, this.testCaseForm, response => {
            this.testCaseForm.id = response.data.id
            this.$success(this.$t('commons.save_success'));
            this.visible = false;
            if (saveAs) {
              this.$emit('saveAsEdit', this.testCaseForm);
            } else {
              this.$emit('refresh');
              this.$emit('createCase', this.testCaseForm);
            }
          })
        } else {
          return false;
        }
      })
    },
    getModuleOptions() {
      // let moduleOptions = [];
      // this.treeNodes.forEach(node => {
      //   buildNodePath(node, {path: ''}, moduleOptions);
      // });
      if(this.currentModule!==undefined){
        this.moduleOptions.forEach(item => {
          if (this.currentModule.id === item.id) {
            this.currentModule.path = item.path;
          }
        });
      }
    },

    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.userOptions = response.data;
      });
    },
    open(currentModule) {
      this.testCaseForm = {maintainer: getCurrentUser().id};
      this.currentModule = currentModule;
      this.getMaintainerOptions();
      this.visible = true;
      this.getModuleOptions()
    }
  }
}
</script>

<style scoped>

</style>
