<template>
  <div class="compare-class">
    <el-card style="width: 50%;" ref="old">
      <el-form :model="oldData" :rules="rule" ref="httpForm" label-width="80px" label-position="right">
        <!-- 操作按钮 -->

          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off"
               style="color: #783987; font-size: 25px; margin-right: 5px; position: relative; top: 5px; float: right; cursor: pointer "/>
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i class="el-icon-star-on"
               style="color: #783987; font-size: 28px; margin-right: 5px; position: relative; top: 5px; float: right; cursor: pointer "/>
          </el-tooltip>

        <br/>
        <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

        <!-- 基础信息 -->
        <div class="base-info">
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('commons.name')" prop="name">
                <el-input class="ms-http-input" size="small" v-model="oldData.name"/>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item :label="$t('api_report.request')" prop="path">
                <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="oldData.path"
                          class="ms-http-input" size="small" style="margin-top: 5px" >
                  <el-select v-model="oldData.method" slot="prepend" style="width: 100px" size="small">
                    <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
                  </el-select>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
                <el-select v-model="oldData.userId"
                           :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                           class="ms-http-select">
                  <el-option
                    v-for="item in maintainerOptions"
                    :key="item.id"
                    :label="item.name + ' (' + item.id + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
                <ms-select-tree size="small" :data="moduleOptions" :defaultKey="oldData.moduleId"
                                :obj="moduleObj" clearable checkStrictly/>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select class="ms-http-select" size="small" v-model="oldData.status">
                  <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('commons.tag')" prop="tag">
                <ms-input-tag :currentScenario="oldData" ref="tag" v-model="oldData.tags"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('commons.description')" prop="description">
                <el-input class="ms-http-textarea"
                          v-model="oldData.description"
                          type="textarea"
                          :autosize="{ minRows: 1, maxRows: 10}"
                          :rows="1" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- MOCK信息 -->
        <ms-form-divider :title="$t('test_track.plan_view.mock_info')"/>
        <div class="base-info mock-info">
          <el-row>
            <el-col :span="20">
              Mock地址：
              <el-link :href="oldMockUrl" target="_blank" style="color: black"
                       type="primary">{{ this.oldMockUrl }}
              </el-link>
            </el-col>
          </el-row>

        </div>

        <!-- 请求参数 -->
        <div>
          <ms-form-divider :title="$t('api_test.definition.request.req_param')"/>
          <ms-api-request-form :showScript="false" :request="request" :headers="request.headers"
                               :isShowEnable="isShowEnable"/>
        </div>

      </el-form>

      <!-- 响应内容-->
      <ms-form-divider :title="$t('api_test.definition.request.res_param')"/>
      <ms-response-text :response="response"/>

      <api-other-info :api="oldData"/>

    </el-card>
    <el-card style="width: 50%;" ref="new">
      <el-form :model="newData" :rules="rule" ref="httpForm" label-width="80px" label-position="right">
        <!-- 操作按钮 -->
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!newShowFollow">
            <i class="el-icon-star-off"
               style="color: #783987; font-size: 25px; margin-right: 5px; position: relative; top: 5px; float: right; cursor: pointer "/>
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="newShowFollow">
            <i class="el-icon-star-on"
               style="color: #783987; font-size: 28px; margin-right: 5px; position: relative; top: 5px; float: right; cursor: pointer "/>
          </el-tooltip>

        <br/>

        <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

        <!-- 基础信息 -->
        <div class="base-info">
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('commons.name')" prop="name">
                <el-input class="ms-http-input" size="small" v-model="newData.name"/>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item :label="$t('api_report.request')" prop="path">
                <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="newData.path"
                          class="ms-http-input" size="small" style="margin-top: 5px" >
                  <el-select v-model="newData.method" slot="prepend" style="width: 100px" size="small">
                    <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
                  </el-select>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
                <el-select v-model="newData.userId"
                           :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                           class="ms-http-select">
                  <el-option
                    v-for="item in maintainerOptions"
                    :key="item.id"
                    :label="item.name + ' (' + item.id + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
                <ms-select-tree size="small" :data="moduleOptions" :defaultKey="newData.moduleId"
                                :obj="moduleObj" clearable checkStrictly/>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select class="ms-http-select" size="small" v-model="newData.status">
                  <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('commons.tag')" prop="tag">
                <ms-input-tag :currentScenario="newData" ref="tag" v-model="newData.tags"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('commons.description')" prop="description">
                <el-input class="ms-http-textarea"
                          v-model="newData.description"
                          type="textarea"
                          :autosize="{ minRows: 1, maxRows: 10}"
                          :rows="1" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- MOCK信息 -->
        <ms-form-divider :title="$t('test_track.plan_view.mock_info')"/>
        <div class="base-info mock-info">
          <el-row>
            <el-col :span="20">
              Mock地址：
              <el-link :href="newMockUrl" target="_blank" style="color: black"
                       type="primary">{{ this.newMockUrl }}
              </el-link>
            </el-col>
          </el-row>

        </div>

        <!-- 请求参数 -->
        <div>
          <ms-form-divider :title="$t('api_test.definition.request.req_param')"/>
          <ms-api-request-form :showScript="false" :request="oldRequest" :headers="oldRequest.headers"
                               :isShowEnable="isShowEnable"/>
        </div>

      </el-form>

      <!-- 响应内容-->
      <ms-form-divider :title="$t('api_test.definition.request.res_param')"/>
      <ms-response-text :response="oldResponse"/>

      <api-other-info :api="newData"/>
    </el-card>
  </div>
</template>
<script>
import {API_STATUS, REQ_METHOD} from "../../../model/JsonData";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import ApiOtherInfo from "@/business/components/api/definition/components/complete/ApiOtherInfo";
import MsResponseText from "../../response/ResponseText";
import MsApiRequestForm from "../../request/http/ApiHttpRequestForm";
import MsSelectTree from "../../../../../common/select-tree/SelectTree";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";

const {diff} = require("@/business/components/performance/v_node_diff");

export default{
  name: "HttpApiVersionDiff",
  components: {
    ApiOtherInfo,
    MsFormDivider,
    MsResponseText,
    MsApiRequestForm,
    MsInputTag,
    MsSelectTree,
  },
  props:{
    oldData:{
      type:Object
    },
    newData:{
      type:Object
    },
    showFollow:{
      type:Boolean
    },
    newShowFollow:{
      type:Boolean
    },
    rule:{
      type:Object
    },
    maintainerOptions:{
      type:Array
    },
    moduleOptions:{},
    oldMockUrl:{
    },
    newMockUrl:{
    },
    request: {},
    oldRequest:{},
    response: {},
    oldResponse:{}
  },

  data(){
    return{
      reqOptions: REQ_METHOD,
      options: API_STATUS,
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      isShowEnable: true,
    }
  },
  methods:{
    getDiff(){
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      //oldVnode.style.backgroundColor = "rgb(241,200,196)";
      console.log(this.$refs.old)
      console.log(this.$refs.new)
      diff(oldVnode,vnode);
    }
  },
  mounted() {
    this.$nextTick(function () {
      setTimeout(this.getDiff,(this.$refs.old.$children.length+1)/2*1000)
    })
  },
  created() {
  }
}
</script>
<style scoped>
.compare-class{
  display: flex;
  justify-content:space-between;
}
</style>
