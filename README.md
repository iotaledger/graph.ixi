# graph.ixi

## About

By nature, IOTA transactions reference only two other transcations - which
may not suffice in all cases. There are scenarios, for example like in qubic or
when determining timestamps for transactions, where arbitrary data pieces need
to be linked with arbitrary number of other data pieces. As specified by
Paul Handy, an alternative, uniform solution for organizing data is required.

**graph.ixi** is an [IXI (IOTA eXtension Interface) module](https://github.com/iotaledger/ixi) for the [Iota Controlled agenT (Ict)](https://github.com/iotaledger/ict).
It extends the core client with the functionality to link arbitrary
data with any number of other data pieces. As a result, it circumvents the usual
limit of transactions that can be referenced.

Paper of graph.ixi: [paper](https://github.com/iotaledger/graph.ixi/blob/master/docs/graph.ixi.pdf)<br>
Specification of graph.ixi: [spec](https://github.com/iotaledger/omega-docs/blob/master/ixi/graph/Spec.md)

