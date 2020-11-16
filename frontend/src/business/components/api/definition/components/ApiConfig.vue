<template>

  <div class="card-container">
    <!-- HTTP 请求参数 -->
    <ms-add-complete-http-api @runTest="runTest" @saveApi="saveApi" :httpData="currentApi" :test="test"
                              :moduleOptions="moduleOptions" :currentProject="currentProject"
                              v-if="reqType === 'HTTP'"></ms-add-complete-http-api>
  </div>
</template>

<script>
  import MsAddCompleteHttpApi from "./complete/AddCompleteHttpApi";
  import {RequestFactory, ResponseFactory, Test, Body} from "../model/ApiTestModel";
  import {getUUID} from "@/common/js/utils";

  export default {
    name: "ApiConfig",
    components: {MsAddCompleteHttpApi},
    data() {
      return {
        reqType: RequestFactory.TYPES.HTTP,
        reqUrl: "",
        test: new Test(),
      }
    },
    props: {
      currentApi: {},
      moduleOptions: {},
      currentProject: {},
    },
    created() {
      this.test = new Test({
        request: this.currentApi.request != null ? new RequestFactory(JSON.parse(this.currentApi.request)) : null,
        response: this.currentApi.response != null ? new ResponseFactory(JSON.parse(this.currentApi.response)) : {
          headers: [],
          body: new Body(),
          statusCode: [],
          type: "HTTP"
        }
      });
      if (this.currentApi != null && this.currentApi.id != null) {
        this.reqUrl = "/api/definition/update";
      } else {
        this.reqUrl = "/api/definition/create";
        this.currentApi.id = getUUID().substring(0, 8);
      }
    },
    methods: {
      runTest(data) {
        data.projectId = this.currentProject.id;
        data.request = data.test.request;
        data.response = data.test.response;
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('runTest', data);
        });
      },

      saveApi(data) {
        data.projectId = this.currentProject.id;
        data.request = data.test.request;
        data.response = data.test.response;
        let bodyFiles = this.getBodyUploadFiles(data);

        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('saveApi', data);
        });

      },
      getBodyUploadFiles(data) {
        let bodyUploadFiles = [];
        data.bodyUploadIds = [];
        let request = data.request;
        if (request.body) {
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
        return bodyUploadFiles;
      },
    }
  }
</script>

<style scoped>


</style>
