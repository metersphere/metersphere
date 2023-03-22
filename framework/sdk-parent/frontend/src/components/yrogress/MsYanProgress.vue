<template>
  <div class="y-progress" :style="{'width':'100%','background-color':tip[3].fillStyle || '#ccc'}">

    <div
      v-for="(item, index) in statusCountItems"
      :key="index"
      class="y-progress_bar"
      :style="{'width':percent(statusCountItems,index) + '%','background-color':tip[index].fillStyle || '#ccc'}"
    >
    </div>
  </div>
</template>
<script>

export default {
  name:'ms-yan-progress',
  data() {
    return {
    };
  },
  props: {
    total: {
      type: Number,
      default: 0
    },
    tip: {
      type: Array,
      default: () => []
    },
    statusCountItems: {
      type: Array,
      default: () => []
    },
  },
  methods: {
    percent(statusCountItems,index) {
      var item = 0;
      for (let i = statusCountItems.length - 1; i >= 0; i--) {
        item += statusCountItems[i-index].value;
        if(index===i){
          var number = parseInt((item / this.total) * 100);
          return number;
        }
      }
    }
  },
};
</script>
<style scoped>
.y-progress {
  position: relative;
  width: 100%;
  height: 8px;
  background-color: #ccc;
  border-radius: 4px;
}
.y-progress_bar {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  border-radius: 4px;
  transition: all 1.5s ease;
}
.y-progress_text {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  text-align: center;
}
.y-progress_text .y-tooltip {
  position: absolute;
  top: 15px;
  right: 0;
  background-color: #333;
  color: #f1f1f1;
  border-radius: 5px;
  padding: 5px 10px;
  visibility: hidden;
  opacity: 0;
  transform: scale(0);
  transition: all ease-in 0.2s;
}
.y-progress_text:hover .y-tooltip {
  visibility: visible;
  opacity: 1;
  transform: scale(1);
}
.y-tooltip:after {
  content: "";
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translate(-50%, 0);
  border: 5px solid;
  border-color: transparent transparent #333 transparent;
}
</style>
