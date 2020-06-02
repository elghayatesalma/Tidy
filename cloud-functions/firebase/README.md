### Setup
Install gcloud sdk [here](https://cloud.google.com/sdk/install)

Get your gcloud project ID from either Firebase or Google Cloud Platform consoles.

Run `gcloud init` to login.

Select your Firebase/GCP project when prompted.

### About
`main.py` contains Python Google Cloud Function code to compute and set user task assignments in Firebase.

The user-assignments are computed whenever a task or user is modified - the user assignment update code is the same for both tasks and user changes.

We use two cloud functions because the Tasks and Users subcollections are separate and must be triggered on individually.

### Deploy 
Navigate to the cloud-functions/firebase directory in the repo

Replace `<PROJECT_ID_HERE>` with your Firebase/GCP project ID (i.e. `tidy-71f32`) and deploy the cloud functions:

`gcloud functions deploy task_updates --trigger-event 'providers/cloud.firestore/eventTypes/document.write' --trigger-resource 'projects/<PROJECT_ID_HERE>/databases/(default)/documents/Households/{HouseHoldID}/Tasks/{TaskID}' --runtime python37`


`gcloud functions deploy user_updates --trigger-event 'providers/cloud.firestore/eventTypes/document.write' --trigger-resource 'projects/<PROJECT_ID_HERE>/databases/(default)/documents/Households/{HouseHoldID}/Users/{UserID}' --runtime python37`

