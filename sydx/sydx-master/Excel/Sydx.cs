using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using ExcelDna.Integration;

namespace Sydx
{
    public class Server
    {
        public class StateObject
        {
            // Client socket
            public Socket workSocket = null;
            // Size of receive buffer
            public const int BufferSize = 1024;
            // Receive buffer
            public byte[] buffer = new byte[BufferSize];
            // Received data string
            public StringBuilder sb = new StringBuilder();
        }

        private int port;

        private Storage storage;

        private ManualResetEvent allDone = new ManualResetEvent(false);

        private Connections connections = new Connections();

        public Server(int port, Storage storage)
        {
            this.port = port;
            this.storage = storage;
        }

        public void Run()
        {
            Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            EndPoint localEndPoint = new IPEndPoint(IPAddress.Any, this.port);
            socket.Bind(localEndPoint);
            socket.Listen(100);
            while (true)
            {
                // Set the event to nonsignaled state
                allDone.Reset();

                socket.BeginAccept(new AsyncCallback(AcceptCallback), socket);

                // Wait until a connection is made before continuing
                allDone.WaitOne();
            }
        }

        public void AcceptCallback(IAsyncResult ar)
        {
            // Signal the thread to continue
            allDone.Set();

            Socket socket = (Socket)ar.AsyncState;
            Socket handler = socket.EndAccept(ar);

            this.storage.Put("connected", 357.0);

            StateObject state = new StateObject();
            state.workSocket = handler;
            handler.BeginReceive(state.buffer, 0, StateObject.BufferSize, 0,
                new AsyncCallback(ReadCallback), state);
        }

        public void ReadCallback(IAsyncResult ar)
        {
            String content = String.Empty;

            StateObject state = (StateObject)ar.AsyncState;
            Socket handler = state.workSocket;

            int bytesRead = handler.EndReceive(ar);

            if (bytesRead > 0)
            {
                // There might be more data, so store the data received so far
                state.sb.Append(Encoding.ASCII.GetString(state.buffer, 0, bytesRead));

                // Check for end-of-file tag. If it is not there, read more data
                content = state.sb.ToString();

                if (content.IndexOf("<EOF>") > -1)
                {
                    // All the data has been read from the client
                    this.storage.Put("message", content);
                    Console.WriteLine(content);

                    Send(handler, "Thank you.");
                }
                else
                {
                    // Not all data received. Get more
                    handler.BeginReceive(state.buffer, 0, StateObject.BufferSize, 0,
                        new AsyncCallback(ReadCallback), state);
                }
            }
        }

        private void Send(Socket handler, String data)
        {
            byte[] byteData = Encoding.ASCII.GetBytes(data);

            handler.BeginSend(byteData, 0, byteData.Length, 0, new AsyncCallback(SendCallback), handler);
        }

        private void SendCallback(IAsyncResult ar)
        {
            try
            {
                // Retrieve the socket from the state object
                Socket handler = (Socket)ar.AsyncState;

                // Complete sending the data to the remote device
                int bytesSent = handler.EndSend(ar);

                handler.Shutdown(SocketShutdown.Both);
                handler.Close();
            }
            catch (Exception e)
            {
                Console.WriteLine("SendCallback failed" + e);
                // TODO Log exception
            }
        }
    }


    public class Connections
    {
        private Dictionary<string, Connection> connections = new Dictionary<string, Connection>();

        public Connections() { }

        public void Add(string handle, Connection connection)
        {
            Monitor.Enter(connections);
            if (!connections.ContainsKey(handle))
            {
                connections.Add(handle, connection);
            }
            else
            {
                connections[handle] = connection;
            }
            Monitor.Exit(connections);
        }

        public void SendToAll(Request request)
        {
            Monitor.Enter(connections);
            foreach(KeyValuePair<string, Connection> entry in connections)
            {
                Connection connection = entry.Value;
                if(connection.Client is null)
                {
                    //TODO: _connect method
                    //connection.Client = _connect(connection.Host, connection.LocalPort);
                }
                connection.Client.SendRequest(request);
            }
            Monitor.Exit(connections);
        }

    }


    public class Connection
    {
        public Connection(object host, string pid, int localPort, DateTime dateTime, Client client)
        {
            this.Host = host;
            this.Pid = pid;
            this.LocalPort = localPort;
            this.DateTime = dateTime;
            this.Client = client;
        }

        public Object Host { get; }
        public String Pid { get; }
        public int LocalPort { get; }
        public DateTime DateTime { get; }
        public Client Client { get; set; }
    }


    public class Storage
    {
        private static readonly Dictionary<string, object> dict = new Dictionary<string, object>();

        public void Put(string name, object value)
        {
            Monitor.Enter(dict);
            if (!dict.ContainsKey(name))
            {
                dict.Add(name, value);
            }
            else
            {
                dict[name] = value;
            }
            Monitor.Exit(dict);
        }

        public object Get(string name)
        {
            object value = null;
            Monitor.Enter(dict);
            if (dict.ContainsKey(name))
            {
                value = dict[name];
            }
            Monitor.Exit(dict);
            return value;
        }

    }

    public static class ExcelFunctions
    {
        private static readonly Storage storage = new Storage();

        private static Boolean portOpen = false;

        [ExcelFunction(Description = "Opens a port for interaction with other Sydx systems")]
        public static string S_Port(
            [ExcelArgument(Name = "port", Description = "port number, e.g. 4782")] int port)
        {
            if (!portOpen)
            {
                Server server = new Server(port, storage);
                Thread serverThread = new Thread(server.Run);
                serverThread.Start();
                portOpen = true;
                return "`success";
            }
            else
            {
                return "`failure(reason='port already open')";
            }
        }

        [ExcelFunction(Description = "Returns the version of the Integral AddIn as a string, e.g. 1.0.0")]
        public static string S_Version()
        {
            return "1.0.0";
        }

        [ExcelFunction(Description = "Puts an object into the Integral dictionary")]
        public static string S_Put(string name, object value)
        {
            storage.Put(name, value);
            return "`" + name + "(type=" + value.GetType().ToString() + ")";
        }

        [ExcelFunction(Description = "Gets an object from the Integral dictionary")]
        public static object S_Get(string name, object value)
        {
            return storage.Get(name);
        }

        [ExcelFunction(Description = "Greets the user by name")]
        public static string IntegralHello(string name)
        {
            return "Hello " + name;
        }

        [ExcelFunction(Description = "An identity function: returns its argument")]
        public static object S_Identity(object arg)
        {
            return arg;
        }

        [ExcelFunction(Description = "Describes the value passed to the function")]
        public static string S_Describe(object arg)
        {
            if (arg is double)
                return "Double: " + (double)arg;
            else if (arg is string)
                return "String: " + (string)arg;
            else if (arg is bool)
                return "Boolean: " + (bool)arg;
            else if (arg is ExcelError)
                return "ExcelError: " + arg.ToString();
            else if (arg is object[,])
                // The object array returned here may contain a mixture of different types,
                // reflecting the different cell contents
                return string.Format("Array[{0},{1}]", ((object[,])arg).GetLength(0), ((object[,])arg).GetLength(1));
            else if (arg is ExcelMissing)
                return "<<Missing>>"; // Would have been System.Reflection.Missing in previous versions of ExcelDna
            else if (arg is ExcelEmpty)
                return "<<Empty>>"; // Would have been null
            else
                return "!? Unheard Of ?!";
        }

        [ExcelFunction(Description = "Applies Simplex algorithm to selected range.")]
        public static string S_Simplex([ExcelArgument(Description = "All constraint function coefficients. G symbolizes greater than sign and L for lesser than sign")]object constraints, 
                                        [ExcelArgument(Description = "Objective function coefficients")]object objective, 
                                        [ExcelArgument(Description = "Enter max to find maximum of objective function and min to find minimum")]String funcType)
        {
            String[] constraints_str = convert_arr_to_string(constraints);
            String objective_str = convert_arr_to_string(objective)[0];

            Double answer = 0;

            return answer.ToString();
        }
    }
}
