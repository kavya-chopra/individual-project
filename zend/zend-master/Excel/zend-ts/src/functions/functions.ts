import { Storage } from "./zend/storage";
import { ZendClient } from "./zend/client";
import { ZendServer } from "./zend/server";
import { Connections } from "./zend/connections";
import { serialize } from "./zend/converters";
import { PutRequest } from "./zend/types";
import { Connection } from "./zend/connection";

// global variables
let storage: Storage = new Storage();
let connections: Connections = new Connections();
let server: ZendServer | null = null;
let serverPort: number | null = null;

/**
 * Adds two numbers.
 * @customfunction ADD
 * @param first First number
 * @param second Second number
 * @returns The sum of the two numbers.
 */
export function add(first: number, second: number): number {
  return first + second;
}

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
export async function put(name: string, value: any): Promise<string> {
  storage.put(name, value);
  connections.sendToAll({ request_type: "PUT_REQUEST", name: name, value: serialize(value) } as PutRequest);
  return "success";
}

/**
 * Gets a value from the global storage.
 * @customfunction GET
 * @param name the key to get the value of from storage.
 * @returns value from storage corresponding to given key.
 */
export function get(name: string): any {
  return storage.get(name);
}

/**
 * Opens a server port for incoming connections from other frameworks.
 * @customfunction OPEN_PORT
 * @param port port number to open server on and listen for connections
 * @param host host name for server; default is localhost
 * @returns string indicating success of opening port
 */
export async function openPort(port: number, host: string = "localhost"): Promise<string> {
  if (serverPort === null) {
    server = new ZendServer(port, host, storage, connections);
    serverPort = port;
    server.listen();
    return "Listening on {port}";
  } else {
    return "Port already open on {serverPort}";
  }
}

/**
 * Connects to a server at given port and hostname for communication.
 * @customfunction CONNECT
 * @param port port number to connect to
 * @param host host name to connect to; default is localhost
 * @returns the string handle for the client that denotes the connection
 */
export async function connect(host: string = "localhost", port: number): Promise<string> {
  if (serverPort === null) {
    return "Must open port before connecting";
  }
  return (await _connect(host, port)).getHandle();
}

// Global function; not an Excel function
export async function _connect(host: string, localPort: number): Promise<ZendClient> {
  let client: ZendClient = new ZendClient(host, localPort, serverPort, storage);
  await client.connect();
  connections.add(client.getHandle(), new Connection(host, client.getPid(), localPort, new Date(), client));
  return client;
}
