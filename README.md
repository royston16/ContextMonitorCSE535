# ContextMonitorCSE535
Context Monitor project 1 done by Royston Fernandes in Android Studio

Q1. Imagine you are new to the programming world and not proficient enough in coding. But, you have a brilliant idea where you want to develop a context-sensing application like Project 1. You come across the Heath-Dev paper and want it to build your application. Specify what Specifications you should provide to the Health-Dev framework to develop the code ideally.

   Answer :
Here is how I would outline my specifications. My system will use the CameraX API of the smartphone to make a rough estimation of the heart rate through frames captured from a video. It will also use the accelerometer or orientation sensor of the smartphone in order to make an estimate of the respiratory rate by body movements. Furthermore, this system should have a local database implemented by SQLLite for heart rate, respiration rate, and symptomatic user information.

I would use a virtual sensor through the CameraX API for frame capture. The sampling frequency could be 30 frames per second, which would be enough for real-time heart rate detection. The algorithm provided as helper code can be used for extracting the heart rate from the variations in skin tone. This will be realized on the Android platform using the CameraX API. Communication protocols may not be required unless data has to be sent across to another device or server. 
Sample AADL specification: 
system implementation CameraHeartRateSystem
  subcomponents
    CameraSensor: system CameraHeartRateSensor {
      SensorParameter::SamplingFrequency => 30Hz;
      SensorParameter::Algorithm => HeartRateExtraction;
      SensorParameter::Platform => Android_CameraX;
    };
end CameraHeartRateSystem;

In the case of estimating respiratory rate, I would use a physical sensor, that is, the accelerometer to pick up movement signals. The frequency at which it will sample is set to 50 Hz, which would be enough to detect respiration patterns. It will make use of an algorithm provided in the helper code to deduce the respiratory rate from these movements. This will also be implemented on the Android using the SensorManager API. Similarly to the above, there may or may not be a need for a communication protocol depending on whether data is exchanged outside the device. 

AADL specification: 
system implementation AccelerometerRespiratoryRateSystem
  subcomponents
    MotionSensor: system RespiratoryRateSensor {
      SensorParameter::SamplingFrequency => 50Hz;
      SensorParameter::Algorithm => RespiratoryRateComputation;
      SensorParameter::Platform => Android_Accelerometer;
    };
end AccelerometerRespiratoryRateSystem;

The following will be my UI elements so that the Health-Dev framework can provide appropriate bindings and controls to my Android application. I will declare a TextView with the ID tvHeartRate for displaying heart rate, and a Button with the ID btnSelectVideo to activate heart rate monitoring. For respiratory rate display, I will declare a TextView with ID tvRespiratoryRate for displaying respiratory rate and a Button with ID btnReadCSV to retrieve historical data or start respiratory rate monitoring. These will also be defined in my AADL specification to ensure that they are appropriately integrated into the application.

system implementation HealthMonitoringApp_UI
  properties
    UIComponent::TextView_HeartRate => "tvHeartRate";
    UIComponent::TextView_RespiratoryRate => "tvRespiratoryRate";
    UIComponent::Button_StartHeartRate => "btnSelectVideo";
    UIComponent::Button_ReadCSV => "btnReadCSV";
end HealthMonitoringApp_UI;


I also need to define where and how the data will be stored; hence, I will be providing an implementation for my application's DatabaseHelper.kt by using either SQLite for data persistence. These data entities include heart rate data, which consists of a heart rate value and respiratory rate value; and symptom ratings, which store ratings entered by the user for symptoms according to 10 attributes. 

system implementation HealthMonitoringApp_DataStorage
  subcomponents
    DatabaseHelper: system LocalDatabase {
      DatabaseParameter::Tables => ("SymptomsRatingsTable");
      DatabaseParameter::Schema => SQLite;
    };
end HealthMonitoringApp_DataStorage;


The app should ideally utilize communication protocols like Bluetooth or Wi-Fi when it has to send data, say to a cloud server or another device. Bluetooth would be used for the communication between devices, while Wi-Fi is used to send the data to any server or into the cloud.
Afterwards, the Health-Dev framework would realize my project by translating my AADL specifications using the parser module into Android-specific code. It would generate Kotlin code using adequate Android APIs, such as CameraX for heart rate and SensorManager for respiratory rate. It will map the specified algorithms, such as PPG and FFT, to actual Kotlin functions that process sensor data. The code generation module will automatically generate this sensor management, UI binding, and data storage. It will implement the algorithms for heart rate extraction and computation of respiratory rate based on data provided by CameraX and SensorManager. The code generated will be integrated with my DatabaseHelper.kt, storing and retrieving sensor data and symptom ratings.
The final output will be a real-time display of heart rate and respiratory rate using the UI elements generated by the framework. It logs data into my local database, from which afterward can be retrieved or forwarded if required. 



Q2. In Project 1 you have stored the user’s symptoms data in the local server. Using the bHealthy application suite how can you provide feedback to the user and develop a novel application to improve context sensing and use that to generate the model of the user?
  Answer:
  My current project is already gathering information about the user's state, such as heart rate and respiration rate, through the use of buttons and text views that are defined within my activity_main.xml. Through the utilization of the bHealthy suite, this may be further enhanced to include sensor data from ECG and EEG sensors directly into my application. This will let me track with far greater precision, in real-time, the user's physiological states, like whether they are in a state of stress or relaxation. In other words, instead of reading respiration rate from a CSV, I will be using sensors to detect live data from changes in the user's state while he or she interacts with my application. 
I would integrate in bHealthy by refactoring these various existing UI elements in MainActivity.kt to give immediate feedback based on physiological data. For instance, if an EEG shows frustration is high, my app could immediately suggest doing a specific activity. For example, taking a break or do some calming exercise through a pop-up or dynamically changing the UI elements highlighting options for relaxing. These are the tvHeartRate and tvRespiratoryRate TextViews, which can be continuously updated with current sensor data to keep the user constantly updated about their physiological condition.
By linking this symptom information with real-time physiological data gathered through bHealthy, we can make a more context aware application. For example, when a user continuously reports headaches when ECG shows high heart rate, my application would automatically suggest some techniques of relaxation that could lower the heart rate. 
Using data from my DatabaseHelper.kt, which maintains the local database, I would generate a model in machine learning that will learn from both historic symptom data and continuous physiological signals. The resultant model would then make my app capable of predicting potential health issues before they are apparent by analyzing the anomalous patterns of a user's data. For instance, it may learn a pattern where a user's heart rate peaks before they show anxiety and proactively offer relaxation activities whenever those conditions are met in the future.
Having combined data between bHealthy and my existing database would further mean that I can make detailed wellness reports that give insight into how the user's physiological data correlates with their symptom ratings. Reports, when accessed through the UI SymptomsScreen.kt, should do a little more than show past lists of data. In such a case, the feedback would definitely be personalized and applicable in real life for the user.

Q3. A common assumption is mobile computing is mostly about app development. After completing Project 1 and reading both papers, have your views changed? If yes, what do you think mobile computing is about and why? If no, please explain why you still think mobile computing is mostly about app development, providing examples to support your viewpoint.
  Answer : The first project was about developing an Android app where the user will be able to track their symptoms or physiological data, such as heart rate and respiration rate. A majority of my work had been in files such as SymptomsScreen.kt, DatabaseHelper.kt, and activity_main.xml to make sure the app was working well on the front side, stored data appropriately on the back end. The bHealthy paper opened my eyes to realize that mobile computing is not all about what happens on the screen, but more of an interaction with the real world through sensors, providing meaningful feedback to the user in real-time.
For instance, other than just recording heart rate, my application could be further enhanced by processing this information in real time using some of the physiological feedback mechanisms introduced in bHealthy, dynamically updating the application based on the state of the user, such as suggesting stress-relieving exercises when a higher-than-average heart rate is detected.
The bHealthy application suite has emphasized real-time data analysis and context-aware computing. That was beyond the usual development of an app I focused on in Project 1. For instance, instead of just reading a respiratory rate data from a CSV and displaying, my app could use real sensor data for continuous monitoring of the user's respiratory rate with instant feedback or alerts if abnormalities are detected.
This level of context awareness, in which the app is not just listening to the user's input but watching out and responding to their physiological state, has opened my eyes to what mobile computing can and should do, particularly in health-related applications.
The Health-Dev paper introduced me to the importance of system-level integration and the reliability of mobile computing systems. It is not just about writing code for an app, but it is about how sensors, mobile devices, and communication protocols all together work. Health-Dev's model-based development approach ensures that generated code will be optimized in terms of performance and reliability-something I did not consider deeply in my initial application development process.
For example, if I used automated code generation by Health-Dev in my project, this would translate to my app not only working right but working efficiently and preserving battery life with accurate data, which is the most important thing for a health monitoring application.
  
