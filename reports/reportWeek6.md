## Goals from a week ago:
- Complete CI setup
- Learn firebase and how to set it up for the main project
- Setup Android Studio project with some basic template screens
## Short term:
**All**
- Finished Testing and CI Assignment (5/5)
- Meet over the course of the week to discuss progress
**Frontend**
- Implement navigation between all currently developed screens sometime before the end of this week
- Work on assigned parts over the course of the week according to the schedule.
**Backend**
- We plan on meeting and creating the foundation for the backend later this week, then splitting off
and working on independent portions through next week
## Long term:
**All**
- Start connecting the frontend and backend sometime soon
- Beta release
**Frontend**
- Add some content to our All Chores and My Chores screens by 5/12
**Backend**
- Have a functioning datapath from the database to the front end
- Have a working checker module to make sure data is valid

## Progress and issues:
We created a few alternative layouts for the backend (diagrams in the requirements sheet), and
further discussed our plans to build the backend system. Everyone made more progress on
understanding Firebase and Firestore.

## Plans and future goals:
**Frontend:** As a general overview, we plan on continuing to develop our assigned modules and
connect them soon with transitions. As soon as the back-end is ready, we would like to start
connecting the front-end and back-end so we need to prepare for when that time comes.
The Beta release assignment is coming up soon, so we just need to continue progressing
and getting ready for that.

**Backend:** We plan on creating a functioning backend soon. For now, we will focus on creating
valid queries and transactions. After that is functioning, we will progress into making a
checker module that will perform any validations on the data (i.e. when chores need to be assigned),
and create any transactions as necessary before passing the data to the Model / Frontend.

## Contributions of individual team members
**Kcee Landon:** Implemented Main Screen skeleton with All Chores and My Chores fragment tabs.
Went through a few basic Android tutorials.
**Jacob Miller:** Implemented login skeleton.
**Salma El Ghayate:** Worked on setup module
**Nick Durand:** Did further work on reviewing example Firebase/Firestore apps. Added several
diagrams detailing possible layouts for the backend, including possible modules
and how they interact.
**Daniel Starikov:** Set up Github Actions CI system to automatically format our code per Google
Style Guide, build our Android app project, and run the Android Unit tests whenever there is a push
to the repository. Email notifications are sent whenever a CI task fails. I started working through
Android Studio, Firebase, and Firebase Test Lab tutorials and documentation.
**John McMahon:** Completed basic video tutorials for Android Studio and Firebase, as well as
reading through documentation for firebase integration. Can now begin writing for the main app
creating requests and stores to our top level document store.
