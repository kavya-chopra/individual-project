// REQUEST TYPES:

export type HandshakeRequest = {
  request_type: "HANDSHAKE_REQUEST";
  host: string;
  pid: number;
  local_port: number;
};

export type PutRequest = {
  request_type: "PUT_REQUEST";
  name: string;
  value: ZendType;
};

export type SyncStorageRequest = {
  request_type: "SYNC_STORAGE_REQUEST";
  storage_snapshot: ZendMap;
};

export type ZendRequest = HandshakeRequest | PutRequest | SyncStorageRequest;

//---------------------------------------------------------------------
// RESPONSE TYPES:

export type HandshakeResponse = {
  response_type: "HANDSHAKE_RESPONSE";
  connection_handle: string;
};

export type PutResponse = {
  response_type: "PUT_RESPONSE";
  result: "success";
};

export type SyncStorageResponse = {
  response_type: "SYNC_STORAGE_RESPONSE";
  storage_snapshot: ZendMap;
};

export type ZendResponse = HandshakeResponse | PutResponse | SyncStorageResponse;

//---------------------------------------------------------------------
// SERIALIZED VALUE TYPES:

export type ZendList = {
  value_type: "list" | "tuple" | "array";
  value: ZendType[];
};

export type ZendDict = {
  value_type: "dict";
  value: ZendMap;
};

export type ZendInt = {
  value_type: "int" | "int32";
  value: number;
};

export type ZendFloat = {
  value_type: "float" | "float64";
  value: number;
};

export type ZendString = {
  value_type: "string";
  value: string;
};

export type ZendMap = {
  [name: string]: ZendType;
};

export type ZendType = ZendList | ZendDict | ZendInt | ZendFloat | ZendString;

//---------------------------------------------------------------------
// ADD-IN TO PROXY REQUEST TYPES:

export type ExcelPutRequest = {
  id: number;
  request_type: "EXCEL_PUT_REQUEST";
  name: string;
  value: ZendType;
};

export type ExcelGetRequest = {
  id: number;
  request_type: "EXCEL_GET_REQUEST";
  name: string;
};

export type ExcelOpenPortRequest = {
  id: number;
  request_type: "EXCEL_OPEN_PORT_REQUEST";
  port: number;
};

export type ZendExcelRequest = ExcelPutRequest | ExcelGetRequest | ExcelOpenPortRequest;

//---------------------------------------------------------------------
//  PROXY TO ADD-IN RESPONSE TYPES:

export type ExcelPutResponse = {
  id: number;
  response_type: "EXCEL_PUT_RESPONSE";
  result: "success";
};

export type ExcelGetResponse = {
  id: number;
  response_type: "EXCEL_GET_RESPONSE";
  result: ZendType;
};

export type ExcelOpenPortResponse = {
  id: number;
  response_type: "EXCEL_OPEN_PORT_RESPONSE";
  result: string;
};

export type ExcelUpdateMessage = {
  response_type: "EXCEL_UPDATE_MESSAGE";
  name: string;
  value: ZendType;
};

export type ExcelBulkUpdateMessage = {
  response_type: "EXCEL_BULK_UPDATE_MESSAGE";
  storage_snapshot: ZendMap;
};

export type ZendExcelResponse = ExcelPutResponse | ExcelGetResponse | ExcelOpenPortResponse;
export type ZendExcelMessage = ExcelUpdateMessage | ExcelBulkUpdateMessage;
