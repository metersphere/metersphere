import {
  COUNT_NUMBER,
  COUNT_NUMBER_SHALLOW,
  ORIGIN_COLOR,
  ORIGIN_COLOR_SHALLOW,
  PRIMARY_COLOR,
} from "./constants";
import i18n from "../i18n";
import html2canvas from "html2canvas";
import JsPDF from "jspdf";
/**
 * 同一行的多个文本框高度保持一致
 * 同时支持 autosize 的功能
 * @param size 同一行中文本框的个数
 * @param index 编辑行的下标
 * 如果编辑某一行，则只调整某一行，提升效率
 */
import calcTextareaHeight from "element-ui/packages/input/src/calcTextareaHeight";
import {saveStep} from "../api/novice";

export function setCustomizeColor(color) {
  // 自定义主题风格
  document.body.style.setProperty("--aside_color", color || "#783887");
  document.body.style.setProperty("--font_color", "#fff");
  document.body.style.setProperty("--font_light_color", "#fff");
}

export function setDefaultTheme() {
  setColor(
    ORIGIN_COLOR,
    ORIGIN_COLOR_SHALLOW,
    COUNT_NUMBER,
    COUNT_NUMBER_SHALLOW,
    PRIMARY_COLOR
  );
}

export function setColor(a, b, c, d, e) {
  // 顶部菜单背景色
  document.body.style.setProperty("--color", a);
  document.body.style.setProperty("--color_shallow", b);
  // 首页颜色
  document.body.style.setProperty("--count_number", c);
  document.body.style.setProperty("--count_number_shallow", d);
  // 主颜色
  document.body.style.setProperty("--primary_color", e);
}

export function setAsideColor() {
  // 自定义主题风格
  document.body.style.setProperty("--aside_color", "#783887");
  document.body.style.setProperty("--font_light_color", "#fff");
  document.body.style.setProperty("--font_color", "#fff");
}

export function setLightColor() {
  // 自定义主题风格
  document.body.style.setProperty("--aside_color", "#fff");
  document.body.style.setProperty("--font_color", "#505266");
  document.body.style.setProperty("--font_light_color", "#783887");
}

export function getUUID() {
  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  }

  return (
    S4() +
    S4() +
    "-" +
    S4() +
    "-" +
    S4() +
    "-" +
    S4() +
    "-" +
    S4() +
    S4() +
    S4()
  );
}

export function listenGoBack(callback) {
  //监听浏览器返回操作，关闭该对话框
  if (window.history && window.history.pushState) {
    history.pushState(null, null, document.URL);
    window.addEventListener("popstate", callback);
  }
}

export function removeGoBackListener(callback) {
  window.removeEventListener("popstate", callback);
}

// 驼峰转换下划线
export function humpToLine(name) {
  return name.replace(/([A-Z])/g, "_$1").toLowerCase();
}

// 下划线转换驼峰
export function lineToHump(name) {
  return name.replace(/\_(\w)/g, function (all, letter) {
    return letter.toUpperCase();
  });
}

export function fullScreenLoading(component) {
  return component.$loading({
    lock: true,
    text: "资源切换中...",
    spinner: "el-icon-loading",
    background: "rgba(218,218,218,0.6)",
    customClass: "ms-full-loading",
  });
}

export function stopFullScreenLoading(loading, timeout) {
  timeout = timeout ? timeout : 2000;
  setTimeout(() => {
    loading.close();
  }, timeout);
}

export function strMapToObj(strMap) {
  if (strMap) {
    let obj = Object.create(null);
    for (let [k, v] of strMap) {
      obj[k] = v;
    }
    return obj;
  }
  return null;
}

export function objToStrMap(obj) {
  let strMap = new Map();
  for (let k of Object.keys(obj)) {
    strMap.set(k, obj[k]);
  }
  return strMap;
}

export function downloadFile(name, content) {
  const blob = new Blob([content]);
  if ("download" in document.createElement("a")) {
    // 非IE下载
    //  chrome/firefox
    let aTag = document.createElement("a");
    aTag.download = name;
    aTag.href = URL.createObjectURL(blob);
    aTag.click();
    URL.revokeObjectURL(aTag.href);
  } else {
    // IE10+下载
    navigator.msSaveBlob(blob, name);
  }
}

export function getTranslateOptions(data) {
  let options = [];
  data.forEach((i) => {
    let option = {};
    Object.assign(option, i);
    option.text = i18n.t(option.text);
    options.push(option);
  });
  return options;
}

export function exportPdf(name, canvasList) {
  let pdf = new JsPDF("", "pt", "a4");

  // 当前页面的当前高度
  let currentHeight = 0;
  for (let canvas of canvasList) {
    if (canvas) {
      let contentWidth = canvas.width;
      let contentHeight = canvas.height;

      //a4纸的尺寸[595.28,841.89]
      let a4Width = 592.28;
      let a4Height = 841.89;

      // html页面生成的canvas在pdf中图片的宽高
      let imgWidth = a4Width;
      let imgHeight = (a4Width / contentWidth) * contentHeight;

      let pageData = canvas.toDataURL("image/jpeg", 1.0);

      // 当前图片的剩余高度
      let leftHeight = imgHeight;

      // 当前页面的剩余高度
      let blankHeight = a4Height - currentHeight;

      if (leftHeight > blankHeight) {
        if (blankHeight < 300) {
          pdf.addPage();
          currentHeight = 0;
        }
        //页面偏移
        let position = 0;
        while (leftHeight > 0) {
          // 本次添加占用的高度
          let occupation = a4Height - currentHeight;
          pdf.addImage(
            pageData,
            "JPEG",
            0,
            position + currentHeight,
            imgWidth,
            imgHeight
          );
          currentHeight = leftHeight;
          leftHeight -= occupation;
          position -= occupation;
          //避免添加空白页
          if (leftHeight > 0) {
            pdf.addPage();
            currentHeight = 0;
          }
        }
      } else {
        pdf.addImage(pageData, "JPEG", 0, currentHeight, imgWidth, imgHeight);
        currentHeight += imgHeight;
      }
    }
  }

  pdf.save(name.replace(" ", "_") + ".pdf");
}

export function operationConfirm(v, tip, success, cancel) {
  if (tip[tip.length - 1] !== "?" && tip[tip.length - 1] !== "？") {
    tip += "?";
  }
  return v
    .$confirm(tip, "", {
      confirmButtonText: i18n.t("commons.confirm"),
      cancelButtonText: i18n.t("commons.cancel"),
      type: "warning",
      center: false,
    })
    .then(() => {
      if (success) {
        success();
      }
    })
    .catch(() => {
      if (cancel) {
        cancel();
      }
    });
}

export function parseTag(data) {
  data.forEach((item) => {
    try {
      let tags = JSON.parse(item.tags);
      if (tags instanceof Array) {
        item.tags = tags ? tags : [];
      } else {
        item.tags = tags ? [tags + ""] : [];
      }
    } catch (e) {
      item.tags = [];
    }
  });
}

export function getNodePath(id, moduleOptions) {
  for (let i = 0; i < moduleOptions.length; i++) {
    let item = moduleOptions[i];
    if (id === item.id) {
      return item.path;
    }
  }
  return "";
}

export function windowPrint(id, zoom) {
  //根据div标签ID拿到div中的局部内容
  let bdhtml = window.document.body.innerHTML;
  let el = document.getElementById(id);
  var jubuData = el.innerHTML;
  document.getElementsByTagName("body")[0].style.zoom = zoom;
  //把获取的 局部div内容赋给body标签, 相当于重置了 body里的内容
  window.document.body.innerHTML = jubuData;
  //调用打印功能
  window.print();
  document.getElementsByTagName("body")[0].style.zoom = 1;
  window.document.body.innerHTML = bdhtml; //重新给页面内容赋值；
  return false;
}

export function handleCtrlSEvent(event, func) {
  if (event.keyCode === 83 && event.ctrlKey) {
    func();
    event.preventDefault();
    event.returnValue = false;
    return false;
  }
}

export function byteToSize(bytes) {
  if (bytes === 0) {
    return "0B";
  }
  let k = 1024,
    sizeUnits = ["B", "K", "M", "G", "T", "P", "E", "Z", "Y"],
    i = Math.floor(Math.log(bytes) / Math.log(k));

  return Math.round(bytes / Math.pow(1024, i)).toFixed(1) + sizeUnits[i];
}

export function sizeToByte(size) {
  let k = 1024,
    sizeUnits = ["B", "K", "M", "G", "T", "P", "E", "Z", "Y"];
  let i = 1;
  for (i++; i < sizeUnits.length; ) {
    let unit = sizeUnits[i];
    if (size.indexOf(unit) !== -1) {
      return size.toString().replace(unit, "") * Math.pow(k, i);
    }
  }
}

export function getTypeByFileName(filename) {
  if (filename === "") {
    return "";
  }
  let type = filename.substr(filename.lastIndexOf(".") + 1);
  return type.toUpperCase();
}

export function resizeTextarea(size = 2, index) {
  let textareaList = document.querySelectorAll(
    ".sync-textarea .el-textarea__inner"
  );
  if (index || index === 0) {
    _resizeTextarea(index * size, size, textareaList);
  } else {
    for (let i = 0; i < textareaList.length; i += size) {
      _resizeTextarea(i, size, textareaList);
    }
  }
}

function _resizeTextarea(i, size, textareaList) {
  // 查询同一行中文本框的最大高度
  let maxHeight = 0;
  for (let j = 0; j < size; j++) {
    let cur = textareaList[i + j];
    let curHeight = parseFloat(calcTextareaHeight(cur).height);
    maxHeight = Math.max(curHeight, maxHeight);
  }

  // 将同一行中的文本框设置为最大高度
  for (let k = 0; k < size; k++) {
    let cur = textareaList[i + k];
    if (cur.clientHeight !== maxHeight) {
      cur.style.height = maxHeight + "px";
    }
  }
}

export function checkMicroMode() {
  return sessionStorage.getItem("MICRO_MODE");
}

export function getUrlParams(url) {
  if (!url) url = window.location.href;
  // 通过 ? 分割获取后面的参数字符串
  let urlStr = url.split('?')[1]
  // 创建空对象存储参数
  let obj = {};
  // 再通过 & 将每一个参数单独分割出来
  let paramsArr = urlStr?.split('&')
  for(let i = 0,len = paramsArr?.length;i < len;i++){
    // 再通过 = 将每一个参数分割为 key:value 的形式
    let arr = paramsArr[i].split('=')
    obj[arr[0]] = arr[1];
  }
  return obj
}

/**
 * @param  ele 要生成 pdf 的DOM元素（容器）
 * @param  pdfName PDF文件生成后的文件名字
 * */
export function downloadPDF(ele, pdfName) {
  let eleW = ele.offsetWidth; // 获得该容器的宽
  let eleH = ele.offsetHeight; // 获得该容器的高
  let eleOffsetTop = ele.offsetTop; // 获得该容器到文档顶部的距离
  let eleOffsetLeft = ele.offsetLeft; // 获得该容器到文档最左的距离
  let canvas = document.createElement("canvas");
  let abs = 0;
  let win_in =
    document.documentElement.clientWidth || document.body.clientWidth; // 获得当前可视窗口的宽度（不包含滚动条）
  let win_out = window.innerWidth; // 获得当前窗口的宽度（包含滚动条）
  if (win_out > win_in) {
    abs = (win_out - win_in) / 2; // 获得滚动条宽度的一半
  }

  canvas.width = eleW * 2; // 将画布宽&&高放大两倍
  canvas.height = eleH * 2;
  let context = canvas.getContext("2d");
  let scale = canvas.height / ((canvas.width / 592.28) * 841.89);
  scale = scale > 3 ? 1 : 2;
  context.translate(-eleOffsetLeft - abs, -eleOffsetTop);
  let scrollWidth = ele.scrollWidth;
  html2canvas(ele, {
    scale: scale, // 背景灰色
    background: "#FFFFFF",
    width: scrollWidth < 1500 ? 1500 : scrollWidth, // 为了使横向滚动条的内容全部展示
    useCORS: true, //允许canvas画布内 可以跨域请求外部链接图片, 允许跨域请求。
  }).then((canvas) => {
    let contentWidth = canvas.width;
    let contentHeight = canvas.height;
    //一页pdf显示html页面生成的canvas高度;
    let pageHeight = (contentWidth / 592.28) * 841.89;
    //未生成pdf的html页面高度
    let leftHeight = contentHeight;
    //页面偏移
    let position = 0;
    //a4纸的尺寸[595.28,841.89]，html页面生成的canvas在pdf中图片的宽高
    let imgWidth = 595.28;
    let imgHeight = (595.28 / contentWidth) * contentHeight;

    let pageData = canvas.toDataURL("image/jpeg", 1.0);
    let pdf = new JsPDF("", "pt", "a4");
    //有两个高度需要区分，一个是html页面的实际高度，和生成pdf的页面高度(841.89)
    //当内容未超过pdf一页显示的范围，无需分页
    if (leftHeight < pageHeight) {
      //在pdf.addImage(pageData, 'JPEG', 左，上，宽度，高度)设置在pdf中显示；
      pdf.addImage(pageData, "JPEG", 0, 0, imgWidth, imgHeight);
    } else {
      // 分页
      while (leftHeight > 0) {
        pdf.addImage(pageData, "JPEG", 0, position, imgWidth, imgHeight);
        leftHeight -= pageHeight + 841.89;
        position -= 841.89;
        //避免添加空白页
        if (leftHeight > 0) {
          pdf.addPage();
        }
      }
    }
    // 判断文件名是否以.pdf文件
    if (decodeURIComponent(pdfName).endsWith(".pdf")) {
      pdf.save(pdfName);
    } else {
      pdf.save(decodeURIComponent(pdfName) + ".pdf");
    }
    //可动态生成
  });
}

export function goSkip(_this) {
  _this.cancel()
}

export function gotoCancel(_this, cancel) {
  localStorage.setItem('resetGuide', 'false')
  if (cancel) {
    _this.cancel()
  } else {
    _this.complete()
  }
  saveStep().then(res => {
    localStorage.setItem('guide', 'true')
  }).catch(error => {
    // 错误的信息
    this.$error({
      message: error.response.data.message
    })
  })
}

// 上一步，下一步
export function gotoNext(_this, path, step) {
  _this.next()
  localStorage.setItem('resetGuide', 'false')
  localStorage.setItem('step', step)
  if (path) {
    this.$router.push(path)
  }
}
