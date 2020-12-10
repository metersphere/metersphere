<template>
  <div direction="vertical" :class="direction" @mousedown="mouseDown"></div>
</template>

<script>
  export default {
    name: "MsDragMoveBar",
    data() {
      return {
        lastX: '',
        lastY: '',
      };
    },

    props: {
      direction: {
        type: String,
        default() {
          return 'vertical';
        }
      }
    },

    created() {
      document.addEventListener("mouseup", this.mouseUp);
    },

    destroyed() {
      document.removeEventListener("mouseup", this.mouseUp);
    },

    methods: {
      mouseDown(event) {
        document.addEventListener("mousemove", this.mouseMove);
        this.lastX = event.screenX;
        this.lastY = event.screenY;
      },
      mouseMove(event) {
        this.$emit("widthChange", this.lastX - event.screenX);
        this.$emit("heightChange", this.lastY - event.screenY);
        this.lastX = event.screenX;
        this.lastY = event.screenY;
      },
      mouseUp() {
        this.lastX = "";
        this.lastY = "";
        document.removeEventListener("mousemove", this.mouseMove);
      }
    }
  };
</script>
<style >
  .drag-bar {
    width: 100%;
    height: 2px;
    cursor: row-resize;
    z-index: 10;
    background: #ccc;
  }

  .horizontal {
    width: 2px;
    height: 100%;
    cursor: col-resize;
    z-index: 10;
  }

  .vertical {
    width: 100%;
    height: 2px;
    cursor: row-resize;
    z-index: 10;
  }

  .vertical:hover {
    height: 3px;
    background-color: #ccc;
    /*-webkit-box-shadow: 0 8px 10px -5px rgba(0,0,0,.2), 0 16px 24px 2px rgba(0,0,0,.14), 0 6px 30px 5px rgba(0,0,0,.12);*/
    /*box-shadow: 0 8px 10px -5px rgba(0,0,0,.2), 0 16px 24px 2px rgba(0,0,0,.14), 0 6px 30px 5px rgba(0,0,0,.12);*/
  }

  .horizontal:hover {
    width: 3px;
    /*background-color: #7C3985;*/
    background-color: #ccc;
  }


</style>

