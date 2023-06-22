/* eslint-disable prettier/prettier */
import { v4 as uuidv4 } from "uuid";
import { Storage } from "./storage";
import { Connections } from "./connections";
import {
  HandshakeResponse,
  PutResponse,
  ZendRequest,
  ZendResponse,
  SyncStorageResponse,
  ZendExcelRequest,
  ZendExcelResponse,
  ExcelPutResponse,
  ExcelGetRequest,
  ExcelGetResponse,
  PutRequest,
} from "./types";
import { Connection } from "./connection";
import { serialize } from "./converters";
import WebSocket from "ws";

export function processRequestFromExternal(request: ZendRequest, storage: Storage, connections: Connections, ws: WebSocket.WebSocket): ZendResponse {
  
  if (request.request_type === "HANDSHAKE_REQUEST") {
    let handle = uuidv4();
    connections.add(handle, new Connection(request.host, request.pid, request.local_port, new Date(), null));
    return { response_type: "HANDSHAKE_RESPONSE", connection_handle: handle } as HandshakeResponse;

  } else if (request.request_type === "PUT_REQUEST") {
    storage.putSerialized(request.name, request.value);
    return { response_type: "PUT_RESPONSE", result: "success" } as PutResponse;

  } else if (request.request_type === "SYNC_STORAGE_REQUEST") {
    storage.putAllSerialized(request.storage);
    return { response_type: "SYNC_STORAGE_RESPONSE", storage_snapshot: storage.getAllSerialized() } as SyncStorageResponse;
    
  } else {
    console.error(new Error("unexpected request type"));
  }
}

export async function processRequestFromExcel(request: ZendExcelRequest, storage: Storage, connections: Connections): Promise<ZendExcelResponse> {

  if (request.request_type === "EXCEL_PUT_REQUEST") {
    storage.putSerialized(request.name, request.value);
    await connections.sendToAll({ request_type: "PUT_REQUEST", name: request.name, value: request.value } as PutRequest);
    return { id: request.id, response_type: "EXCEL_PUT_RESPONSE", result: "success" } as ExcelPutResponse;

  } else if (request.request_type === "EXCEL_GET_REQUEST") {
    let res = storage.getSerialized(request.name);
    if (res === undefined) {
      // if the result isn't in the storage, return empty string
      res = serialize("");
    }

    return { id: request.id, response_type: "EXCEL_GET_RESPONSE", result: res} as ExcelGetResponse;

  } else {
    console.error(new Error("unexpected excel request type"));
  }

}
 