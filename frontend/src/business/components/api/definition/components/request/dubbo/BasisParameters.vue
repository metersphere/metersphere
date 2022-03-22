<template>
  <div>
    <el-row>
      <el-col :span="spanNum" style="padding-bottom: 20px">
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100% ;">

          <el-form :model="request" ref="request" label-width="100px" :disabled="isReadOnly" style="margin: 10px"
                   class="ms-el-tabs__nav-scroll">

            <el-form-item :label="$t('api_test.request.dubbo.protocol')" prop="protocol">
              <el-select v-model="request.protocol" size="small">
                <el-option label="dubbo://" :value="protocols.DUBBO"/>
              </el-select>
            </el-form-item>

            <el-tabs v-model="activeName" @tab-click="tabClick">
              <el-tab-pane label="Interface" name="interface" v-if="isBodyShow">
                <ms-dubbo-interface :request="request" :is-read-only="isReadOnly"/>
              </el-tab-pane>
              <el-tab-pane label="Config Center" name="config">
                <ms-dubbo-config-center :config="request.configCenter" :is-read-only="isReadOnly"
                                        :description="$t('api_test.request.dubbo.form_description')"/>
              </el-tab-pane>
              <el-tab-pane label="Registry Center" name="registry">
                <ms-dubbo-registry-center :registry="request.registryCenter" :is-read-only="isReadOnly"
                                          :description="$t('api_test.request.dubbo.form_description')"/>
              </el-tab-pane>
              <el-tab-pane label="Consumer & Service" name="consumer">
                <ms-dubbo-consumer-service :consumer="request.consumerAndService" :is-read-only="isReadOnly"
                                           :description="$t('api_test.request.dubbo.form_description')"/>
              </el-tab-pane>

              <el-tab-pane label="Args" name="args">
                <ms-api-key-value :is-read-only="isReadOnly" :items="request.args"
                                  key-placeholder="Param Type" value-placeholder="Param Value"/>
              </el-tab-pane>
              <el-tab-pane label="Attachment Args" name="attachment">
                <ms-api-key-value :is-read-only="isReadOnly" :items="request.attachmentArgs"/>
              </el-tab-pane>

              <!-- 脚本步骤/断言步骤 -->
              <el-tab-pane :label="$t('api_test.definition.request.pre_operation')" name="preOperate" v-if="showScript">
                 <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                 {{ $t('api_test.definition.request.pre_operation') }}
              <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.preSize > 0">
              <div class="el-step__icon-inner">{{ request.preSize }}</div>
              </div>
                  </span>
                <ms-jmx-step :request="request" :apiId="request.id" :response="response" :tab-type="'pre'"
                             ref="preStep"/>
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.definition.request.post_operation')" name="postOperate"
                           v-if="showScript">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                {{ $t('api_test.definition.request.post_operation') }}
                  <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.postSize > 0">
                  <div class="el-step__icon-inner">{{ request.postSize }}</div>
                 </div>
                </span>
                <ms-jmx-step :request="request" :apiId="request.id" :response="response" :tab-type="'post'"
                             ref="postStep"/>
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.definition.request.assertions_rule')" name="assertionsRule"
                           v-if="showScript">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                {{ $t('api_test.definition.request.assertions_rule') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.ruleSize > 0">
                  <div class="el-step__icon-inner">{{ request.ruleSize }}</div>
                </div>
              </span>
                <ms-jmx-step :request="request" :apiId="request.id" :response="response" @reload="reloadBody"
                             :tab-type="'assertionsRule'" ref="assertionsRule"/>
              </el-tab-pane>
            </el-tabs>

          </el-form>
        </div>

      </el-col>

    </el-row>

  </div>
</template>

<script>
  import MsApiKeyValue from "../../ApiKeyValue";
  import MsApiAssertions from "../../assertion/ApiAssertions";
  import MsApiExtract from "../../extract/ApiExtract";
  import ApiRequestMethodSelect from "../../collapse/ApiRequestMethodSelect";
  import MsCodeEdit from "../../../../../common/components/MsCodeEdit";
  import MsApiScenarioVariables from "../../ApiScenarioVariables";
  import {createComponent} from "../../jmeter/components";
  import {Assertions, Extract, DubboRequest} from "../../../model/ApiTestModel";
  import MsDubboInterface from "../../request/dubbo/Interface";
  import MsDubboRegistryCenter from "../../request/dubbo/RegistryCenter";
  import MsDubboConfigCenter from "../../request/dubbo/ConfigCenter";
  import MsDubboConsumerService from "../../request/dubbo/ConsumerAndService";
  import {getUUID} from "@/common/js/utils";
  import MsJsr233Processor from "../../../../automation/scenario/component/Jsr233Processor";
  import MsJmxStep from "../../step/JmxStep";
  import {stepCompute, hisDataProcessing} from "@/business/components/api/definition/api-definition";

  export default {
    name: "MsDatabaseConfig",
    components: {
      MsJsr233Processor,
      MsApiScenarioVariables,
      MsCodeEdit,
      ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiKeyValue, MsDubboConsumerService,
      MsDubboConfigCenter,
      MsDubboRegistryCenter,
      MsDubboInterface,
      MsJmxStep
    },
    props: {
      request: {},
      basisData: {},
      moduleOptions: Array,
      response: {},
      isReadOnly: {
        type: Boolean,
        default: false
      },
      showScript: {
        type: Boolean,
        default: true,
      }
    },
    data() {
      return {
        spanNum: 24,
        activeName: "interface",
        activeName2: "args",
        isBodyShow: true,
        protocols: DubboRequest.PROTOCOLS,
        isReloadData: false,
      }
    },
    created() {
      if (this.request.hashTree) {
        this.initStepSize(this.request.hashTree);
        this.historicalDataProcessing(this.request.hashTree);
      }
    },
    watch: {
      'request.hashTree': {
        handler(v) {
          this.initStepSize(this.request.hashTree);
        },
        deep: true
      }
    },
    methods: {
      tabClick() {
        if (this.activeName === 'preOperate') {
          this.$refs.preStep.filter();
        }
        if (this.activeName === 'postOperate') {
          this.$refs.postStep.filter();
        }
        if (this.activeName === 'assertionsRule') {
          this.$refs.assertionsRule.filter();
        }
      },
      historicalDataProcessing(array) {
        hisDataProcessing(array, this.request);
      },
      initStepSize(array) {
        stepCompute(array, this.request);
        this.reloadBody();
      },
      reloadBody() {
        // 解决修改请求头后 body 显示错位
        this.isBodyShow = false;
        this.$nextTick(() => {
          this.isBodyShow = true;
        });
      },
      remove(row) {
        let index = this.request.hashTree.indexOf(row);
        this.request.hashTree.splice(index, 1);
        this.reload();
      },
      copyRow(row) {
        let obj = JSON.parse(JSON.stringify(row));
        obj.id = getUUID();
        this.request.hashTree.push(obj);
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
      validateApi() {
        this.$refs['basicForm'].validate();
      },
      saveApi() {
        this.basisData.method = this.basisData.protocol;
        this.$emit('saveApi', this.basisData);
      },
      runTest() {

      },
    }
  }
</script>

<style scoped>
  .one-row .el-form-item {
    display: inline-block;
  }

  .one-row .el-form-item:nth-child(2) {
    margin-left: 60px;
  }

  .ms-left-cell {
    margin-top: 40px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  .ms-header {
    background: #783887;
    color: white;
    height: 18px;
    font-size: xx-small;
    border-radius: 50%;
  }

  .ms-left-cell {
    margin-top: 40px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  .ms-el-tabs__nav-scroll >>> .el-tabs__nav-scroll {
    width: calc(100% - 10px);
  }

</style>
