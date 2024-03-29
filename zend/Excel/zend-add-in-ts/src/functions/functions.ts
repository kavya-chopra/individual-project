﻿import { deserialize, serialize } from "./zend/converters";
import { InvocationMap } from "./zend/invocationmap";
import {
  ExcelGetRequest,
  ExcelGetResponse,
  ExcelOpenPortRequest,
  ExcelOpenPortResponse,
  ExcelPutRequest,
  ExcelPutResponse,
  ZendExcelMessage,
  ZendExcelRequest,
  ZendExcelResponse,
} from "./zend/types";
import { BSON } from "bson";

// For handling ws communication
// TODO: make this URL user provided
const pendingResponses: Map<number, any> = new Map();

const ws: WebSocket = new WebSocket("wss://localhost:8080");
ws.binaryType = "arraybuffer";
ws.onopen = (event) => {
  console.log("Connected to proxy");
};
ws.onmessage = (event) => {
  const message = event.data;
  try {
    console.log(`received message`);
    if (!(message instanceof ArrayBuffer)) {
      return;
    }

    const deserialized = BSON.deserialize(new Uint8Array(message)) as ZendExcelResponse | ZendExcelMessage;
    console.log(`deserialized message: ${JSON.stringify(deserialized)}`);

    if ("id" in deserialized && pendingResponses.has(deserialized.id)) {
      // this is a response
      const resolve = pendingResponses.get(deserialized.id);
      pendingResponses.delete(deserialized.id);
      resolve(deserialized);
      return;
    } else {
      // this is an update action
    }
  } catch (e) {
    console.error(e);
  }
};

/**
 * Displays the current time once a second.
 * @customfunction CLOCK
 * @param invocation Custom function handler
 */
export function clock(invocation: CustomFunctions.StreamingInvocation<string>): void {
  const timer = setInterval(() => {
    const time = currentTime();
    invocation.setResult(time);
  }, 1000);

  invocation.onCanceled = () => {
    clearInterval(timer);
  };
}

/**
 * Returns the current time.
 * @returns String with the current time formatted for the current locale.
 */
export function currentTime(): string {
  return new Date().toLocaleTimeString();
}

/**
 * Increments a value once a second.
 * @customfunction INCREMENT
 * @param incrementBy Amount to increment
 * @param invocation Custom function handler
 */
export function increment(incrementBy: number, invocation: CustomFunctions.StreamingInvocation<number>): void {
  let result = 0;
  const timer = setInterval(() => {
    result += incrementBy;
    invocation.setResult(result);
  }, 1000);

  invocation.onCanceled = () => {
    clearInterval(timer);
  };
}

/**
 * Writes a message to console.log().
 * @customfunction LOG
 * @param message String to write.
 * @returns String to write.
 */
export function logMessage(message: string): string {
  console.log(message);
  return message;
}

/**
 * Puts a value into global storage and sends value to other frameworks.
 * @customfunction PUT
 * @param name the key for value.
 * @param value the value to put in storage.
 * @returns string respresenting success of putting the value.
 */
export async function put(name: string, value: any[][]): Promise<string> {
  const transformed = transformFromExcel(value);
  const req: ExcelPutRequest = {
    id: getNextId(),
    request_type: "EXCEL_PUT_REQUEST",
    name: name,
    value: serialize(transformed),
  };
  console.log(`Preparing put request: ${req}`);
  const response = (await sendWsRequest(req)) as ExcelPutResponse;
  return response.result;
}

/**
 * Gets a value from the global storage.
 * @customfunction GET
 * @param name the key to get the value of from storage.
 * @param verticalLists whether or not to display vertical lists vertically
 * @returns value from storage corresponding to given key.
 */
export async function get(name: string, verticalLists?: boolean): Promise<any[][]> {
  if (verticalLists === null) {
    verticalLists = false;
  }

  const req: ExcelGetRequest = { id: getNextId(), request_type: "EXCEL_GET_REQUEST", name: name };
  const response = (await sendWsRequest(req)) as ExcelGetResponse;
  const value = deserialize(response.result);

  // Convert to any[][] for Excel
  console.log(`here: ${value}`)
  return transformToExcel(name, value, verticalLists);
}

/**
 * Opens a server port for incoming connections from other frameworks.
 * @customfunction OPEN_PORT
 * @param port port number to open server on and listen for connections
 * @returns string indicating success of opening port
 */
export async function openPort(port: number): Promise<string> {
  const req: ExcelOpenPortRequest = {
    id: getNextId(),
    request_type: "EXCEL_OPEN_PORT_REQUEST",
    port: port,
  };
  const response = (await sendWsRequest(req)) as ExcelOpenPortResponse;
  return response.result;
}

function sendWsRequest(req: ZendExcelRequest): Promise<ZendExcelResponse> {
  return new Promise((resolve) => {
    pendingResponses.set(req.id, resolve);
    let serializedReq = BSON.serialize(req);
    ws.send(serializedReq);
    console.log(`sent ws request: ${JSON.stringify(req)}`);
  });
}

function getNextId(): number {
  return Math.floor(Math.random() * 10000);
}

function transformFromExcel(value: any[][]): any {
  let transformed: any = null;
  if (value.length === 1) {
    if (value[0].length === 1) {
      transformed = value[0][0];
    } else {
      transformed = value[0];
    }
  } else {
    if (value.every((row) => row.length == 1)) {
      transformed = value.map((v) => v[0]);
    } else {
      transformed = value;
    }
  }
  return transformed;
}

function transformToExcel(name: string, value: any, verticalLists: boolean): any[][] {
  let transformed = null;
  if (Array.isArray(value)) {
    if (Array.isArray(value[0])) {
      transformed = value;
    } else {
      transformed = verticalLists ? value.map((v) => [v]) : [value];
    }
  } else if (typeof value === "string" || typeof value === "number") {
    transformed = [[value]];
  } else {
    transformed = [
      [
        {
          type: Excel.CellValueType.entity,
          text: name,
          properties: {
            ...value,
          },
        },
      ],
    ];
  }
  return transformed;
}
