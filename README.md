Minimalistic Java implementation of a P2P chat, created primarily for fun and learning.

The chat consists of three main components:

NavigationServer – Its main purpose is to convey information about all the peers connected to the chat at any given moment.
ServerConnection – Responsible for connecting to the navigation server and retrieving the current data about connected peers.
Peer – Acts as both a server and a client for the other connected peers.
Although ServerConnection and Peer run on separate threads, they share common data about connected peers via SharedResources.

Currently, the allowed number of peers is limited to two, but there are plans to support many more.

