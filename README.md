# individual-project
Computing BEng Individual Project of 2023 - Imperial College London

How to use the Library:

Step 1: start the proxy
- open command line and cd to zend-add-in-ts directory
- run 'npm run start:proxy'

Step 2: Connect the Add-in
- run 'npm run start:desktop'


Excel Custom Functions:

1. ZEND.OPEN_PORT(<port_number>) : opens a TCP port on the given port number for communication with other components
2. ZEND.PUT(<key>, <excel_range>) : adds a key-value pair to storage. The key must be a "string" value. The Excel range can be a value in a single cell or in a range.
3. ZEND.GET(<key>) : retrieves the value associated to the "string" key and prints it row-wise. If you wish to print the associated value column-wise, then use the function as ZEND.GET(<key>, TRUE).

