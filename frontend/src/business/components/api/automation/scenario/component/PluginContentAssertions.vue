<template>
  <div :draggable="draggable" style="margin-left: 60px">
    <el-row :gutter="10">
      <el-col :span="8">
        <span>{{ this.$t('api_report.assertions_content') }}</span>
        <el-select :disabled="isReadOnly" v-model="value.contentType"
                   style="margin-left: 10px; width: 120px;height: 32px;"
                   :placeholder="$t('api_test.request.assertions.select_type')" size="small">
          <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
          <el-option :label="'JSONPath'" :value="options.JSON_PATH"/>
          <el-option :label="'XPath'" :value="options.XPATH2"/>
        </el-select>
        <el-button type="primary" size="mini" @click="add" style="margin-left: 20px">
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
      <el-col :span="8" :offset="3">
        <span>{{ this.$t('api_test.request.sql.timeout') }}</span>
        <el-input size="mini" style="width: 100px; margin-left: 10px" v-model="value.timeOut"></el-input>
      </el-col>
    </el-row>
    <el-row>
      <el-container>
        <el-aside width="73px" style="overflow: hidden" v-if="value.list.length > 1">
          <div style="height: 100%" id="moreOptionTypeDiv">
            <div class="ms-top-line-box"
                 :style="{ height:lineDivTopHeight+'px',marginTop:lineDivMarginTopHeight+'px'}">
            </div>
            <div>
              <el-select class="ms-http-select" size="small" v-model="value.filterType" style="width: 70px">
                <el-option v-for="item in filterTypes" :key="item.id" :label="item.label" :value="item.id"/>
              </el-select>
            </div>
            <div class="ms-bottom-line-box" :style="{ height:lineDivBottomHeight+'px'}">
            </div>
          </div>
        </el-aside>
        <el-main style="padding: 0px 20px 0px 0px; overflow: hidden">

          <div v-for="(data,index) in value.list" :key="index">
            <el-col name="itemOptions">
              <div v-if="data.type === options.TEXT" class="assertion-item-editing">
                <el-row :gutter="10" type="flex" justify="space-between" align="middle">
                  <el-col class="ms-assertion-select">
                    <el-select :disabled="true" style="width: 120px;height: 32px;" v-model="data.type"
                               :placeholder="$t('api_test.request.assertions.select_type')" size="small">
                      <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
                      <el-option :label="'JSONPath'" :value="options.JSON_PATH"/>
                      <el-option :label="'XPath'" :value="options.XPATH2"/>
                    </el-select>
                  </el-col>
                  <el-col class="ms-assertion-select">
                    <el-select :disabled="isReadOnly" style="width: 120px;height: 32px;" v-model="data.option"
                               size="small"
                               :placeholder="$t('api_test.request.assertions.select_condition')">
                      <el-option :label="$t('api_test.request.assertions.contains')" value="CONTAINS"/>
                      <el-option :label="$t('api_test.request.assertions.not_contains')" value="NOT_CONTAINS"/>
                      <el-option :label="$t('api_test.request.assertions.equals')" value="EQUALS"/>
                      <el-option :label="$t('api_test.request.assertions.start_with')" value="START_WITH"/>
                      <el-option :label="$t('api_test.request.assertions.end_with')" value="END_WITH"/>
                    </el-select>
                  </el-col>
                  <el-col>
                    <el-input :disabled="isReadOnly" v-model="data.value" maxlength="500" size="small" show-word-limit
                              :placeholder="$t('api_test.request.assertions.value')" width="500px"/>
                  </el-col>
                  <el-col class="assertion-btn">
                    <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top">
                      <el-switch v-model="data.enable" class="enable-switch" size="mini" :disabled="isReadOnly"
                                 style="width: 30px;margin-right:10px"/>
                    </el-tooltip>

                    <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle
                               @click="remove(data)"
                    />
                  </el-col>
                </el-row>
              </div>

              <div v-if="data.type === options.JSON_PATH " class="assertion-item-editing">
                <el-row :gutter="10" type="flex" justify="space-between" align="middle">
                  <el-col class="ms-assertion-select">
                    <el-select :disabled="true" style="width: 120px;height: 32px;" v-model="data.type"
                               :placeholder="$t('api_test.request.assertions.select_type')" size="small">
                      <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
                      <el-option :label="'JSONPath'" :value="options.JSON_PATH"/>
                      <el-option :label="'XPath'" :value="options.XPATH2"/>
                    </el-select>
                  </el-col>
                  <el-col>
                    <el-input :disabled="isReadOnly" v-model="data.expression" maxlength="500" size="small"
                              show-word-limit
                              :placeholder="$t('api_test.request.extract.json_path_expression')"/>
                  </el-col>
                  <el-col>
                    <el-select v-model="data.option" class="ms-col-type" size="small"
                               style="width:100px;margin-right: 10px" @change="reload">
                      <el-option :label="$t('api_test.request.assertions.contains')" value="CONTAINS"/>
                      <el-option :label="$t('api_test.request.assertions.not_contains')" value="NOT_CONTAINS"/>
                      <el-option :label="$t('api_test.request.assertions.equals')" value="EQUALS"/>
                      <el-option :label="$t('commons.adv_search.operators.not_equals')" value="NOT_EQUALS"/>
                      <el-option :label="$t('commons.adv_search.operators.gt')" value="GT"/>
                      <el-option :label="$t('commons.adv_search.operators.lt')" value="LT"/>
                      <el-option :label="$t('api_test.request.assertions.regular_match')" value="REGEX"/>
                    </el-select>
                    <el-input :disabled="isReadOnly" v-model="data.expect" size="small" show-word-limit
                              :placeholder="$t('api_test.request.assertions.expect')" style="width: 50%"/>
                    <el-tooltip placement="top" v-if="data.option === 'REGEX'">
                      <div slot="content">{{ $t('api_test.request.assertions.regex_info') }}</div>
                      <i class="el-icon-question" style="cursor: pointer"/>
                    </el-tooltip>
                  </el-col>
                  <el-col class="assertion-btn">
                    <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top">
                      <el-switch v-model="data.enable" class="enable-switch" size="mini" :disabled="isReadOnly"
                                 style="width: 30px;margin-right: 10px"/>
                    </el-tooltip>
                    <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle
                               @click="remove(data)"/>
                  </el-col>
                </el-row>
              </div>

              <div v-if="data.type === options.XPATH2 " class="assertion-item-editing">
                <el-row :gutter="10" type="flex" justify="space-between" align="middle">
                  <el-col class="ms-assertion-select">
                    <el-select :disabled="true" style="width: 120px;height: 32px;" v-model="data.type"
                               :placeholder="$t('api_test.request.assertions.select_type')" size="small">
                      <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
                      <el-option :label="'JSONPath'" :value="options.JSON_PATH"/>
                      <el-option :label="'XPath'" :value="options.XPATH2"/>
                    </el-select>
                  </el-col>
                  <el-col>
                    <el-input :disabled="isReadOnly" v-model="data.expression" maxlength="500" size="small"
                              show-word-limit
                              :placeholder="$t('api_test.request.extract.xpath_expression')"/>
                  </el-col>
                  <el-col class="assertion-btn">
                    <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top">
                      <el-switch v-model="data.enable" class="enable-switch" size="mini" :disabled="isReadOnly"
                                 style="width: 30px;margin-right:10px"/>
                    </el-tooltip>
                    <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle
                               @click="remove(data)"/>
                  </el-col>
                </el-row>
              </div>
            </el-col>
          </div>
        </el-main>
      </el-container>
    </el-row>

  </div>


</template>
<script>


import MsApiAssertionJsonPath from "@/business/components/api/definition/components/assertion/ApiAssertionJsonPath";
import MsApiAssertionXPath2 from "@/business/components/api/definition/components/assertion/ApiAssertionXPath2";
import {
  ASSERTION_TYPE,
  Regex
} from "@/business/components/api/definition/model/ApiTestModel";

export default {
  name: "MsPluginContentAssertions",
  components: {
    MsApiAssertionXPath2,
    MsApiAssertionJsonPath,
  },
  props: {
    draggable: {
      type: Boolean,
      default: false,
    },
    value: Object
  },
  data() {
    return {
      isReadOnly: false,
      options: ASSERTION_TYPE,
      loading: false,
      filterTypes: [
        {id: 'And', label: 'And'},
        {id: 'Or', label: 'Or'},
      ],
      jsonPath: {
        expression: '',
        enable: true,
        type: ASSERTION_TYPE.JSON_PATH,
        option: 'REGEX',
        expect: ''
      },
      xPath2: {
        expression: '',
        enable: true,
        type: ASSERTION_TYPE.XPATH2
      },
      text: {
        enable: true,
        type: ASSERTION_TYPE.TEXT,
        option: "",
        value: "",
      },
      lineDivTopHeight: 0,
      lineDivMarginTopHeight: 0,
      lineDivBottomHeight: 0,
      h: document.documentElement.clientHeight + 80,
    }
  },
  watch: {
    value: {
      handler() {
        this.getAsideStyle();
      },
      deep: true
    }
  },
  created() {
    this.getAsideStyle();
  },
  methods: {
    getAsideStyle: function () {
      this.$nextTick(() => {
        this.lineDivHeight = 0;
        setTimeout(() => {
          let itemOptions = document.getElementsByName("itemOptions");
          if (itemOptions && itemOptions.length > 1) {
            let optionTypeHeight = 0;
            for (let i = 0; i < itemOptions.length; i++) {
              let itemHeight = itemOptions[i].offsetHeight;
              optionTypeHeight += itemHeight;
            }
            this.lineDivMarginTopHeight = 26;
            this.lineDivTopHeight = (optionTypeHeight / 2 - 32 - 5);
            this.lineDivBottomHeight = this.lineDivTopHeight;
          }
        }, 100);
      });
    },
    add() {
      if (this.value.contentType === this.options.TEXT) {
        this.value.list.push({
          enable: true,
          type: ASSERTION_TYPE.TEXT,
          option: "",
          value: "",
        });
      }
      if (this.value.contentType === this.options.JSON_PATH) {
        this.value.list.push({
          expression: '',
          enable: true,
          type: ASSERTION_TYPE.JSON_PATH,
          option: 'REGEX',
          expect: ''
        });
      }
      if (this.value.contentType === this.options.XPATH2) {
        this.value.list.push({
          expression: '',
          enable: true,
          type: ASSERTION_TYPE.XPATH2
        });
      }
      this.value.contentType = '';
    },

    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    remove(data) {
      let index = this.value.list.indexOf(data);
      this.value.list.splice(index, 1);
    },

  }
}
</script>

<style scoped>
.ms-assertion-item {
  width: 100%;
  height: 32px;
}

.ms-top-line-box {
  border-top: 1px solid;
  border-left: 1px solid;
  margin-left: 32px;
  border-top-left-radius: 10px;
}

.ms-bottom-line-box {
  border-bottom: 1px solid;
  border-left: 1px solid;
  margin-left: 32px;
  border-bottom-left-radius: 10px;
}


.assertion-item-editing {
  margin-top: 10px;
}

/deep/ .ms-assertion-select {
  width: 140px;
}

.enable-switch {
  margin-right: 10px;
}

.assertion-btn {
  width: 60px;
}

</style>
