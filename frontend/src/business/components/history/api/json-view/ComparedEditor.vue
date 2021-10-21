<template>
  <div id="app" v-loading="loading">
    <div style="width:  98%">
      <compared-editor class="schema" :value="schema" lang="zh_CN" custom/>
    </div>
  </div>
</template>

<script>
const Convert = require('./convert/convert.js');
const MsConvert = new Convert();
export default {
  name: 'App',
  components: {},
  props: {
    body: {},
    showPreview: {
      type: Boolean,
      default: true
    },
  },
  created() {
    if (!this.body.jsonSchema && this.body.raw && this.checkIsJson(this.body.raw)) {
      let obj = {"root": MsConvert.format(JSON.parse(this.body.raw))}
      this.schema = obj;
    } else if (this.body.jsonSchema) {
      this.schema = {"root": this.body.jsonSchema};
    }
    this.body.jsonSchema = this.schema.root;
  },
  watch: {
    schema: {
      handler(newValue, oldValue) {
        this.body.jsonSchema = this.schema.root;
      },
      deep: true
    },
    body: {
      handler(newValue, oldValue) {
        if (!this.body.jsonSchema && this.body.raw && this.checkIsJson(this.body.raw)) {
          let obj = {"root": MsConvert.format(JSON.parse(this.body.raw))}
          this.schema = obj;
        } else if (this.body.jsonSchema) {
          this.schema = {"root": this.body.jsonSchema};
        }
        this.body.jsonSchema = this.schema.root;
      },
      deep: true
    }
  },
  data() {
    return {
      schema:
        {
          "root": {
            "type": "object",
            "properties": {},
          }
        },
      loading: false,
      preview: null,
      activeName: "apiTemplate",
    }
  },
  methods: {
    openOneClickOperation() {
      this.$refs.importJson.openOneClickOperation();
    },
    checkIsJson(json) {
      try {
        JSON.parse(json);
        return true;
      } catch (e) {
        return false;
      }
    },
    jsonData(data) {
      this.schema.root = {};
      this.$nextTick(() => {
        this.schema.root = data;
        this.body.jsonSchema = this.schema.root;
      })
    }
  }
}
</script>
<style>

</style>
