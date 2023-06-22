import { Storage } from "./zend/storage";
import { ZendServer } from "./zend/server";
import { Connections } from "./zend/connections";
import { ZendExcelRequest, ZendExcelResponse } from "./zend/types";
import WebSocket from "ws";
import { BSON } from "bson";
import { processRequestFromExcel } from "./zend/Interpreter";
import { ZendClient } from "./zend/client";
import { Connection } from "./zend/connection";
import https from "https";
import { getHttpsServerOptions } from "office-addin-dev-certs";

// global variables
let storage: Storage = new Storage();
let connections: Connections = new Connections();
let server: ZendServer | null = null;
let serverPort: number | null = null;
let wsServer: WebSocket.Server | null = null;

async function startWsServer(wsServerPort: number, host: string = "127.0.0.1") {
  const httpsServer = https.createServer(await getHttpsServerOptions());
  wsServer = new WebSocket.Server({ server: httpsServer });

  wsServer.on("connection", async (ws) => {
    console.log("Connected to Add-in");

    ws.binaryType = "arraybuffer";
    ws.on("message", async (message) => {
      try {
        console.log(`received message: ${message}`)
        if (message instanceof ArrayBuffer) {
          const deserialized = BSON.deserialize(new Uint8Array(message)) as ZendExcelRequest;
          console.log(`deserialized message: ${JSON.stringify(deserialized)}`);

          let response: ZendExcelResponse;
          if (deserialized.request_type === "EXCEL_OPEN_PORT_REQUEST") {
            // handle this separately since we need to set server and serverPort
            const responseString = await openPort(deserialized.port, host);
            response = { id: deserialized.id, response_type: "EXCEL_OPEN_PORT_RESPONSE", result: responseString };
          } else {
            response = await processRequestFromExcel(deserialized, storage, connections);
          }

          ws.send(BSON.serialize(response));
          console.log(`sent response: ${JSON.stringify(response)}`)
        }
      } catch (e) {
        console.error(e);
      }
    });

    ws.on("close", () => {
      console.log("Closed proxy server");
    });
  });

  httpsServer.listen(wsServerPort, () => {
    console.log(`Secure WebSocket server is running on port ${wsServerPort}`);
  });
}

/**
 * Opens a server port for incoming connections from other frameworks.
 * @param port port number to open server on and listen for connections
 * @param host host name for server; default is localhost
 * @returns string indicating success of opening port
 */
export async function openPort(port: number, host: string = "127.0.0.1"): Promise<string> {
  if (serverPort === null || server === null) {
    server = new ZendServer(port, host, storage, connections);
    serverPort = port;
    await server.listen();
    return `Listening on ${port}`;
  } else {
    return `Port already open on ${serverPort}`;
  }
}

startWsServer(8080);

// /**
//  * Connects to a server at given port and hostname for communication.
//  * @param port port number to connect to
//  * @param host host name to connect to; default is localhost
//  * @returns the string handle for the client that denotes the connection
//  */
// export async function connect(host: string = "localhost", port: number): Promise<string> {
//   if (serverPort === null) {
//     return "Must open port before connecting";
//   }
//   return (await _connect(host, port)).getHandle();
// }

// Global function; not an Excel function
export async function _connect(host: string, localPort: number): Promise<ZendClient> {
  let client: ZendClient = new ZendClient(host, localPort, serverPort);
  await client.connect();
  await client.syncStorage(storage);
  connections.add(client.getHandle(), new Connection(host, client.getPid(), localPort, new Date(), client));
  return client;
}

// function sendData(data: object) {
//   if (webSocket.readyState === WebSocket.OPEN) {
//     const serializedReq = BSON.serialize(response);
//             const sizeBuffer = Buffer.from(toBytesInt32(serializedRes.length));
//             socket.write(sizeBuffer);
//             const responseBuffer = Buffer.from(serializedRes);
//             socket.write(Buffer.from(responseBuffer));
//             console.log("Sent response: ", serializedRes);

//     webSocket.send(JSON.stringify(data));
//   } else {
//     console.log("WebSocket not ready, cannot send data");
//   }
// }

// function createWebSocket(port: number, host: string): void {
//   webSocket = new WebSocket("${host}:${port}");
//   webSocket.onopen = () => {
//     console.log("Opened websocket connected to proxy");
//   }

// }
