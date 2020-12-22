<template>
  <div v-loading="loading">
    <el-card>
      <el-row>
        <div class="el-step__icon is-text ms-api-col">
          <div class="el-step__icon-inner">{{scenario.index}}</div>
        </div>
        <el-button class="ms-title-buttion" size="small">{{$t('api_test.automation.scenario_import')}}</el-button>
        {{scenario.name}}
        <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='Deleted'" type="danger">{{$t('api_test.automation.reference_deleted')}}</el-tag>
        <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='Copy'">{{ $t('commons.copy') }}</el-tag>
        <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>
        <div style="margin-right: 20px; float: right">
          <el-switch v-model="scenario.enable" style="margin-left: 10px"/>
          <el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow" style="margin-left: 10px"/>
          <el-button size="mini" icon="el-icon-delete" type="danger" circle @click="remove" style="margin-left: 10px"/>
        </div>
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
      copyRow() {
        this.$emit('copyRow', this.scenario, this.node);
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

  /deep/ .el-card__body {
    padding: 15px;
  }

  .ms-title-buttion {
    background-color: #F4F4F5;
    margin-right: 20px;
    color: #606266;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

</style>
