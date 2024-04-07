/**
 *  合计多个 px 值
 * @param values 多个 px 值
 */
export function addPixelValues(...values: string[]) {
  const pixelValues = values.filter((v) => v.endsWith('px')).map((v) => parseInt(v, 10));
  const totalValue = pixelValues.reduce((acc, val) => acc + val, 0);
  return `${totalValue}px`;
}
/**
 * px 转 number
 * @param str size 字符串
 * @returns number
 */
export function translatePxToNumber(str: string): number {
  const result = Number(str.replace('px', ''));
  return Number.isNaN(result) ? 0 : result;
}

export function translateTextToPX(text?: string) {
  if (!text) {
    return 0;
  }
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');
  if (context != null) {
    const metrics = context.measureText(text);
    return metrics.width;
  }
  return 0;
}
