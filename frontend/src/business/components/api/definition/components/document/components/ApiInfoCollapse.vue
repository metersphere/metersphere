<template>
  <div>
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        <el-row>
          <div class="tip" style="float: left">
            <span>{{ title }}</span>
            <span v-if="remarks" style="font-weight: 400;font-size: smaller;background: #F1F0F0;margin-left: 5px;">
              {{ remarks }}
            </span>
          </div>
          <div class="right-button">
            <el-link v-if="active" @click="changeActive">
              {{ $t('api_test.definition.document.close') }} <i class="el-icon-arrow-up"/>
            </el-link>
            <el-link v-if="!active" @click="changeActive">
              {{ $t('api_test.definition.document.open') }} <i class="el-icon-arrow-down"/>
            </el-link>
          </div>
        </el-row>
        <el-collapse-transition>
          <div v-show="active">
            <div v-if="!showSlotCompnent">
              <div v-if="isText">
                <div class="showDataDiv">
                  <br/>
                  <p style="margin: 0px 20px;"
                     v-html="stringData">
                  </p>
                  <br/>
                </div>
              </div>
              <div v-else-if="getJsonArr(stringData).length===0">
                <div class="simpleFontClass" style="margin-top: 10px">
                  {{ $t('api_test.definition.document.data_set.none') }}
                </div>
              </div>
              <div v-else>
                <el-table border :show-header="true"
                          :data="getJsonArr(stringData)" class="test-content document-table">
                  <el-table-column v-for="item in tableColoumArr" :key="item.id"
                                   :prop="item.prop"
                                   :label="item.label"
                                   show-overflow-tooltip/>
                </el-table>
              </div>
            </div>
            <slot name="request"></slot>
            <slot name="response"></slot>
          </div>
        </el-collapse-transition>
      </div>
    </el-row>
  </div>
</template>

<script>

export default {
  name: "ApiInfoCollapse",
  components: {},
  data() {
    return {
      active: true,
      tableColoumArr: [
        {id: 1, prop: "name", label: this.$t('api_test.definition.document.table_coloum.name')},
        {id: 2, prop: "isRequired", label: this.$t('api_test.definition.document.table_coloum.is_required')},
        {id: 3, prop: "value", label: this.$t('api_test.definition.document.table_coloum.value')},
        {id: 4, prop: "description", label: this.$t('api_test.definition.document.table_coloum.desc')},
      ],
    };
  },
  props: {
    title: String,
    tableColoumType: String,
    remarks: String,
    isRequest: Boolean,
    isResponse: Boolean,
    isText:Boolean,
    stringData: {
      type: String,
      default() {
        return "{}";
      }
    },
  },
  activated() {
  },
  created: function () {
  },
  mounted() {
  },
  computed: {
    showSlotCompnent() {
      return this.isRequest || this.isResponse;
    }
  },
  watch: {},
  methods: {
    getJsonArr(jsonString) {
      let returnJsonArr = [];
      if (jsonString === '无' || jsonString === null) {
        return returnJsonArr;
      }
      let jsonArr = JSON.parse(jsonString);
      //遍历，把必填项空的数据去掉
      for (var index = 0; index < jsonArr.length; index++) {
        var item = jsonArr[index];
        if (item.name !== "" && item.name !== null) {
          if (item.required) {
            item.isRequired = "true";
          } else {
            item.isRequired = "false";
          }
          returnJsonArr.push(item);
        }
      }
      return returnJsonArr;
    },
    changeActive() {
      this.active = !this.active;
    },
  },
};
</script>

<style scoped>

.apiInfoRow {
  margin: 10px 10px;
}

.apiInfoRow.el-row {
  margin: 10px 10px;
}

.simpleFontClass {
  font-weight: normal;
  font-size: 14px;
  margin-left: 10px;
}

.blackFontClass {
  font-weight: bold;
  font-size: 14px;
}

.document-table {
  margin: 10px 0px 10px 10px;
  width: auto;
}

.document-table /deep/ .el-table__row {
  font-size: 12px;
  font-weight: initial;
}

.document-table /deep/ .has-gutter {
  font-size: 12px;
  color: #404040;
}

.document-table /deep/ td {
  border-right: 0px solid #EBEEF5
}

.document-table /deep/ th {
  background-color: #FAFAFA;
  border-right: 0px solid #EBEEF5
}

.right-button {
  float: right;
}

.showDataDiv {
  background-color: #F5F7F9;
  margin: 10px 10px;
  max-height: 300px;
  overflow: auto;
}

</style>
