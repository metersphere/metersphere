<template>
  <div v-loading="loading">
<!--    <el-card :style="{height: h + 'px'}" class="ms-card">-->
    <el-card class="ms-card">
      <el-row style="padding-top: 10px">
        <p class="tip"><span style="margin-left: 5px"></span> {{ $t('commons.report_statistics.options') }}</p>
      </el-row>
      <el-row class="ms-row">
        <p>{{ $t('commons.report_statistics.report_filter.xaxis') }}</p>
        <el-select class="ms-http-select" size="small" v-model="option.xaxis" style="width: 100%">
          <el-option v-for="item in xAxisOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-row>
      <el-row class="ms-row">
        <p>{{ $t('commons.report_statistics.report_filter.yaxis') }}</p>
        <el-select class="ms-http-select" size="small" v-model="option.yaxis" multiple style="width: 100%">
          <el-option v-for="item in yAxisOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-row>
      <el-row class="ms-row">
        <p>{{ $t('commons.create_time')}}</p>
        <div style="width: 25%;float: left">
          <el-select class="ms-http-select" size="small" v-model="option.timeType" >
            <el-option v-for="item in timeTypeOptions" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </div>
        <div v-if="option.timeType === 'fixedTime'" style="width: 70%;margin-left: 20px;float: left">
          <el-date-picker
            size="small"
            v-model="option.times"
            align="right"
            type="datetimerange"
            value-format="timestamp"
            :range-separator="$t('api_monitor.to')"
            :start-placeholder="$t('commons.date.start_date')"
            :end-placeholder="$t('commons.date.end_date')"
            style="margin-right: 10px;width: 100%">
          </el-date-picker>
        </div>
        <div v-if="option.timeType === 'dynamicTime'" style="width: 70%;margin-left: 20px;float: left">
          <span style="width: 20%">{{ $t('commons.report_statistics.report_filter.recently') }}</span>
          <el-select class="ms-http-select" size="small" v-model="option.timeFilter.timeRange" style="width: 30%;margin-left: 10px;width: 40%">
            <el-option v-for="item in timeRangeNumberMax" :key="item" :label="item" :value="item"/>
          </el-select>
          <el-select class="ms-http-select" size="small" v-model="option.timeFilter.timeRangeUnit"
                     @change="timeRangeUnitChange"
                     style="width: 30%;margin-left: 10px;width: 40%">
            <el-option v-for="item in timeRangeUnitOptions" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </div>
      </el-row>
      <el-row class="ms-row" style="margin-left: 0px;margin-right: 0px; margin-top: 20px">
        <el-collapse v-model="collapseActiveNames">
          <el-collapse-item :title="$t('commons.report_statistics.report_filter.more_options')" name="1">
            <el-container>
              <el-aside width="73px" style="overflow: hidden">
                <div v-if="option.filters.length > 1" style="height: 100%" id="moreOptionTypeDiv">
                  <div class="top-line-box" :style="{ height:lineDivTopHeight+'px',marginTop:lineDivMarginTopHeight+'px'}">
                  </div>
                  <div>
                    <el-select class="ms-http-select" size="small" v-model="option.filterType" style="width: 70px">
                      <el-option v-for="item in filterTypes" :key="item.id" :label="item.label" :value="item.id"/>
                    </el-select>
                  </div>
                  <div class="bottom-line-box" :style="{ height:lineDivBottomHeight+'px'}">
                  </div>
                </div>
              </el-aside>
              <el-main  v-if="optionLoad" style="padding: 0px">
                <el-row v-for="filterItem in option.filters" :key="filterItem.id" style="margin-bottom: 5px">
                  <el-col :span="24" name="itemOptions">
                    <el-select style="width: 100px" class="ms-http-select" size="small" v-model="filterItem.type">
                      <el-option v-for="item in getFilterOptionKey(filterItem.type)" :key="item.type" :label="item.name" :value="item.type"/>
                    </el-select>
                    <span style="margin-left:10px;margin-right:10px">{{ $t('commons.report_statistics.report_filter.belone') }}</span>

                    <el-select style="width:173px" class="ms-http-select" size="small" multiple filterable v-model="filterItem.values" v-if="getFilterOptions(filterItem.type).length > 0">
                      <el-option v-for="itemOption in getFilterOptions(filterItem.type)" :key="itemOption.id" :label="itemOption.label" :value="itemOption.id"/>
                    </el-select>
                    <el-input  style="width:173px" v-model="filterItem.value" size="small" v-else ></el-input>
                    <el-button @click="addFilterOptions(filterItem.type)"
                               @keydown.enter.native.prevent
                               type="primary"
                               icon="el-icon-plus"
                               circle
                               style="color:white;padding: 0px 0.1px;width: 20px;height: 20px;margin-left:5px;"
                               size="mini"/>
                    <el-button @click="removeFilterOptions(filterItem.type)"
                               @keydown.enter.native.prevent
                               type="danger"
                               icon="el-icon-minus"
                               circle
                               style="color:white;padding: 0px 0.1px;width: 20px;height: 20px;margin-left:5px;"
                               size="mini"/>
                  </el-col>
                </el-row>
              </el-main>
            </el-container>
          </el-collapse-item>
        </el-collapse>
      </el-row>


      <el-row type="flex">
        <el-col style="height: 100%" :span="4" >

        </el-col>
        <el-col :span="20">

        </el-col>
      </el-row>
      <el-row align="middle">
          <el-button style="margin-left: 200px;margin-top: 20px" type="primary" size="mini" @click="init">{{ $t('commons.confirm') }}</el-button>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import {getCurrentProjectID, getUUID} from "@/common/js/utils";
import MsSelectTree from "@/business/components/common/select-tree/SelectTree";

export default {
  name: "TestAnalysisTable",
  components: {MsSelectTree},
  data() {
    return {
      collapseActiveNames: "",
      option: {
        xaxis: "creator",
        yaxis: ["testCase","apiCase","scenarioCase","loadCase"],
        timeType: "dynamicTime",
        projectId: getCurrentProjectID(),
        filterType: "And",
        timeFilter:{
          timeRange: 7,
          timeRangeUnit: "day",
        },
        times: [new Date().getTime() - 6 * 24 * 3600 * 1000, new Date().getTime()],
        filters:[
          {
            type:"",
            name:"",
            compType:"input",
            isShow:false,
          },
        ],
      },
      h: document.documentElement.clientHeight + 80,
      lineDivTopHeight: 0,
      lineDivMarginTopHeight: 0,
      lineDivBottomHeight: 0,
      disabled: false,
      loading: false,
      optionLoad: true,
      result: {},
      items: [],
      modules: [],
      xAxisOptions: [
        {id: 'creator', label: this.$t('commons.report_statistics.report_filter.select_options.creator')},
        {id: 'maintainer', label: this.$t('commons.report_statistics.report_filter.select_options.maintainer')},
        {id: 'casetype', label: this.$t('commons.report_statistics.report_filter.select_options.case_type')},
        {id: 'casestatus', label: this.$t('commons.report_statistics.report_filter.select_options.case_status')},
        {id: 'caselevel', label: this.$t('commons.report_statistics.report_filter.select_options.case_level')},
      ],
      yAxisOptions: [
        {id: 'testCase', label: this.$t('api_test.home_page.failed_case_list.table_value.case_type.functional')},
        {id: 'apiCase', label: this.$t('api_test.home_page.failed_case_list.table_value.case_type.api')},
        {id: 'scenarioCase', label: this.$t('api_test.home_page.failed_case_list.table_value.case_type.scene')},
        {id: 'loadCase', label: this.$t('api_test.home_page.failed_case_list.table_value.case_type.load')},
      ],
      filterTypes: [
        {id: 'And', label: 'And'},
        {id: 'Or', label: 'Or'},
      ],
      timeTypeOptions: [
        {id: 'fixedTime', label: this.$t('commons.report_statistics.report_filter.time_options.fixed_time')},
        {id: 'dynamicTime', label: this.$t('commons.report_statistics.report_filter.time_options.dynamic_time')},
      ],
      timeRangeNumberMax: 31,
      timeRangeUnitOptions: [
        {id: 'day', label: this.$t('commons.report_statistics.report_filter.time_options.day')},
        {id: 'month', label: this.$t('commons.report_statistics.report_filter.time_options.month')},
        {id: 'year', label: this.$t('commons.report_statistics.report_filter.time_options.year')},
      ],
      priorityFilters: [
        {id: 'P0', label: 'P0'},
        {id: 'P1', label: 'P1'},
        {id: 'P2', label: 'P2'},
        {id: 'P3', label: 'P3'}
      ],
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      moreOptionsSelectorKeys:[
        {
          type:"casetype",
          name:this.$t('commons.report_statistics.report_filter.select_options.case_type'),
        },
        {
          type:"creator",
          name:this.$t('commons.report_statistics.report_filter.select_options.creator'),
        },
        {
          type:"maintainer",
          name:this.$t('commons.report_statistics.report_filter.select_options.maintainer'),
        },
        {
          type:"casestatus",
          name:this.$t('commons.report_statistics.report_filter.select_options.case_status'),
        },
        {
          type:"caselevel",
          name:this.$t('commons.report_statistics.report_filter.select_options.case_level'),
        },
      ],
      moreOptionsSelectorValues: {
        id: 'id',
        label: 'label',
      },
    };
  },
  created() {
    this.init();
    this.initMoreOptionsSelectorValues();
  },
  computed: {
  },
  watch: {
    option: {
      handler: function () {
        this.$nextTick(() => {
          this.lineDivHeight = 0;
          setTimeout(() => {
            let itemOptions = document.getElementsByName("itemOptions");
              if(itemOptions && itemOptions.length > 1){
                let optionTypeHeight = 0;
                for(let i = 0; i < itemOptions.length; i ++){
                  let itemHeight = itemOptions[i].offsetHeight;
                  if(optionTypeHeight != 0){
                    optionTypeHeight += 5;
                  }
                  optionTypeHeight+= itemHeight;
                }
                let firstHeight = itemOptions[0].offsetHeight;
                let endHeight = itemOptions[itemOptions.length-1].offsetHeight;
                this.lineDivMarginTopHeight = ((firstHeight-32)/2+16);
                this.lineDivTopHeight = ((optionTypeHeight/2 - this.lineDivMarginTopHeight-16));
                let divMarginBottom = ((endHeight-32)/2+16);
                this.lineDivBottomHeight = ((optionTypeHeight - 32 - this.lineDivTopHeight - this.lineDivMarginTopHeight - divMarginBottom ));
              }
          }, 100);
        });
      },
      deep: true
    }
  },
  methods: {
    initSelectOption(opt){
      this.loading = true;
      this.option = opt;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    addFilterOptions: function (type){
      this.optionLoad = false;
      let otherOptionKeys = this.getFilterOptionKey("");
      if(otherOptionKeys.length > 0 && this.option.filters.length < 5) {
        let addOptions = {
          type: "",
          id: getUUID(),
          name: "",
          compType: "selector",
          isShow: false,
          itemOptions: this.priorityFilters,
        };
        this.option.filters.push(addOptions);
      } else {
        this.$alert(this.$t('commons.report_statistics.alert.cannot_add_more_options'));
      }
      this.$nextTick(() => {
        this.optionLoad = true;
      });
    },
    getFilterOptions(type){
      let optionArray = [];
      if(this.moreOptionsSelectorValues && this.moreOptionsSelectorValues[type]){
        optionArray = this.moreOptionsSelectorValues[type];
      }
      return optionArray;
    },
    getFilterOptionKey(type){
      let optionArray = [];
      for(let i = 0; i < this.moreOptionsSelectorKeys.length; i++){
        let keyObj = this.moreOptionsSelectorKeys[i];
        let inOptions = false;
        if(keyObj.type !== type){
          for(let j = 0; j < this.option.filters.length; j ++){
            if(keyObj.type === this.option.filters[j].type){
              inOptions = true;
            }
          }
        }
        if(!inOptions){
          optionArray.push(keyObj);
        }
      }
      return optionArray;
    },
    removeFilterOptions: function (type){
      let removeOptionsIndex = -1;
      for(let index = 0; index < this.option.filters.length; index ++){
        let item = this.option.filters[index];
        if(item.type === type){
          removeOptionsIndex = index;
        }
      }
      if(removeOptionsIndex >= 0){
        this.option.filters.splice(removeOptionsIndex,1);
      }
      if(this.option.filters.length === 0){
        let addOptions = {
          type: "",
          id: getUUID(),
          name: "",
          compType: "selector",
          isShow: false,
          itemOptions: this.priorityFilters,
        };
        this.option.filters.push(addOptions);
      }
    },
    init: function () {
      this.$emit('filterCharts', this.option);
    },
    onTimeChange() {
      if (this.option.times[1] > new Date().getTime()) {
        this.$alert(this.$t('commons.report_statistics.alert.end_time_cannot_over_than_start_time'));
      }
    },
    initMoreOptionsSelectorValues() {
      let selectParam = {
        projectId:getCurrentProjectID()
      };
      this.$post('/report/test/case/count/initDatas',selectParam, response => {
        this.moreOptionsSelectorValues = response.data;
      });
    },
    timeRangeUnitChange(val){
      if(val === 'day'){
        this.timeRangeNumberMax = 31;
      }else if(val === 'month'){
        this.timeRangeNumberMax = 12;
      }else {
        this.timeRangeNumberMax = 1;
      }
      this.option.timeFilter.timeRange = 1;
    },
    getOption(){
      return this.option;
    }
  },
};
</script>

<style scoped>

.tip {
  float: left;
  font-size: 14px;
  border-radius: 2px;
  border-left: 2px solid #783887;
  margin: 0px 10px 0px;
}

.ms-row {
  margin: 0px 10px 0px;
}

.ms-card {
  width: 480px;
  overflow: auto;
}

.top-line-box{
  border-top: 1px solid;
  border-left: 1px solid;
  margin-left: 32px;
  border-top-left-radius: 10px;
}

.bottom-line-box{
  border-bottom: 1px solid;
  border-left: 1px solid;
  margin-left: 32px;
  border-bottom-left-radius: 10px;
}

/deep/ .el-select__tags-text {
  display: inline-block;
  max-width: 117px;
  overflow: hidden;
  text-overflow:ellipsis;
  /*white-space: nowrap;*/
}
</style>
