const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();
setGlobalOptions({ maxInstances: 10 });

exports.sendMessageNotification = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    const message = event.data.data();
    const conversationId = event.params.conversationId;
    const senderId = message.senderId;

    const conversationDoc = await admin.firestore()
      .collection("conversations").doc(conversationId).get();
    const conversation = conversationDoc.data();
    const participants = conversation.participants;
    const receiverId = participants.find(id => id !== senderId);

    const senderDoc = await admin.firestore()
      .collection("users").doc(senderId).get();
    const senderName = senderDoc.data()?.displayName ?? "Utilisateur";

    const receiverDoc = await admin.firestore()
      .collection("users").doc(receiverId).get();
    const token = receiverDoc.data()?.fcmToken;
    const activeConversation = receiverDoc.data()?.activeConversationId;

    if (activeConversation === conversationId) {
      console.log("Utilisateur déjà dans la conversation, notification ignorée");
      return;
    }
    if (!token) {
      console.log("Pas de token FCM");
      return;
    }

    await admin.messaging().send({
      token: token,
      notification: { title: senderName, body: message.text },
      data: { conversationId: conversationId }
    });
  }
);

exports.sendRentalRequestNotification = onDocumentCreated(
  "rentals/{rentalId}",
  async (event) => {
    const rental = event.data.data();
    const { renterId, ownerId, startDate, endDate, basePriceInCents } = rental;

    const renterDoc = await admin.firestore().collection("users").doc(renterId).get();
    const renterName = renterDoc.data()?.displayName ?? "Un locataire";

    const ownerDoc = await admin.firestore().collection("users").doc(ownerId).get();
    const token = ownerDoc.data()?.fcmToken;
    if (!token) { console.log("Pas de token FCM pour le propriétaire"); return; }

    const formatDate = (ts) => new Intl.DateTimeFormat("fr-FR", {
      weekday: "short", day: "numeric", month: "short"
    }).format(ts.toDate());

    const amount = new Intl.NumberFormat("fr-FR", {
      style: "currency", currency: "EUR"
    }).format(basePriceInCents / 100);

    await admin.messaging().send({
      token: token,
      notification: {
        title: `${renterName} veut louer ton vélo !`,
        body: `• Du ${formatDate(startDate)} au ${formatDate(endDate)}\n• Accepte pour recevoir ${amount}`
      },
      data: { conversationId: rental.id }
    });
  }
);