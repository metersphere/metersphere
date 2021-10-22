<template>

  <el-dialog :append-to-body="appendToBody"
    :visible.sync="dialogVisible" destroy-on-close @close="close">
    <div style="padding: 10px">
      <el-switch active-text="JSON-SCHEMA" v-model="item.jsonType" @change="formatChange" active-value="JSON-SCHEMA"/>
    </div>
    <div v-if="codeEditActive">
      <ms-json-code-edit
        v-if="item.jsonType==='JSON-SCHEMA'"
        :body="item"
        ref="jsonCodeEdit"/>
      <ms-code-edit
        v-else
        :read-only="isReadOnly"
        :data.sync="item.value"
        :mode="'json'"
        height="400px"
        ref="codeEdit"/>
    </div>
  </el-dialog>
</template>

<script>

import MsCodeEdit from "../../../common/components/MsCodeEdit";
import Convert from "@/business/components/common/json-schema/convert/convert";
import MsJsonCodeEdit from "../../../common/json-schema/JsonSchemaEditor";


export default {
  name: "MsApiVariableJson",
  components: {MsJsonCodeEdit, MsCodeEdit},
  props: {
    isReadOnly: {
      type: Boolean,
      default: false
    },
    appendToBody: {
      type: Boolean,
      default() {
        return false;
      }
    },
  },
  data() {
    return {
      dialogVisible: false,
      jsonSchema: "JSON",
      codeEditActive: true,
      item: {}
    }
  },
  watch: {
    'item.value'() {
      if (this.item.jsonType !== 'JSON-SCHEMA' && this.item.value) {
        try {
          const MsConvert = new Convert();
          let data = MsConvert.format(JSON.parse(this.item.value));
          if (this.item.jsonSchema) {
            this.item.jsonSchema = this.deepAssign(this.item.jsonSchema, data);
          }
        } catch (ex) {
          this.item.jsonSchema = "";
        }
      }
    }
  },

  methods: {
    open(item) {
      this.item.value = item.value;
      this.dialogVisible = true;
      this.reloadCodeEdit();
    },
    reloadCodeEdit() {
      this.codeEditActive = false;
      this.$nextTick(() => {
        this.codeEditActive = true;
      });
    },
    formatChange() {
      const MsConvert = new Convert();
      if (this.item.jsonType === 'JSON-SCHEMA') {
        if (this.item.value && !this.item.jsonSchema) {
          this.item.jsonSchema = MsConvert.format(JSON.parse(this.item.value));
        }
      } else {
        if (this.item.jsonSchema) {
          MsConvert.schemaToJsonStr(this.item.jsonSchema, (result) => {
            this.$set(this.item, 'value', result);
            this.$emit('callback', result);
          });
        }
      }
    },
    saveAdvanced() {
      this.dialogVisible = false;
      if (this.item.jsonType === 'JSON-SCHEMA') {
        this.item.jsonType = 'JSON';
        this.formatChange();
      } else {
        this.$emit('callback', this.item.value);
      }
      this.item = {};
      this.reloadCodeEdit();
    },
    close(){
      this.saveAdvanced();
    }
  },

}
</script>
