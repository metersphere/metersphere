/**
 * 比较两个一维数组对象是否相等，不考虑顺序，
 * @param arr1 数组1
 * @param arr2 数组2
 * @returns boolean
 */
export function isArraysEqualWithOrder<T>(arr1: T[], arr2: T[]): boolean {
  if (arr1.length !== arr2.length) {
    return false;
  }

  for (let i = 0; i < arr1.length; i++) {
    const obj1 = arr1[i];
    const obj2 = arr2[i];

    // 逐一比较对象
    if (JSON.stringify(obj1) !== JSON.stringify(obj2)) {
      return false;
    }
  }

  return true;
}

export default {};
