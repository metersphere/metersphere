<template>

  <el-drawer
    :title="testCase.name"
    :before-close="handleClose"
    :visible.sync="dialog"
    direction="ttb"
    size="100%"
    ref="drawer">

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
            <el-table
              :data="testCase.steptResults"
              class="tb-edit"
              size="mini"
              height="200px"
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
            <div style="margin-bottom: 5px;">
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

      <test-plan-test-case-status-button class="status-button"
        @statusChange="statusChange"
        :status="testCase.status"/>

        <el-row type="flex" justify="end">
          <el-col :span="5">
            <div>
              <el-button @click="cancel">{{$t('test_track.cancel')}}</el-button>
              <el-button type="primary" @click="saveCase">{{$t('test_track.save')}}</el-button>
            </div>
          </el-col>
        </el-row>

    </div>
  </el-drawer>

</template>

<script>
  import TestPlanTestCaseStatusButton from '../common/TestPlanTestCaseStatusButton';

  export default {
      name: "TestPlanTestCaseEdit",
    components: {TestPlanTestCaseStatusButton},
    data() {
        return {
          dialog: false,
          testCase: {TestPlanTestCaseStatusButton}
        };
      },
      methods: {
        handleClose(done) {
          this.dialog = false;
        },
        cancel() {
          this.dialog = false;
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

  .el-row {
    margin-bottom: 2%;
  }

  .cast_label {
    color: dimgray;
  }

  .status-button {
    padding-left: 4%;
  }

</style>
