### Setup
Install gcloud sdk [here](https://cloud.google.com/sdk/install)

Get your gcloud project ID from either Firebase or Google Cloud Platform consoles.

Run `gcloud init` to login.

Select your Firebase/GCP project when prompted.

### Deploy 
Navigate to the cloud-functions/firebase directory

Replace `<PROJECT_ID_HERE>` with your Firebase/GCP project ID (i.e. `tidy-71f32`) and run this:

`gcloud functions deploy update_assignments --trigger-event 'providers/cloud.firestore/eventTypes/document.write' --trigger-resource 'projects/<PROJECT_ID_HERE>/databases/(default)/documents/Households/{HouseHoldID}/Tasks/{TaskID}' --runtime python37`

