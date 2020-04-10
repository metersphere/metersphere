<template>
  <div v-loading="result.loading">

    <el-card>
      <template v-slot:header>
        <div>
          <el-row type="flex" justify="space-between" align="middle">
          <span class="title">{{$t('commons.test_resource_pool')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
            <span class="search">
            <el-input type="text" size="small" :placeholder="$t('test_resource_pool.search_by_name')"
                      prefix-icon="el-icon-search"
                      maxlength="60" v-model="condition" @change="search" clearable/>
          </span>
          </el-row>
        </div>
      </template>
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column prop="type" :label="$t('test_resource_pool.type')">
          <template v-slot:default="scope">
            <span v-if="scope.row.type === 'NODE'">Single Docker</span>
            <span v-if="scope.row.type === 'K8S'">Kubernetes</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('test_resource_pool.enable_disable')">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.status"
                       active-color="#13ce66"
                       inactive-color="#ff4949"
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
        <el-table-column>
          <template v-slot:default="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog v-loading="result.loading"
               :title="$t('test_resource_pool.create_resource_pool')"
               :visible.sync="createVisible" width="70%"
               @closed="closeFunc"
               :destroy-on-close="true">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="createTestResourcePoolForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('test_resource_pool.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('test_resource_pool.select_pool_type')"
                     @change="changeResourceType()">
            <el-option key="K8S" value="K8S" label="Kubernetes">Kubernetes</el-option>
            <el-option key="NODE" value="NODE" label="Node">Single Docker</el-option>
          </el-select>
        </el-form-item>
        <div v-for="(item,index) in infoList " :key="index">
          <div class="node-line" v-if="form.type === 'K8S'">
            <div class="k8s-master">
              <el-col :span="11">

              </el-col>
              <el-form-item prop="masterUrl" label="Master URL">
                <el-input v-model="item.masterUrl" autocomplete="off"/>
              </el-form-item>
            </div>
            <div class="k8s-token">
              <el-form-item prop="token" label="Token">
                <el-input v-model="item.token" show-password autocomplete="off"/>
              </el-form-item>
            </div>
            <div style="width: 30%;float: left">
              <el-form-item prop="maxConcurrency" :label="$t('test_resource_pool.max_threads')">
                <el-input-number v-model="item.maxConcurrency" :min="1" :max="9999"></el-input-number>
              </el-form-item>
            </div>
          </div>
          <div class="node-line" v-if="form.type === 'NODE'">
            <div style="width: 30%;float: left">
              <el-form-item prop="ip" label="IP">
                <el-input v-model="item.ip" autocomplete="off"/>
              </el-form-item>
            </div>
            <div style="width: 30%;float: left">
              <el-form-item prop="port" label="Port">
                <el-input-number v-model="item.port" :min="1" :max="9999"></el-input-number>
              </el-form-item>
            </div>
            <div style="width: 30%;float: left">
              <el-form-item prop="maxConcurrency" :label="$t('test_resource_pool.max_threads')">
                <el-input-number v-model="item.maxConcurrency" :min="1" :max="9999"></el-input-number>
              </el-form-item>
            </div>
            <div class="op">
                <span class="box">
                    <el-button @click="addResourceInfo()" type="primary" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'plus']"/>
                    </el-button>
                </span>
              <span class="box">
                    <el-button @click="removeResourceInfo(index)" type="primary" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'minus']"/>
                    </el-button>
                </span>
            </div>
          </div>
        </div>

      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" onkeydown="return false;"
                     @click="createTestResourcePool('createTestResourcePoolForm')"
                     size="medium">{{$t('commons.create')}}</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-loading="result.loading"
      :title="$t('test_resource_pool.update_resource_pool')" :visible.sync="updateVisible" width="70%"
      :destroy-on-close="true"
      @close="closeFunc">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="updateTestResourcePoolForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('test_resource_pool.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('test_resource_pool.select_pool_type')"
                     @change="changeResourceType()">
            <el-option key="K8S" value="K8S" label="Kubernetes">Kubernetes</el-option>
            <el-option key="NODE" value="NODE" label="Node">Single Docker</el-option>
          </el-select>
        </el-form-item>
        <div v-for="(item,index) in infoList " :key="index">
          <div class="node-line" v-if="form.type === 'K8S'">
            <div class="k8s-master">
              <el-form-item prop="masterUrl" label="Master URL">
                <el-input v-model="item.masterUrl" autocomplete="off"/>
              </el-form-item>
            </div>
            <div class="k8s-token">
              <el-form-item prop="password" label="Token" style="padding-left: 20px">
                <el-input v-model="item.token" show-password autocomplete="off"/>
              </el-form-item>
            </div>
            <div style="width: 30%;float: left">
              <el-form-item prop="maxConcurrency" :label="$t('test_resource_pool.max_threads')"
                            style="padding-left: 20px">
                <el-input-number v-model="item.maxConcurrency" :min="1" :max="9999"></el-input-number>
              </el-form-item>
            </div>
          </div>
          <div class="node-line" v-if="form.type === 'NODE'">
            <div style="width: 30%;float: left">
              <el-form-item prop="ip" label="IP">
                <el-input v-model="item.ip" autocomplete="off"/>
              </el-form-item>
            </div>
            <div style="width: 30%;float: left">
              <el-form-item prop="port" label="Port" style="padding-left: 20px">
                <el-input-number v-model="item.port" :min="1" :max="9999"></el-input-number>
              </el-form-item>
            </div>
            <div style="width: 30%;float: left">
              <el-form-item prop="maxConcurrency" :label="$t('test_resource_pool.max_threads')"
                            style="padding-left: 20px">
                <el-input-number v-model="item.maxConcurrency" :min="1" :max="9999"></el-input-number>
              </el-form-item>
            </div>
            <div class="op">
                <span class="box">
                    <el-button @click="addResourceInfo()" type="primary" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'plus']"/>
                    </el-button>
                </span>
              <span class="box">
                    <el-button @click="removeResourceInfo(index)" type="primary" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'minus']"/>
                    </el-button>
                </span>
            </div>
          </div>
        </div>
      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" onkeydown="return false;"
                     @click="updateTestResourcePool('updateTestResourcePoolForm')"
                     size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import MsTablePagination from "../../common/pagination/TablePagination";

  export default {
    name: "MsTestResourcePool",
    components: {MsCreateBox, MsTablePagination},
    data() {
      return {
        result: {},
        createVisible: false,
        infoList: [],
        updateVisible: false,
        btnTips: this.$t('test_resource_pool.create_resource_pool'),
        btnTipsAdd: this.$t("commons.add"),
        btnTipsDel: this.$t("commons.delete"),
        queryPath: "testresourcepool/list",
        condition: "",
        items: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        form: {},
        rule: {
          name: [
            {required: true, message: this.$t('test_resource_pool.input_pool_name'), trigger: 'blur'},
            {min: 2, max: 64, message: this.$t('commons.input_limit', [2, 64]), trigger: 'blur'},
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
        }
      }
    },
    created() {
      this.initTableData();
    },
    methods: {
      initTableData() {
        let param = {
          name: this.condition
        };

        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
          let data = response.data;
          this.items = data.listObject;
          this.total = data.itemCount;
        })
      },
      changeResourceType() {
        this.infoList = [];
        this.infoList.push({})
      },

      addResourceInfo() {
        this.infoList.push({})
      },
      removeResourceInfo(index) {
        if (this.infoList.length > 1) {
          this.infoList.splice(index, 1)
        } else {
          this.$message({
            type: 'warning',
            message: this.$t('test_resource_pool.cannot_remove_all_node')
          });
        }
      },
      validateResourceInfo() {
        if (this.infoList.length <= 0) {
          return {validate: false, msg: this.$t('test_resource_pool.cannot_empty')}
        }

        let resultValidate = {validate: true, msg: this.$t('test_resource_pool.fill_the_data')};
        this.infoList.forEach(function (info) {
          for (let key in info) {
            if (info[key] != '0' && !info[key]) {
              resultValidate.validate = false
              return false;
            }
          }

          if (!info.maxConcurrency) {
            resultValidate.validate = false
            return false;
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
        this.createVisible = true;
      },
      edit(row) {
        this.updateVisible = true;
        this.form = JSON.parse(JSON.stringify(row));
        this.convertResources();
      },
      convertResources() {
        let resources = [];
        if (this.form.resources) {
          this.form.resources.forEach(function (resource) {
            resources.push(JSON.parse(resource.configuration));
          })
        }
        this.infoList = resources;
      },
      del(row) {
        window.console.log(row);
        this.$confirm(this.$t('test_resource_pool.delete_prompt'), this.$t('commons.prompt'), {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get(`/testresourcepool/delete/${row.id}`).then(() => {
            this.initTableData();
          });
          this.$message({
            type: 'success',
            message: this.$t('commons.delete_success')
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancel')
          });
        });
      },
      createTestResourcePool(createTestResourcePoolForm) {
        if (this.result.loading) {
          return;
        }
        this.$refs[createTestResourcePoolForm].validate(valide => {
          if (valide) {
            let vri = this.validateResourceInfo();
            if (vri.validate) {
              this.convertSubmitResources();
              this.result = this.$post("/testresourcepool/add", this.form, () => {
                this.$message({
                    type: 'success',
                    message: this.$t('commons.save_success')
                  },
                  this.createVisible = false,
                  this.initTableData());
              });
            } else {
              this.$message({
                type: 'warning',
                message: vri.msg
              });
              return false;
            }

          } else {
            return false;
          }
        })
      },
      convertSubmitResources() {
        let resources = [];
        let poolId = this.form.id;
        this.infoList.forEach(function (info) {
          let resource = {"configuration": JSON.stringify(info)};
          if (poolId) {
            resource.testResourcePoolId = poolId;
          }
          resources.push(resource);
        });
        this.form.resources = resources;
      },
      updateTestResourcePool(updateTestResourcePoolForm) {
        if (this.result.loading) {
          return;
        }
        this.$refs[updateTestResourcePoolForm].validate(valide => {
          if (valide) {
            let vri = this.validateResourceInfo();
            if (vri.validate) {
              this.convertSubmitResources();
              this.result = this.$post("/testresourcepool/update", this.form, () => {
                this.$message({
                    type: 'success',
                    message: this.$t('commons.modify_success')
                  },
                  this.updateVisible = false,
                  this.initTableData(),
                  self.loading = false);
              });
            } else {
              this.$message({
                type: 'warning',
                message: vri.msg
              });
              return false;
            }
          } else {
            return false;
          }
        });
      },
      closeFunc() {
        this.form = {};
      },
      changeSwitch(row) {
        this.result = this.$post('/testresourcepool/update', row).then(() => {
          this.$message({
            type: 'success',
            message: this.$t('test_resource_pool.status_change_success')
          });
        })
      }
    }
  }
</script>

<style scoped>
  .search {
    width: 240px;
  }

  .form-input {
    height: 32px !important;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .op {
    float: left;
    width: 10%;
  }

  .box {
    padding-left: 5px;
  }

  .k8s-master {
    width: 34%;
    float: left
  }

  .k8s-token {
    width: 36%;
    float: left
  }

  .k8s-token .el-form-item__label {
    padding-left: 20px;
  }

  .node-line {
    clear: both;
  }
</style>
