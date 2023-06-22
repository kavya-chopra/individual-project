import { ZendClient } from "./client";

export class Connection {
  host: string;
  pid: number;
  localPort: number;
  date: Date;
  client: ZendClient;

  constructor(host: string, pid: number, localPort: number, date: Date, client: ZendClient) {
    this.host = host;
    this.pid = pid;
    this.localPort = localPort;
    this.date = date;
    this.client = client;
  }

  getClient(): ZendClient {
    return this.client;
  }

  setClient(client: ZendClient): void {
    this.client = client;
  }

  getHost(): string {
    return this.host;
  }

  getLocalPort(): number {
    return this.localPort;
  }
}
