# Zanaiti_MobileProgramming_Project_2026
What is Zanaiti? A Bulgarian craft explorer for Android
Zanaiti (CraftGuide) lets you discover traditional Bulgarian crafts on a map, scan them with AR, take quizzes, and earn points — all from your Android phone.

Zanaiti — also known as CraftGuide — is an Android app that brings Bulgaria’s traditional crafts to life. You can browse a curated catalogue of crafts, find nearby workshops on an interactive map, use your camera to identify craft items through augmented reality, and test your knowledge with built-in quizzes. Authenticated users earn points for every quiz they complete and craft they visit, tracked on a personal profile and a shared leaderboard.
​
Key features
Craft catalogue
Browse the full list of traditional Bulgarian crafts with descriptions, images, and location details.
Interactive map
See crafts and workshops near you on a live map, filtered by your current location.
AR scanner
Point your camera at a craft item and let the app identify it using augmented reality.
Quizzes
Answer questions about the crafts you have explored and earn points for correct answers.
Points and leaderboard
Accumulate points from quiz results and craft visits, and compare your score with other users.
Profile and progress
View your full name, username, total points, crafts visited, and quiz history in one place.
​
System requirements
Requirement	Details
Operating system	Android (ARCore-compatible device recommended)
Camera	Required for the AR scanner feature
Location	Required for the nearby-crafts map view
Internet	Required for all content and account features
The app declares android.hardware.camera.ar as a required hardware feature. If your device does not support ARCore, the AR scanner will be unavailable, but all other features remain accessible.
​
Guest mode vs. authenticated mode
When you first open Zanaiti you are presented with two paths. You do not need an account to get started.
Guest mode — browse the craft catalogue and view the map without signing in. No account or personal data is required.
Authenticated mode — create a free account to unlock quizzes, earn points, track your progress, and appear on the leaderboard.
To create an account or sign in, see Create and manage your Zanaiti account.

Get started with Zanaiti: install and explore crafts
Install CraftGuide on your Android device, create an account, and start exploring traditional Bulgarian crafts through maps, AR scanning, and quizzes.

Zanaiti CraftGuide is an Android app that lets you discover traditional Bulgarian crafts near you, identify craft objects with your camera, and test your knowledge through quizzes. This guide walks you through everything you need to go from a fresh install to browsing crafts, using the map, scanning objects, and earning your first quiz points.
1
Install the app

CraftGuide requires an Android device running API level 25 (Android 7.0 Nougat) or higher.
Before you start, make sure your device has:
Camera access — required for the AR scanner feature
Location access — required for the interactive map to show nearby crafts
Install CraftGuide from the Google Play Store or build it from the source repository.
You can choose your preferred display language — Bulgarian, English, or German — directly on the welcome screen before signing in.
2
Create an account or continue as guest

When the app opens, tap Let’s start the tour to proceed to the authentication screen.
To create a new account, tap the toggle at the bottom of the screen to switch to registration mode, then fill in:
Username
Email
Full name
Password
Tap Register to create your account immediately — no email confirmation is required.
To sign in to an existing account, enter your username and password, then tap Login.
To browse without an account, tap the guest login option. You can explore the craft list and map, but quiz points are only saved when you are signed in.
3
Browse the craft list

After signing in, you land on the home screen. Tap Craft list to open the full catalogue of traditional Bulgarian crafts.
Each entry in the list shows the craft’s name, a thumbnail image, and a short description. Tap any craft card to open its detail page, where you can read the full history, see more images, view it on the map, and start a quiz.
The craft list and all text in the app are translated automatically when you switch languages from the welcome screen.
4
Use the interactive map

From the home screen, tap Map to open the interactive map powered by OpenStreetMap.
When prompted, grant the app location permission so the map can centre on your current position and show craft locations near you.
Tap any marker on the map to see the craft associated with that location. From the marker pop-up you can navigate directly to that craft’s detail page.
5
Try the AR scanner

The AR scanner lets you point your camera at a craft object and have the app identify it using on-device machine learning.
Open the scanner in one of two ways:
Tap the camera icon in the top-right corner of the toolbar from any screen
Tap the camera floating action button on the home screen or craft list
When the scanner opens, tap Scan and point your camera at a craft object. The app analyses the image and displays the best matching craft result.
The scanner uses ML Kit on-device image labelling and does not require an internet connection to identify objects.
6
Take a quiz and earn points

Each craft has its own quiz. Open any craft’s detail page, then tap Start Quiz.
Answer the questions that appear. Correct answers earn you points, which are recorded against your account and visible on the Leaderboard — accessible from the toolbar when you are signed in.
Quiz points are only saved to your account when you are signed in. Guest users can still take quizzes, but their scores are not persisted.

Register, sign in, and manage your Zanaiti account
Register, log in, and update your Zanaiti profile to unlock quizzes, points tracking, and the full craft explorer experience on Android.

A Zanaiti account lets you take quizzes, earn points for craft visits, and track your progress over time. Registration takes under a minute and only requires a username, email address, password, and your full name. Once you are signed in, a JWT token is stored on your device so you stay logged in between sessions.
You can browse the craft catalogue and explore the map without an account. Quizzes, points, and the leaderboard require you to be signed in.
​
Create an account
1
Open the auth screen

On the welcome screen, tap Let’s start the tour. On the next screen, tap Don’t have an account? Register to switch to registration mode.
2
Enter your username

Type a username between 3 and 50 characters. Usernames must not be blank.
3
Enter your email address

Provide a valid email address. It must follow standard email format (e.g. name@example.com).
4
Enter your full name

Type your full name as you would like it displayed on your profile. This field must not be blank.
5
Choose a password

Choose a password of at least 4 characters. Passwords are masked as you type.
6
Submit

Tap the Register button. If all fields are valid, your account is created and you are signed in immediately.
​
Sign in to an existing account
1
Open the auth screen

On the welcome screen, tap Let’s start the tour. The screen opens in login mode by default.
2
Enter your username and password

Type the username and password you registered with.
3
Tap Sign in

Tap the Sign in button. The app verifies your credentials and signs you in immediately if they are correct.
​
What happens after sign-in
When login or registration succeeds, the API returns a response that includes:
A JWT token used to authenticate all subsequent requests
Your user ID, username, email, and full name
Your current total points and account role
The app stores this token securely on your device. You are redirected into the app and can immediately access all authenticated features, including quizzes, points, and your profile.
​
Update your profile
Your profile displays your full name, username, total points, crafts visited, and average quiz score.
To view your profile, navigate to the Profile screen from within the app. Your full name appears as the headline and your username is shown below it prefixed with @.
For detailed guidance on editing profile information, see Your profile.
​
Language
Zanaiti supports Bulgarian, English, and German. You can switch languages from the language selector on the welcome screen at any time. All interface text — including auth screen labels — updates automatically. For more information, see Language settings.

Features
Browse the traditional Bulgarian craft library on Zanaiti
Explore a curated library of traditional Bulgarian crafts, each with rich history, step-by-step making guides, photos, and optional video animations.

The Crafts section is the heart of CraftGuide. Here you can explore Bulgarian traditional crafts — from pottery to weaving — each presented with historical context, detailed descriptions of how the craft is made, and a location so you can find the nearest workshop.
​
What a craft profile contains
Each craft entry is built from the following fields:
Field	Description
Name	The craft’s name, displayed in your current language
Description	A short overview of the craft and its cultural significance
Historical facts	Background on the craft’s origins and evolution
Making process	A step-by-step explanation of how the craft is produced
Image	A representative photograph of the craft
Video animation	An optional embedded video showing the craft being made
Location	Geographic coordinates used to place the craft on the map
All text content — name, description, historical facts, and the making process — is available in multiple languages. The app automatically translates content to match your selected language.
​
Browsing the crafts list
When you open the Crafts tab, the app loads the full catalogue and displays each entry as a card. Every card shows:
A thumbnail image of the craft
The craft name (in your current language)
A short preview of the description (up to 80 characters)
Tap any card to open the full craft profile.
​
Viewing a craft detail
The detail screen gives you the complete profile for a single craft.
1
Open a craft

Tap a craft card from the list. The detail screen loads with the full image at the top.
2
Read the description

The description appears below the image, followed by the historical facts and the making process.
3
Watch the video (if available)

If the craft has a video animation, a player appears below the making process section. Playback starts automatically and pauses when you leave the screen.
4
Explore further

Use the Show on map button to see the craft’s location on the map, or tap Start quiz to test your knowledge about this craft.

Find craft workshops near you with the Zanaiti map
Use the interactive Zanaiti map to locate traditional craft workshops, get directions, and explore crafts closest to your current position.

The Map feature lets you discover traditional craft workshops and sites across Bulgaria on an interactive map. Each craft in the catalogue has geographic coordinates, so you can see exactly where it is practised and get turn-by-turn directions from your current location.
The map requires location permission to show your position and to calculate routes. If you deny the permission, the map cannot display your location or provide directions, but craft markers are still visible after you grant access.
​
How the map works
CraftGuide uses OpenStreetMap tiles rendered by the osmdroid library. Every craft in the catalogue appears as a marker on the map, placed at the craft’s stored latitude and longitude. The map supports pinch-to-zoom and drag-to-pan with multi-touch controls.
​
Granting location permission
1
Open the Map tab

Tap the Map icon in the bottom navigation bar.
2
Grant location access

If the app does not yet have location permission, you will see a prompt. Tap Grant access to allow the app to read your device’s GPS or network location.
3
See your position

Once permission is granted, a red marker labelled “You are here” appears at your current position, and the map centres on it.
​
Exploring craft markers
Each craft is shown as a pin on the map. Tap any marker to open a quick-info card at the bottom of the screen. The card displays:
The craft name
A short description snippet
Two action buttons: Details and Take me there
​
Getting directions
1
Tap a craft marker

Select any marker on the map to reveal the info card.
2
Tap 'Take me there'

The app calculates a route from your current location to the craft site using the OSRM routing engine and draws a blue line on the map.
3
Follow the route

The map re-centres on your position so you can track the route as you move.
4
Open the full profile

Tap Details on the info card to navigate to the craft’s full detail screen.
​
Re-centring the map
Tap the target icon button (bottom-right corner) at any time to animate the map back to your current position and zoom in to street level.

Identify craft objects with the Zanaiti AR scanner
Point your camera at a craft object and let CraftGuide identify it using on-device ML Kit object detection, then jump straight to its full profile.

The AR Scanner lets you identify traditional craft objects in the real world using your phone’s camera. CraftGuide analyses the live camera feed with Google ML Kit’s object detection and, when it recognises a supported object, takes you directly to the matching craft profile.
The AR Scanner requires camera permission. If the permission is denied, the scanner cannot function. You will be prompted to grant access when you first open the feature.
​
How AR scanning works
The scanner uses ML Kit in streaming mode to continuously analyse frames from the rear camera. It looks at the size, shape, and aspect ratio of detected objects to match them against known craft types. A match must be confirmed across three consecutive frames before the app considers the detection reliable and navigates to the craft.
Currently, the scanner is trained to recognise pottery (guvyche) — a traditional Bulgarian round clay vessel. Hold the object steady and ensure it fills at least a quarter of the camera frame for best results. Support for additional craft objects will expand in future updates.
​
Scanning an object step by step
1
Open the AR Scanner

Tap the scanner icon in the app navigation to open the AR Scanner screen.
2
Grant camera permission

If prompted, tap Grant permission to allow camera access. You must approve this before the camera preview appears.
3
Tap the Scan button

Tap the Scan button at the bottom of the screen. The status bar will show “Scanning…” to confirm the analyser is active.
4
Point at the object

Hold your phone so that the craft object is clearly visible and centred in the frame. Keep it steady and ensure good lighting.
5
Wait for recognition

The status bar shows the progress of the detection (e.g. “Recognising… 1/3”, “Recognising… 2/3”). When the object is confirmed, you will see a confirmation card on screen.
6
View the craft profile

After a brief moment, the app automatically navigates to the matched craft’s detail screen where you can read its full history, making process, and more.
​
What happens when an object is detected
When the scanner confirms a match, a card appears at the centre of the screen indicating the recognised craft. After 1.5 seconds the app navigates to the corresponding craft profile. You do not need to tap anything — the navigation happens automatically.

Test your Bulgarian craft knowledge with Zanaiti quizzes
Take per-craft multiple-choice quizzes, get instant feedback on every answer, and earn points that count towards the Zanaiti leaderboard.

Each craft in CraftGuide has its own quiz that tests what you have learned about that craft. Quizzes consist of multiple-choice questions with four options each, instant feedback after every answer, and a summary screen at the end showing how many you got right and how many points you earned.
​
How quizzes work
A quiz is tied to a specific craft and loads a set of questions from the server for that craft. Each question has four answer options labelled A, B, C, and D. You select one option, confirm your choice, and immediately see whether you were right or wrong. The quiz then moves to the next question automatically after showing the feedback.
Every correct answer awards a number of points defined by that question’s pointsReward value. Your running score is shown at the top of the screen throughout the quiz.
​
Starting a quiz
1
Open a craft profile

Browse the Crafts list and tap any craft to open its detail screen.
2
Tap 'Start quiz'

Scroll to the bottom of the detail screen and tap the Start quiz button.
3
Answer each question

Read the question card, then tap one of the four answer buttons to select your answer.
4
Confirm your answer

Tap Confirm to submit. The app immediately shows whether your answer was correct or incorrect, then advances to the next question.
5
Complete the quiz

After the final question, the result screen appears.
​
Quiz result screen
When you finish all questions, you see a summary that includes:
Correct answers — the number of questions you answered correctly out of the total
Points earned — the total points accumulated during this quiz session
Tap Back to details to return to the craft profile.
​
Saving progress and earning points
You must be logged in to save your quiz results and earn points that appear in your profile and on the leaderboard. If you are browsing as a guest, you can still take quizzes and see your score for the session, but the results will not be saved and no points will be added to your account.
When you are logged in and finish a quiz, CraftGuide automatically sends your result to the server and records the points under your account. These points are reflected in your profile statistics and contribute to your leaderboard ranking.

Earn points and climb the Zanaiti craft leaderboard
Discover how CraftGuide awards points for quizzes and craft visits, and how the leaderboard ranks all players by their total score.

CraftGuide rewards your curiosity about Bulgarian crafts with a points system. Complete quizzes, visit craft sites, and receive bonus points to build up your score — then see how you compare with other users on the leaderboard.
You must be logged in to earn points, save your progress, and appear on the leaderboard. Guest users can explore crafts and take quizzes, but points are not recorded.
​
How you earn points
Points are awarded from three sources:
Source	Label in app	When awarded
Quiz completion	QUIZ	When you finish a craft quiz while logged in. Points are calculated from the number of correct answers and each question’s individual reward value.
Craft visit	VISIT	When the server records that you have visited a craft (interacted with a craft while logged in).
Admin bonus	ADMIN	A bonus granted manually by an administrator.
​
Viewing the leaderboard
1
Open the Leaderboard screen

Tap the leaderboard icon in the app navigation.
2
Browse the rankings

The leaderboard shows all users sorted by total points from highest to lowest. Each row displays the user’s rank, username, and point total.
3
Spot the top players

The top three positions are marked with medal icons: gold for 1st place, silver for 2nd, and bronze for 3rd. All other ranks display their position number.
​
Viewing your points history
Your personal points history is available on your Profile screen. Navigate to your profile to see:
Total points — your cumulative score across all activities
Crafts visited — the number of distinct crafts you have interacted with
Average quiz score — your average percentage across completed quizzes
Points history — a chronological list of individual point awards, each showing the source (QUIZ, VISIT, or ADMIN), a description, and how many points were awarded

View your Zanaiti profile, stats, and points history
See your full name, username, activity stats, and points history — and update your display name or username directly from the Zanaiti app.

Your profile is your personal hub inside Zanaiti. It shows who you are, how far you have explored the world of traditional crafts, and a full history of every point you have earned along the way.
You must be logged in to access your profile. If no account session is active, the app shows a prompt asking you to sign in first.
​
What the profile screen shows
When you open your profile, you see three sections:
Identity card — your full name and username (@handle) displayed at the top.
Stats card — a summary of your activity across all crafts:
Stat	What it means
Points	Your total accumulated points
Visited	The number of distinct crafts you have visited
Avg. score	Your average quiz score across all completed quizzes, shown as a percentage
Points history — the last 15 point-earning events on your account, ordered from most recent. Each entry shows:
Source — where the points came from: Quiz, Visit, or Admin bonus
Description — a short note about the specific event (for example, which craft a quiz was completed for)
Points awarded — the number of points added for that event
​
How to open your profile
1
Open the navigation menu

Tap the navigation icon in the app to open the bottom navigation bar or side drawer.
2
Select Profile

Tap Profile to open the profile screen. The app loads your identity card, stats, and points history automatically.
3
Refresh if needed

If any section fails to load, tap Try again to reload all profile data.
​
How to update your profile
You can change your full name and username from within the app. When you save a change, the app submits the update and your profile reflects the new information immediately.
1
Open your profile

Navigate to the profile screen as described above.
2
Tap Edit

Tap the edit control on your identity card to enter edit mode.
3
Update your details

Change your full name, your username, or both.
4
Save

Confirm the change. The app submits the update to the server and refreshes the displayed values.
​
Stats explained
Total points is the sum of every point you have earned from craft visits, completed quizzes, and any admin-awarded bonuses.
Crafts visited is the count of unique crafts for which a visit has been recorded. Revisiting the same craft does not increase this number.
Average quiz score is calculated across all quizzes you have completed. It is expressed as a percentage — for example, 80.0% means you answered 80 % of all quiz questions correctly on average.
​
Points history entries
The history list displays up to 15 of your most recent entries. Each entry has three fields:
Source identifies the type of event: Quiz for quiz completions, Visit for craft visits, and Admin bonus for points awarded manually by an administrator.
Description gives context — for example, the name of the craft the quiz was for.
Points shows how many points that single event added to your total.

Switch languages in Zanaiti: Bulgarian, English, German
Zanaiti supports Bulgarian, English, and German. Learn how to switch languages and what content gets translated automatically across the entire app.

Zanaiti is available in Bulgarian, English, and German, with Bulgarian set as the default. You can switch languages at any time from the welcome screen, and the app translates UI labels, craft content, and quiz material on the fly.
​
How to switch the language
1
Open Settings or the language toggle

Find the language control in the app’s settings or the dedicated language toggle in the navigation.
2
Select your language

Tap English or Bulgarian to set your preferred language. The app begins applying the change immediately.
3
Wait for the translation model to download

The first time you switch to English, the app downloads the translation model in the background. A loading indicator is shown while the model is being prepared. This download requires a Wi-Fi connection.
4
Use the app normally

Once the model is ready, the new language is active across the entire app.
The translation model download requires Wi-Fi. If you are not connected to Wi-Fi when you first switch to English, the download will not start and the app will remain in Bulgarian until a Wi-Fi connection is available.
​
What gets translated
When you switch to English, the following content is translated:
Craft names and descriptions — the title and overview text shown on each craft card and detail screen
Historical facts — the historical background sections within craft details
Making process — step-by-step process descriptions for each craft
Quiz questions and answer options — all question text and selectable answers for every craft quiz
UI labels — navigation items, button labels, screen titles, and status messages throughout the app
Map markers — labels on the map view that identify craft locations
AR scanner status text — prompts and status messages shown during augmented reality scanning
Craft content is translated on demand using the in-app translation service. If a translation cannot be produced — for example, due to a network error — the app falls back to showing the original Bulgarian text.
​
How translation works
Zanaiti uses Google ML Kit’s on-device translation engine. When you switch to English or German, the app passes the Bulgarian source text through the translation model and displays the result in place of the original.
Because translation happens on-device after the model is downloaded, most translations work without an active internet connection once the model is ready. The model only needs to be downloaded once per language.

Track your Zanaiti craft visits, quizzes, and progress
Learn how Zanaiti records craft visits and quiz completions, what your progress summary includes, and how to climb the leaderboard.

Zanaiti keeps a record of every craft you explore and every quiz you complete. Your progress builds over time, earning you points and a place on the leaderboard among all Zanaiti users.
​
What progress tracking covers
For each craft, Zanaiti tracks:
Whether you have visited it — a visit is recorded the first time you interact with a craft
Quiz completion — whether you have completed the quiz for that craft, and your score
Points earned — the total points you have accumulated from visits and quizzes across all crafts
These three dimensions combine into a progress summary that you can view from your profile at any time.
​
How visits are recorded
A visit is recorded when you explicitly record it for a craft. The app sends the visit to the server, which creates or updates your progress entry for that craft.
1
Open a craft

Navigate to any craft from the map, the craft list, or the AR scanner.
2
Record the visit

Tap View on map or the dedicated visit action on the craft detail screen. The app registers the visit with the server.
3
Check your profile

Your crafts-visited count on the profile stats card increases to reflect the new visit.
​
Quiz completion and scoring
When you finish a quiz for a craft, the app submits your answers and the number of correct answers to the server. The server calculates your score, awards points, and stores the result against your progress record for that craft.
Each craft’s progress entry tracks:
Field	Description
Quiz completed	Whether you have finished the quiz at least once
Attempt count	How many times you have attempted the quiz
Quiz score	Your score on the most recent attempt
Last interaction date	When you last visited or completed the quiz
​
Progress summary stats
Your progress summary, visible on the profile screen, contains four figures:
Total points — the sum of all points earned from visits, quizzes, and admin bonuses
Crafts visited — the number of unique crafts you have visited
Quizzes completed — the number of crafts for which you have finished the quiz
Average score — your mean quiz score across all completed quizzes, expressed as a percentage
​
How to view your progress
Your progress summary is shown on the Profile screen. Open the navigation bar and tap Profile to see your current stats at a glance.
For a craft-by-craft breakdown, the app fetches your full progress list from the server — one entry per craft you have interacted with.
​
The leaderboard
The leaderboard ranks every Zanaiti user by total points. It is available from within the app and is updated in real time as users earn new points.
The fastest way to climb the leaderboard is to visit new crafts and complete their quizzes. Each visit and each completed quiz awards points — and a higher quiz score earns more points than a partial one. Focus on crafts you have not visited yet to maximise your gains.

FAQ
Zanaiti FAQ: features, account, AR, and offline use
Answers to common questions about the Zanaiti / CraftGuide Android app — features, requirements, account, language, and offline use.

Zanaiti (also known as CraftGuide) is an Android app that helps you discover and learn about traditional Bulgarian crafts. The questions below cover everything from account setup and device requirements to how points and the AR scanner work.
What is Zanaiti / CraftGuide?
Zanaiti is an Android app dedicated to exploring traditional Bulgarian crafts such as pottery and weaving. You can browse a list of crafts, view them on an interactive map, read historical facts and descriptions, take quizzes to test your knowledge, and use the AR scanner to identify craft objects through your camera. The app supports multiple languages and tracks your progress on a leaderboard.

Is Zanaiti free to use?
Yes. You can browse crafts, view the map, and read craft details without paying anything. Creating an account is also free. Some features — such as saving quiz progress and appearing on the leaderboard — require you to register and log in, but registration itself has no cost.

Do I need an account to use the app?
No. You can explore crafts, view the map, and use the AR scanner as a guest. However, your quiz scores and points are only saved to the server when you are logged in. Guest users can still take quizzes and see their score locally during that session, but the results will not appear on the leaderboard or persist after you close the app.

What languages does the app support?
The app interface supports three languages: Bulgarian (BG), English (EN), and German (DE). You can switch between them using the language selector on the welcome screen. The app uses Google ML Kit to translate UI text on the fly, so switching may require a brief download of the language model the first time you select a new language. Note that some craft content — such as names and descriptions — may only be available in Bulgarian if an English translation has not been added yet.

Which Android version is required?
The app targets Android 13 (API level 33, “Tiramisu”) and requires a device with a rear camera that supports ARCore. The android.hardware.camera.ar feature is declared as required in the app manifest, so the app is only available to devices that meet that hardware requirement. Older devices without ARCore support may not be able to install or run the app correctly.

Does the app work offline?
Partially. The map tiles and craft data are loaded from the backend server and from OpenStreetMap, so an internet connection is needed to view up-to-date content. The AR scanner processes images on-device using Google ML Kit and does not require a network connection to detect objects. Quiz questions, however, are fetched from the server each time, so you need to be online to load them. Saving quiz results also requires a live connection to the backend.

How do I earn points?
Points are awarded when you complete a quiz for a craft while logged in. Each correct answer in a quiz earns you the number of points defined for that question — in the default data set, each correct answer is worth 10 points. Your total points accumulate across all crafts. Points are only recorded on the server when you are signed in; completing a quiz as a guest does not save any points.

What are the top 3 spots on the leaderboard?
The leaderboard ranks all registered users by their total accumulated points. The top three positions are marked with medals:
1st place — gold medal
2nd place — silver medal
3rd place — bronze medal
All other positions are shown with their numeric rank. You can view the leaderboard from the main screen. It is refreshed from the server each time you open it.

How does the AR scanner work?
The AR scanner uses your device’s rear camera together with Google ML Kit’s object detection to identify craft objects in real time. To scan, open the AR Scanner screen, point your camera at an object, and tap the scan button. The app processes frames continuously and requires at least three consecutive detections of the same object before confirming a result. Currently the scanner is trained to recognise pottery objects (specifically clay pots). Make sure the object is well-lit, fills a reasonable portion of the frame, and that you hold the camera steady for the best results.

What crafts can I explore?
The app currently includes two crafts in its default data set:
Pottery (Грънчарство) — the traditional Bulgarian craft of shaping clay vessels on a potter’s wheel and firing them in a kiln. Both Bulgarian and English content are available.
Weaving (Тъкане) — the craft of producing handmade carpets and textiles on a loom, known for its unique patterns. Both Bulgarian and English content are available.
Each craft includes a name, description, historical facts, a making process guide, an image, a video animation, GPS coordinates so you can find nearby workshops on the map, and a set of quiz questions.
