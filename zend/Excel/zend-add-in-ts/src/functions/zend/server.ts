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

      let buffer = Buffer.alloc(0);
      let requestLength: number | null = null;

      const handleReceivedRequest = (requestBuffer: Buffer) => {
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
      };

      const processData = () => {
        while (buffer.length > 0) {
          if (requestLength === null && buffer.length > 4) {
            requestLength = buffer.readInt32BE(0);
            buffer = buffer.subarray(4);
          }
          if (requestLength !== null && buffer.length >= requestLength) {
            const requestBuffer = buffer.subarray(0, requestLength);
            buffer = buffer.subarray(requestLength);
            requestLength = null;
            handleReceivedRequest(requestBuffer);
          } else {
            break;
          }
        }
      };

      socket.on("data", (data) => {
        buffer = Buffer.concat([buffer, data]);
        processData();
      });
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
