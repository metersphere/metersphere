<template>
  <div class="compare-class">
    <el-card ref="old" style="width: 50%">
      <ms-component-config
        :type="oldData.type"
        :scenario="oldData"
        :node="oldNode"
        :env-map="oldProjectEnvMap"
        :show-version="false"
      />
    </el-card>
    <el-card ref="new" style="width: 50%">
      <ms-component-config
        :type="newData.type"
        :scenario="newData"
        :node="newNode"
        :env-map="newProjectEnvMap"
        :show-version="false"
      />
    </el-card>
  </div>
</template>
<script>
import MsComponentConfig from "@/business/components/api/automation/scenario/component/ComponentConfig";
const {diff} = require("@/business/components/performance/v_node_diff");
export default{
  name:"ScenarioChildDiff",
  props:{
    oldData:{

    },
    oldNode:{

    },
    oldProjectEnvMap:{

    },
    newData:{

    },
    newNode:{

    },
    newProjectEnvMap:{

    },
    oldVNode:{
    },
    newVNode:{},
    oldColor:String,
    newColor:String
  },
  components:{
    MsComponentConfig,
  },
  data(){
    return{
      showData:false,
      currentItervalID:'',
    }
  },
  methods:{
    getDiff(){
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      diff(oldVnode,vnode,this.oldColor,this.newColor);
    },
    getVnode(){
      let oldVnode = this.$refs.old
      let vnode = this.$refs.new
      if(oldVnode&&vnode){
        window.clearInterval(this.currentItervalID);
        this.getDiff()
      }
    }
  },
  created() {
  },
  mounted() {
    this.$nextTick(function () {
      this.currentItervalID = window.setInterval(this.getVnode, 1000);
    })
  }
}
</script>
<style scoped>
.compare-class{
  display: flex;
  justify-content:space-between;
}
</style>
