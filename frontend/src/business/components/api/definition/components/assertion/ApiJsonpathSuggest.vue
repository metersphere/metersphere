<template>
  <ms-drawer class="json-path-picker" :visible="visible" :size="30" @close="close" direction="right">
    <template v-slot:header>
      <ms-instructions-icon :content="tip"/>
      {{tip}}
    </template>
    <jsonpath-picker :code="data" v-on:path="pathChangeHandler" ref="jsonpathPicker"/>
  </ms-drawer>
</template>

<script>
  import MsDrawer from "../../../../common/components/MsDrawer";
  import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";

  export default {
    name: "MsApiJsonpathSuggest",
    components: {MsInstructionsIcon, MsDrawer},
    data() {
      return {
        visible: false,
        isCheckAll: false,
      };
    },
    props: {
      data: {},
      tip: {
        type: String,
        default() {
          return ""
        }
      }
    },
    methods: {
      close() {
        this.visible = false;
      },
      open() {
        this.visible = true;
      },
      pathChangeHandler(data) {
        let paramNames = data.split('.');
        let result = this.getParamValue(this.data, 0, paramNames);
        result.path = '$.' + data;
        this.$emit('addSuggest', result);
      },
      getParamValue(obj, index, params) {
        if (params.length < 1) {
          return "";
        }

        let param = params[index];
        let childObj;

        let reg = /\[\d\]$/;
        let regIndex = param.search(reg);
        if (regIndex > -1) {
          let paramName = param.substring(0, regIndex);
          let paramIndex = param.substring(regIndex + 1, param.length - 1);
          param =  paramIndex;
          childObj = obj[paramName][paramIndex];
        } else {
          childObj = obj[params[index]];
        }
        if (index === params.length - 1) {
          if (childObj instanceof Object) {
            childObj = JSON.stringify(childObj);
          } else if (childObj == null) {
            childObj = "null";
          }
          return {
            key: param,
            value: childObj
          };
        }
        index++;
        return this.getParamValue(childObj, index, params);
      }
    }
  }
</script>

<style scoped>

  .json-path-picker {
    padding: 10px 13px;
  }

  .json-path-picker >>> .json-tree {
    margin-top: 0px;
    margin-left: 6px;
  }

</style>
