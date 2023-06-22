import { deserialize, serialize } from "./converters";
import { ZendMap, ZendType } from "./types";

export class Storage {
  dict: Map<string, ZendType>;

  constructor() {
    this.dict = new Map<string, any>();
  }

  put(name: string, value: any): void {
    this.dict.set(name, serialize(value));
  }

  putSerialized(name: string, value: ZendType): void {
    this.dict.set(name, value);
  }

  putAll(dictToPut: Map<string, any>): void {
    dictToPut.forEach((value: any, key: string) => {
      this.dict.set(key, serialize(value));
    });
  }

  putAllSerialized(dictToPut: ZendMap): void {
    if (dictToPut === undefined || dictToPut === null) return;
    for (const [key, value] of Object.entries(dictToPut)) {
      this.dict.set(key, value);
    }
  }

  get(name: string): any {
    return deserialize(this.dict.get(name));
  }

  getSerialized(name: string): ZendType {
    return this.dict.get(name);
  }

  getAll(): Map<string, any> {
    let dictCopy = new Map<string, any>();
    this.dict.forEach((value: any, key: string) => {
      dictCopy.set(key, deserialize(value));
    });

    return dictCopy;
  }

  getAllSerialized(): ZendMap {
    let dictCopy = new Map<string, ZendType>();
    this.dict.forEach((value: ZendType, key: string) => {
      dictCopy.set(key, value);
    });

    return Object.fromEntries(dictCopy.entries()) as ZendMap;
  }
}
