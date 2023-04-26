# import os, sys
# sys.path.append(os.path.abspath('../main'))

import sydx

import numpy as np

import importlib
importlib.reload(sydx)


sydx.port(8001)
sydx.connect("localhost", 8000)

sydx.server_port()

# Python object you want to send
data = ["hello", "world"]

sydx.put('first', 1)

sydx.put('list', data)