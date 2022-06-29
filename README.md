# cariq-gcp-functions
All car IQ gcp function will be added under this repo in github

1. Add-fuel-transaction-to-Geotab
   Job fetch latest fuel Transaction from DB and call GeoTab Api to push data. GCP scheduler hit this Job every 10 minutes.
   
   Resources:

2. Add-fuel-transaction-to-Fleetio
   Job fetch latest fuel Transaction from DB and call Fleetio Api to push data. GCP scheduler hit this Job every 10 minutes.
   
   Resources:
   
   https://developer.fleetio.com/docs/create-fuel-entry
   
   https://developer.fleetio.com/docs/fuel-types
