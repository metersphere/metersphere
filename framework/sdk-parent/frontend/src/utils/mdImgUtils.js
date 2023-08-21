/**
 * 解析md中的图片
 * @param text
 * @returns {*[]}
 */
export function parseMdImage(text) {
  let regex = /\!\[.*?\]\(\/resource\/md\/get\?fileName=(.*?)\)/g;
  let fileNames = [];
  let match = regex.exec(text);
  while (match !== null) {
    // 提取文件名并添加到数组中
    fileNames.push(match[1]);
    match = regex.exec(text);
  }
  return fileNames;
}
