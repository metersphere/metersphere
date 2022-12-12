const left2RightDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientX;
      const parent = el.parentNode;
      const initWidth = parent.offsetWidth;
      document.onmousemove = function (e) {
        const end = e.clientX;
        const newWidth = end - init + initWidth;
        if (newWidth < document.body.clientWidth - 10 && newWidth > 10) {
          parent.style.width = newWidth + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};

const right2LeftDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientX;
      const parent = el.parentNode;
      const initWidth = parent.offsetWidth;
      document.onmousemove = function (e) {
        const end = e.clientX;
        const newWidth = initWidth - (end - init);
        if (newWidth < document.body.clientWidth - 10 && newWidth > 10) {
          parent.style.width = newWidth + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};

const bottom2TopDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientY;
      const parent = el.parentNode;
      const initHeight = parent.offsetHeight;
      document.onmousemove = function (e) {
        const end = e.clientY;
        const newHeight = initHeight - (end - init);
        if (newHeight < document.body.clientHeight - 10 && newHeight > 10) {
          parent.style.height = newHeight + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};

const top2BottomDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientY;
      const parent = el.parentNode;
      const initHeight = parent.offsetHeight;
      document.onmousemove = function (e) {
        const end = e.clientY;
        const newHeight = initHeight + (end - init);
        if (newHeight < document.body.clientHeight - 10 && newHeight > 10) {
          parent.style.height = newHeight + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};

/**
 * 拖拽时改变相邻兄弟节点的高度，实现垂直方向的拖拽
 */
const verticalDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientY;
      const previous = el.previousElementSibling;
      const next = el.nextElementSibling;
      const previousInitHeight = previous.offsetHeight;
      const nextInitHeight = next.offsetHeight;
      document.onmousemove = function (e) {
        const end = e.clientY;
        const previousNewHeight = previousInitHeight + (end - init);
        const nextNewHeight = nextInitHeight - (end - init);
        let topMinHeight = binding.value.topMinHeight || 10;
        let bottomMinHeight = binding.value.bottomMinHeight || 10;
        if (nextNewHeight > bottomMinHeight && previousNewHeight > topMinHeight) {
          next.style.height = nextNewHeight + "px";
          previous.style.height = previousNewHeight + "px";
        }
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};


export default {
  left2RightDrag,
  right2LeftDrag,
  bottom2TopDrag,
  top2BottomDrag,
  verticalDrag
}
