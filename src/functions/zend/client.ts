import { Storage } from "./storage";
import {
  HandshakeRequest,
  HandshakeResponse,
  ZendRequest,
  ZendResponse,
  SyncStorageRequest,
  SyncStorageResponse,
} from "./types";
import * as net from "net";
import { BSON } from "bson";
import { is } from "typescript-is";
import PromiseSocket from "promise-socket";
import os from "os";
import { fromBytesInt32, toBytesInt32 } from "./utils";

export class ZendClient {
  host: string;
  port: number;
  localPort: number;
  storage: Storage;
  socket: PromiseSocket<net.Socket>;

  pid: number;
  handle: string;

  constructor(host: string, port: number, localPort: number, storage: Storage) {
    this.host = host;
    this.port = port;
    this.localPort = localPort;
    this.storage = storage;
    this.socket = new PromiseSocket();
  }

  async sendRequest(request: ZendRequest): Promise<ZendResponse> {
    // send request
    await this.socket.connect({ port: this.port, host: this.host });
    const serializedReq = BSON.serialize(request);

    const sizeBuffer = Buffer.from(toBytesInt32(serializedReq.length));
    await this.socket.write(sizeBuffer);
    const requestBuffer = Buffer.from(serializedReq);
    await this.socket.write(requestBuffer);

    console.log("Sent request: ", serializedReq);

    // receive response
    const responseLength: number = fromBytesInt32((await this.socket.read(4)) as Buffer);
    const serializedResponse: Buffer = (await this.socket.read(responseLength)) as Buffer;
    const response = BSON.deserialize(serializedResponse);
    await this.socket.end();
    if (is<ZendResponse>(response)) {
      console.log("Received response: ", response);
      return response;
    } else {
      console.error("received invalid response");
    }
  }

  async connect(): Promise<void> {
    // send handshake request
    this.pid = process.pid;
    let requestHandshake: HandshakeRequest = {
      request_type: "HANDSHAKE_REQUEST",
      host: os.hostname(),
      pid: this.pid,
      local_port: this.localPort,
    } as HandshakeRequest;
    let responseHandshake: HandshakeResponse = (await this.sendRequest(requestHandshake)) as HandshakeResponse;
    this.handle = responseHandshake.connection_handle;

    // send sync storage request
    let requestSyncStorage: SyncStorageRequest = {
      request_type: "SYNC_STORAGE_REQUEST",
      storage: this.storage.getAllSerialized(),
    } as SyncStorageRequest;
    let responseSyncStorage: SyncStorageResponse = (await this.sendRequest(requestSyncStorage)) as SyncStorageResponse;
    this.storage.putAllSerialized(responseSyncStorage.storage_snapshot);
  }

  getHandle(): string {
    return this.handle;
  }

  getHost(): string {
    return this.host;
  }

  getPid(): number {
    return this.pid;
  }
}
