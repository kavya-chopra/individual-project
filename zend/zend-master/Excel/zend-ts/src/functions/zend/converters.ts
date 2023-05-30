import { is } from "typescript-is";
import { ZendType, ZendMap, ZendString, ZendFloat, ZendDict, ZendList } from "./types";

export function serialize(value: any): ZendType {
  if (is<Number>(value)) {
    return { value_type: "float", value: value } as ZendFloat;
  } else if (is<String>(value)) {
    return { value_type: "string", value: value } as ZendString;
  } else if (is<Map<string, any>>(value)) {
    let serializedMap: ZendMap = {} as ZendMap;
    value.forEach((val: any, key: string) => {
      serializedMap[key] = serialize(val);
    });
    return { value_type: "dict", value: serializedMap } as ZendDict;
  } else if (is<any[]>(value)) {
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
      deserialize(elemSerialized);
    });
  } else if (json.value_type === "dict") {
    return Object.entries(json.value).map((elem: [string, ZendType]) => {
      elem[1] = deserialize(elem[1]);
    });
  } else {
    return json.value;
  }
}
