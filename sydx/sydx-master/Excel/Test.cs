using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using MongoDB.Bson;
using MongoDB.Bson.IO;
using MongoDB.Bson.Serialization;

class Request
{
    public string RequestType { get; set; }
    public int SomeInt { get; set; }
    public string SomeOtherData { get; set; }
}

class Response
{
    public string Status { get; set; }
    public string Message { get; set; }
}

class Program
{
    static void Main()
    {
        // Create a server socket and accept a connection from the Python client
        var serverSocket = new TcpListener(IPAddress.Any, 12345);
        serverSocket.Start();
        using (var clientSocket = serverSocket.AcceptTcpClient())
        using (var stream = clientSocket.GetStream())
        {
            // Receive the length of the BSON data
            var lengthBuffer = new byte[4];
            stream.Read(lengthBuffer, 0, 4);
            int bsonDataLength = BitConverter.ToInt32(lengthBuffer, 0);

            // Receive the serialized BSON data based on the length
            var bsonData = new byte[bsonDataLength];
            stream.Read(bsonData, 0, bsonDataLength);

            // Deserialize the BSON data into a Request object
            var request = BsonSerializer.Deserialize<Request>(new MemoryStream(bsonData));

            // Process the request and create a response
            var response = new Response { Status = "success", Message = "Request processed" };

            // Serialize the response into BSON
            
            var bsonResponse = new MemoryStream();
            IBsonWriter bsonWriter = new BsonBinaryWriter(bsonResponse);
            BsonSerializer.Serialize(bsonWriter, response);

            // Send the length of the BSON response
            var responseLength = BitConverter.GetBytes((int)bsonResponse.Length);
            stream.Write(responseLength, 0, 4);

            // Send the serialized BSON response
            stream.Write(bsonResponse.ToArray(), 0, (int)bsonResponse.Length);
        }

        serverSocket.Stop();
    }
}
