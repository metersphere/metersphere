<template>
  <div v-if="remarkObj">
    <el-row class="apiInfoRow">
      <div class="blackFontClass">
        <el-row>
          <div class="tip" style="float: left">
            <span>{{$t("commons.remark")}}</span>
          </div>
        </el-row>
      </div>
    </el-row>
    <el-row>
      <el-form>
        <form-rich-text-item class="remark-item" :disabled="true" :data="remarkObj" prop="remark" label-width="0"/>
      </el-form>
    </el-row>
  </div>
</template>

<script>
import formRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
export default {
  name: "ApiRemarkShow",
  components: {formRichTextItem},
  data() {
    return {
      active: true,
      remarkObj:null
    };
  },
  props: {
    data: String,
  },
  created() {
  },
  activated() {
  },
  mounted() {
  },
  computed: {},
  watch: {
    data(){
      if(this.data){
        this.remarkObj = {
          "remark":this.data,
        };
      }
    }
  },
  methods: {},
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
</style>
