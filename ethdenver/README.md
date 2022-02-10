# Building web3 on Algorand

# Developer Environment - Setup

## Sandbox
Algorand Sandbox provides APIs to Algorand infrastructure running locally on your workstation. Docker Desktop is required. 

### Install
```
git clone https://github.com/algorand/sandbox
cd sandbox
```

### Starting a local Algorand network

> :information_source: **Recommended**
> Most developers should begin their development using a local private network. See later section for starting a public network

```
./sandbox up
```
### Components
Sandbox provides:

| Tool | Description | 
| ---- | ----------- | 
| `goal` | CLI transaction builder 
| `kmd` | wallet (key manager) | 
| `indexer` | blockchain query tool | 

### CLI tools
Sandbox provides `goal` as the primary CLI tool for building, signing and sending transactions. SDKs provide clients to `algod` for accessing node APIs using:

- Address: `http://localhost:4001` 
- Token: `aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa`

### KMD wallet
Sandbox provides `kmd` the _key management daemon_ for storing private keys and signing transactions. SDKs provide clients for accessing APIs using:

- Address: `http://localhost:4002` 
- Token: `aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa`

> :information_source: **Notice**
> `goal` will automatically start `kmd` when requesting transaction signing. SDK clients must ensure `kmd` is running to successfully access the APIs.

### Indexer
Sandbox provides `indexer` as the primary query tool for committed blockchain data. SDKs provide clients for accessing APIs using:

- Address: `http://localhost:8980`

> :information_source: **Notice**
> A _token_ is not required

### Interacting with Sandbox

#### Default Accounts
Sandbox running as a _local private network_ provides three (3) default accounts when the network is initialized. You may view these accounts at anytime using the following command: 
```
./sandbox goal account list
```
Sample output:
```
[online]    QYL3G7H63N3JUH6LUX3LPHWOW6AT2XYQ6GD4E3OP7KBTFKWXGTF7FW3VM4  QYL3G7H63N3JUH6LUX3LPHWOW6AT2XYQ6GD4E3OP7KBTFKWXGTF7FW3VM4  4000000000000000 microAlgos
[online]    RCJBNCQOMRBBBDG4TEVOYKUQMCFMLZ2RUWBQP3HARYBJRH5YBYRP52MFKI  RCJBNCQOMRBBBDG4TEVOYKUQMCFMLZ2RUWBQP3HARYBJRH5YBYRP52MFKI  4000000000000000 microAlgos
[online]    457AGKGZUM5QC5ESLXMGL5YD3I2A7MJDRNPYFBHKJ3PGLZMS4SQYXQBMOE  457AGKGZUM5QC5ESLXMGL5YD3I2A7MJDRNPYFBHKJ3PGLZMS4SQYXQBMOE  2000000000000000 microAlgos
```

> :information_source: **Notice**
>These pre-funded accounts are stored within a wallet maintained by `kmd`

#### First Transaction
Send your first transaction using the `goal` CLI tool:
```
./sandbox goal clerk send --amount 123456789 \
                          --from RCJBNCQOMRBBBDG4TEVOYKUQMCFMLZ2RUWBQP3HARYBJRH5YBYRP52MFKI \
                          --to 457AGKGZUM5QC5ESLXMGL5YD3I2A7MJDRNPYFBHKJ3PGLZMS4SQYXQBMOE
```

> :information_source: **Notice**
>the `--amount` flag specifies the unit of microAlgos. 1 ALGO is 1_000_000 microAlgo.

#### Query Confirmed Transactions
Use `curl` to query the Indexer for all transactions:
```
curl "localhost:8980/v2/transactions?pretty"
```

### APIs

### SDKs

### Integrated Development Environment (IDE)

#### VSCode Extention

## Account Generation

## Asset Creation (ASA)

## Atomic Transactions

## Smart Contracts

### Programs

### State Schema

### Deploying

### Calling
