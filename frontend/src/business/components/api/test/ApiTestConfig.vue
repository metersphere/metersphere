<template>
  <div class="container">
    <div class="main-content">
      <el-card>
        <el-container class="test-container" v-loading="result.loading">
          <el-header>
            <el-row type="flex" align="middle">
              <el-input class="test-name" v-model="test.name" maxlength="64" :placeholder="$t('api_test.input_name')">
                <el-select class="test-project" v-model="test.projectId" slot="prepend"
                           :placeholder="$t('api_test.select_project')">
                  <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id"/>
                </el-select>
              </el-input>

              <el-button type="primary" plain :disabled="isDisabled" @click="saveTest">
                {{$t('commons.save')}}
              </el-button>

              <el-button type="primary" plain v-if="!isDisabled" @click="saveRunTest">
                {{$t('load_test.save_and_run')}}
              </el-button>

              <el-button type="primary" plain v-if="isDisabled" @click="runTest">
                {{$t('api_test.run')}}
              </el-button>
              <el-button type="warning" plain @click="cancel">{{$t('commons.cancel')}}</el-button>
            </el-row>
          </el-header>
          <ms-api-scenario-config :scenarios="test.scenarioDefinition" ref="config"/>
        </el-container>
      </el-card>
    </div>
  </div>
</template>

<script>
  import MsApiScenarioConfig from "./components/ApiScenarioConfig";
  import {Test} from "./model/ScenarioModel"

  export default {
    name: "MsApiTestConfig",

    components: {MsApiScenarioConfig},

    props: ["id"],

    data() {
      return {
        create: false,
        result: {},
        projects: [],
        change: false,
        test: new Test()
      }
    },

    watch: {
      '$route': 'init',
      test: {
        handler: function () {
          this.change = true;
        },
        deep: true
      }
    },

    methods: {
      init: function () {
        this.result = this.$get("/project/listAll", response => {
          this.projects = response.data;
        })
        if (this.id) {
          this.create = false;
          this.getTest(this.id);
        } else {
          this.create = true;
          this.test = new Test();
          if (this.$refs.config) {
            this.$refs.config.reset();
          }
        }
      },
      getTest: function (id) {
        this.result = this.$get("/api/get/" + id, response => {
          if (response.data) {
            let item = response.data;

            this.test = new Test({
              id: item.id,
              projectId: item.projectId,
              name: item.name,
              scenarioDefinition: JSON.parse(item.scenarioDefinition),
            });
            this.$refs.config.reset();
          }
        });
      },
      save: function (callback) {
        this.change = false;
        let url = this.create ? "/api/create" : "/api/update";
        this.result = this.$request(this.getOptions(url), () => {
          this.create = false;
          if (callback) callback();
        });
      },
      saveTest: function () {
        this.save(() => {
          this.$success(this.$t('commons.save_success'));
        })
      },
      runTest: function () {
        this.result = this.$post("/api/run", {id: this.test.id}, () => {
          this.$success(this.$t('api_test.running'));
        });
      },
      saveRunTest: function () {
        this.change = false;

        this.save(() => {
          this.$success(this.$t('commons.save_success'));
          this.runTest();
        })
      },
      cancel: function () {
        this.$router.push('/api/test/list/all');
      },
      getOptions: function (url) {
        let formData = new FormData();
        let request = {
          id: this.test.id,
          projectId: this.test.projectId,
          name: this.test.name,
          scenarioDefinition: JSON.stringify(this.test.scenarioDefinition)
        }
        let requestJson = JSON.stringify(request);

        formData.append('request', new Blob([requestJson], {
          type: "application/json"
        }));
        let jmx = this.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        formData.append("files", new File([blob], jmx.name));

        return {
          method: 'POST',
          url: url,
          data: formData,
          headers: {
            'Content-Type': undefined
          }
        };
      }
    },

    computed: {
      isDisabled() {
        return !(this.test.projectId && this.test.name && this.change)
      }
    },

    created() {
      this.init();
    }
  }
</script>

<style scoped>
  .test-container {
    height: calc(100vh - 150px);
    min-height: 600px;
  }

  .test-name {
    width: 600px;
    margin-left: -20px;
    margin-right: 20px;
  }

  .test-project {
    min-width: 150px;
  }
</style>
