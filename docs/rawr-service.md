# RAWR Service
The RAWR service allows you to perform high level operations
on the RAWR song database. It takes a special RPC protocol,
encoded using Protobuf.

## RPC Protocol
Calls are performed by sending a 4-byte length followed by
a Protobuf `RawrCall` message. The `RawrCall` `functionCode`
is computed from the method name and the fully qualified names
of the arguments. For example, `fun getSomething(s: String)`
would be encoded as `getSomething(java.lang.String)`. The exact
code can be found in `RawrCallCodeExtractor`.

Arguments are sent as repeated byte arrays, one for each
Protobuf-encoded argument.
