# Graph.ixi

## About

By nature, IOTA transactions reference only two other transcations - which
may not suffice in all cases. There are scenarios, for example like in qubic or
when determining timestamps for transactions, where arbitrary data pieces need
to be linked with arbitrary number of other data pieces. As specified by
Paul Handy, an alternative, uniform solution for organizing data is required [1].

**Graph.ixi** is an [IXI (IOTA eXtension Interface) module](https://github.com/iotaledger/ixi) for the [Iota Controlled agenT (Ict)](https://github.com/iotaledger/ict).
It extends the core client with the functionality to link arbitrary
data with any number of other data pieces. As a result, it circumvents the usual
limit of transactions that can be referenced.

[1] [Specification of Graph.ixi](https://github.com/iotaledger/omega-docs/blob/master/ixi/graph/Spec.md)<br>
[2] [Documentation paper of Graph.ixi](https://github.com/iotaledger/graph.ixi/docs/Graph.ixi.pdf)

## Installation

### Step 1: Install Ict

Please find instructions on [iotaledger/ict](https://github.com/iotaledger/ict#installation).

Make sure you are connected to the main network and not to an island, otherwise you won't be able to message anyone in the main network.

### Step 2: Get Graph.ixi

There are two ways to do this:

#### Simple Method

Go to [releases](https://github.com/iotaledger/graph.ixi/releases) and download the **graph-{VERSION}.jar**
from the most recent release.

#### Advanced Method

You can also build the .jar file from the source code yourself. You will need **Git** and **Gradle**.

```shell
# download the source code from github to your local machine
git clone https://github.com/iotaledger/graph.ixi
# if you don't have git, you can also do this instead:
#   wget https://github.com/iotaledger/graph.ixi/archive/master.zip
#   unzip master.zip

# change into the just created local copy of the repository
cd graph.ixi

# build the graph-{VERSION}.jar file
gradle ixi
```

### Step 3: Install Graph.ixi
Move graph-{VERSION}.jar to the **modules/** directory of your Ict:
```shell
mv graph-{VERSION}.jar ict/modules
```

### Step 4: Run Ict
Switch to Ict directory and run:
```shell
java -jar ict-{VERSION}.jar
```

