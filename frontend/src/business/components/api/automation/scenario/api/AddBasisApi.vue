<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.definition.request.title')" :visible.sync="httpVisible"
             width="45%"
             :destroy-on-close="true" append-to-body>
    <el-form :model="httpForm" label-position="right" label-width="80px" size="small" :rules="rule" ref="httpForm" v-if="!loading">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="httpForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
      </el-form-item>

      <!--HTTP 协议特有参数-->
      <el-form-item :label="$t('api_report.request')" prop="path" v-if="currentProtocol==='HTTP'">
        <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="httpForm.path"
                  class="ms-http-input" size="small">
          <el-select v-model="httpForm.method" slot="prepend" style="width: 100px" size="small">
            <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-input>
      </el-form-item>

      <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
        <el-select v-model="httpForm.userId"
                   :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                   style="width: 100%">
          <el-option
            v-for="item in maintainerOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>

      </el-form-item>
      <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
        <ms-select-tree size="small" :data="moduleOptions" :defaultKey="httpForm.moduleId" @getValue="setModule" :obj="moduleObj" clearable checkStrictly/>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description" style="margin-bottom: 29px">
        <el-input class="ms-http-textarea" v-model="httpForm.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>
      <el-form-item class="create-tip">
        {{$t('api_test.definition.create_tip')}}
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="httpVisible = false"
        @confirm="saveApi" v-prevent-re-click>
      </ms-dialog-footer>

    </template>

  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import {REQ_METHOD} from "../../../definition/model/JsonData";
  import {getCurrentProjectID, getCurrentUser} from "@/common/js/utils";
  import {createComponent, Request} from "../../../definition/components/jmeter/components";
  import {getUUID} from "@/common/js/utils";
  import MsSelectTree from "@/business/components/common/select-tree/SelectTree";
  import {buildTree} from "../../../definition/model/NodeTree";


  export default {
    name: "MsAddBasisApi",
    components: {MsDialogFooter, MsSelectTree},
    props: {
      currentProtocol: {
        type: String,
        default: "HTTP"
      },
    },
    data() {
      let validateURL = (rule, value, callback) => {
        if (!this.httpForm.path.startsWith("/") || this.httpForm.path.match(/\s/) != null) {
          callback(this.$t('api_test.definition.request.path_valid_info'));
        }
        callback();
      };
      return {
        httpForm: {environmentId: "", moduleId: "default-module"},
        moduleOptions: [],
        httpVisible: false,
        currentModule: {},
        maintainerOptions: [],
        loading: false,
        moduleObj: {
          id: 'id',
          label: 'name',
        },
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
          ],
          path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}, {validator: validateURL, trigger: 'blur'}],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          // moduleId: [{required: true, message: this.$t('test_track.module.module'), trigger: 'change'}],
        },
        value: REQ_METHOD[0].id,
        options: REQ_METHOD,
      }
    }
    ,
    methods: {
      saveApi() {
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            if (this.httpForm.path && this.httpForm.path.match(/\s/) != null) {
              this.$error(this.$t("api_test.definition.request.path_valid_info"));
              return false;
            }
            this.save(this.httpForm);
          } else {
            return false;
          }
        })
      },
      save(data) {
        this.setParameters(data);
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload("/api/definition/create", null, bodyFiles, data, () => {
          this.saveCase(data);
        });
      },
      saveCase(api) {
        let obj = {apiDefinitionId: api.id, name: api.name, priority: 'P0', active: true, uuid: getUUID(), request: api.request};
        obj.projectId = getCurrentProjectID();
        obj.id = obj.uuid;
        let url = "/api/testcase/create";
        let bodyFiles = this.getBodyUploadFiles(obj);
        this.$fileUpload(url, null, bodyFiles, obj, (response) => {
          this.$success(this.$t('commons.save_success'));
          this.httpVisible = false;
        });
      },
      setParameters(data) {
        data.projectId = getCurrentProjectID();
        data.request.name = data.name;
        if (data.protocol === "DUBBO" || data.protocol === "dubbo://") {
          data.request.protocol = "dubbo://";
        }
        data.id = data.request.id;
        if (!data.method) {
          data.method = data.protocol;
        }
      },
      getBodyUploadFiles(data) {
        let bodyUploadFiles = [];
        data.bodyUploadIds = [];
        let request = data.request;
        if (request.body) {
          if (request.body.kvs) {
            request.body.kvs.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    let fileId = getUUID().substring(0, 8);
                    item.name = item.file.name;
                    item.id = fileId;
                    data.bodyUploadIds.push(fileId);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
          if (request.body.binary) {
            request.body.binary.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    let fileId = getUUID().substring(0, 8);
                    item.name = item.file.name;
                    item.id = fileId;
                    data.bodyUploadIds.push(fileId);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
        }
        return bodyUploadFiles;
      },
      setParameter() {
        switch (this.currentProtocol) {
          case Request.TYPES.SQL:
            this.initSQL();
            break;
          case Request.TYPES.DUBBO:
            this.initDUBBO();
            break;
          case Request.TYPES.TCP:
            this.initTCP();
            break;
          default:
            this.initHTTP();
            break;
        }
        this.httpForm.bodyUploadIds = [];
        this.httpForm.projectId = getCurrentProjectID();
        this.httpForm.id = this.httpForm.request.id;
        this.httpForm.protocol = this.currentProtocol;

        this.httpForm.request.name = this.httpForm.name;
        this.httpForm.request.protocol = this.currentProtocol;
        if (this.currentProtocol === 'HTTP') {
          this.httpForm.request.method = this.httpForm.method;
          this.httpForm.request.path = this.httpForm.path;
        }
        if (this.currentModule != null) {
          this.httpForm.modulePath = this.currentModule.method != undefined ? this.currentModule.method : null;
          this.httpForm.moduleId = this.currentModule.id;
        }

      },
      initHTTP() {
        let request = createComponent("HTTPSamplerProxy");
        request.path = this.httpForm.path;
        this.httpForm.request = request;
      },
      initSQL() {
        this.httpForm.method = Request.TYPES.SQL;
        this.httpForm.request = createComponent("JDBCSampler");
      },
      initTCP() {
        this.httpForm.method = Request.TYPES.TCP;
        this.httpForm.request = createComponent("TCPSampler");
      },
      initDUBBO() {
        this.httpForm.method = "dubbo://";
        this.httpForm.request = createComponent("DubboSampler");
      },
      getMaintainerOptions() {
        this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()},response => {
          this.maintainerOptions = response.data;
        });
      },
      list(data) {
        if (data.protocol === "dubbo://") {
          data.protocol = "DUBBO";
        }
        let url = "/api/module/list/" + getCurrentProjectID() + "/" + data.protocol;
        this.result = this.$get(url, response => {
          if (response.data != undefined && response.data != null) {
            this.moduleOptions = response.data;
            this.moduleOptions.forEach(node => {
              buildTree(node, {path: ''});
            });
          }
        });
      },
      setModule(id, data) {
        this.httpForm.moduleId = id;
        this.httpForm.modulePath = data.path;
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      open(api) {
        if (api) {
          let data = JSON.parse(JSON.stringify(api));
          if (data.protocol === "dubbo://") {
            data.protocol = "DUBBO";
          }
          data.id = getUUID();
          this.httpForm = {id: data.id, name: data.name, protocol: data.protocol, path: data.path, method: api.method, userId: getCurrentUser().id, request: data, moduleId: "default-module"};
          this.getMaintainerOptions();
          this.list(data);
          this.httpVisible = true;
        }
      },
    }
  }
</script>

<style scoped>

  .create-tip {
    color: #8c939d;
  }

</style>
