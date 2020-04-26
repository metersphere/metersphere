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

              <el-button type="primary" plain :disabled="isDisabled" @click="runTest">
                {{$t('load_test.save_and_run')}}
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
          this.getTest(this.id);
        } else {
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
      saveTest: function () {
        this.change = false;

        this.result = this.$post("/api/save", this.getParam(), response => {
          this.test.id = response.data;
          this.$success(this.$t('commons.save_success'));
        });
      },
      runTest: function () {
        this.change = false;

        this.result = this.$post("/api/run", this.getParam(), response => {
          this.test.id = response.data;
          this.$success(this.$t('commons.save_success'));
        });
      },
      cancel: function () {
        this.$router.push('/api/test/list/all');
      },
      getParam: function () {
        return {
          id: this.test.id,
          projectId: this.test.projectId,
          name: this.test.name,
          scenarioDefinition: JSON.stringify(this.test.scenarioDefinition)
        }
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
