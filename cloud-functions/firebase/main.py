""" Google Cloud Function for Firestore Chore Assignments """
from google.cloud import firestore
from google.api_core import datetime_helpers
import logging
import random
import datetime

# Create a firestore client instance
client = firestore.Client()

# Run on any changes to /Households/{HouseHoldID}/Users/{UserID} documents
def user_updates(data, context):
    update_assignments(data, context)

# Run on any changes to /Households/{HouseHoldID}/Tasks/{TaskID} documents
def task_updates(data, context):
    update_assignments(data, context)

# Compute assignment updates on changes to both users or tasks
def update_assignments(data, context):
    logging.debug(data)
    # Break up the path to the triggered resource (task)
    path_parts = context.resource.split('/documents/')[1].split('/')
    collection_path = path_parts[0]
    household_path = path_parts[1]

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
                la = task['assignedDate']
                # convert to microseconds and compute time difference
                us_la = datetime_helpers.to_microseconds(la)
                us_now = datetime_helpers.to_microseconds(now)
                us_diff = us_now - us_la
                timediff = datetime.timedelta(microseconds=us_diff)

                # TODO: use task frequency field instead of a single day
                # reassign if it's been more than a day
                if timediff.days > 0:
                    unassigned.append(task)
                elif 'assignedTo' in task:
                    uid = task['assignedTo']
                    # This user is not in the household anymore
                    if uid not in num_assignments:
                        unassigned.append(task)
                    else:
                        # Count the number of existing assignments
                        num_assignments[uid] += 1

                # Count up the number of completed tasks
                if 'completed' in task and task['completed']:
                    num_completed += 1
            else: # Never assigned before
                unassigned.append(task)

        # reassign all tasks if all are completed
        if num_completed == len(tasks):
            unassigned = tasks
            for uid in num_assignments:
                num_assignments[uid] = 0

        unassigned_ids = []
        for task in unassigned:
            unassigned_ids.append(task['taskId'])

        logging.debug("all tasks: " + str(taskIds))
        if len(unassigned_ids) == 0:
            logging.debug("no tasks to assign. done")
            return

        logging.debug("unassigned ids: " + str(unassigned_ids))

        preference_map = {}
        for user_data in users:
            uid = user_data['firebaseId']
            if 'preferences' not in user_data:
                # Give them a random preference list
                user_data['preferences'] = random.sample(taskIds, len(taskIds))

            # Make a user preference map for only unassigned tasks
            prefs = user_data['preferences']

            # Only keep preferences for tasks in the household
            to_keep = [p for p in prefs if p in taskIds]

            # Add any missing preferences randomly to the end of pref list
            to_add = [p for p in taskIds if p not in to_keep]
            random.shuffle(to_add)
            all_prefs = to_keep + to_add

            userDoc = userCollection.document(uid)

            # merge in the new user preferences back to firebase
            transaction.update(userDoc,
                               {
                                   u'preferences': all_prefs
                               })

            # Get unassigned preferences
            unassigned_prefs = [p for p in all_prefs if p in unassigned_ids]
            preference_map[uid] = unassigned_prefs

        logging.info("unassigned preferences " + str(preference_map))

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
            min_priority_users = [u for u in priorities \
                                    if priorities[u] == min_priority]

            # randomly pick a user if there are conflicts
            selected_user = random.choice(min_priority_users)

            # Remove the item from selected user's preferences
            # Leave other user's preferences untouched
            preference_map[selected_user].remove(taskId)
            logging.info('assigning task ' + taskId + ' to user ' + \
                    selected_user + ' with preference ' + str(min_priority))

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
