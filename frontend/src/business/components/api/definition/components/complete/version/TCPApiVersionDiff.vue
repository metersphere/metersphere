<template>
  <div>
    <el-row>
      <el-col :span="12">
        <el-tag>当前{{oldData.versionName }}</el-tag><span style="margin-left: 10px">{{oldData.userName}}</span><span style="margin-left: 10px">{{oldData.createTime | timestampFormatDate }}</span>
      </el-col>
      <el-col :span="12">
        <el-tag>{{ newData.versionName }}</el-tag><span style="margin-left: 10px">{{newData.userName}}</span><span style="margin-left: 10px">{{newData.createTime | timestampFormatDate }}</span>
      </el-col>
    </el-row>
    <div class="compare-class" v-loading="isReloadData">
      <el-card style="width: 50%;" ref="old">
        <div style="background-color: white;">
          <!-- 基础信息 -->
          <p class="tip">{{ $t('test_track.plan_view.base_info') }} </p>
          <br/>
          <el-row>
            <el-col>
              <ms-tcp-basic-api
                :is-diff = true
                :method-types="methodTypes"
                :moduleOptions="moduleOptions"
                :basisData="oldData" ref="basicForm"/>
            </el-col>
          </el-row>
          <!-- MOCK信息 -->
          <p class="tip">{{ $t('test_track.plan_view.mock_info') }} </p>
          <div class="mock-info">
            <el-row>
              <el-col :span="20">
                Mock地址：
                <el-link v-if="this.mockInfo !== '' " target="_blank" style="color: black"
                         type="primary">{{ this.mockInfo }}
                </el-link>
                <el-link v-else target="_blank" style="color: darkred"
                         type="primary">当前项目未开启Mock服务
                </el-link>
              </el-col>
            </el-row>
          </div>
          <!-- 请求参数 -->
          <div v-if="oldApiProtocol==='TCP'">
            <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>

            <ms-tcp-format-parameters :show-script="false" :request="oldRequest"  ref="tcpFormatParameter"/>
          </div>
          <div v-else-if="oldApiProtocol==='ESB'">
            <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
            <esb-definition v-xpack v-if="showXpackCompnent" :show-script="false" :is-read-only="true" :request="oldRequest" ref="esbDefinition"/>
            <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
            <esb-definition-response v-xpack v-if="showXpackCompnent" :is-api-component="true" :is-read-only="true"
                                     :request="oldRequest"/>
            <!--      <api-response-component :currentProtocol="apiCase.request.protocol" :api-item="apiCase"/>-->
          </div>

          <ms-form-divider :title="$t('test_track.case.other_info')"/>
          <api-info-container>
            <el-form :model="oldData" ref="api-form" label-width="100px">
              <el-collapse-transition>
                <el-tabs v-model="activeName" style="margin: 20px">
                  <el-tab-pane :label="$t('commons.remark')" name="remark" class="pane">
                    <form-rich-text-item class="remark-item" :disabled="true" :data="oldData" prop="remark" label-width="0"/>
                  </el-tab-pane>
                  <el-tab-pane :label="$t('commons.relationship.name')" name="dependencies" class="pane">
                    <template v-slot:label>
                      <tab-pane-count :title="$t('commons.relationship.name')" :count="oldRelationshipCount"/>
                    </template>
                    <dependencies-list  @setCount="setOldCount" :read-only="true" :resource-id="oldData.id" resource-type="API" ref="oldDependencies"/>
                  </el-tab-pane>
                </el-tabs>
              </el-collapse-transition>
            </el-form>
          </api-info-container>

        </div>
      </el-card>
      <el-card style="width: 50%;" ref="new">
        <div style="background-color: white;">
          <!-- 基础信息 -->
          <p class="tip">{{ $t('test_track.plan_view.base_info') }} </p>
          <br/>
          <el-row>
            <el-col>
              <ms-tcp-basic-api
                :is-diff = true
                :method-types="methodTypes"
                :moduleOptions="moduleOptions"
                :basisData="newData" ref="basicForm"/>
            </el-col>
          </el-row>
          <!-- MOCK信息 -->
          <p class="tip">{{ $t('test_track.plan_view.mock_info') }} </p>
          <div class="mock-info">
            <el-row>
              <el-col :span="20">
                Mock地址：
                <el-link v-if="this.mockInfo !== '' " target="_blank" style="color: black"
                         type="primary">{{ this.mockInfo }}
                </el-link>
                <el-link v-else target="_blank" style="color: darkred"
                         type="primary">当前项目未开启Mock服务
                </el-link>
              </el-col>
            </el-row>
          </div>
          <!-- 请求参数 -->
          <div v-if="apiProtocol=='TCP'">
            <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
            <ms-tcp-format-parameters :show-script="false" :request="request"  ref="tcpFormatParameter"/>
          </div>
          <div v-else-if="apiProtocol=='ESB'">
            <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
            <esb-definition v-xpack v-if="showXpackCompnent" :show-script="false"  :is-read-only="true"  :request="request" ref="esbDefinition"/>
            <p class="tip">{{ $t('api_test.definition.request.res_param') }}</p>
            <esb-definition-response v-xpack v-if="showXpackCompnent" :is-api-component="true"  :is-read-only="true"
                                     :request="request"/>
            <!--      <api-response-component :currentProtocol="apiCase.request.protocol" :api-item="apiCase"/>-->
          </div>

          <!-- 其他信息-->
          <ms-form-divider :title="$t('test_track.case.other_info')"/>
          <api-info-container>
            <el-form :model="newData" ref="api-form" label-width="100px">
              <el-collapse-transition>
                <el-tabs v-model="activeName" style="margin: 20px">
                  <el-tab-pane :label="$t('commons.remark')" name="remark" class="pane">
                    <form-rich-text-item class="remark-item" :disabled="true" :data="newData" prop="remark" label-width="0"/>
                  </el-tab-pane>
                  <el-tab-pane :label="$t('commons.relationship.name')" name="dependencies" class="pane">
                    <template v-slot:label>
                      <tab-pane-count :title="$t('commons.relationship.name')" :count="relationshipCount"/>
                    </template>
                    <dependencies-list @setCount="setCount" :read-only="true" :resource-id="newData.id" resource-type="API" ref="newDependencies"/>
                  </el-tab-pane>
                </el-tabs>
              </el-collapse-transition>
            </el-form>
          </api-info-container>


        </div>
      </el-card>
    </div>
  </div>
</template>
<script>


import MsTcpBasicApi from ".././TCPBasicApi";
import MsTcpFormatParameters from "../../request/tcp/TcpFormatParameters";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import ApiInfoContainer from "@/business/components/api/definition/components/complete/ApiInfoContainer";
import DependenciesList from "@/business/components/common/components/graph/DependenciesList";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import {getRelationshipCountApi} from "@/network/api";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const esbDefinition = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinition.vue") : {};
const esbDefinitionResponse = (requireComponent != null && requireComponent.keys().length) > 0 ? requireComponent("./apidefinition/EsbDefinitionResponse.vue") : {};
const {diff} = require("@/business/components/performance/v_node_diff");

export default{
  name:"TCPApiVersionDiff",
  components:{
    MsTcpBasicApi,
    MsTcpFormatParameters,
    MsFormDivider,
    ApiInfoContainer,
    TabPaneCount,
    FormRichTextItem,
    DependenciesList,
    "esbDefinition": esbDefinition.default,
    "esbDefinitionResponse": esbDefinitionResponse.default,
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
    moduleOptions:{},
    request: {},
    oldRequest:{},
    mockInfo:String,
    apiProtocol:String,
    oldApiProtocol:String,
    showXpackCompnent:{
      type:Boolean,
      default:false
    },
    methodTypes:Array,
  },
  watch: {
    activeName() {
      if (this.activeName === 'dependencies') {
        this.$refs.oldDependencies.open();
        this.$refs.newDependencies.open();
      }
    },
    relationshipCount(){
      if(this.relationshipCount>0||this.oldRelationshipCount>0){
        this.getChildDiff()
      }
    },
    oldRelationshipCount(){
      if(this.relationshipCount>0||this.oldRelationshipCount>0){
        this.getChildDiff()
      }
    }
  },
  data(){
    return{
      activeName: 'remark',
      relationshipCount: 0,
      oldRelationshipCount: 0,
      isReloadData:true,
      oldColor:"",
      newColor:""
    }
  },
  methods:{
    getDiff(){
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      if(this.oldData.createTime>this.newData.createTime){
        this.oldColor = "rgb(121, 225, 153,0.3)";
        this.newColor = "rgb(241,200,196,0.45)"
      }else{
        this.oldColor = "rgb(241,200,196,0.45)"
        this.newColor = "rgb(121, 225, 153,0.3)";
      }
      diff(oldVnode,vnode,this.oldColor,this.newColor);
      this.isReloadData = false
    },
    setCount(count) {
      this.relationshipCount = count;
      this.$nextTick(function () {
        setTimeout(this.getChildDiff,1000)
      })
    },
    setOldCount(count) {
      this.oldRelationshipCount = count;
    },
    getChildDiff(){
      let oldVnode = this.$refs.oldDependencies
      let vnode = this.$refs.newDependencies
      if(oldVnode._data.postCount>0||oldVnode._data.preCount>0||vnode._data.postCount>0||vnode._data.preCount>0){
        diff(oldVnode,vnode,this.oldColor,this.newColor);
      }
    }
  },
  mounted() {
    getRelationshipCountApi(this.newData.id, (data) => {
      this.relationshipCount = data;
    });
    getRelationshipCountApi(this.oldData.id, (data) => {
      this.oldRelationshipCount = data;
    });
    this.$nextTick(function () {
      setTimeout(this.getDiff,(this.$refs.old.$children.length+1)/2*1000)
    })
  }

}
</script>
<style scoped>
.compare-class{
  display: flex;
  justify-content:space-between;
}
</style>
