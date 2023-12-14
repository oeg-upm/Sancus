# Sancus

**Sancus** is a software that offers semantic representations of the Ethereum blockchain using web semantic formats like JSON-LD 1.1. It focuses on translating Ethereum's blockchain data to semantic web languages.

[Webpage](https://dlt.linkeddata.es)

## Ethereum block retriever

The **Ethereum Block Retriever** is a component that fetches metadata from Ethereum blocks and lists their transactions. It gathers basic information like block number and transaction hashes.

## ABI contract retriever

The **ABI Contract Retriever** is a component that extracts and provides the Application Binary Interface (ABI) of smart contracts on the Ethereum blockchain. It focuses on retrieving the essential interface information that defines how to interact with these contracts.

## Solidity contract retriever

The **Solidity Contract Retriever** is a component designed to retrieve and display Solidity smart contracts from the Ethereum blockchain. It focuses on accessing the source code of contracts written in Solidity and providing the semantic representation.

## Compiling Sancus

To complile the  _jar_  run the following command:

```
mvn clean package -DskipTests
```
A folder called "target" will be created containing the compiled  _jar_  and a folder with dependencies.


## Extra information

The code needs to be optimised and organised, but for now...

![it just works](https://media.tenor.com/b_bxDDs23yoAAAAd/it-just-works-todd-howard.gif)
