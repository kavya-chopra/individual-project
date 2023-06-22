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
import PromiseSocket from "promise-socket";
import os from "os";
import { fromBytesInt32, toBytesInt32 } from "./utils";

export class ZendClient {
  host: string;
  port: number;
  localPort: number;
  socket: PromiseSocket<net.Socket> | null;

  pid: number;
  handle: string;

  constructor(host: string, port: number, localPort: number) {
    this.host = host;
    this.port = port;
    this.localPort = localPort;
    this.socket = null;
  }

  async sendRequest(request: ZendRequest): Promise<ZendResponse> {
    // send request
    if (this.socket === null) {
      this.socket = new PromiseSocket();
      console.log(`Connecting on socket ${this.socket}`);
      console.log(`Connecting to ${this.host}:${this.port}`);
      await this.socket.connect({ port: this.port, host: this.host });
      console.log(`Successfully connected`);
    }

    const serializedReq = BSON.serialize(request);

    const sizeBuffer = Buffer.from(toBytesInt32(serializedReq.length));
    await this.socket.write(sizeBuffer);
    console.log(`Sent length: ${serializedReq.length}`);

    const requestBuffer = Buffer.from(serializedReq);
    await this.socket.write(requestBuffer);
    console.log(`Sent request: ${JSON.stringify(request)}`);

    // receive response
    const responseLength: number = fromBytesInt32((await this.socket.read(4)) as Buffer);
    console.log(`Received length: ${responseLength}`);

    const serializedResponse: Buffer = (await this.socket.read(responseLength)) as Buffer;
    const response = BSON.deserialize(serializedResponse);
    console.log(`Received response: ${JSON.stringify(response)}`);

    // assume correct and return for now
    return response as ZendResponse;
  }

  async connect(): Promise<void> {
    // send handshake request
    this.pid = process.pid;
    let requestHandshake: HandshakeRequest = {
      request_type: "HANDSHAKE_REQUEST",
      host: this.host,
      pid: this.pid,
      local_port: this.localPort,
    } as HandshakeRequest;
    let responseHandshake: HandshakeResponse = (await this.sendRequest(requestHandshake)) as HandshakeResponse;
    this.handle = responseHandshake.connection_handle;
  }

  async syncStorage(storage: Storage): Promise<void> {
    // send sync storage request
    let requestSyncStorage: SyncStorageRequest = {
      request_type: "SYNC_STORAGE_REQUEST",
      storage: storage.getAllSerialized(),
    } as SyncStorageRequest;
    let responseSyncStorage: SyncStorageResponse = (await this.sendRequest(requestSyncStorage)) as SyncStorageResponse;
    storage.putAllSerialized(responseSyncStorage.storage_snapshot);
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
