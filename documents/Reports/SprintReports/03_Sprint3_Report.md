# Sprint 3 Report (11/6/25 - 12/7/25)

## [YouTube link of Sprint 3 Video](https://www.youtube.com/watch?v=udwn3J6kzpc)
## What's New (User Facing)
 * Inventory variable quantity input implemented
 * Student sign-in and registration functionalities updated
 * Landing page has been updated with student portal and staff/admin portal
 * Role-based user sign-in and registration features implemented
 * Users now have separate sign-in pages and homepages depending on role
 * Staff/admin users can now access food inventory, volunteer forms, and card reader
 * Staff/admin users can now add admin users and see a list of current admin users
 * Customer users can now access barcode scanner

## Work Summary (Developer Facing)
During Sprint 3, our team focused on beginning the development of the sign-in and registration system requirement, based on a previous user recall feature implemented for this system. Users were separated into students and admin/staff roles, giving each role their own set of accessible operations based on their role. New student users must still provide their class standing and major upon registration, while returning student users are recalled and brought to the student homepage with access to the barcode scanner. Staff/admin users now have access to appropriate operations, such as the food inventory database and the volunteer forms. Staff/admin users can also create new admin users for new volunteers at the food pantry, as well as see a list of current admin users. For this final sprint of the semester, our team faced issues regarding communication caused by scheduling conflicts. An influx of end-of-semester assignments and exams made it difficult for the members of our team to manage deliverables for this project; however, we managed to persevere due to strategies and understandings developed during previous sprint deadlines. This, along with developing testing strategies between components in our system, served as key learning moments during the timeline of this sprint. These testing strategies will assist us in the future to ensure our application is operating as expected. 

## Unfinished Work
We were able to nearly complete the work we expected to during the duration of this sprint. However, the sign-in and registration system is still incomplete without magstripe reader integration, which will be the focus of our next sprint.

## Completed Issues/User Stories
Here are links to the issues that were completed in this sprint:

 * [Create Frontend for Landing Page](https://github.com/meebloe/11-FA25-SP26-F-WEB/issues/9)
 
 ## Incomplete Issues/User Stories
Here are links to issues that we did not complete during this sprint but will focus on during future sprints:

* [Implement Login System](https://github.com/meebloe/11-FA25-SP26-F-WEB/issues/7)
* [Integrate Hardware Tools](https://github.com/meebloe/11-FA25-SP26-F-WEB/issues/10)
* [Inventory Functionality](https://github.com/meebloe/11-FA25-SP26-F-WEB/issues/11)

## Code Files for Review
Please review the following code files, which were actively developed during this sprint, for quality:
 * [All code files in the folder backend were added and created during this sprint](https://github.com/meebloe/11-FA25-SP26-F-WEB/tree/sprint3-student_admin/code/backend)

## Retrospective Summary
Here's what went well:
  * We were able to begin the testing and verification process. 
  * We completed the issues and began functionalities we set out to do for Sprint 3.
  * We updated our clients frequently on our project, and they were very happy with our progress.
 
Here's what we'd like to improve: 
   * We would like to improve UI to begin receiving feedback on this portion.
   * We would like to keep up with biweekly meetings with our client during next semester.
   * We would like to update and organize documentation more effectively. 
  
Here are changes we plan to implement in the next sprint:
   * We will begin magstripe reader integration for our next sprint. 
   * We will be working on setting up the environment for our clients to use for testing user workflows.
   * We will continue our inventory management implementation by beginning storage of food checked out, which will be used for future report generation. 
