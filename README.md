# individual-project
Computing BEng Individual Project of 2023 - Imperial College London

-------------------------------------------------------------------------------------

How to use the Excel componnet of the Library:

Step 1: start the proxy
- open command line and cd to zend-add-in-ts directory
- run 'npm run start:proxy'

Step 2: Start the Add-in
- run 'npm run start:desktop'

-------------------------------------------------------------------------------------

When closing the add-in:

Step 1: Close the Excel application
Step 2: run 'npm run stop' in the zend-add-in-ts directory
Step 3: run 'sh clear-excel-cache.sh' to clear the cache of the add-in

-------------------------------------------------------------------------------------

Excel Custom Functions:

1. ZEND.OPEN_PORT(<port_number>) : opens a TCP port on the given port number for communication with other components
2. ZEND.PUT(<key>, <excel_range>) : adds a key-value pair to storage. The key must be a "string" value. The Excel range can be a value in a single cell or in a range.
3. ZEND.GET(<key>) : retrieves the value associated to the "string" key and prints it row-wise. If you wish to print the associated value column-wise, then use the function as ZEND.GET(<key>, TRUE).

-------------------------------------------------------------------------------------

In external component Python:

1. Use zend.open_port(<port_number>) to open a TCP server on Python side
2. Use zend.connect(<hostname>, <port_number>) to create a TCP client that connects to the TCP server on Excel side, passing in hostname and port number that Excel TCP server is running on
3. use zend.put(<key>, <value>) to put values into storage. key must be string
4. use zend.get(<key>) to get values from storage

-------------------------------------------------------------------------------------

In external component Java:

0. Create a Zend object "Zend zend = new Zend()"

1. Use zend.openPort(<port_number>) to open a TCP server on Java side
2. Use zend.connect(<hostname>, <port_number>) to connect to another TCP server
3. use zend.put(<key>, <value>) to put value into storage. key must be string
4. use zend.get(<key>) to get values from storage
5. use zend.closePort() to close the TCP connection with the other server

-------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------