// 邮箱校验
export const emailRegex = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/;
// 手机号校验，11位
export const phoneRegex = /^\d{11}$/;
// 密码校验，8-32位
export const passwordLengthRegex = /^.{8,32}$/;
// 密码校验，必须包含数字和字母，特殊符号范围校验
export const passwordWordRegex = /^(?=.*\d)(?=.*[a-zA-Z])[0-9a-zA-Z!@#$%^&*()_+.]+$/;
// Git地址校验
export const gitRepositoryUrlRegex = /\.git$/;

/**
 * 校验邮箱
 * @param email 邮箱
 * @returns boolean
 */
export function validateEmail(email: string): boolean {
  return emailRegex.test(email);
}

/**
 * 校验手机号
 * @param phone 手机号
 * @returns boolean
 */
export function validatePhone(phone: string): boolean {
  return phoneRegex.test(phone);
}

/**
 * 校验密码长度
 * @param password 密码
 * @returns boolean
 */
export function validatePasswordLength(password: string): boolean {
  return passwordLengthRegex.test(password);
}

/**
 * 校验密码组成
 * @param password 密码
 * @returns boolean
 */
export function validateWordPassword(password: string): boolean {
  return passwordWordRegex.test(password);
}

/**
 * 校验密码
 * @param password 密码
 * @returns boolean
 */
export function validatePassword(password: string): boolean {
  return validatePasswordLength(password) && validateWordPassword(password);
}

/**
 * 校验Git地址
 * @param url Git地址
 * @returns boolean
 */
export function validateGitUrl(url: string): boolean {
  return gitRepositoryUrlRegex.test(url);
}
