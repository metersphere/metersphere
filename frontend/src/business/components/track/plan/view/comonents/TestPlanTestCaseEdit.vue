<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    :modal-append-to-body="false"
    size="100%"
    ref="drawer"
    v-loading="result.loading">

      <template v-slot:default="scope">
        <div class="container">

          <el-scrollbar>

            <el-header>

              <el-row type="flex" class="head-bar">

                <el-col :span="12">
                  <el-button plain size="mini"
                             icon="el-icon-back"
                             @click="cancel">{{$t('test_track.return')}}</el-button>
                </el-col>

                <el-col :span="12" class="head-right">
                <span class="head-right-tip" v-if="index + 1 == testCases.length">
                  {{$t('test_track.plan_view.pre_case')}} : {{testCases[index - 1] ? testCases[index - 1].name : ''}}
                </span>
                  <span class="head-right-tip" v-if="index + 1 != testCases.length">
                  {{$t('test_track.plan_view.next_case')}} : {{testCases[index + 1] ? testCases[index + 1].name : ''}}
                </span>

                  <el-button plain size="mini" icon="el-icon-arrow-up"
                             :disabled="index + 1 <= 1"
                             @click="handlePre()"/>
                  <span>  {{index + 1}}/{{testCases.length}} </span>
                  <el-button plain size="mini" icon="el-icon-arrow-down"
                             :disabled="index + 1 >= testCases.length"
                             @click="handleNext()"/>
                  <el-divider direction="vertical"></el-divider>

                  <el-button type="primary" size="mini" @click="saveCase(false)">{{$t('test_track.save')}}</el-button>
                </el-col>

              </el-row>

              <el-row style="margin-top: 0px;">
                <el-col>
                  <el-divider content-position="left">{{testCase.name}}</el-divider>
                </el-col>
              </el-row>

            </el-header>

            <div class="case_container">
              <el-row >
                <el-col :span="4" :offset="1">
                  <span class="cast_label">{{$t('test_track.case.priority')}}：</span>
                  <span class="cast_item">{{testCase.priority}}</span>
                </el-col>
                <el-col :span="5">
                  <span class="cast_label">{{$t('test_track.case.case_type')}}：</span>
                  <span class="cast_item" v-if="testCase.type == 'functional'">{{$t('commons.functional')}}</span>
                  <span class="cast_item" v-if="testCase.type == 'performance'">{{$t('commons.performance')}}</span>
                  <span class="cast_item" v-if="testCase.type == 'api'">{{$t('commons.api')}}</span>
                </el-col>
                <el-col :span="13">
                  <test-plan-test-case-status-button class="status-button"
                                                     @statusChange="statusChange"
                                                     :status="testCase.status"/>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="4" :offset="1">
                  <span class="cast_label">{{$t('test_track.case.method')}}：</span>
                  <span v-if="testCase.method == 'manual'">{{$t('test_track.case.manual')}}</span>
                  <span v-if="testCase.method == 'auto'">{{$t('test_track.case.auto')}}</span>
                </el-col>
                <el-col :span="5">
                  <span class="cast_label">{{$t('test_track.case.module')}}：</span>
                  <span class="cast_item">{{testCase.nodePath}}</span>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="20" :offset="1">
                  <div>
                    <span class="cast_label">{{$t('test_track.case.steps')}}：</span>
                  </div>
                  <el-table
                    :data="testCase.steptResults"
                    class="tb-edit"
                    size="mini"
                    :border="true"
                    :default-sort = "{prop: 'num', order: 'ascending'}"
                    highlight-current-row>
                    <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="5%"></el-table-column>
                    <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="29%">
                      <template v-slot:default="scope">
                        <span>{{scope.row.desc}}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="28%">
                      <template v-slot:default="scope">
                        <span>{{scope.row.result}}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('test_track.plan_view.actual_result')" min-width="29%">
                      <template v-slot:default="scope">
                        <el-input
                          size="mini"
                          type="textarea"
                          :rows="2"
                          v-model="scope.row.actualResult"
                          :placeholder="$t('commons.input_content')"
                          clearable></el-input>
                        <span>{{scope.row.actualResult}}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('test_track.plan_view.step_result')" min-width="9%">
                      <template v-slot:default="scope">
                        <el-select
                          v-model="scope.row.executeResult"
                          size="mini">
                          <el-option :label="$t('test_track.plan_view.pass')" value="Pass" style="color: #7ebf50;"></el-option>
                          <el-option :label="$t('test_track.plan_view.failure')" value="Failure" style="color: #e57471;"></el-option>
                          <el-option :label="$t('test_track.plan_view.blocking')" value="Blocking" style="color: #dda451;"></el-option>
                          <el-option :label="$t('test_track.plan_view.skip')" value="Skip" style="color: #919399;"></el-option>
                        </el-select>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="15" :offset="1">
                  <div>
                    <span class="cast_label">{{$t('commons.remark')}}：</span>
                    <span v-if="testCase.remark == null || testCase.remark == ''" style="color: darkgrey">{{$t('commons.not_filled')}}</span>
                  </div>
                  <div>
                    <el-input :rows="3"
                              type="textarea"
                              v-if="testCase.remark"
                              disabled
                              v-model="testCase.remark"></el-input>
                  </div>
                </el-col>
              </el-row>
            </div>

          </el-scrollbar>

        </div>

      </template>

  </el-drawer>


</template>

<script>
  import TestPlanTestCaseStatusButton from '../../common/TestPlanTestCaseStatusButton';

  export default {
    name: "TestPlanTestCaseEdit",
    components: {TestPlanTestCaseStatusButton},
    data() {
      return {
        result: {},
        showDialog: false,
        testCase: {},
        index: 0,
        testCases: []
      };
    },
    props: {
      total: {
        type: Number
      },
      searchParam: {
        type: Object
      }
    },
    methods: {
      handleClose(done) {
        this.showDialog = false;
      },
      cancel() {
        this.showDialog = false;
        this.$emit('refreshTable');
      },
      statusChange(status) {
        this.testCase.status = status;
      },
      saveCase(isContinuous) {
        let param = {};
        param.id = this.testCase.id;
        param.status = this.testCase.status;
        param.results = [];
        this.testCase.steptResults.forEach(item => {
          let result = {};
          result.actualResult = item.actualResult;
          result.executeResult = item.executeResult;
          param.results.push(result);
        });
        param.results = JSON.stringify(param.results);
        this.$post('/test/plan/case/edit', param, () => {
          if (isContinuous) {
            this.updateTestCases(param);
            return;
          }
          this.$refs.drawer.closeDrawer();
          this.$success(this.$t('commons.save_success'));
          this.$emit('refresh');
        });
      },
      handleNext() {
        this.saveCase(true);
        this.index++;
        this.getTestCase(this.index);
      },
      handlePre() {
        this.saveCase(true);
        this.index--;
        this.getTestCase(this.index);
      },
      getTestCase(index) {
        let testCase = this.testCases[index];
        let item = {};
        Object.assign(item, testCase);
        item.results = JSON.parse(item.results);
        item.steps = JSON.parse(item.steps);
        item.steptResults = [];
        for (let i = 0; i < item.steps.length; i++){
          if(item.results[i]){
            item.steps[i].actualResult = item.results[i].actualResult;
            item.steps[i].executeResult = item.results[i].executeResult;
          }
          item.steptResults.push(item.steps[i]);
        }
        this.testCase = item;
      },
      openTestCaseEdit(testCase) {
        this.showDialog = true;
        this.initData(testCase);
      },
      updateTestCases(testCase) {
        this.testCases.forEach(item => {
           if (testCase.id === item.id) {
             Object.assign(item, testCase);
           }
        });
      },
      initData(testCase) {
        this.result = this.$post('/test/plan/case/list/all', this.searchParam, response => {
          this.testCases = response.data;
          for (let i = 0; i < this.testCases.length; i++) {
            if (this.testCases[i].id === testCase.id) {
              this.index = i;
              this.getTestCase(i);
            }
          }
        });
      }
    }
  }
</script>

<style scoped>


  .tb-edit .el-textarea {
    display: none;
  }
  .tb-edit .current-row .el-textarea {
    display: block;
  }
  .tb-edit .current-row .el-textarea+span {
    display: none;
  }

  .cast_label {
    color: dimgray;
  }

  .status-button {
    padding-left: 4%;
  }

  .head-right {
    text-align: right;
  }

  .el-col {
    line-height: 50px;
  }

  .status-button {
    float: right;
  }

  .head-right-tip {
    color: darkgrey;
  }

  .el-scrollbar {
    height: 100%;
  }

  .container {
    height: 100vh;
  }

  .case_container > .el-row{
    margin-top: 1%;
  }

</style>
