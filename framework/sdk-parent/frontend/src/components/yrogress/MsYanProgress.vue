<template>
  <div class="y-progress" :style="{'width':againPercent + '%','background-color':tip[3].fillStyle || '#ccc'}">
    <a class="y-progress_text" :style="{'width':((again/total)*100)+'%'}">
      <span class="y-tooltip" v-text="tip[3].text.replace('X',again>total?total:again)"></span>
    </a>
    <div
      class="y-progress_bar"
      :style="{'width':underwayPercent+ '%','background-color':tip[0].fillStyle || '#000'}"
    >
      <a class="y-progress_text" :style="{'width':((underway/total)*100)+'%'}">
        <span class="y-tooltip" v-text="tip[0].text.replace('X',underway>total?total:underway)"></span>
      </a>
    </div>
    <div
      class="y-progress_bar"
      :style="{'width':unPassPercent+'%','background-color':tip[2].fillStyle || '#080'}"
    >
      <a class="y-progress_text" :style="{'width':((unPass/total)*100)+'%'}">
        <span class="y-tooltip" v-text="tip[2].text.replace('X',unPass>total?total:unPass)"></span>
      </a>
    </div>
    <div
      class="y-progress_bar"
      :style="{'width':passPercent+'%','background-color':tip[1].fillStyle || '#9c3'}"
    >
      <a class="y-progress_text" :style="{'width':((pass/total)*100)+'%'}">
        <span class="y-tooltip" v-text="tip[1].text.replace('X',pass>total?total:pass)"></span>
      </a>
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
    pass: {
      type: Number,
      default: 0
    },
    tip: {
      type: Array,
      default: () => []
    },
    unPass: {
      type: Number,
      default: 0
    },
    again: {
      type: Number,
      default: 0
    },
    underway: {
      type: Number,
      default: 0
    }
  },
  computed: {
    passPercent() {
      if (this.pass >= this.total) {
        return 1e2;
      }
      return parseInt((this.pass / this.total) * 100);
    },
    unPassPercent() {
      if (this.unPass >= this.total) {
        return 1e2;
      }
      return parseInt(((this.unPass + this.pass)/this.total) * 100);
    },
    againPercent() {
      if (this.again >= this.total) {
        return 1e2;
      }
      return parseInt(((this.again + this.pass + this.unPass + this.underway)/ this.total) * 100);
    },
    underwayPercent() {
      if (this.underway >= this.total) {
        return 1e2;
      }
      return parseInt(((this.underway + this.pass + this.unPass)/ this.total) * 100);
    },
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
