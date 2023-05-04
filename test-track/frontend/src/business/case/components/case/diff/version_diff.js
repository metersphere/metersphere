import i18n from "@/i18n";
/**
 * 存储版本信息的数据结构
 */
class VersionData {
  constructor({ diffArr }) {
    this.diffArr = diffArr || [];
  }
}

/**
 * 对比状态枚举
 */
class StatusType {
  /**
   * 无差异
   */
  static NORMAL = 0;

  /**
   * 创建
   */
  static CREATE = 1;

  /**
   * 删除
   */
  static DELETE = 2;

  /**
   * 格式变化
   */
  static FORMAT_CHANGE = 3;
}

/**
 * 内容格式组件枚举
 */
class ContentType {
  /**
   * 文本类型
   */
  static TEXT = "TEXT";

  /**
   * 数组类型
   */
  static ARRAY = "ARRAY";

  /**
   * 自定义字段
   */
  static FIELD = "FIELD";

  /**
   * 文件对比
   */
  static FILE = "FILE";

  /**
   * 表格数据对比
   */
  static TABLE = "TABLE";
}

class AbstractVersionDiffExecutor {
  diff() {}
}
/**
 * 版本对比执行器
 */
export default class DefaultDiffExecutor extends AbstractVersionDiffExecutor {
  /**
   * 构造器
   * @param {*} origin 原始对象
   * @param {*} target 对比对象
   * @param {*} extra 扩展属性
   */
  constructor(origin, target, extra = {}) {
    super();
    this.origin = origin || {};
    this.target = target || {};

    /**
     * 基础信息
     * VersionData
     */
    this.baseInfoDiffData = {};

    this.tagDiffData = {};

    /**
     * 详细信息
     */
    this.contentDiffData = {};
    /**
     * 自定义信息
     */
    this.customDiffData = [];

    /**
     * 附件信息对比
     */
    this.attachmentDiffData = [];
  }

  diff() {
    // 处理 基础信息对比
    //模块变更检测
    this.baseInfoDiffData.modules = [
      {
        diffArr: DiffUtil.diffText(this.origin.nodePath, this.target.nodePath),
      },
    ];
    // 关联需求检测
    this.baseInfoDiffData.story = [
      {
        diffArr: DiffUtil.diffText(this.origin.demandId, this.target.demandId),
      },
    ];
    // 需求id检测
    this.baseInfoDiffData.storyId = [
      {
        diffArr: DiffUtil.diffText(
          this.origin.demandName,
          this.target.demandName
        ),
      },
    ];
    // 标签信息处理
    this.tagDiffData.tags = [
      {
        diffArr: DiffUtil.diffArray(this.origin.tags, this.target.tags),
      },
    ];

    // 详细信息对比
    //名称对比
    this.contentDiffData.caseName = [
      {
        diffArr: DiffUtil.diffText(this.origin.name, this.target.name),
      },
    ];
    //前置条件对比
    this.contentDiffData.prerequisite = [
      {
        diffArr: DiffUtil.diffText(
          this.origin.prerequisite,
          this.target.prerequisite
        ),
      },
    ];

    let originStepModel = this.origin.stepModel === 'STEP' ? i18n.t('test_track.case.step_describe') : i18n.t('test_track.case.text_describe');
    let targetStepModel = this.target.stepModel === 'STEP' ? i18n.t('test_track.case.step_describe') : i18n.t('test_track.case.text_describe');
    this.contentDiffData.originStepModel = this.origin.stepModel;
    this.contentDiffData.targetStepModel = this.target.stepModel;
    this.contentDiffData.stepModel = [
      {
        diffArr: DiffUtil.diffText(originStepModel, targetStepModel),
      },
    ];

    // 步骤描述不一致
    if (this.origin.stepModel !== this.target.stepModel) {
      if (this.origin.stepModel === 'STEP') {
        this.contentDiffData.diffStep = [
          {
            diffArr: DiffUtil.diffText(this.origin.steps, this.target.stepDescription + '\n' + this.target.expectedResult),
          },
        ];
      } else {
        this.contentDiffData.diffStep = [
          {
            diffArr: DiffUtil.diffText(this.origin.stepDescription + '\n' + this.origin.expectedResult, this.target.steps),
          },
        ];
      }
    }

    // 步骤描述
    this.contentDiffData.steps = [
      {
        diffArr: DiffUtil.diffText(
          this.origin.steps,
          this.target.steps
        ),
      },
    ];

    // 文本描述
    this.contentDiffData.stepDescription = [
      {
        diffArr: DiffUtil.diffText(
          this.origin.stepDescription,
          this.target.stepDescription
        ),
      },
    ];
    // 预期结果
    this.contentDiffData.expectedResult = [
      {
        diffArr: DiffUtil.diffText(
          this.origin.expectedResult,
          this.target.expectedResult
        ),
      },
    ];

    //备注
    this.contentDiffData.remark = [
      {
        diffArr: DiffUtil.diffText(
          this.origin.remark,
          this.target.remark
        ),
      },
    ];
  }

  diffAttachment(origin, target) {
    this.attachmentDiffData.attachment = DiffUtil.diffAttachment(
      origin,
      target
    );
    return this.attachmentDiffData.attachment;
  }

  diffTableData(origin, target, key, props = []) {
    return DiffUtil.diffTableData(origin, target, key, props);
  }

  diffCustomData(fields1, fields2) {
    // 自定义信息处理
    this.customDiffData = DiffUtil.diffCustomData(fields1, fields2);
    return this.customDiffData;
  }
}

class DiffUtil {
  static buildDiffData(status, content = "", type = ContentType.TEXT) {
    let res = {};
    res.status = status;
    res.body = {
      type: type,
      content: content,
    };
    return res;
  }
  /**
   * 对比 文本内容
   */
  static diffText(s1, s2, format = false) {
    let resArr = [];

    //统一空参数
    if (s1 == "" || s1 == null || s1 == undefined) {
      s1 = "";
    }
    if (s2 == "" || s2 == null || s2 == undefined) {
      s2 = "";
    }
    // 无变化 -- s1===s2
    if (s1 == s2) {
      //s1 s2 均可
      resArr.push(this.buildDiffData(StatusType.NORMAL, s1));
      return resArr;
    }

    // 新增 -- s1不存在 s2存在
    if (!s1 && s2) {
      resArr.push(this.buildDiffData(StatusType.CREATE, s2));
      return resArr;
    }

    // 删除 -- s1存在 s2不存在
    if (s1 && !s2) {
      resArr.push(this.buildDiffData(StatusType.DELETE, s1));
      return resArr;
    }

    // 都不为空
    // 格式变化 -- s1、s2 均存在 且内容不一致
    if (format) {
      resArr.push(this.buildDiffData(StatusType.FORMAT_CHANGE, s2));
      return resArr;
    }

    // 差异按照 新增、删除 进行标记
    resArr.push(this.buildDiffData(StatusType.CREATE, s2));
    resArr.push(this.buildDiffData(StatusType.DELETE, s1));
    return resArr;
  }

  /**
   * 对比 数组
   *
   * 从数组总找出 新增和删除的
   */
  static diffArray(arr1, arr2) {
    let resArr = [];
    //矫正参数
    if (typeof arr1 === 'string') {
      arr1 = JSON.parse(arr1);
    }
    if (typeof arr2 === 'string') {
      arr2 = JSON.parse(arr2);
    }
    if (!Array.isArray(arr1)) {
      arr1 = [];
    }
    if (!Array.isArray(arr2)) {
      arr2 = [];
    }
    //返回原始数据
    if ((!arr1 && !arr2) || arr1 == arr2) {
      resArr.push(
        this.buildDiffData(StatusType.NORMAL, arr1, ContentType.ARRAY)
      );
      return resArr;
    }

    let createArr = [];
    let deleteArr = [];
    let normalArr = [];

    if (arr1.length <= 0 && arr2.length > 0) {
      // arr2 全部为新增
      createArr = arr2;
      resArr.push(
        this.buildDiffData(StatusType.CREATE, createArr, ContentType.ARRAY)
      );
      return resArr;
    }

    if (arr1.length > 0 && arr2.length <= 0) {
      //arr1 全部为删除
      deleteArr = arr1;
      resArr.push(
        this.buildDiffData(StatusType.DELETE, deleteArr, ContentType.ARRAY)
      );
      return resArr;
    }

    //以旧数组为基准 判断新数组 新增或删除的
    for (let i = 0; i < arr2.length; i++) {
      // 检测新增
      let f1 = arr1.find((v) => v == arr2[i]);
      if (!f1) {
        createArr.push(arr2[i]);
      } else {
        normalArr.push(arr2[i]);
      }
    }

    for (let i = 0; i < arr1.length; i++) {
      // 检测删除
      let f2 = arr2.find((v) => v == arr1[i]);
      if (!f2) {
        deleteArr.push(arr1[i]);
      }
    }

    if (createArr.length > 0) {
      resArr.push(
        this.buildDiffData(StatusType.CREATE, createArr, ContentType.ARRAY)
      );
    }
    if (deleteArr.length > 0) {
      resArr.push(
        this.buildDiffData(StatusType.DELETE, deleteArr, ContentType.ARRAY)
      );
    }

    // if (normalArr.length > 0) {
    //   resArr.push(
    //     this.buildDiffData(StatusType.NORMAL, normalArr, ContentType.ARRAY)
    //   );
    // }

    //无差异返回原来的
    if (createArr.length <= 0 && deleteArr.length <= 0) {
      resArr.push(
        this.buildDiffData(StatusType.NORMAL, arr2, ContentType.ARRAY)
      );
    }
    return resArr;
  }

  /**
   *  对比自定义字段信息
   */
  static diffCustomData(fields1, fields2) {
    let resArr = [];

    if (!fields1) {
      fields1 = {};
    }
    if (!fields2) {
      fields2 = {};
    }
    if ((!fields1 && !fields2) || fields1 == fields2) {
      // 无差异
      Object.keys(fields2).forEach((e) => {
        resArr.push({ key: e, value: this.diffText(fields2[e], fields2[e]) });
      });
      return resArr;
    }

    // fields1 不存在 fields2 存在 则fields2均为 新增字段
    // fields1 存在 fields2 不存在 则fields1均为 删除字段
    // 对比新增删除

    Object.keys(fields2).forEach((e) => {
      let findKey = Object.prototype.hasOwnProperty.call(fields1, e);
      if (!findKey) {
        resArr.push({
          key: e,
          value: this.diffText(undefined, fields2[e]),
        });
      } else {
        //找到了 判断是否变更
        let oldData = fields1[e];
        let newData = fields2[e];
        resArr.push({ key: e, value: this.diffText(oldData, newData) });
      }
    });

    return resArr;
  }

  /**
   *  对比 文件
   */
  static diffAttachment(origin, target) {
    //矫正参数
    if (!Array.isArray(origin)) {
      origin = [];
    }
    if (!Array.isArray(target)) {
      target = [];
    }
    let resArr = [];
    let targetMap = new Map();
    let originMap = new Map();
    target.forEach((t) => {
      targetMap.set(t.name, t);
    });
    originMap.forEach((o) => {
      originMap.set(o.name, o);
    });

    // 判读
    target.forEach((t) => {
      let o = originMap.get(t.name);
      if (!o) {
        //新增
        t.diffStatus = StatusType.CREATE;
        resArr.push(t);
      } else {
        //存在则对比 是否变更
        if (
          t.size !== o.size ||
          t.progress !== o.progress ||
          t.type !== o.type ||
          t.creator !== o.creator ||
          t.updateTime !== o.updateTime
        ) {
          // 格式变化
          t.diffStatus = StatusType.FORMAT_CHANGE;
          resArr.push(t);
        } else {
          t.diffStatus = StatusType.NORMAL;
          resArr.push(t);
        }
      }
    });

    origin.forEach((o) => {
      let t = targetMap.get(o.name);
      if (!t) {
        //标识已经删除
        o.diffStatus = StatusType.DELETE;
        resArr.push(o);
      }
    });
    return resArr;
  }

  /**
   * 对比表格数据
   */
  static diffTableData(origin, target, key, props = []) {
    //对比两个表格数组 并填充 diffStatus属性
    if (!key) {
      throw new Error("Diff key is undefined, please check it~");
    }
    //矫正参数
    if (!Array.isArray(origin)) {
      origin = [];
    }
    if (!Array.isArray(target)) {
      target = [];
    }
    if (!Array.isArray(props)) {
      props = [];
    }

    let resArr = [];
    //首先 基于key 将数组转为map备用
    let originMap = new Map();
    let targetMap = new Map();
    origin.forEach((o) => {
      originMap.set(o[key], o);
    });
    target.forEach((t) => {
      targetMap.set(t[key], t);
    });

    target.forEach((t) => {
      //从原始数组中找是否存在，不存在则为新建状态
      let o = originMap.get(t[key]);
      if (!o) {
        t.diffStatus = StatusType.CREATE;
        resArr.push(t);
      } else {
        //存在则对比 props中的项目 查看差异
        let factor = true;
        for (let i = 0; i < props.length; i++) {
          let p = props[i];
          if (t[p] !== o[p]) {
            factor = false;
            break;
          }
        }
        t.diffStatus = factor ? StatusType.NORMAL : StatusType.FORMAT_CHANGE;
        resArr.push(t);
      }
    });

    //逆向查找 找到已删除的
    origin.forEach((o) => {
      let t = targetMap.get(o[key]);
      if (!t) {
        o.diffStatus = StatusType.DELETE;
        resArr.push(o);
      }
    });

    return resArr;
  }
}
