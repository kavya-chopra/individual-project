import sys, os
# sys.path.append(os.path.abspath('../main'))

import sydx

import numpy as np


sydx.port(8001)
sydx.connect("localhost", 8000)

print("BIRCH")
# Python object you want to send
data = np.array("hello", "world")

sydx.put('first', 1)

sydx.put('list', data)