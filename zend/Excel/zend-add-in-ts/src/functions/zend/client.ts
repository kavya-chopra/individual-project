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
import { _syncStorage } from "../proxy";

export class ZendClient {
  host: string;
  port: number;
  localPort: number;
  socket: PromiseSocket<net.Socket> | null;
  socketOpen: boolean;
  pid: number;
  handle: string;

  maxReconnectionAttempts: number = 3;

  constructor(host: string, port: number, localPort: number) {
    this.host = host;
    this.port = port;
    this.localPort = localPort;
    this.socket = null;
    this.socketOpen = false;
  }

  private async initializeSocket() {
    this.socket = new PromiseSocket();
    console.log(`Connecting on socket ${this.socket}`);
    console.log(`Connecting to ${this.host}:${this.port}`);
    await this.socket.connect({ port: this.port, host: this.host });
    console.log(`Successfully connected`);
    this.socketOpen = true;

    this.socket.stream.on("end", () => {
      console.log("Socket closed");
      this.socketOpen = false;
    });

    this.socket.stream.on("error", (err) => {
      console.error("Socket error:", err);
      this.socketOpen = false;
    });
  }

  async sendRequest(request: ZendRequest, reconnectionAttempt: number = 0): Promise<ZendResponse> {
    // send request
    if (this.socket === null) {
      await this.initializeSocket();
    }

    const receiveAll = async (socket: PromiseSocket<net.Socket>, n: number): Promise<Buffer> => {
      let data = Buffer.alloc(0);
      while (data.length < n) {
        try {
          const receivedData = (await socket.read(n - data.length)) as Buffer;
          if (receivedData) {
            data = Buffer.concat([data, receivedData]);
          } else {
            return null;
          }
        } catch (error) {
          console.error("Error while receiving data:", error);
          return null;
        }
      }

      return data;
    };

    if (this.socketOpen) {
      const serializedReq = BSON.serialize(request);

      const sizeBuffer = Buffer.from(toBytesInt32(serializedReq.length));
      await this.socket.write(sizeBuffer);
      console.log(`Sent length: ${serializedReq.length}`);

      const requestBuffer = Buffer.from(serializedReq);
      await this.socket.write(requestBuffer);
      console.log(`Sent request: ${JSON.stringify(request)}`);
    } else {
      console.log("Cannot write data: socket is closed");
      if (reconnectionAttempt <= this.maxReconnectionAttempts) {
        console.log("Trying to reconnect...");
        try {
          await this.initializeSocket();
          await _syncStorage(this);
          return await this.sendRequest(request);
        } catch (error) {
          console.error("Error while reconnecting to closed socket: ", error);
          return;
        }
      } else {
        console.log("Max reconnection attempts reached, aborting.");
      }
      return;
    }

    // receive response
    const responseLength: number = fromBytesInt32((await receiveAll(this.socket, 4)) as Buffer);
    console.log(`Received length: ${responseLength}`);

    const serializedResponse: Buffer = (await receiveAll(this.socket, responseLength)) as Buffer;
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
      storage_snapshot: storage.getAllSerialized(),
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
