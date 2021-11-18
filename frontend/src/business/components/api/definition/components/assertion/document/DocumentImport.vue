<template>
  <el-dialog
    :title="$t('commons.import') + document.type"
    :visible.sync="importVisible"
    width="50%"
    append-to-body
    show-close
    :close-on-click-modal="false"
    @closed="handleClose">
    <div style="height: 400px">
      <ms-code-edit
        :mode="mode"
        :data.sync="json" theme="eclipse" :modes="[]"
        ref="codeEdit"/>
    </div>
    <span slot="footer" class="dialog-footer">
     <ms-dialog-footer
       @cancel="importVisible = false"
       @confirm="saveConfirm"/>
    </span>
  </el-dialog>
</template>

<script>
import MsDialogFooter from '../../../../../common/components/MsDialogFooter'
import MsCodeEdit from "../../../../../common/components/MsCodeEdit";
import json5 from 'json5';
import MsConvert from "@/business/components/common/json-schema/convert/convert";

export default {
  name: "MsDocumentImport",
  components: {MsDialogFooter, MsCodeEdit},
  data() {
    return {
      importVisible: false,
      activeName: "JSON",
      mode: "json",
      json: "",
    };
  },
  watch: {},
  props: {
    document: {}
  },
  created() {
  },
  methods: {
    openOneClickOperation() {
      this.mode = this.document.type.toLowerCase();
      this.importVisible = true;
    },
    checkIsJson(json) {
      try {
        json5.parse(json);
        return true;
      } catch (e) {
        return false;
      }
    },
    /*
    * 验证xml格式的正确性
    */
    validateXML(xmlContent) {
      //errorCode 0是xml正确，1是xml错误，2是无法验证
      let xmlDoc, errorMessage, errorCode = 0;
      if (document.implementation.createDocument) {
        let parser = new DOMParser();
        xmlDoc = parser.parseFromString(xmlContent, "text/xml");
        let error = xmlDoc.getElementsByTagName("parsererror");
        if (error.length > 0) {
          if (xmlDoc.documentElement.nodeName == "parsererror") {
            errorCode = 1;
            errorMessage = xmlDoc.documentElement.childNodes[0].nodeValue;
          } else {
            errorCode = 1;
            errorMessage = xmlDoc.getElementsByTagName("parsererror")[0].innerHTML;
          }
        } else {
          errorMessage = "格式正确";
        }
      } else {
        errorCode = 2;
        errorMessage = "浏览器不支持验证，无法验证xml正确性";
      }
      return {
        "msg": errorMessage,
        "error_code": errorCode
      };
    },
    saveConfirm() {
      if (this.document.type === "JSON" && !this.checkIsJson(this.json)) {
        this.$error("导入的数据非JSON格式");
        return;
      }
      if (this.document.type === "XML" && this.validateXML(this.json).error_code > 0) {
        this.$error("导入的数据非XML格式");
        return;
      }
      let url = "/api/definition/jsonGenerator";
      this.$post(url, {raw: this.json, type: this.document.type}, response => {
        if (response.data) {
          this.$emit('setJSONData', response.data);
        }
      });
      this.importVisible = false;
    },
    handleClose() {
      this.importVisible = false;
    },
  }
}
</script>

<style scoped>

</style>
