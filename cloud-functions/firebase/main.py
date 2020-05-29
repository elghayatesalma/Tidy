""" Google Cloud Function for Firestore Chore Assignments """
from google.cloud import firestore
import logging

# Create a firestore client instance
client = firestore.Client()

# Run on any changes to /Households/{HouseHoldID}/Tasks/{TaskID} documents
def update_assignments(data, context):
    # Break up the path to the triggered resource (task)
    path_parts = context.resource.split('/documents/')[1].split('/')
    collection_path = path_parts[0]
    household_path = path_parts[1]
    tasks_path = path_parts[2]

    # Get the document and collections for this Task update trigger
    householdDoc = client.collection(collection_path).document(household_path)
    taskCollection = householdDoc.collection(u'Tasks')
    userCollection = householdDoc.collection(u'Users')

    @firestore.transactional
    def update_assignments_in_transaction(transaction, taskCollection, userCollection):
        # Retrieve all the tasks in this household
        tasks = []
        for taskDoc in taskCollection.stream(transaction):
            tasks.append(taskDoc.to_dict())

        # Build a preference map from users to their preferences
        preference_map = {}
        for user in userCollection.stream(transaction):
            userData = user.to_dict()
            preference_map[userData['firebaseId']] = userData['preferences']

        # Compute user assignments
        for task in tasks:
            taskId = task['taskId']
            min_priority = 1000000
            selected_user = None
            # Go through all the preferences of users
            for user in preference_map.keys():
                # Find the index of the task in their preference list
                priority = preference_map[user].index(taskId)
                # Keep the minimum index and use that as the assignment
                if priority < min_priority:
                    min_priority = priority
                    selected_user = user

            task['assigned'] = selected_user
            logging.info('assigning task ' + taskId + ' to user ' + selected_user + ' with preference ' + str(min_priority))

        # Update the assignments in firestore
        for task in tasks:
            # get the document using the taskID
            taskDoc = taskCollection.document(task['taskId'])
            # merge in the new assigned field
            transaction.update(taskDoc, { u'assigned': task['assigned'] })

        logging.info('all tasks: ' + str(tasks))

    # Run the update assignments transaction
    transaction = client.transaction()
    update_assignments_in_transaction(transaction, taskCollection, userCollection)

    logging.info('done updating task documents')
