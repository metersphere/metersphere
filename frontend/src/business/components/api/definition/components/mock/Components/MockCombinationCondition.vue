<template>
  <div>
    <el-container>
      <el-aside width="110px" style="overflow: hidden">
        <div v-if="parameters && parameters.length > 1" style="height: 100%" id="moreOptionTypeDiv">
          <div class="top-line-box" :style="{ height:lineDivTopHeight+'px',marginTop:lineDivMarginTopHeight+'px'}">
          </div>
          <div>
            <el-select class="ms-http-select" size="small" v-model="filterTypeObject.paramsFilterType"
                       style="width: 100px">
              <el-option v-for="item in filterTypes" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </div>
          <div class="bottom-line-box" :style="{ height:lineDivBottomHeight+'px'}">
          </div>
        </div>
      </el-aside>
      <el-main style="padding: 0px">
        <mock-api-variable ref="mockApiVariableComp" :append-dialog-to-body="true"
                           :suggestions="suggestions"
                           :with-mor-setting="true"
                           :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="parameters"/>
      </el-main>
    </el-container>
  </div>
</template>

<script>

import MockApiVariable from "@/business/components/api/definition/components/mock/Components/MockApiVariable";

export default {
  name: "CombinationCondition",
  components: {MockApiVariable},
  data() {
    return {
      lineDivTopHeight: 0,
      lineDivMarginTopHeight: 0,
      lineDivBottomHeight: 0,
      filterTypes: [
        {id: 'And', label: 'AND'},
        {id: 'Or', label: 'OR'},
      ],
    }
  },
  props: {
    suggestions: Array,
    parameters: Array,
    filterTypeObject: Object,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    isShowEnable: {
      type: Boolean,
      default: true
    },
  },
  created() {
  },
  mounted() {
    this.initFilterDiv();
  },
  watch: {
    parameters: {
      handler: function () {
        this.$nextTick(() => {
          setTimeout(() => {
            this.initFilterDiv();
          }, 100);
        });
      },
      deep: true
    }
  },
  methods: {
    initFilterDiv() {
      if (this.parameters && this.parameters.length > 1) {
        this.lineDivHeight = 0;
        let itemHeigh = 32 + 10;

        let optionTypeHeight = this.parameters.length * itemHeigh;
        let firstHeight = 32;
        let endHeight = 32;

        let marginTopHeight = ((firstHeight - 32) / 2 + 21);
        let topHeightLine = ((optionTypeHeight / 2 - marginTopHeight - 16));
        let divMarginBottom = ((endHeight - 32) / 2 + 16);
        let bottomHeight = optionTypeHeight - 32 - (topHeightLine + marginTopHeight + divMarginBottom);

        this.lineDivTopHeight = topHeightLine;
        this.lineDivMarginTopHeight = marginTopHeight;
        this.lineDivBottomHeight = (bottomHeight > 0 ? bottomHeight : 0);
      }
    }
  }

}
</script>

<style scoped>
.top-line-box {
  border-top: 1px solid;
  border-left: 1px solid;
  margin-left: 32px;
  border-top-left-radius: 10px;
}

.bottom-line-box {
  border-bottom: 1px solid;
  border-left: 1px solid;
  margin-left: 32px;
  border-bottom-left-radius: 10px;
}
</style>
