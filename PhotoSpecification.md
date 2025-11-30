Components:
1. Database: PostgreSQL
2. Backend: Java Spring with Hibernate for ORM, OAuth 2.0 to authenticate users against Google, JGit for working with git
3. Frontend: React Javascript
4. Scripts: in Python or bash
5. Configuration file: Javascript; will include the directory of the images, database connection parameters, OAuth connection parameters, and other configurational data
6. STAG package used to tag photos; it is contained in stag-main/stag.py
7. Git repository directory with images; its location will be contained in the configuration file

Background:
The front end of the application is in REACT. The backend using Spring connected to a PostgreSQL database.
Application color scheme is burgundy, navy blue, and cream
The application finds new photos contained in a directory that is updated by GIT.
When a new image is detected, the STAG Python script is run to generate tags. The image may also have another text file with metadata data. The tags, the metadata, file information, and EXIF information is stored in a database. EXIF information is read using the metadata-extractor library.
There are two permission levels of the application: user and administrator.
Users can search and view images and view and edit metadata for their images. 
Administrators have the abilty to search any image, view it, edit its metadata; search users and view and edit their metdata; and configure scripts.
Table pages will have a column name row that is navy blue background and cream letters, and alternating cream backgrdound (with navy text) and burgundy background (with cream text) rows. Clicking on any column header will sort the table by that column (in ascending order). Clicking again will sort the table by that column (in descending order). Above and below the table are button bars that have: A double left facing arrow button (go to first page), a single left facing arrow button (go to previous page), a drop down that allows the user to select the row count: {10, 20, 50} with a default of 10, a single right facing arrow button (go to next page), and a double right facing arrow button (go to last page). Above the top button bar is a block with a long, one line text entry, and a "search" button. Next to it is a radio button with "Quick Search" and "Long Search". When quick search is selected, the long entry text box and search button are displayed. That is the default. When "Long Search is selected, three lines with dropdowns (each of which contains the list of columns in the table) are displayed with text boxes next to them, followed by a drop down with two entries "Must contain" and "Must not contain".

Steps:

Step 1: Database configuration
Components: PostgreSQL database and configuration file
Description: Create a PostgreSQL database named PhotoSortData 
Probably the tables and fields will include:
User table: includes user name and user ID
File database: includes file ID, file name, file create date time, file modified date time, file added to system date time, file relative path, script name, script run date time the file owner, whether the file is private or public (and all exif fields)
Metadata fieldname table: includes field ID and field name
Metadata to file:includes field Id, file ID, metadata value
Tag table: includes tag ID, tag value
Tag to file: includes tag ID, file ID
File User Permissions: includes File ID, User ID
Metadata to user columns table: includes user ID and metadata ID
Additional columns table: User ID, file name, file creation date, file update date, file location, tag list, file size, file dimensions
Scripts table: Script name, script file name, script contents, run time, periodicity, file extension
Test cases: confirm that can connect to database using information in the configuration file, confirm that can create tables, add rows to tables, search data in the tables.

Step 2: Create Java Spring application 
Components: Backend java Spring application, database, configuration 
Description: Create an application named PhotoSortServices
It will include Spring Web, JDBC, Spring Data, Hibernate, PostgreSQL, OAuth 2, RESTful web services
The application will handle providing an interface to the database, authenticating against Google, and provide all necessary web services for the front end
Test cases: 
Confirm that the application starts up, authenticates against Google, can be used to search the database, can be used to add rows to the database, update rows in the databse

Step 3: Create React Javascript front end
Components React Javascript front end
Description: Create an application named PhotoSort that connects to the backend
Test cases: 
Confirm application starts up, can connect to the Spring backend.
Confirm user can login via OAuth.

Step 4: Create the user table page
Components: Frontend, backend, database
Description: When a user with administrator privileges logs in, they'll see the user table. It has the following columns: user name, first login date, last login date, user type (user or administrator), and number of images owned. There is also a button for each user that says "view images". The administrator can change a user's type. When the administrator pushes the view images page, a photo page with the user's images is displayed. 
When an administrator is logged in, there is a menu bar at the top with three options: "Users" and "Photos" and "Scripts" and "Configuration". When the administrator clicks on "Users", the users table page is displayed. When the administrator clicks on "Photos", the photo table page is displayed. When the administrator clicks on "Scripts", the scripts table page is displayed. When the administrator clicks on "Configuration" the Edit Configuration page is displayed.
Test cases: 
Confirm button bar buttons work, all users are displayed

Step 5: Create the photo table page
Components: Frontend, backend, database
Description: When a user without administrator privileges logs in, they'll see the photo table with their images, public images, and private images whose owners have added the user to the access list. When an administrator clicks on "View Images" on a user's entry in the user table, they'll see the photo table. When the administrator sees the photo table, it will have the following columns: File name, Owner, File creation date, File update date, and a thumbnail of the file. The next to last column will contain a button for "View image". When pushed, the Image display page will be displayed. If the file's access is set to private, the last column will contain a button for "Set Users". If the file's access is set to public, there will be no "Set Users" button. When the "Set users" button is pressed, the User Access dialog will be displayed.
When a user sees the photo table, the default columns they will see are: file name, file creation date, and a thumbnail. Additionally, there will be a button in the top button bar: "Modify columns". When pushed, the Modify columns dialog will be displayed
Test cases: 
Confirm that the columns displayed are the ones that are contained for this user in the metadata to user columns table and the additional columns table. 
Confirm that the thumbnail is displayed in the thumbnail column, if it's selected. 
Confirm that the "View Image" button is pushed, the Image Display page is displayed. 
Confirm that when the "Modify Columns" button is pressed, the modify columns dialog is displayed. 
Confirm that the quick search works correctly and displays the correct rows. 
Confirm that the user can add columns to search on in the long search, that the mmetadata values can be set for the long search, that the must/must not criteria can be added for the long search, and that the long search works correctly. 
Confirm that the sort on columns works.

Step 6: Creaet the modify Columns dialog
Components: Frontend, backend, database
Description: 
A popup dialog. The top has a list box with entries for "file name", "file creation date", "owner", "tags", "thumbnail", and each metadata tag. Each line has a checkbox to its left. The bottom has two buttons: "Set columns" and "Cancel".
Test cases: 
Confirm that all of the specific tags and every metadata field is in the box and that if a field or line is currently displayed in the table, that the checkbox next to it is checked (otherwise it's not). 
Confirm that when "Set Columns" is pressed, the appropriate entries are made in the metadata to user table or additional columns table in the database. 
Confirm no changes are made when Cancel is presed.

Step 7: Create the user Access dialog
Components: Frontend, backend, database
Description:
A popup dialog. The top has a list box with entries for each user in the system and a checkbox to the left of the name. The bottom has two buttons "Set Users" and "Cancel".
Test cases: 
Confirm that when the dialog comes up, all users are listed, and users who currently have access to the image have the checkbox next to their names checked (otherwise, it's not).
Confirm that when the user clicks "Set Users" appropriate entries are made in the File User Permissions table.
Confirm that when the user clicks "Cancel", no changes are made.

Step 8: Create the image display page
Components: Frontend, backend, database
Display the image in a large box to the left side of the page. Display a list of fields or metadata names and their values to the right. Provide the ability for users to delete, modify, and add metadata field values.  Display a list of tags at the bottom. Provide ability for the user to delete, modify, and add tags to the list. There is a button at the bottom "Return to list". When pressed, the 
Test cases:
Confirm the correct image is displayed
Confirm that if "Return to list" button is pressed, the Photo Table page is displayed, with the same parameters-- page number, sort, and search-- as before this page was displayed.

Step 9: Create Scripts table page
Components: Frontend, backend, database
Description: A table page with one line per script and a button at the bottom to "add script". The table has columns for: Script Name, Script File, Run time, Periodicity, File Extension. The last column is "Edit Script". When the user pushes either the "Add Script" or "Edit Script" button, the "Edit Script" dialog will popup. If Add script, the values will be blank. If "Edit Script", then the values will be the correct ones for the selected script.
Test cases:
Confirm that the full list of scripts is displayed from the data table.
Confirm for each script that the correct information is displayed.
Confirm that the Add Script button displays the edit script dialog with empty fields.
Confirm that the Edit Script button displays the edit script dialog with the values for the current script.

Step 10: Create the edit script dialog
Components: Frontend, backend, database
Description: A page with the following lines: Script name with a text entry box; Script file with a file selection box; Run Time with two boxes that allow the user to set hour and minutes with up down arrows and integer values; Periodicity with a drop down with: 1 minute, five minutes, 10 minutes, one hour, two hour, six hours, and one day; File Extension with a text entry box, and a large text edit box with the script contents. At the bottom are two buttons "Save" and "Cancel". When the "Save" button is clicked, trigger the script run engine to reread the script data table.
Test cases:
Confirm that the dialog starts with the correct values (or empty values).
Confirm that when the user presses "Save" the values are saved back to the database
Confirm that when the user presses "Cancel" the values are not saved back to the database
Confirm that if the user presses either "Save" or "Cancel" then the scripts table page is displayed with the same search, page number, and sort criteria as before the edit script dialog was displayed.

Step 11: Create the Edit Configuration page
Components: Front end, back end, Configuration file
Dewcription: When it starts up, the configuration values will be read from the file in the GIT repo directory path named "configuration.js". It will be in javascript format. Create one line with entry box for the following parameters: 
Database URI
Database username
Database password
GIT repo directory path (directory selector)
GIT username
GIT password
GIT URL
GIT run periodicity (integer text box with up/down buttons)
At the bottom will be a button for "Save configuration". Save the data back to the configuration file.
Test cases:
Confirm that the information read from the configuration file is displayed on the screen when it starts up.
Confirm that the information from the screen gets correctly written to the configuration file.

Step 12: Create the script run engine
Components: Frontend, backend, database, configuration
Description: Write a separate thread to run scripts. On startup, read the contents of the scripts table. For any script with a run time value, start up a thread to run that script each day at that time. For any script with a periodicity, start up a thread with that periodicity to run the script. For each file with a file extension, make an entry in a FileScript dictionary with the extension and the script ID. Read the configuration file. Read the GIT URL from it and other GIT required information and the git periodicity. Start up a thread to run and with that periodicity run a GIT pull in the file directory. Use jgit to determine what files have changed. Check the FileScript dictionary for each file: find a script to run on that file, run it, then if the file has been added, make an entry in the file table, read EXIF information from the photo and save in the file table. If there is another file in the directory with the same base name but an extension of ".metadata", then read it. Each line will have a metadata field name, an equals sign, and a value. If the name is "tags", then parse the value as a comma separated value of tags and add each tag to the tag table and create a tag to file row. Otherwise, convert the metadata field name to upper case and see if it already exists in the metadata fieldname table. If not, add it to the metadata fieldname table. Add a metadata to file row for that metadata field, this file, and the value.

