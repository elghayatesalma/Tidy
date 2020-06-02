import random
import copy

users = [1, 2, 3, 4]
taskIds = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p']
preference_map = {uid : random.sample(taskIds, len(taskIds)) for uid in users}
prevTasks = {taskId: ([], False) for taskId in taskIds}


#  print(tasks, users)

numTasks = len(taskIds)

for i in range(20):
    pref_map = copy.deepcopy(preference_map)
    assignments = {}
    for taskId in taskIds:
        priorities = {}
        # Go through all the preferences of users
        for user, preferences in pref_map.items():
            numAssignments = (numTasks - len(preferences))**2
            # Find the index of the task in their preference list
            priorities[user] = pref_map[user].index(taskId) + numAssignments

        # Find the minimum priority users
        min_priority = min(priorities.values()) 
        min_priority_users = [user for user in priorities \
                                if priorities[user] == min_priority]

        selected_user = None

        # Resolve priority conflicts by looking at history
        if len(min_priority_users) > 1:
            max_priority = -1
            for user in min_priority_users:
                # Give the task to the user who had it least recently
                if user in prevTasks[taskId][0]:
                    priority = prevTasks[taskId][0].index(user)
                    if priority > max_priority:
                        selected_user = user
                        max_priority = priority
                else:
                    # This user hasn't been assigned this task recently at all
                    selected_user = user
                    break
        else:
            selected_user = min_priority_users[0]

        if selected_user in assignments:
            assignments[selected_user].append(taskId)
        else:
            assignments[selected_user] = [taskId]

        pref_map[selected_user].remove(taskId)

    for user, tasks in assignments.items():
        for task in tasks:
            if user in prevTasks[task][0]:
                prevTasks[task].remove(user)

            prevTasks[task][0].insert(0, user)

    for user in users:
        print(str(user) + ":", assignments[user])

    print("\n")
