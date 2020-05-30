""" Google Cloud Function for Firestore Chore Assignments """
from google.cloud import firestore
import logging
import random
import datetime

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
        now = datetime.datetime.now()

        # Retrieve all the tasks in this household
        tasks = []
        taskIds = []
        for task_doc in taskCollection.stream(transaction):
            task_dict = task_doc.to_dict()
            taskIds.append(task_dict['taskId'])
            tasks.append(task_dict)

        # Collect the users
        users = []
        num_assignments = {}
        for user in userCollection.stream(transaction):
            user_data = user.to_dict()
            num_assignments[user_data['firebaseId']] = 0
            users.append(user_data)

        unassigned = []
        num_completed = 0
        # Go through tasks and look for unassigned tasks
        for task in tasks:
            if 'assignedDate' in task:
                last_assigned = task['assignedDate']
                # reassign if it's been more than a day
                # TODO: use task frequency field instead of a single day
                if (now - last_assigned).days >= 1:
                    unassigned.append(task)
                elif 'assignedTo' in task and \
                        ('completed' not in task \
                            or task['completed'] is False):
                    uid = task['assignedTo']
                    # This user is not in the household anymore
                    if uid not in num_assignments:
                        unassigned.append(task)
                    else:
                        num_assignments[uid] += 1
                else:
                    num_completed += 1
            else: # Never assigned before
                unassigned.append(task)

        # reassign all tasks if all are completed
        if num_completed == len(tasks):
            unassigned = tasks

        preference_map = {}
        for user_data in users:
            uid = user_data['firebaseId']
            # Make a user preference map for only unassigned tasks
            if 'preferences' in user_data:
                prefs = user_data['preferences']

                # Add any missing preferences randomly to the end of pref list
                to_add = [p for p in taskIds if p not in prefs]
                random.shuffle(to_add)
                all_prefs = prefs.extend(to_add)

                # Get unassigned preferences
                preference_map[uid] = [p for p in all_prefs if p in unassigned]
            else:
                # Give them a random preference list
                preference_map[uid] = random.sample(unassigned, len(unassigned))


        # Compute user assignments
        num_tasks = len(unassigned)
        for task in unassigned:
            priorities = {}
            taskId = task['taskId']

            # Go through all the preferences of users
            for uid, prefs in preference_map.items():
                n = (num_tasks - len(prefs)) + num_assignments[uid]
                priorities[uid] = prefs.index(taskId) + (n**2)

            # Find the minimum priority users
            min_priority = min(priorities.values()) 
            min_priority_users = [u for u in priorities.keys() \
                                    if priorities[u] == min_priority]

            # randomly pick a user if there are conflicts 
            selected_user = random.choice(min_priority_users)
            task['assignedTo'] = selected_user
            logging.info('assigning task ' + taskId + ' to user ' + selected_user + ' with preference ' + str(min_priority))

            taskDoc = taskCollection.document(taskId)
            # merge in the new assigned field and assigned date
            transaction.update(taskDoc,
                               {
                                   u'assignedTo': selected_user,
                                   u'assignedDate': now,
                                   u'completed': False
                               })


    # Run the update assignments transaction
    transaction = client.transaction()
    update_assignments_in_transaction(transaction, taskCollection, userCollection)

    logging.info('done updating task documents')
