<template>

  <div class="card-container">
    <ms-add-complete-http-api @runTest="runTest" @saveApi="saveApi" :httpData="currentApi" :test="test"
                              :moduleOptions="moduleOptions" :currentProject="currentProject"
                              v-if="reqType === 'HTTP'"></ms-add-complete-http-api>
  </div>
</template>

<script>
  import MsAddCompleteHttpApi from "./complete/AddCompleteHttpApi";
  import {RequestFactory, Test} from "../model/ScenarioModel";
  import {downloadFile, getUUID} from "@/common/js/utils";

  export default {
    name: "ApiConfig",
    components: {MsAddCompleteHttpApi},
    data() {
      return {
        reqType: RequestFactory.TYPES.HTTP,
        test: new Test(),
        postUrl: "",
      }
    },
    props: {
      currentApi: {},
      moduleOptions: {},
      currentProject: {},
    },
    created() {
      if (this.currentApi != null && this.currentApi.id != null) {
        let item = this.currentApi;
        this.test = new Test({
          id: item.id,
          projectId: item.projectId,
          name: item.name,
          status: item.status,
          path: item.path,
          request: new RequestFactory(JSON.parse(item.request)),
        });
        this.postUrl = "/api/delimit/update";
      } else {
        this.test = new Test();
        this.postUrl = "/api/delimit/create";
      }
    },
    methods: {
      runTest(data) {
        this.$emit('runTest', data);
      },
      getBodyUploadFiles() {
        let bodyUploadFiles = [];
        this.test.bodyUploadIds = [];
        let request = this.test.request;
        if (request.body) {
          request.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  this.test.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        return bodyUploadFiles;
      },
      saveApi(data) {
        this.test = data;
        let bodyFiles = this.getBodyUploadFiles();
        let jmx = this.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], jmx.name);
        this.$fileUpload(this.postUrl, file, bodyFiles, this.test, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('saveApi', this.test);
          this.postUrl = "/api/delimit/update";
        });
      }
    }
  }
</script>

<style scoped>


</style>
