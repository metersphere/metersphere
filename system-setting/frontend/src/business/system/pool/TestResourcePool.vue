<template>
  <div>
    <el-card class="table-card" v-loading="loading">
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
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
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
      v-loading="dialogLoading"
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
          <el-form-item :label="$t('test_resource_pool.backend_listener')" prop="backendListener" v-xpack>
            <el-switch v-model="form.backendListener"/>
          </el-form-item>
          <el-form-item :label="$t('test_resource_pool.usage')" prop="usage">
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
          <el-form-item prop="type" :label="$t('test_resource_pool.type')">
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
                  <el-form-item label="Namespace" :rules="requiredRules">
                    <template v-slot:label>
                      Namespace
                      <el-popover
                        placement="bottom"
                        width="450"
                        trigger="hover">
                        <div>
                          <strong>{{ $t('test_resource_pool.k8s_sa_tips') }}</strong><br>
                          <el-link type="primary" @click="downloadYaml(item, 'role')">role.yaml</el-link>
                        </div>

                        <div style="padding-top: 20px">
                          <strong>{{ $t('test_resource_pool.k8s_deploy_type_tips') }}</strong><br>
                          <el-link type="primary" @click="downloadYaml(item, 'DaemonSet')">daemonset.yaml</el-link>
                          &nbsp;
                          <el-link type="primary" @click="downloadYaml(item, 'Deployment')">deployment.yaml</el-link>
                        </div>
                        <i class="el-icon-info" slot="reference"></i>
                      </el-popover>
                    </template>
                    <el-input v-model="item.namespace" type="text"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col>
                  <el-form-item label="Deploy Name" :rules="requiredRules">
                    <el-input v-model="item.deployName"/>
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
                <el-col :span="6">
                  <el-form-item :label="$t('test_resource_pool.max_threads')"
                                :rules="requiredRules">
                    <el-input-number v-model="item.maxConcurrency" :min="1" :max="1000000000"/>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item :label="$t('test_resource_pool.pod_thread_limit')"
                                :rules="requiredRules">
                    <el-input-number v-model="item.podThreadLimit" :min="1" :max="1000000"/>
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item :label="$t('test_resource_pool.sync_jar')">
                    <el-checkbox v-model="item.enable"/>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item>
                    <template v-slot:label>
                      <el-link type="primary" @click="jobTemplate">{{ $t('system.test_resource_pool.edit_job_template') }}</el-link>
                      <el-tooltip :content="$t('system.test_resource_pool.edit_job_template_tip')"
                                  effect="light"
                                  trigger="hover">
                        <i class="el-icon-info"></i>
                      </el-tooltip>
                    </template>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            <job-template ref="jobTemplate" @saveJobTemplate="saveJobTemplate"/>
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
                    <el-button icon="el-icon-circle-plus-outline" plain size="mini"
                               @click="batchAddResource">
                      {{ $t('commons.batch_add') }}
                    </el-button>
                  </el-col>
                </el-row>
                <el-table :data="infoList" class="tb-edit" align="center" border highlight-current-row>
                  <el-table-column type="index" width="50"/>
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
                  <el-table-column
                    align="center"
                    prop="enable"
                    :label="$t('test_resource_pool.sync_jar')">
                    <template v-slot:default="{row}">
                      <el-checkbox size="small" v-model="row.enable"/>
                    </template>
                  </el-table-column>
                  <el-table-column align="center" :label="$t('commons.operating')">
                    <template v-slot:default="{$index}">
                      <el-button @click="removeResourceInfo($index)" type="danger" icon="el-icon-delete" size="mini"
                                 circle/>
                    </template>
                  </el-table-column>

                </el-table>
              </el-col>
            </el-row>
          </div>
          <batch-add-resource ref="batchAddResource" @batchSave="batchSave"/>
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
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import BatchAddResource from "../components/BatchAddResource";
import {getYaml} from "./test-resource-pool";
import {
  checkResourcePoolUse,
  createResourcePool,
  delResourcePoolById,
  getResourcePoolPages,
  modifyResourcePool,
  modifyResourcePoolStatus
} from "../../../api/resource-pool";
import {getSystemVersion} from "../../../api/system";
import {operationConfirm} from "metersphere-frontend/src/utils";
import JobTemplate from "@/business/system/components/JobTemplate";

export default {
  name: "MsTestResourcePool",
  components: {JobTemplate, BatchAddResource, MsTablePagination, MsTableHeader, MsTableOperator, MsDialogFooter},
  data() {
    return {
      loading: false,
      dialogLoading: false,
      result: {},
      dialogVisible: false,
      infoList: [],
      queryPath: "testresourcepool/list",
      condition: {},
      items: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      form: {performance: true, api: true, backendListener: true},
      screenHeight: 'calc(100vh - 155px)',
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
      },
      apiImage: '',
      apiImageTag: '',
    };
  },
  activated() {
    this.initTableData();
    this.getApiImageTag();
  },
  methods: {
    initTableData() {
      this.loading = getResourcePoolPages(this.currentPage, this.pageSize, this.condition)
        .then(res => {
          let {listObject, itemCount} = res.data;
          this.items = listObject;
          this.total = itemCount;
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
        info.deployType = 'DaemonSet';
        info.deployName = 'ms-node-controller';
      }
      info.maxConcurrency = 100;
      this.infoList.push(info);
    },

    addResourceInfo() {
      this.infoList.push({
        port: '8082',
        monitorPort: '9100',
        maxConcurrency: 100
      });
    },
    removeResourceInfo(index) {
      if (this.infoList.length > 1) {
        this.infoList.splice(index, 1);
      } else {
        this.$warning(this.$t('test_resource_pool.cannot_remove_all_node'));
      }
    },
    batchAddResource() {
      this.$refs.batchAddResource.open();
    },
    jobTemplate() {
      this.$refs.jobTemplate.open(this.infoList[0].jobTemplate);
    },
    batchSave(resources) {
      let targets = this._handleBatchVars(resources);
      targets.forEach(row => {
        this.infoList.push(row);
      });
    },
    _handleBatchVars(data) {
      let params = data.split("\n");
      let keyValues = [];
      params.forEach(item => {
        let line = item.split(/，|,/);
        if (line.length < 3) {
          return;
        }
        keyValues.push({
          ip: line[0],
          port: line[1],
          monitorPort: line[2],
          maxConcurrency: line[3],
        });
      });
      return keyValues;
    },
    saveJobTemplate(template) {
      this.infoList.forEach(item => {
        item.jobTemplate = template;
      });
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
          if (key === 'nodeSelector' || key === 'apiImage') {
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
          configuration.deployType = configuration.deployType || 'DaemonSet';
          configuration.deployName = configuration.deployName || 'ms-node-controller';
          resources.push(configuration);
        });
      }
      this.infoList = resources;
    },
    del(row) {
      operationConfirm(this, this.$t('test_resource_pool.delete_prompt'), () => {
        this.loading = delResourcePoolById(row.id).then(() => {
          this.initTableData();
          this.$success(this.$t('commons.delete_success'));
        })
      }, () => {
        this.$info(this.$t('commons.delete_cancel'));
      })
    },
    createTestResourcePool() {
      this.$refs.testResourcePoolForm.validate(valid => {
        if (!valid) {
          return false;
        }
        let vri = this.validateResourceInfo();
        if (!vri.validate) {
          this.$warning(vri.msg);
          return false;
        }
        this.convertSubmitResources();
        this.dialogLoading = createResourcePool(this.form).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.dialogVisible = false;
          this.initTableData();
        });
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
        if (!valid) {
          return false;
        }
        let vri = this.validateResourceInfo();
        if (!vri.validate) {
          return false;
        }
        this.convertSubmitResources();
        this.dialogLoading = modifyResourcePool(this.form).then(() => {
          this.$success(this.$t('commons.modify_success'));
          this.dialogVisible = false;
          this.initTableData();
        });
      });
    },
    closeFunc() {
      this.form = {performance: true, api: true, backendListener: true};
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
            let testIndex = this.updatePool.testName.indexOf(";")
            let subPrompt = this.updatePool.testName.substring(0, testIndex);
            this.$confirm(this.$t('test_resource_pool.update_prompt', [subPrompt]), this.$t('commons.prompt'), {
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
      return checkResourcePoolUse(row.id).then(res => {
        this.updatePool = res.data;
      })
    },
    updatePoolStatus(row) {
      modifyResourcePoolStatus(row.id, row.status).then(() => {
        this.$success(this.$t('test_resource_pool.status_change_success'));
      }).catch(() => {
        this.$error(this.$t('test_resource_pool.status_change_failed'));
        row.status = 'INVALID';
      })
    },
    downloadYaml(item, deployType) {
      if (!item.namespace) {
        this.$error(this.$t('test_resource_pool.fill_the_data'));
        return;
      }
      if (!item.deployName) {
        this.$error(this.$t('test_resource_pool.fill_the_data'));
        return;
      }
      let apiImage = 'registry.cn-qingdao.aliyuncs.com/metersphere/node-controller:' + this.apiImageTag;
      if (item.apiImage) {
        apiImage = item.apiImage;
      }
      let yaml = getYaml(deployType, item.deployName, item.namespace, apiImage);
      let blob = new Blob([yaml], {type: 'application/yaml'});
      let url = URL.createObjectURL(blob);
      let downloadAnchorNode = document.createElement('a')
      downloadAnchorNode.setAttribute("href", url);
      downloadAnchorNode.setAttribute("download", deployType.toLowerCase() + ".yaml")
      downloadAnchorNode.click();
      downloadAnchorNode.remove();
    },
    getApiImageTag() {
      getSystemVersion().then(res => {
        if (!res.data) {
          this.apiImageTag = 'dev';
          return;
        }
        let i = res.data.lastIndexOf('-');
        this.apiImageTag = res.data.substring(0, i);
      }).catch(err => {
      })
    },
    isJsonString(str) {
      try {
        if (typeof JSON.parse(str) == "object") {
          return true;
        }
      } catch (e) {
        return false;
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
