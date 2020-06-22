<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <div>
          <el-row class="table-title" type="flex" justify="space-between" align="middle">
            <span class="title">API Keys</span>
          </el-row>
          <el-row type="flex" justify="space-between" align="middle">
            <el-button @click="createApiKey()" plain type="el-icon-question" icon="el-icon-circle-plus-outline"
                       size="mini">
              {{$t('commons.create')}}
            </el-button>
          </el-row>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="accessKey" label="Access Key"/>
        <el-table-column prop="secretKey" label="Secret Key">
          <template v-slot:default="scope">
            <el-link type="info" @click="showSecretKey(scope.row)">{{$t('commons.show')}}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('commons.status')">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.status"
                       active-color="#13ce66"
                       inactive-color="#ff4949"
                       active-value="ACTIVE"
                       inactive-value="DISABLED"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator-button :tip="$t('commons.delete')" icon="el-icon-delete"
                                      type="danger" @exec="deleteApiKey(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

  </div>
</template>

<script>
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import {getCurrentUser} from "../../../../common/js/utils";
  import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
  import MsTableHeader from "../../common/components/MsTableHeader";

  export default {
    name: "MsApiKeys",
    components: {MsDialogFooter, MsTableOperatorButton, MsTableHeader},
    data() {
      return {
        result: {},
        updateVisible: false,
        editPasswordVisible: false,
        apiKeysVisible: false,
        condition: {},
        tableData: [],
      }
    },

    activated() {
      this.search();
    },

    methods: {
      currentUser: () => {
        return getCurrentUser();
      },
      search() {
        this.result = this.$get("/user/key/info", response => {
            this.tableData = response.data;
            this.tableData.forEach(d => d.show = false);
          }
        )
      },
      deleteApiKey(row) {
        this.$confirm(this.$t('user.apikey_delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get("/user/key/delete/" + row.id, response => {
            this.$success(this.$t('commons.delete_success'));
            this.search();
          })
        }).catch(() => {
          this.$info(this.$t('commons.delete_cancel'));
        });

      },

      createApiKey() {
        this.result = this.$get("/user/key/generate", response => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        })
      },

      changeSwitch(row) {
        if (row.status === 'ACTIVE') {
          this.result = this.$get("/user/key/active/" + row.id, response => {
            this.$success(this.$t('commons.save_success'));
          });
        }
        if (row.status === 'DISABLED') {
          this.result = this.$get("/user/key/disable/" + row.id, response => {
            this.$success(this.$t('commons.save_success'));
          });
        }
      },
      showSecretKey(row) {
        this.$alert(row.secretKey, 'Secret Key');
      }
    }
  }
</script>

<style scoped>

</style>
