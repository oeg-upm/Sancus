# Sancus

**Sancus** is a software that offers semantic representations of the Ethereum blockchain using web semantic formats like JSON-LD 1.1. It focuses on translating Ethereum's blockchain data to semantic web languages.

[Webpage](https://dlt.linkeddata.es)

## Ethereum block retriever

The **Ethereum Block Retriever** is a component that fetches metadata from Ethereum blocks and lists their transactions. It gathers basic information like block number and transaction hashes such as:
- Block number  
- Transaction hashes  
- Timestamp  
- Basic block-related metadata  



## ABI contract retriever

The **ABI Contract Retriever** is a component that extracts and provides the Application Binary Interface (ABI) of smart contracts on the Ethereum blockchain. It focuses on retrieving the essential interface information that defines how to interact with these contracts, providing:
- Contract functions  
- Events  
- Parameter types  
- Structural interaction definitions  

This module enables the conversion of contract specifications into semantic vocabularies.

## Solidity contract retriever

The **Solidity Contract Retriever** is a component designed to retrieve and display Solidity smart contracts from the Ethereum blockchain. It focuses on accessing the source code of contracts written in Solidity and providing the semantic representation.

## Running Sancus

Just run SancusProjectApplication.java. The service should be available at localhost:80

## Compiling Sancus

To complile the  _jar_  run the following command:

```
mvn clean package -DskipTests
```
A folder called "target" will be created containing the compiled  _jar_  and a folder with dependencies.


## Requirements

- **Java** 
- **Maven 3+**  
- For blocks, access to an Ethereum node (local or provider). For ABI or Solidity smart contract, access to a third party service.

## API Keys Configuration

Sancus requires two API keys to interact with Ethereum:

1. Create a `keys.json` file in the project root directory:

```json
{
    "ethKey": "https://mainnet.infura.io/v3/YOUR_INFURA_KEY",
    "etherscanKey": "YOUR_ETHERSCAN_API_KEY"
}
```

2. Replace the placeholder values with your actual keys:
   - **ethKey**: An Ethereum node endpoint (e.g. from [Infura](https://infura.io))
   - **etherscanKey**: An API key from [Etherscan](https://etherscan.io/apis)

Keys can also be provided via command-line arguments: `--ethkey="..." --etherscankey="..."`

> **Note:** `keys.json` is listed in `.gitignore` and will not be committed to the repository.

## Running the Demo

To launch Sancus in demo mode, which provides an interactive web interface for exploring the Ethereum blockchain:

```
java -jar target/sancus.jar --demo
```

Or run `SancusProjectApplication.java` with the `--demo` argument from your IDE.

The demo will be available at [http://localhost:80](http://localhost:80). From the web interface you can:

- Enter any Ethereum block number to retrieve its data as RDF
- Discover smart contracts deployed in that block
- Obtain semantic representations of contract ABIs and Solidity source code
- Select different RDF serialisation formats
