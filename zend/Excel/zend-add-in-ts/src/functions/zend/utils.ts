export function toBytesInt32(x: number): Buffer {
  const arr = new ArrayBuffer(4);
  const view = new DataView(arr);
  view.setInt32(0, x, false);
  return Buffer.from(new Uint8Array(arr));
}

export function fromBytesInt32(buf: Buffer): number {
  return buf.readInt32BE(0);
}
