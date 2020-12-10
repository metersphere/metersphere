<template>
  <div v-loading="loading">
    <el-card>
      <el-row>
        <div class="el-step__icon is-text ms-api-col" style="float: left">
          <div class="el-step__icon-inner">{{scenario.index}}</div>
        </div>
        <div style="margin-left: 20px;float: left"> {{scenario.name}}</div>
        <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='Deleted'" type="danger">{{$t('api_test.automation.reference_deleted')}}</el-tag>
        <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>
        <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="remove" style="margin-right: 20px; float: right"/>
      </el-row>
    </el-card>
  </div>
</template>

<script>
  import MsSqlBasisParameters from "../../definition/components/request/database/BasisParameters";
  import MsTcpBasisParameters from "../../definition/components/request/tcp/BasisParameters";
  import MsDubboBasisParameters from "../../definition/components/request/dubbo/BasisParameters";
  import MsApiRequestForm from "../../definition/components/request/http/ApiRequestForm";

  export default {
    name: "ApiScenarioComponent",
    props: {
      scenario: {},
      node: {},
      currentProject: {},
    },
    watch: {},
    created() {
      if (this.scenario.id && this.scenario.referenced === 'REF') {
        this.result = this.$get("/api/automation/getApiScenario/" + this.scenario.id, response => {
          if (response.data) {
            this.scenario.name = response.data.name;
            this.reload();
          } else {
            this.scenario.referenced = "Deleted";
          }
        })
      }
    },
    components: {MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm},
    data() {
      return {loading: false}
    },
    methods: {
      remove() {
        this.$emit('remove', this.scenario, this.node);
      },
      active(item) {
        item.active = !item.active;
        this.reload();
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
    }
  }
</script>

<style scoped>
  .ms-api-col {
    background-color: #F4F4F5;
    border-color: #606266;
    margin-right: 10px;
    color: #606266;
  }

  .ms-api-col-create {
    background-color: #EBF2F2;
    border-color: #008080;
    margin-right: 10px;
    color: #008080;
  }

  /deep/ .el-card__body {
    padding: 15px;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

</style>
