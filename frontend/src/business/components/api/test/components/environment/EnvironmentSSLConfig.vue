<template>

  <div>
    <div style="float: right;">
      <el-button size="mini" @click="open" :disabled="isReadOnly">{{$t('test_track.case.import.click_upload')}}</el-button>
    </div>
    <div class="tip">{{ this.$t('commons.ssl.files') }}
    </div>

    <div class="ms-border">
      <el-table :data="sslConfig.files" highlight-current-row>
        <el-table-column prop="name" :label="$t('load_test.file_name')" show-overflow-tooltip width="180"/>
        <el-table-column prop="type" :label="$t('api_test.definition.request.esb_table.type')" show-overflow-tooltip min-width="100px"/>
        <el-table-column prop="password" show-overflow-tooltip min-width="120px" :label="$t('commons.password')">
          <template v-slot:default="{row}">
            <el-input size="small" v-model="row.password" clearable show-password/>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" show-overflow-tooltip min-width="120px" :label="$t('load_test.last_modify_time')">
          <template v-slot:default="{row}">
            <span>{{ row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" width="100px">
          <template v-slot:default="{row}">
            <div>
              <ms-table-operator-button :tip="$t('commons.update')" icon="el-icon-edit"
                                        type="primary" @exec="edit(row)"/>
              <ms-table-operator-button :tip="$t('api_test.automation.remove')"
                                        icon="el-icon-delete" @exec="remove(row)" type="danger"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <p class="tip">{{ this.$t('commons.ssl.entry') }} </p>
    <div class="ms-border">
      <el-table :data="sslConfig.entrys" highlight-current-row v-if="!loading">
        <el-table-column prop="originalAsName" :label="$t('commons.ssl.original_as_name')" show-overflow-tooltip width="180"/>
        <el-table-column prop="newAsName" :label="$t('commons.ssl.new_as_name')" show-overflow-tooltip min-width="100px">
          <template v-slot:default="{row}">
            <el-input size="mini" v-model="row.newAsName"></el-input>
          </template>
        </el-table-column>
        <el-table-column prop="type" show-overflow-tooltip min-width="120px" :label="$t('api_test.definition.request.esb_table.type')"/>
        <el-table-column prop="password" show-overflow-tooltip min-width="120px" :label="$t('commons.password')">
          <template v-slot:default="{row}">
            <el-input size="mini" v-model="row.password" show-password></el-input>
          </template>
        </el-table-column>

        <el-table-column prop="sourceName" show-overflow-tooltip min-width="120px" :label="$t('commons.ssl.source')"/>
        <el-table-column :label="$t('commons.ssl.default')" width="100px">
          <template v-slot:default="{row}">
            <el-checkbox v-model="row.default" @change="changeCheck(row)"/>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <ms-s-s-l-file-upload :config="fileConfig" :sslConfig="sslConfig" :callback="addConfig" ref="sslConfigUpload"/>
  </div>
</template>

<script>
  import {SSLConfig} from "../../model/EnvironmentModel";
  import MsApiKeyValue from "../ApiKeyValue";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsSelectTree from "../../../../common/select-tree/SelectTree";
  import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
  import {getUUID} from "@/common/js/utils";
  import MsSSLFileUpload from "./SSLFileUpload";

  export default {
    name: "MsEnvironmentSSLConfig",
    components: {MsApiKeyValue, MsSelectTree, MsTableOperatorButton, MsSSLFileUpload},
    props: {
      sslConfig: new SSLConfig(),
      projectId: String,
      isReadOnly: {
        type: Boolean,
        default: false
      },
    },
    created() {
    },
    data() {
      return {
        loading: false,
        fileConfig: {},
      };
    },
    watch: {
      projectId() {
      },
    },
    methods: {
      open() {
        this.$refs.sslConfigUpload.open();
      },
      addConfig(config, file) {
        let sslFile = {id: config.id, name: file.name, type: file.type, updateTime: new Date().getTime(), password: config.password, file: file};
        if (!sslFile.type && sslFile.name) {
          let type = sslFile.name.substr(sslFile.name.lastIndexOf(".") + 1);
          sslFile.type = type;
        }
        if (file.size > 0) {
          this.getEntry(sslFile);
        }
      },
      edit(row) {
        this.$refs.sslConfigUpload.open(row);
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        });
      },
      getEntry(sslFile) {
        let url = '/api/environment/get/entry';
        this.$fileUpload(url, sslFile.file, null, sslFile.password, response => {
          let data = response.data;
          if (data) {
            if (!sslFile.id) {
              sslFile.id = getUUID();
              data.forEach(item => {
                if (item) {
                  item.id = getUUID();
                  item.sourceId = sslFile.id;
                  item.sourceName = sslFile.name;
                }
                item.password = "";
                item.default = false;

                this.sslConfig.entrys.unshift(item);
              })
              this.sslConfig.files.unshift(sslFile);
            } else {
              // 更新条目
              this.remove(sslFile);
              data.forEach(item => {
                if (item) {
                  item.id = getUUID();
                  item.sourceId = sslFile.id;
                  item.sourceName = sslFile.name;
                }
                item.password = "";
                item.default = false;
                this.sslConfig.entrys.unshift(item);
              })
              this.sslConfig.files.unshift(sslFile);
            }
          }
        });
      },
      remove(row) {
        const index = this.sslConfig.files.findIndex((d) => d.id === row.id);
        this.sslConfig.files.splice(index, 1);
        // 同时删除条目
        if (this.sslConfig.entrys) {
          let removeKeys = [];
          this.sslConfig.entrys.forEach(item => {
            if (item && item.sourceId === row.id) {
              const index = this.sslConfig.entrys.findIndex((d) => d.sourceId === row.id);
              removeKeys.push(index);
            }
          });
          removeKeys.forEach(index => {
            if (index !== -1) {
              this.sslConfig.entrys.splice(index, 1);
            }
          })
        }
      },
      changeCheck(row) {
        if (row.default) {
          this.sslConfig.entrys.forEach(item => {
            if (item && item.sourceId !== row.id) {
              item.default = false;
            }
          });
          row.default = true;
        }
      },
    }
  }
</script>

<style scoped>
  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  .ms-el-form-item__content >>> .el-form-item__content {
    line-height: 20px;
  }
</style>
