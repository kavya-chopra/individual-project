import { Connection } from "./connection";
import { ZendRequest } from "./types";
import { _connect } from "../proxy";

export class Connections {
  connections: Map<string, Connection>;

  constructor() {
    this.connections = new Map<string, Connection>();
  }

  addIfAbsent(handle: string, connection: Connection): void {
    if (this.connections.has(handle)) return;
    this.connections.set(handle, connection);
  }

  findConnection(host: string, port: number): string {
    for (const entry of Array.from(this.connections.entries())) {
      const handle = entry[0];
      const connection = entry[1];
      if (connection.host == host && connection.localPort == port) {
        return handle;
      }
    }
    return null;
  }

  getConnection(handle: string): Connection {
    return this.connections.get(handle);
  }

  async sendToAll(request: ZendRequest): Promise<void> {
    const sendOne = async ([, connection]: [string, Connection]) => {
      if (connection.getClient() === null) {
        console.log(`Connecting to ${connection.getHost()}:${connection.getLocalPort()}`);
        connection.setClient(await _connect(connection.getHost(), connection.getLocalPort()));
      }

      console.log(`Sending request ${JSON.stringify(request)} to ${connection.getHost()}:${connection.getLocalPort()}`)
      await connection.getClient().sendRequest(request);
    };
    await Promise.all(Array.from(this.connections).map(sendOne));
  }
}
