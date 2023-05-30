import { Connection } from "./connection";
import { ZendRequest } from "./types";
import { _connect } from "../functions";

export class Connections {
  connections: Map<string, Connection>;

  constructor() {
    this.connections = new Map<string, Connection>();
  }

  add(handle: string, connection: Connection): void {
    this.connections.set(handle, connection);
  }

  async sendToAll(request: ZendRequest): Promise<void> {
    const sendOne = async ([, connection]: [string, Connection]) => {
      if (connection.getClient() === null) {
        connection.setClient(await _connect(connection.getHost(), connection.getLocalPort()));
      }
      await connection.getClient().sendRequest(request);
    };
    await Promise.all(Array.from(this.connections).map(sendOne));
  }
}
