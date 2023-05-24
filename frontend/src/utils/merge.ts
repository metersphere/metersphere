/**
 * 合并对象
 * @param target
 * @param source
 * @returns {Object} 返回合并后的对象
 */
function merge(target: any, source: any): any {
  target = target || {};
  Object.keys(source).forEach((key) => {
    if (Object.prototype.hasOwnProperty.call(source, key)) {
      const obj = source[key];
      if (Object.prototype.toString.call(obj) === '[object Object]') {
        target[key] = merge(target[key], obj);
      } else {
        target[key] = obj;
      }
    }
  });
  return target;
}
export default merge;
