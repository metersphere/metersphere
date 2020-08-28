<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="search" @create="create"
                         :create-tip="$t('test_resource_pool.create_resource_pool')" :title="$t('commons.test_resource_pool')"/>
      </template>
      <el-table  border class="adjust-table" :data="items" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column prop="type" :label="$t('test_resource_pool.type')">
          <template v-slot:default="scope">
            <span v-if="scope.row.type === 'NODE'">Node</span>
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
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="del(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog
      :close-on-click-modal="false"
      :title="$t('test_resource_pool.create_resource_pool')"
      :visible.sync="createVisible" width="70%"
      @closed="closeFunc"
      :destroy-on-close="true"
      v-loading="result.loading"
    >
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
            <el-option key="NODE" value="NODE" label="Node">Node</el-option>
          </el-select>
        </el-form-item>
        <div v-for="(item,index) in infoList " :key="index">
          <div class="node-line" v-if="form.type === 'NODE'">
            <el-row>
              <el-col :span="8">
                <el-form-item prop="ip" label="IP">
                  <el-input v-model="item.ip" autocomplete="off"/>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item prop="port" label="Port" style="padding-left: 20px">
                  <el-input-number v-model="item.port" :min="1" :max="65535"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item prop="maxConcurrency" :label="$t('test_resource_pool.max_threads')"
                              style="padding-left: 20px">
                  <el-input-number v-model="item.maxConcurrency" :min="1" :max="1000000000"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :offset="2" :span="2">
                <span class="box">
                    <el-button @click="addResourceInfo()" type="success" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'plus']"/>
                    </el-button>
                </span>
                <span class="box">
                    <el-button @click="removeResourceInfo(index)" type="danger" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'minus']"/>
                    </el-button>
                </span>
              </el-col>
            </el-row>
          </div>
        </div>

      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="createVisible = false"
          @confirm="createTestResourcePool('createTestResourcePoolForm')"/>
      </template>
    </el-dialog>

    <el-dialog
      :close-on-click-modal="false"
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
            <el-option key="NODE" value="NODE" label="Node">Node</el-option>
          </el-select>
        </el-form-item>
        <div v-for="(item,index) in infoList " :key="index">
          <div class="node-line" v-if="form.type === 'NODE'">
            <el-row>
              <el-col :span="8">
                <el-form-item prop="ip" label="IP">
                  <el-input v-model="item.ip" autocomplete="off"/>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item prop="port" label="Port" style="padding-left: 20px">
                  <el-input-number v-model="item.port" :min="1" :max="65535"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item prop="maxConcurrency" :label="$t('test_resource_pool.max_threads')"
                              style="padding-left: 20px">
                  <el-input-number v-model="item.maxConcurrency" :min="1" :max="1000000000"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :offset="2" :span="2">
                <span class="box">
                    <el-button @click="addResourceInfo()" type="success" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'plus']"/>
                    </el-button>
                </span>
                <span class="box">
                    <el-button @click="removeResourceInfo(index)" type="danger" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'minus']"/>
                    </el-button>
                </span>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="updateVisible = false"
          @confirm="updateTestResourcePool('updateTestResourcePoolForm')"/>
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
  import {listenGoBack, removeGoBackListener} from "../../../../common/js/utils";

  export default {
    name: "MsTestResourcePool",
    components: {MsCreateBox, MsTablePagination, MsTableHeader, MsTableOperator, MsDialogFooter},
    data() {
      return {
        result: {},
        createVisible: false,
        infoList: [],
        updateVisible: false,
        queryPath: "testresourcepool/list",
        condition: {},
        items: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        form: {},
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
        }
      }
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
          this.$warning(this.$t('test_resource_pool.cannot_remove_all_node'))
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
        listenGoBack(this.closeFunc);
      },
      edit(row) {
        this.updateVisible = true;
        this.form = JSON.parse(JSON.stringify(row));
        this.convertResources();
        listenGoBack(this.closeFunc);
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
          this.result = this.$get(`/testresourcepool/delete/${row.id}`,() => {
            this.initTableData();
            this.$success(this.$t('commons.delete_success'));
          });
        }).catch(() => {
          this.$info(this.$t('commons.delete_cancel'));
        });
      },
      createTestResourcePool(createTestResourcePoolForm) {
        this.$refs[createTestResourcePoolForm].validate(valid => {
          if (valid) {
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
              this.$warning(vri.msg);
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
        this.$refs[updateTestResourcePoolForm].validate(valid => {
          if (valid) {
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
              this.$warning(vri.msg);
              return false;
            }
          } else {
            return false;
          }
        });
      },
      closeFunc() {
        this.form = {};
        this.updateVisible = false;
        this.createVisible = false;
        removeGoBackListener(this.closeFunc);
      },
      changeSwitch(row) {
        this.result.loading = true;
        this.$info(this.$t('test_resource_pool.check_in'), 1000);
        this.$get('/testresourcepool/update/' + row.id + '/' + row.status)
          .then(() => {
            this.$success(this.$t('test_resource_pool.status_change_success'));
            this.result.loading = false;
          }).catch(() => {
            this.$error(this.$t('test_resource_pool.status_change_failed'));
            row.status = 'INVALID';
            this.result.loading = false;
          })
      }
    }
  }
</script>

<style scoped>

  .box {
    padding-left: 5px;
  }

</style>
