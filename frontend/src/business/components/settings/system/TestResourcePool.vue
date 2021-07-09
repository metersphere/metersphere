<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :create-permission="['SYSTEM_TEST_POOL:READ+CREATE']" :condition.sync="condition"
                         @search="search" @create="create"
                         :create-tip="$t('test_resource_pool.create_resource_pool')"
                         :title="$t('commons.test_resource_pool')"/>
      </template>
      <el-table border class="adjust-table" :data="items" style="width: 100%"
                :height="screenHeight"
      >
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column prop="type" :label="$t('test_resource_pool.type')">
          <template v-slot:default="scope">
            <span v-if="scope.row.type === 'NODE'">Node</span>
            <span v-if="scope.row.type === 'K8S'" v-xpack>Kubernetes</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('test_resource_pool.enable_disable')">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.status"
                       inactive-color="#DCDFE6"
                       active-value="VALID"
                       inactive-value="INVALID"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :edit-permission="['SYSTEM_TEST_POOL:READ+EDIT']"
                                 :delete-permission="['SYSTEM_TEST_POOL:READ+DELETE']"
                                 @editClick="edit(scope.row)" @deleteClick="del(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog
      :close-on-click-modal="false"
      :title="form.id ? $t('test_resource_pool.update_resource_pool') : $t('test_resource_pool.create_resource_pool')"
      :visible.sync="dialogVisible" width="80%"
      top="5%"
      @closed="closeFunc"
      :destroy-on-close="true"
      v-loading="result.loading"
    >
      <div style="height: 60vh;overflow: auto;">
        <el-form :model="form" label-position="right" label-width="140px" size="small" :rules="rule"
                 ref="testResourcePoolForm">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="form.name" autocomplete="off"/>
          </el-form-item>
          <el-form-item :label="$t('commons.description')" prop="description">
            <el-input v-model="form.description" autocomplete="off"/>
          </el-form-item>
          <el-form-item :label="$t('commons.image')" prop="image">
            <el-input v-model="form.image"/>
          </el-form-item>
          <el-form-item :label="$t('test_resource_pool.usage')" prop="image">
            <el-checkbox :label="$t('commons.api')" v-model="form.api"></el-checkbox>
            <el-checkbox :label="$t('commons.performance')" v-model="form.performance"></el-checkbox>
          </el-form-item>
          <el-form-item label="JMeter HEAP" prop="HEAP">
            <el-input v-model="form.heap" placeholder="-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m"/>
          </el-form-item>
          <el-form-item label="JMeter GC_ALGO" prop="GC_ALGO">
            <el-input v-model="form.gcAlgo"
                      placeholder="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1ReservePercent=20"/>
          </el-form-item>
          <el-form-item :label="$t('test_resource_pool.type')" prop="type">
            <el-select v-model="form.type" :placeholder="$t('test_resource_pool.select_pool_type')"
                       @change="changeResourceType(form.type)">
              <el-option key="NODE" value="NODE" label="Node">Node</el-option>
              <el-option key="K8S" value="K8S" label="Kubernetes" v-xpack>Kubernetes</el-option>
            </el-select>
          </el-form-item>
          <div class="node-line" v-if="form.type === 'K8S'" v-xpack>
            <div v-for="(item,index) in infoList " :key="index">
              <el-row>
                <el-col>
                  <el-form-item label="Master URL"
                                :rules="requiredRules">
                    <el-input v-model="item.masterUrl" autocomplete="new-password"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col>
                  <el-form-item label="Token"
                                :rules="requiredRules">
                    <el-input v-model="item.token" type="password" show-password autocomplete="new-password"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col>
                  <el-form-item label="Namespace"
                                :rules="requiredRules">
                    <el-input v-model="item.namespace" type="text"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col>
                  <el-form-item label="API Image">
                    <el-input v-model="item.apiImage" type="text"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item :label="$t('test_resource_pool.max_threads')"
                                :rules="requiredRules">
                    <el-input-number v-model="item.maxConcurrency" :min="1" :max="1000000000"/>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="$t('test_resource_pool.pod_thread_limit')"
                                :rules="requiredRules">
                    <el-input-number v-model="item.podThreadLimit" :min="1" :max="1000000"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col>
                  <el-form-item label="nodeSelector">
                    <el-input v-model="item.nodeSelector" placeholder='{"disktype": "ssd",...}'/>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </div>

          <div class="node-line" v-if="form.type === 'NODE'">
            <el-row>
              <el-col :span="22" :offset="2">
                <el-row style="margin-bottom: 10px;">
                  <el-col :span="8">
                    <el-button icon="el-icon-circle-plus-outline" plain size="mini"
                               @click="addResourceInfo()">
                      {{ $t('commons.add') }}
                    </el-button>
                  </el-col>
                </el-row>
                <el-table :data="infoList" class="tb-edit" align="center" border highlight-current-row>
                  <el-table-column
                    align="center"
                    prop="ip"
                    label="IP">
                    <template v-slot:default="{row}">
                      <el-input size="small" v-model="row.ip" autocomplete="off"/>
                    </template>
                  </el-table-column>
                  <el-table-column
                    align="center"
                    prop="port"
                    label="Port">
                    <template v-slot:default="{row}">
                      <el-input-number size="small" v-model="row.port" :min="1" :max="65535"></el-input-number>
                    </template>
                  </el-table-column>
                  <el-table-column
                    align="center"
                    prop="monitorPort"
                    label="Monitor">
                    <template v-slot:default="{row}">
                      <el-input-number size="small" v-model="row.monitorPort" :min="1" :max="65535"></el-input-number>
                    </template>
                  </el-table-column>
                  <el-table-column
                    align="center"
                    prop="maxConcurrency"
                    :label="$t('test_resource_pool.max_threads')">
                    <template v-slot:default="{row}">
                      <el-input-number size="small" v-model="row.maxConcurrency" :min="1"
                                       :max="1000000000"></el-input-number>
                    </template>
                  </el-table-column>
                  <el-table-column align="center" :label="$t('commons.operating')">
                    <template v-slot:default="{row, $index}">
                      <el-button @click="removeResourceInfo($index)" type="danger" icon="el-icon-delete" size="mini"
                                 circle/>
                    </template>
                  </el-table-column>

                </el-table>
              </el-col>
            </el-row>
          </div>

        </el-form>
      </div>
      <template v-slot:footer>
        <ms-dialog-footer
          v-if="form.id"
          @cancel="dialogVisible = false"
          @confirm="updateTestResourcePool()"/>
        <ms-dialog-footer
          v-else
          @cancel="dialogVisible = false"
          @confirm="createTestResourcePool()"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";

export default {
  name: "MsTestResourcePool",
  components: {MsCreateBox, MsTablePagination, MsTableHeader, MsTableOperator, MsDialogFooter},
  data() {
    return {
      result: {},
      dialogVisible: false,
      infoList: [],
      queryPath: "testresourcepool/list",
      condition: {},
      items: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      form: {performance: true, api: true},
      screenHeight: 'calc(100vh - 195px)',
      requiredRules: [{required: true, message: this.$t('test_resource_pool.fill_the_data'), trigger: 'blur'}],
      rule: {
        name: [
          {required: true, message: this.$t('test_resource_pool.input_pool_name'), trigger: 'blur'},
          {min: 2, max: 20, message: this.$t('commons.input_limit', [2, 20]), trigger: 'blur'},
          {
            required: true,
            pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
            message: this.$t('test_resource_pool.pool_name_valid'),
            trigger: 'blur'
          }
        ],
        description: [
          {max: 60, message: this.$t('commons.input_limit', [0, 60]), trigger: 'blur'}
        ],
        type: [
          {required: true, message: this.$t('test_resource_pool.select_pool_type'), trigger: 'blur'}
        ]
      },
      updatePool: {
        testName: '',
        haveTestUsePool: false
      }
    };
  },
  activated() {
    this.initTableData();
  },
  methods: {
    initTableData() {

      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.items = data.listObject;
        this.total = data.itemCount;
      });
    },
    changeResourceType(type) {
      this.infoList = [];
      let info = {};
      if (type === 'NODE') {
        info.ip = '';
        info.port = '8082';
        info.monitorPort = '9100';
      }
      if (type === 'K8S') {
        info.masterUrl = '';
        info.token = '';
        info.namespace = '';
        info.podThreadLimit = 5000;
      }
      info.maxConcurrency = 100;
      this.infoList.push(info);
    },

    addResourceInfo() {
      this.infoList.push({});
    },
    removeResourceInfo(index) {
      if (this.infoList.length > 1) {
        this.infoList.splice(index, 1);
      } else {
        this.$warning(this.$t('test_resource_pool.cannot_remove_all_node'));
      }
    },
    validateResourceInfo() {
      if (this.infoList.length <= 0) {
        return {validate: false, msg: this.$t('test_resource_pool.cannot_empty')};
      }
      let resourcePoolType = this.form.type;
      let resultValidate = {validate: true, msg: this.$t('test_resource_pool.fill_the_data')};
      this.infoList.forEach(info => {
        for (let key in info) {
          // 排除非必填项
          if (key === 'nodeSelector') {
            continue;
          }
          if (info[key] != '0' && !info[key]) {
            resultValidate.validate = false;
            return false;
          }
        }

        if (!info.maxConcurrency) {
          resultValidate.validate = false;
          return false;
        }
        if (resourcePoolType === 'K8S' && info.nodeSelector) {
          let validate = this.isJsonString(info.nodeSelector);
          if (!validate) {
            resultValidate.validate = false;
            resultValidate.msg = this.$t('test_resource_pool.node_selector_invalid');
          }
        }
      });

      return resultValidate;
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    search() {
      this.initTableData();
    },
    create() {
      this.dialogVisible = true;
      listenGoBack(this.closeFunc);
    },
    edit(row) {
      this.dialogVisible = true;
      this.form = JSON.parse(JSON.stringify(row));
      this.convertResources();
      listenGoBack(this.closeFunc);
    },
    convertResources() {
      let resources = [];
      if (this.form.resources) {
        this.form.resources.forEach(function (resource) {
          let configuration = JSON.parse(resource.configuration);
          configuration.id = resource.id;
          configuration.monitorPort = configuration.monitorPort || '9100';
          resources.push(configuration);
        });
      }
      this.infoList = resources;
    },
    del(row) {
      this.$confirm(this.$t('test_resource_pool.delete_prompt'), this.$t('commons.prompt'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get(`/testresourcepool/delete/${row.id}`, () => {
          this.initTableData();
          this.$success(this.$t('commons.delete_success'));
        });
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'));
      });
    },
    createTestResourcePool() {
      this.$refs.testResourcePoolForm.validate(valid => {
        if (valid) {
          let vri = this.validateResourceInfo();
          if (vri.validate) {
            this.convertSubmitResources();
            this.result = this.$post("/testresourcepool/add", this.form, () => {
              this.$message({
                  type: 'success',
                  message: this.$t('commons.save_success')
                },
                this.dialogVisible = false,
                this.initTableData());
            });
          } else {
            this.$warning(vri.msg);
            return false;
          }

        } else {
          return false;
        }
      });
    },
    convertSubmitResources() {
      let resources = [];
      let poolId = this.form.id;
      this.infoList.forEach(function (info) {
        let configuration = JSON.stringify(info);
        let resource = {"configuration": configuration, id: info.id};
        if (poolId) {
          resource.testResourcePoolId = poolId;
        }
        resources.push(resource);
      });
      this.form.resources = resources;
    },
    updateTestResourcePool() {
      this.$refs.testResourcePoolForm.validate(valid => {
        if (valid) {
          let vri = this.validateResourceInfo();
          if (vri.validate) {
            this.convertSubmitResources();
            this.result = this.$post("/testresourcepool/update", this.form, () => {
              this.$message({
                  type: 'success',
                  message: this.$t('commons.modify_success')
                },
                this.dialogVisible = false,
                this.initTableData(),
                self.loading = false);
            });
          } else {
            this.$warning(vri.msg);
            return false;
          }
        } else {
          return false;
        }
      });
    },
    closeFunc() {
      this.form = {performance: true, api: true};
      this.dialogVisible = false;
      removeGoBackListener(this.closeFunc);
    },
    changeSwitch(row) {
      this.result.loading = true;
      this.$info(this.$t('test_resource_pool.check_in'), 1000);
      if (row.status === 'VALID') {
        this.updatePoolStatus(row);
        return false;
      }
      // 禁用时检查是否有正在使用该资源池的性能测试
      if (row.status === 'INVALID') {
        this.checkHaveTestUsePool(row).then(() => {
          if (this.updatePool && this.updatePool.haveTestUsePool) {
            this.$confirm(this.$t('test_resource_pool.update_prompt', [this.updatePool.testName]), this.$t('commons.prompt'), {
              confirmButtonText: this.$t('commons.confirm'),
              cancelButtonText: this.$t('commons.cancel'),
              type: 'warning'
            }).then(() => {
              this.updatePoolStatus(row);
            }).catch(() => {
              row.status = 'VALID';
              this.result.loading = false;
              this.$info(this.$t('commons.cancel'));
            });
          } else {
            this.updatePoolStatus(row);
          }
        });
      }
    },
    checkHaveTestUsePool(row) {
      return new Promise((resolve) => {
        this.$get('/testresourcepool/check/use/' + row.id, result => {
          this.updatePool = result.data;
          resolve();
        });
      });
    },
    updatePoolStatus(row) {
      this.$get('/testresourcepool/update/' + row.id + '/' + row.status)
        .then(() => {
          this.$success(this.$t('test_resource_pool.status_change_success'));
          this.result.loading = false;
        }).catch(() => {
        this.$error(this.$t('test_resource_pool.status_change_failed'));
        row.status = 'INVALID';
        this.result.loading = false;
      });
    },
    isJsonString(str) {
      try {
        if (typeof JSON.parse(str) == "object") {
          return true;
        }
      } catch (e) {
        console.log('json invalid');
      }
      return false;
    }
  }
};
</script>

<style scoped>

.box {
  padding-left: 5px;
}

</style>
