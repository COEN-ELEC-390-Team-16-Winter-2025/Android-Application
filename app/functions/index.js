const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");

admin.initializeApp();

// Alerts function
exports.onAlertCreated = onDocumentCreated('/users/{userId}/Alerts/{alertId}', async (event) => {
  const alertData = event.data.data(); // Get data of the alert
  const userId = event.params.userId;

  // Log the alert for debugging
  console.log('New Alert:', alertData);

  // Example: Send a push notification to the user
  const message = {
    notification: {
      title: "New Alert!",
      body: alertData.message || "You have a new alert.",
    },
    token: alertData.userToken, // Assuming the alert document contains the user's token
  };

  // Send message using Firebase Cloud Messaging (FCM)
  await admin.messaging().send(message);

  return null;
});

// Recommendations function
exports.onRecommendationCreated = onDocumentCreated('/users/{userId}/Recommendations/{recommendationId}', async (event) => {
  const recommendationData = event.data.data();
  const userId = event.params.userId;

  console.log('New Recommendation:', recommendationData);

  const message = {
    notification: {
      title: "New Recommendation!",
      body: recommendationData.message || "You have a new recommendation.",
    },
    token: recommendationData.userToken,
  };

  await admin.messaging().send(message);

  return null;
});

// Reminders function
exports.onReminderCreated = onDocumentCreated('/users/{userId}/reminders/{reminderId}', async (event) => {
  const reminderData = event.data.data();
  const userId = event.params.userId;

  console.log('New Reminder:', reminderData);

  const message = {
    notification: {
      title: "New Reminder!",
      body: reminderData.message || "You have a new reminder.",
    },
    token: reminderData.userToken,
  };

  await admin.messaging().send(message);

  return null;
});
