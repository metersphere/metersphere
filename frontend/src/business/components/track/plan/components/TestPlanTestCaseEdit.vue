<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    size="100%"
    ref="drawer">

    <template v-slot:default="scope">

      <el-header>

        <el-row type="flex" style="margin-top: 1%" justify="end">

          <el-col :span="12">
            <el-button plain size="mini"
                       icon="el-icon-back"
                       @click="cancel">返回</el-button>
          </el-col>

          <el-col :span="12" class="head-right">
            <span v-if="index + 1 == tableData.length">
              上一条用例 : {{tableData[index - 1].name}}
            </span>
            <span v-if="index + 1 < tableData.length">
              下一条用例 : {{tableData[index + 1].name}}
            </span>

            <el-button plain size="mini" icon="el-icon-arrow-up"
                       :disabled="index + 1 <= 1"
                       @click="handlePre()"/>
            <span>  {{index + 1}}/{{tableData.length}} </span>
            <el-button plain size="mini" icon="el-icon-arrow-down"
                       :disabled="index + 1 >= tableData.length"
                       @click="handleNext()"/>
            <el-divider direction="vertical"></el-divider>

            <el-button type="primary" size="mini" @click="saveCase">{{$t('test_track.save')}}</el-button>
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
            <span class="cast_label">{{$t('test_track.priority')}}：</span>
            <span class="cast_item">{{testCase.priority}}</span>
          </el-col>
          <el-col :span="5">
            <span class="cast_label">{{$t('test_track.case_type')}}：</span>
            <span class="cast_item" v-if="testCase.type == 'functional'">{{$t('test_track.functional_test')}}</span>
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
            <span class="cast_label">{{$t('test_track.method')}}：</span>
            <span v-if="testCase.method == 'manual'">{{$t('test_track.manual')}}</span>
            <span v-if="testCase.method == 'auto'">{{$t('test_track.auto')}}</span>
          </el-col>
          <el-col :span="5">
            <span class="cast_label">{{$t('test_track.module')}}：</span>
            <span class="cast_item">{{testCase.nodePath}}</span>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="20" :offset="1">
            <div>
              <span class="cast_label">测试步骤：</span>
            </div>
            <el-table
              :data="testCase.steptResults"
              class="tb-edit"
              size="mini"
              height="250px"
              border="true"
              :default-sort = "{prop: 'num', order: 'ascending'}"
              highlight-current-row>
              <el-table-column :label="$t('test_track.number')" prop="num" min-width="5%"></el-table-column>
              <el-table-column :label="$t('test_track.step_desc')" prop="desc" min-width="29%">
                <template v-slot:default="scope">
                  <span>{{scope.row.desc}}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('test_track.expected_results')" prop="result" min-width="28%">
                <template v-slot:default="scope">
                  <span>{{scope.row.result}}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('test_track.actual_result')" min-width="29%">
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
              <el-table-column :label="$t('test_track.step_result')" min-width="9%">
                <template v-slot:default="scope">
                  <el-select
                    v-model="scope.row.executeResult"
                    size="mini">
                    <el-option :label="$t('test_track.pass')" value="Pass"></el-option>
                    <el-option :label="$t('test_track.failure')" value="Failure"></el-option>
                    <el-option :label="$t('test_track.blocking')" value="Blocking"></el-option>
                    <el-option :label="$t('test_track.skip')" value="Skip"></el-option>
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
    </template>
  </el-drawer>

</template>

<script>
  import TestPlanTestCaseStatusButton from '../common/TestPlanTestCaseStatusButton';

  export default {
    name: "TestPlanTestCaseEdit",
    components: {TestPlanTestCaseStatusButton},
    data() {
      return {
        showDialog: false,
        testCase: {},
        index: 0
      };
    },
    props: {
      tableData: {
        type: Array
      },
      total: {
        type: Number
      }
    },
    methods: {
      handleClose(done) {
        this.showDialog = false;
      },
      cancel() {
        this.showDialog = false;
      },
      statusChange(status) {
        this.testCase.status = status;
      },
      saveCase() {
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
          this.$refs.drawer.closeDrawer();
          this.$message.success("保存成功！");
          this.$emit('refresh');
        });
      },
      handleNext() {
        this.index++;
        this.getTestCase(this.index);
      },
      handlePre() {
        this.index--;
        this.getTestCase(this.index);
      },
      getTestCase(index) {
        let testCase = this.tableData[index];
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
    margin-right: 1px;
  }

</style>
