export const horizontalDrag = {
  inserted(el, binding) {
    el.onmousedown = function (e) {
      const init = e.clientX;
      const parent = el.parentNode;
      const initWidth = parent.offsetWidth;
      document.onmousemove = function (e) {
        const end = e.clientX;
        const newWidth = end - init + initWidth;
        parent.style.width = newWidth + "px";
      };
      document.onmouseup = function () {
        document.onmousemove = document.onmouseup = null;
      };
    };
  }
};
