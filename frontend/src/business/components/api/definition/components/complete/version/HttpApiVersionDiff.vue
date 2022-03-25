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
        <el-form :model="oldData" :rules="rule" ref="httpForm" label-width="80px" label-position="right" >
          <!-- 操作按钮 -->
          <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

          <!-- 基础信息 -->
          <div class="base-info">
            <el-row>
              <el-col :span="8">
                <el-form-item :label="$t('commons.name')" prop="name">
                  <el-input class="ms-http-input" size="small" v-model="oldData.name" :disabled="true"/>
                </el-form-item>
              </el-col>
              <el-col :span="16">
                <el-form-item :label="$t('api_report.request')" prop="path">
                  <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="oldData.path" :disabled="true"
                            class="ms-http-input" size="small" style="margin-top: 5px" >
                    <el-select v-model="oldData.method" slot="prepend" style="width: 100px" size="small" :disabled="true">
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
                             :placeholder="$t('api_test.definition.request.responsible')" filterable size="small" :disabled="true"
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
                  <el-select class="ms-http-select" size="small" v-model="oldData.status" :disabled="true">
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
                            :rows="1" size="small" :disabled="true"/>
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
                <el-link  target="_blank" style="color: black"
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

      </el-card>
      <el-card style="width: 50%;" ref="new">
        <el-form :model="newData" :rules="rule" ref="httpForm" label-width="80px" label-position="right" >
          <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

          <!-- 基础信息 -->
          <div class="base-info">

            <el-row>
              <el-col :span="8">
                <el-form-item :label="$t('commons.name')"  prop="name">
                  <el-input class="ms-http-input" size="small" v-model="newData.name" :disabled="true"/>
                </el-form-item>
              </el-col>
              <el-col :span="16">
                <el-form-item :label="$t('api_report.request')" prop="path">
                  <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="newData.path" :disabled="true"
                            class="ms-http-input" size="small" style="margin-top: 5px" >
                    <el-select v-model="newData.method" slot="prepend" style="width: 100px" size="small" :disabled="true">
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
                             :placeholder="$t('api_test.definition.request.responsible')" filterable size="small" :disabled="true"
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
                  <el-select class="ms-http-select" size="small" v-model="newData.status" :disabled="true">
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
                            :rows="1" size="small" :disabled="true"/>
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
                <el-link  target="_blank" style="color: black"
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
      </el-card>
    </div>
  </div>
</template>
<script>
import {API_STATUS, REQ_METHOD} from "../../../model/JsonData";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import MsResponseText from "../../response/ResponseText";
import MsApiRequestForm from "../../request/http/ApiHttpRequestForm";
import MsSelectTree from "../../../../../common/select-tree/SelectTree";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import ApiInfoContainer from "@/business/components/api/definition/components/complete/ApiInfoContainer";
import DependenciesList from "@/business/components/common/components/graph/DependenciesList";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import {hasPermissions} from "@/common/js/utils";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";
import {getRelationshipCountApi} from "@/network/api";

const {diff} = require("@/business/components/performance/v_node_diff");

export default{
  name: "HttpApiVersionDiff",
  components: {
    MsFormDivider,
    MsResponseText,
    MsApiRequestForm,
    MsInputTag,
    MsSelectTree,
    TabPaneCount,
    FormRichTextItem,
    DependenciesList,
    ApiInfoContainer
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
  computed: {
    hasPermissions() {
      return hasPermissions('PROJECT_API_DEFINITION:READ+EDIT_API');
    }
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
      reqOptions: REQ_METHOD,
      options: API_STATUS,
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      isShowEnable: true,
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
      console.log(this.$refs.old)
      console.log(this.$refs.new)
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
