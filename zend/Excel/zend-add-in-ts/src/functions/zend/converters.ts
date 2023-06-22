import { ZendType, ZendMap, ZendString, ZendFloat, ZendDict, ZendList } from "./types";

export function serialize(value: any): ZendType {
  if (typeof value === "number" || value instanceof Number) {
    return { value_type: "float", value: value } as ZendFloat;
  } else if (typeof value === "string" || value instanceof String) {
    return { value_type: "string", value: value } as ZendString;
  } else if (value instanceof Map) {
    let serializedMap: ZendMap = {} as ZendMap;
    value.forEach((val: any, key: string) => {
      serializedMap[key] = serialize(val);
    });
    return { value_type: "dict", value: serializedMap } as ZendDict;
  } else if (Array.isArray(value)) {
    let serializedList: ZendType[] = [];
    value.forEach((elem: any) => {
      serializedList.push(serialize(elem));
    });
    return { value_type: "list", value: serializedList } as ZendList;
  } else {
    console.error("Cannot serialize unsupported type");
  }
}

export function deserialize(json: ZendType): any {
  if (json.value_type === "list" || json.value_type === "tuple" || json.value_type === "array") {
    return json.value.map((elemSerialized: ZendType) => {
      return deserialize(elemSerialized);
    });
  } else if (json.value_type === "dict") {
    let value = { ...json.value };
    Object.keys(value).forEach((k) => {
      value[k] = deserialize(value[k]);
    });
    return value;
  } else {
    return json.value;
  }
}
