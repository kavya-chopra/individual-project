/* eslint-disable prettier/prettier */
import { v4 as uuidv4 } from "uuid";
import { Storage } from "./storage";
import { Connections } from "./connections";
import { HandshakeResponse, PutResponse, ZendRequest, ZendResponse, SyncStorageResponse } from "./types";
import { Connection } from "./connection";

export function processRequest(request: ZendRequest, storage: Storage, connections: Connections): ZendResponse {
  
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
