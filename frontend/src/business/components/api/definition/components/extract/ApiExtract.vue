<template>
  <div :style="customizeStyle" v-loading="loading">
    <el-card>
      <div class="el-step__icon is-text" style="color: #015478;background-color: #E6EEF2;margin-right: 10px" v-if="extract.index">
        <div class="el-step__icon-inner">{{extract.index}}</div>
      </div>
      <el-button class="ms-left-buttion" size="small" style="color: #015478;background-color: #E6EEF2">{{$t('api_test.definition.request.extract_param')}}</el-button>
      <el-input size="small" v-model="extract.name" style="width: 40%;margin-left: 20px" :placeholder="$t('commons.input_name')"/>
      <div style="margin-right: 20px; float: right">
        <i class="icon el-icon-arrow-right" :class="{'is-active': extract.active}" @click="active(extract)" style="margin-left: 20px"/>
        <el-switch v-model="extract.enable" style="margin-left: 10px"/>
        <el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow" style="margin-left: 10px"/>
        <el-button size="mini" icon="el-icon-delete" type="danger" circle @click="remove" style="margin-left: 10px"/>
      </div>
      <!-- 请求参数-->
      <el-collapse-transition>
        <div v-if="extract.active">
          <div style="margin: 20px">
            <div class="extract-description">
              {{$t('api_test.request.extract.description')}}
            </div>
            <div class="extract-add">
              <el-row :gutter="10">
                <el-col :span="2">
                  <el-select :disabled="isReadOnly" class="extract-item" v-model="type" :placeholder="$t('api_test.request.extract.select_type')"
                             size="small">
                    <el-option :label="$t('api_test.request.extract.regex')" :value="options.REGEX"/>
                    <el-option label="JSONPath" :value="options.JSON_PATH"/>
                    <el-option label="XPath" :value="options.XPATH"/>
                  </el-select>
                </el-col>
                <el-col :span="22">
                  <ms-api-extract-common :is-read-only="isReadOnly" :extract-type="type" :list="list" v-if="type" :callback="after"/>
                </el-col>

                <el-button v-if="!type" :disabled="true" type="primary" size="small">Add</el-button>
              </el-row>
            </div>
            <ms-api-extract-edit :is-read-only="isReadOnly" :reloadData="reloadData" :extract="extract"/>
          </div>
        </div>
      </el-collapse-transition>
    </el-card>
  </div>
</template>

<script>
  import {EXTRACT_TYPE} from "../../model/ApiTestModel";
  import MsApiExtractEdit from "./ApiExtractEdit";
  import MsApiExtractCommon from "./ApiExtractCommon";
  import {getUUID} from "@/common/js/utils";

  export default {
    name: "MsApiExtract",

    components: {
      MsApiExtractCommon,
      MsApiExtractEdit,
    },

    props: {
      extract: {},
      node: {},
      customizeStyle: {
        type: String,
        default: "margin-top: 10px"
      },
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        options: EXTRACT_TYPE,
        type: "",
        reloadData: "",
        loading: false,
      }
    },

    methods: {
      after() {
        this.type = "";
        this.reloadData = getUUID().substring(0, 8);
      },
      remove() {
        this.$emit('remove', this.extract, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.extract, this.node);
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      active(item) {
        item.active = !item.active;
        this.reload();
      },
    },
    computed: {
      list() {
        switch (this.type) {
          case EXTRACT_TYPE.REGEX:
            return this.extract.regex;
          case EXTRACT_TYPE.JSON_PATH:
            return this.extract.json;
          case EXTRACT_TYPE.XPATH:
            return this.extract.xpath;
          default:
            return [];
        }
      }
    }
  }
</script>

<style scoped>
  .extract-description {
    font-size: 13px;
    margin-bottom: 10px;
  }

  .extract-item {
    width: 100%;
  }

  .extract-add {
    padding: 10px;
    border: #DCDFE6 solid 1px;
    margin: 5px 0;
    border-radius: 5px;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

  /deep/ .el-card__body {
    padding: 15px;
  }
</style>
