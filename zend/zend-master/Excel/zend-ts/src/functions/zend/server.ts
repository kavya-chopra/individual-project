import * as net from "net";
import { BSON } from "bson";
import { ZendRequest, ZendResponse } from "./types";
import { processRequest } from "./Interpreter";
import { is } from "typescript-is";
import { Storage } from "./storage";
import { Connections } from "./connections";
import { toBytesInt32 } from "./utils";

export class ZendServer {
  port: number;
  host: string;
  storage: Storage;
  connections: Connections;

  constructor(port: number, host: string = "localhost", storage: Storage, connections: Connections) {
    this.port = port;
    this.host = host;
    this.storage = storage;
    this.connections = connections;
  }

  async listen(): Promise<void> {
    const server = net.createServer((socket) => {
      console.log("Server accepted client.");

      let requestLength: number | null = null;

      const readSize = (sizeBuffer: Buffer) => {
        requestLength = sizeBuffer.readInt32BE(0);
        // tells program to not call readSize when more data received; transfer subsequent data to readRequest
        socket.removeListener("data", readSize);
        socket.on("data", readRequest);
      };

      const readRequest = (requestBuffer: Buffer) => {
        if (requestLength !== null && requestBuffer.length === requestLength) {
          const receivedRequest = BSON.deserialize(requestBuffer);
          if (is<ZendRequest>(receivedRequest)) {
            console.log("Received request: ", receivedRequest);
            // process request and get response
            let response: ZendResponse = processRequest(receivedRequest, this.storage, this.connections);
            // Send response
            const serializedRes = BSON.serialize(response);
            const sizeBuffer = Buffer.from(toBytesInt32(serializedRes.length));
            socket.write(sizeBuffer);
            const responseBuffer = Buffer.from(serializedRes);
            socket.write(Buffer.from(responseBuffer));
            console.log("Sent response: ", serializedRes);
          } else {
            console.error("Invalid request received");
          }
        } else {
          console.log("Invalid size received");
        }

        socket.removeListener("data", readRequest);
        socket.on("data", readSize);
      };

      socket.on("data", readSize);
    });

    server.listen(this.port, this.host);
  }
}
