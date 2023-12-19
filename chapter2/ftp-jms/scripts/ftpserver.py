from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import FTPHandler
from pyftpdlib.servers import FTPServer

authorizer = DummyAuthorizer()
authorizer.add_user("rider", "secret", "tmp", perm="elradfmw")
authorizer.add_anonymous(".")

handler = FTPHandler
handler.authorizer = authorizer

server = FTPServer(("127.0.0.1", 2121), handler)
server.serve_forever()