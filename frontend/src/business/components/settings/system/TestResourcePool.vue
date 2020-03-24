<template>
  <div v-loading="loading">

    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">测试资源池
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" placeholder="根据名称搜索" prefix-icon="el-icon-search"
                      maxlength="60" v-model="condition" @change="search" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="name" label="名称"/>
        <el-table-column prop="description" label="描述"/>
        <el-table-column prop="type" label="类型">
          <template slot-scope="scope">
            <span v-if="scope.row.type === 'NODE'">独立节点</span>
            <span v-if="scope.row.type === 'K8S'">Kubernetes</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="启用/禁用">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status"
                       active-color="#13ce66"
                       inactive-color="#ff4949"
                       active-value="1"
                       inactive-value="0"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column>
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
      <div>
        <el-row>
          <el-col :span="22" :offset="1">
            <div class="table-page">
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page.sync="currentPage"
                :page-sizes="[5, 10, 20, 50, 100]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total">
              </el-pagination>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-dialog title="创建资源池" :visible.sync="createVisible" width="70%" @closed="closeFunc"
               :destroy-on-close="true">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="createTestResourcePoolForm">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="资源类型" prop="type">
          <el-select v-model="form.type" placeholder="选择资源类型" @change="changeResourceType()">
            <el-option key="K8S" value="K8S" label="Kubernetes">Kubernetes</el-option>
            <el-option key="NODE" value="NODE" label="独立节点">独立节点</el-option>
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
              <el-form-item prop="maxConcurrency" label="最大并发数">
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
              <el-form-item prop="maxConcurrency" label="最大并发数">
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
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="createTestResourcePool('createTestResourcePoolForm')"
                   size="medium">创建</el-button>
      </span>
    </el-dialog>

    <el-dialog title="修改资源池" :visible.sync="updateVisible" width="70%" :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="updateTestResourcePoolForm">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="资源类型" prop="type">
          <el-select v-model="form.type" placeholder="选择资源类型" @change="changeResourceType()">
            <el-option key="K8S" value="K8S" label="Kubernetes">Kubernetes</el-option>
            <el-option key="NODE" value="NODE" label="独立节点">独立节点</el-option>
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
              <el-form-item prop="maxConcurrency" label="最大并发数" style="padding-left: 20px">
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
              <el-form-item prop="maxConcurrency" label="最大并发数" style="padding-left: 20px">
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
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateTestResourcePool('updateTestResourcePoolForm')"
                   size="medium">修改</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";

  export default {
    name: "MsTestResourcePool",
    components: {MsCreateBox},
    data() {
      return {
        loading: false,
        testLoading: false,
        createVisible: false,
        infoList: [],
        updateVisible: false,
        btnTips: "添加资源池",
        btnTipsAdd: "添加",
        btnTipsDel: "删除",
        queryPath: "testresourcepool/list",
        condition: "",
        items: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        form: {},
        rule: {
          name: [
            {required: true, message: '请输入资源池名称', trigger: 'blur'},
            {min: 2, max: 64, message: this.$t('commons.input_limit', [2, 64]), trigger: 'blur'},
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: '资源池名称不支持特殊字符',
              trigger: 'blur'
            }
          ],
          description: [
            {max: 60, message: '最大长度 60 个字符', trigger: 'blur'}
          ],
          type: [
            {required: true, message: '请选择资源类型', trigger: 'blur'}
          ]
        }
      }
    },
    created() {
      this.initTableData();
    },
    methods: {
      initTableData() {
        this.loading = true;
        let param = {
          name: this.condition
        };

        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
          let data = response.data;
          this.items = data.listObject;
          this.total = data.itemCount;
          this.loading = false;
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
            message: "不能删除所有独立节点"
          });
        }
      },
      validateResourceInfo() {
        if (this.infoList.length <= 0) {
          return {validate: false, msg: "资源池不能为空"}
        }

        let resultValidate = {validate: true, msg: "请完善数据"}
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
      handleSizeChange(size) {
        this.pageSize = size;
      },
      handleCurrentChange(current) {
        this.currentPage = current;
      },
      create() {
        this.createVisible = true;
      },
      edit(row) {
        window.console.log(row);
        // this.loading = true;
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
        this.$confirm('此操作将永久删除该资源池, 是否继续?', '提示', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.$get(`/testresourcepool/delete/${row.id}`).then(() => {
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
        this.$refs[createTestResourcePoolForm].validate(valide => {
          if (valide) {
            let vri = this.validateResourceInfo();
            if (vri.validate) {
              this.testLoading = true;
              this.convertSubmitResources();
              this.$post("/testresourcepool/add", this.form)
                .then(() => {
                  this.$message({
                      type: 'success',
                      message: '添加成功!'
                    },
                    this.createVisible = false,
                    this.initTableData());
                  this.testLoading = false;
                });
            } else {
              this.$message({
                type: 'warning',
                message: vri.msg
              });
              this.testLoading = false;
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
        this.$refs[updateTestResourcePoolForm].validate(valide => {
          if (valide) {
            this.testLoading = true;
            let vri = this.validateResourceInfo();
            if (vri.validate) {
              this.convertSubmitResources();
              this.$post("/testresourcepool/update", this.form)
                .then(() => {
                  this.$message({
                      type: 'success',
                      message: this.$t('commons.modify_success')
                    },
                    this.updateVisible = false,
                    this.initTableData(),
                    self.loading = false);
                  this.testLoading = false;
                });
            } else {
              this.$message({
                type: 'warning',
                message: vri.msg
              });
              this.testLoading = false;
              return false;
            }
          } else {
            return false;
          }
        })
      },
      closeFunc() {
        this.form = {};
      },
      changeSwitch(row) {
        this.$post('/testresourcepool/update', row).then(() => {
          this.$message({
            type: 'success',
            message: '状态修改成功!'
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
