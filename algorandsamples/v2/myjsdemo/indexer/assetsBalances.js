const algosdk = require('algosdk');
// const indexer_token = "";
// const indexer_server = "http://localhost";
// const indexer_port = 8981;
// const indexer_server = "https://testnet-algorand.api.purestake.io/idx2/";
// const indexer_port = "";
// const indexer_token = {
//     'X-API-key': 'B3SU4KcVKi94Jap2VXkK83xx38bsv95K5UZm2lab',
// }
const indexer_server = "https://testnet.algoexplorerapi.io/idx2/";
const indexer_port = "";
const indexer_token = "";
// Instantiate the indexer client wrapper
let indexerClient = new algosdk.Indexer(indexer_token, indexer_server, indexer_port);

(async () => {
    let assetIndex = 29340683;
    let assetInfo = await indexerClient.lookupAssetBalances(assetIndex)
    .do();
    console.log("Information for Asset: " + JSON.stringify(assetInfo, undefined, 2));
})().catch(e => {
    console.log(e);
    console.trace();
});