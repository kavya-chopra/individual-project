import * as net from "net";
import { BSON } from "bson";
import { ZendRequest, ZendResponse } from "./types";
import { processRequestFromExternal } from "./Interpreter";
import { Storage } from "./storage";
import { Connections } from "./connections";
import { toBytesInt32 } from "./utils";
import WebSocket from "ws";

export class ZendServer {
  port: number;
  host: string;
  storage: Storage;
  connections: Connections;
  ws: WebSocket.WebSocket;

  constructor(port: number, host: string = "127.0.0.1", storage: Storage, connections: Connections) {
    this.port = port;
    this.host = host;
    this.storage = storage;
    this.connections = connections;
  }

  setWebSocket(ws: WebSocket.WebSocket) {
    this.ws = ws;
  }

  listen(): Promise<void> {
    const server = net.createServer((socket) => {
      console.log(`Server accepted client: ${JSON.stringify(socket.address())}`);

      let requestLength: number | null = null;

      const readSize = (sizeBuffer: Buffer) => {
        requestLength = sizeBuffer.readInt32BE(0);
        // tells program to not call readSize when more data received; transfer subsequent data to readRequest
        socket.removeListener("data", readSize);
        socket.on("data", readRequest);
      };

      const readRequest = (requestBuffer: Buffer) => {
        console.log(requestBuffer);
        if (requestLength !== null && requestBuffer.length === requestLength) {
          try {
            const receivedRequest = BSON.deserialize(requestBuffer) as ZendRequest;
            console.log("Received request: ", receivedRequest);
            // process request and get response
            let response: ZendResponse = processRequestFromExternal(
              receivedRequest,
              this.storage,
              this.connections,
              this.ws
            );
            // Send response
            const serializedRes = BSON.serialize(response);
            const sizeBuffer = Buffer.from(toBytesInt32(serializedRes.length));
            socket.write(sizeBuffer);
            const responseBuffer = Buffer.from(serializedRes);
            socket.write(Buffer.from(responseBuffer));
            console.log("Sent response: ", serializedRes);
          } catch (e) {
            console.log(e);
            console.error("Invalid request received");
          }
        } else {
          console.log("Invalid size received");
          console.log(`Received request length: ${requestLength}`);
          console.log(`Length of recevied data: ${requestBuffer.length}`);
        }

        socket.removeListener("data", readRequest);
        socket.on("data", readSize);
        socket.on("error", (err) => {
          console.error("Socket error:", err);
        });
      };

      socket.on("data", readSize);
      socket.on("end", () => console.log(`Client disconnected: ${JSON.stringify(socket.address())}`));
    });

    server.on("error", (err) => {
      console.error("Server error:", err);
    });

    return new Promise((resolve) => {
      server.listen(this.port, this.host, () => {
        //server.listen('/tmp/socket', () => {
        console.log(`Zend Server listening on port ${this.port}`);
        resolve();
      });
      server.on("connection", () => {
        console.log("Client connected to Zend Server.");
      });
    });
  }
}
