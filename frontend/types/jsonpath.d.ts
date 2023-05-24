declare module 'jsonpath-picker-vanilla' {
  export function jsonPathPicker(
    source: HTMLElement | null,
    json: object,
    target: (HTMLInputElement | null)[],
    opt: Record<string, any>
  ): void;
  export function clearJsonPathPicker(source: HTMLElement | null): void;
}
